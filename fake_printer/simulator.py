#!/usr/bin/env python3
"""
Fake Moonraker API simulator for the 3D Printer Digital Twin project.

Simulates a realistic print job cycling through:
    warmup (5%) → printing (93%) → complete (2%) → [loops]

Usage:
    python simulator.py [--port 7125] [--duration 1800] [--file demo_benchy.gcode]

Then, in the digital twin app, add a new printer with:
    - Type : MOONRAKER
    - IP   : <this machine's ethernet IP>
    - Port : 7125  (or whatever --port you chose)
    - API key: leave empty

Requires only Python 3 standard library (no pip installs).
"""

import json
import math
import random
import time
import argparse
from http.server import BaseHTTPRequestHandler, HTTPServer

# ─── Simulation parameters (overridden by CLI args) ───────────────────────────
PRINT_DURATION_S = 1800     # 30 min default
WARMUP_FRAC      = 0.05     # first 5 % of the cycle = warmup
FILENAME         = "demo_benchy.gcode"

TARGET_NOZZLE = 220.0
TARGET_BED    =  60.0
ROOM_TEMP     =  22.0
CHAMBER_TEMP  =  35.0

BED_W, BED_D = 350.0, 350.0    # Creality K2 Plus build plate (mm)

START_TIME = time.time()

# Static 9×9 bed-mesh levelling matrix (realistic ±0.15 mm surface deviation)
BED_MESH = [
    [-0.127, -0.098, -0.063, -0.041, -0.022, -0.018, -0.031, -0.055, -0.089],
    [-0.102, -0.074, -0.048, -0.026, -0.009,  0.001, -0.012, -0.038, -0.071],
    [-0.083, -0.054, -0.028, -0.007,  0.012,  0.021,  0.009, -0.016, -0.052],
    [-0.071, -0.042, -0.015,  0.007,  0.025,  0.035,  0.023, -0.001, -0.039],
    [-0.065, -0.035, -0.008,  0.014,  0.033,  0.042,  0.031,  0.007, -0.031],
    [-0.071, -0.041, -0.014,  0.008,  0.026,  0.036,  0.024,  0.000, -0.037],
    [-0.082, -0.053, -0.026, -0.004,  0.014,  0.023,  0.011, -0.013, -0.050],
    [-0.099, -0.071, -0.044, -0.022, -0.005,  0.005, -0.008, -0.033, -0.068],
    [-0.124, -0.096, -0.061, -0.039, -0.020, -0.016, -0.029, -0.053, -0.087],
]

# ─── Simulation helpers ───────────────────────────────────────────────────────

def noise(scale=1.0):
    return (random.random() - 0.5) * 2.0 * scale

def elapsed():
    return time.time() - START_TIME

def progress():
    """Cyclic 0.0→1.0 progress that loops automatically."""
    return (elapsed() % PRINT_DURATION_S) / PRINT_DURATION_S

def phase(p):
    if p < WARMUP_FRAC:
        return "warmup"
    if p < 0.98:
        return "printing"
    return "complete"

def nozzle_temp(p):
    ph = phase(p)
    if ph == "warmup":
        t = p / WARMUP_FRAC
        return round(ROOM_TEMP + (TARGET_NOZZLE - ROOM_TEMP) * (1 - math.exp(-5 * t)) + noise(0.3), 2)
    if ph == "printing":
        return round(TARGET_NOZZLE + noise(0.5), 2)
    cool = (p - 0.98) / 0.02
    return round(TARGET_NOZZLE - (TARGET_NOZZLE - ROOM_TEMP) * cool * 0.15 + noise(0.3), 2)

def bed_temp(p):
    ph = phase(p)
    if ph == "warmup":
        t = p / WARMUP_FRAC
        return round(ROOM_TEMP + (TARGET_BED - ROOM_TEMP) * (1 - math.exp(-3.5 * t)) + noise(0.2), 2)
    if ph == "printing":
        return round(TARGET_BED + noise(0.3), 2)
    cool = (p - 0.98) / 0.02
    return round(TARGET_BED - (TARGET_BED - ROOM_TEMP) * cool * 0.1 + noise(0.2), 2)

def toolhead_position(p):
    """Returns (x, y, z, e) for the given cycle progress."""
    ph = phase(p)
    if ph == "warmup":
        return 175.0, 175.0, 0.0, 0.0     # parked at center

    # Printing progress within the printing window (0→1)
    pp = (p - WARMUP_FRAC) / (0.98 - WARMUP_FRAC)

    total_layers = 50
    layer   = int(pp * total_layers)
    z       = round(layer * 0.2, 3)

    layer_p = (pp * total_layers) - layer       # 0→1 within current layer
    # Alternate scan direction each layer (raster pattern)
    if layer % 2 == 0:
        x = 20.0 + (BED_W - 40.0) * layer_p
    else:
        x = BED_W - 20.0 - (BED_W - 40.0) * layer_p

    # Y advances one line per two layers
    y_lines = int((BED_D - 40.0) / 10.0)
    y = 20.0 + (layer % y_lines) * 10.0

    e = round(pp * 8500.0, 2)       # ~8.5 m of filament for a full cycle
    return round(x + noise(0.01), 3), round(y + noise(0.01), 3), z, e

def print_progress(p):
    """Progress of the print itself (0→1), not counting warmup."""
    if phase(p) == "warmup":
        return 0.0
    return round(min((p - WARMUP_FRAC) / (0.98 - WARMUP_FRAC), 1.0), 4)

def state_str(p):
    return {"warmup": "standby", "printing": "printing", "complete": "complete"}[phase(p)]

def velocity(p):
    return round(abs(150.0 + noise(25.0)), 1) if phase(p) == "printing" else 0.0

# ─── HTTP handler ─────────────────────────────────────────────────────────────

class Handler(BaseHTTPRequestHandler):

    def log_message(self, fmt, *args):
        pass    # silence per-request logs (1 Hz polling is very noisy)

    def ok(self, data):
        body = json.dumps({"result": data}).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        path = self.path

        p   = progress()
        ph  = phase(p)
        st  = state_str(p)
        nt  = nozzle_temp(p)
        bt  = bed_temp(p)
        x, y, z, e = toolhead_position(p)
        pp  = print_progress(p)
        vx  = velocity(p)
        fan = 0.8 if ph == "printing" else 0.0
        cpu = round(15.0 + noise(5.0) if ph == "printing" else 5.0 + noise(2.0), 1)

        # ── routing ───────────────────────────────────────────────────────────

        if "/server/info" in path:
            self.ok({"klippy_connected": True, "klippy_state": "ready",
                     "moonraker_version": "v0.8.0-sim"})

        elif "/printer/info" in path and "objects" not in path:
            self.ok({"state": st, "state_message": f"Printer is {st}",
                     "hostname": "fake-printer", "software_version": "v0.11.0-sim"})

        elif "extruder" in path and "filament" not in path:
            self.ok({"status": {"extruder": {
                "temperature": nt,
                "target": TARGET_NOZZLE if ph != "warmup" else 0.0,
                "power": 0.35,
                "can_extrude": ph == "printing",
            }}})

        elif "heater_bed" in path:
            self.ok({"status": {"heater_bed": {
                "temperature": bt,
                "target": TARGET_BED if ph != "warmup" else 0.0,
                "power": 0.08,
            }}})

        elif "temperature_sensor" in path or "chamber_temp" in path:
            self.ok({"status": {"temperature_sensor chamber_temp": {
                "temperature": round(CHAMBER_TEMP + noise(1.0), 2),
                "measured_min_temp": 20.5,
                "measured_max_temp": 38.2,
            }}})

        elif "toolhead" in path:
            self.ok({"status": {"toolhead": {
                "position": [x, y, z, e],
                "homed_axes": "xyz" if ph != "warmup" else "",
                "max_velocity": 500.0,
                "max_accel": 8000.0,
            }}})

        elif "print_stats" in path:
            el = elapsed() % PRINT_DURATION_S
            warmup_s = WARMUP_FRAC * PRINT_DURATION_S
            print_dur = max(0.0, el - warmup_s) if ph != "warmup" else 0.0
            self.ok({"status": {"print_stats": {
                "filename": FILENAME if ph != "warmup" else "",
                "state": st,
                "print_duration": round(print_dur, 1),
                "total_duration": round(el, 1),
                "filament_used": e,
                "message": "",
            }}})

        elif "display_status" in path:
            self.ok({"status": {"display_status": {
                "progress": pp,
                "message": f"Printing {FILENAME}" if ph == "printing" else st,
            }}})

        elif "motion_report" in path:
            self.ok({"status": {"motion_report": {
                "live_position": [round(x + noise(0.05), 3), round(y + noise(0.05), 3), z, e],
                "live_velocity": vx,
                "live_extruder_velocity": round(vx * 0.05, 3),
            }}})

        elif "fan_feedback" in path:
            self.ok({"status": {"fan_feedback": {
                "speed": fan,
                "rpm": round(fan * 4500 + noise(50), 0) if fan > 0 else 0.0,
            }}})

        elif "filament_switch_sensor" in path or "filament_sensor" in path:
            self.ok({"status": {
                "filament_switch_sensor filament_sensor": {
                    "enabled": True,
                    "filament_detected": True,
                }
            }})

        elif "proc_stats" in path:
            self.ok({
                "cpu_temp": round(45.0 + noise(3.0), 1),
                "cpu": cpu,
                "memory": f"{int(512 + noise(20))} / 2048 MB",
                "system_cpu_usage": {"cpu": cpu},
                "throttled_state": None,
            })

        elif "bed_mesh" in path:
            self.ok({"status": {"bed_mesh": {
                "profile_name": "default",
                "mesh_min": [15.0, 15.0],
                "mesh_max": [335.0, 335.0],
                "probed_matrix": BED_MESH,
                "mesh_matrix":   BED_MESH,
            }}})

        elif "z_tilt" in path:
            self.ok({"status": {"z_tilt": {"applied": ph != "warmup"}}})

        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        # Accept every command (pause, resume, gcode, emergency stop…) silently
        self.ok("ok")

# ─── Entry point ──────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Fake Moonraker API simulator")
    parser.add_argument("--port",     type=int, default=7125,
                        help="Port to listen on (default: 7125)")
    parser.add_argument("--duration", type=int, default=1800,
                        help="Simulated print duration in seconds (default: 1800 = 30 min)")
    parser.add_argument("--file",     type=str, default="demo_benchy.gcode",
                        help="Fake filename shown in print stats")
    args = parser.parse_args()

    global PRINT_DURATION_S, FILENAME
    PRINT_DURATION_S = args.duration
    FILENAME         = args.file

    server = HTTPServer(("0.0.0.0", args.port), Handler)

    print(f"[FakePrinter] Listening on 0.0.0.0:{args.port}  (all interfaces)")
    print(f"[FakePrinter] File     : {FILENAME}")
    print(f"[FakePrinter] Cycle    : {args.duration} s  ({args.duration // 60} min), loops automatically")
    print(f"[FakePrinter] Phases   : warmup {int(WARMUP_FRAC*100)}%  →  printing 93%  →  complete 2%")
    print(f"")
    print(f"[FakePrinter] In the app, add a new printer:")
    print(f"              Type    = MOONRAKER")
    print(f"              IP      = <this machine's ethernet IP>")
    print(f"              Port    = {args.port}")
    print(f"              API key = (leave empty)")
    print(f"")
    print(f"[FakePrinter] Ctrl+C to stop")

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n[FakePrinter] Stopped.")

if __name__ == "__main__":
    main()
