package com.fablab.backend.commands;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandCatalog {

    private final Map<CommandKey, CommandDefinition> definitions;

    public CommandCatalog() {
        Map<CommandKey, CommandDefinition> commands = new EnumMap<>(CommandKey.class);

        register(commands, CommandKey.BED_DOWN, "Plateau au plus bas", "Mouvement", """
                G28 Z
                G90
                G1 Z999 F3000
                """, false, null, false);

        register(commands, CommandKey.PARK_HEAD, "Park tête (coin avant gauche)", "Mouvement", """
                G28
                G90
                G1 X10 Y10 F6000
                """, false, null, false);

        register(commands, CommandKey.HOME_ALL, "Home complet", "Mouvement", "G28", false, null, false);
        register(commands, CommandKey.DISABLE_MOTORS, "Désactiver moteurs", "Mouvement", "M84", false, null, false);

        register(commands, CommandKey.PURGE_NOZZLE, "Purger buse", "Filament", """
                M109 S200
                G92 E0
                G1 E20 F200
                G1 E-2 F1200
                """, false, null, false);

        register(commands, CommandKey.PURGE_LINE, "Purge line", "Filament", """
                M109 S200
                G90
                G1 Z0.28 F1200
                G1 X5 Y5 F6000
                G92 E0
                G1 X200 E15 F1200
                G1 Y8 F6000
                G1 X5 E15 F1200
                G92 E0
                """, false, null, false);

        register(commands, CommandKey.LOAD_FILAMENT, "Load filament", "Filament", """
                M109 S220
                G92 E0
                G1 E60 F300
                G1 E10 F120
                """, false, null, false);

        register(commands, CommandKey.UNLOAD_FILAMENT, "Unload filament", "Filament", """
                M109 S220
                G92 E0
                G1 E5 F200
                G1 E-80 F1200
                """, false, null, false);

        register(commands, CommandKey.FAN_ON, "Fan 100%", "Sécurité", "M106 S255", false, null, false);
        register(commands, CommandKey.FAN_OFF, "Fan OFF", "Sécurité", "M107", false, null, false);
        register(commands, CommandKey.HEATERS_OFF, "Stop chauffe", "Sécurité", """
                TURN_OFF_HEATERS
                M107
                """, false, null, true);

        register(commands, CommandKey.PAUSE, "Pause", "Impression", "PAUSE", false, null, false);
        register(commands, CommandKey.RESUME, "Resume", "Impression", "RESUME", false, null, false);
        register(commands, CommandKey.CANCEL, "Cancel", "Impression", "CANCEL_PRINT", false, null, true);

        register(commands, CommandKey.BED_MESH, "Bed mesh calibrate", "Calibration", "BED_MESH_CALIBRATE", true, "BED_MESH_CALIBRATE", false);
        register(commands, CommandKey.SCREWS_TILT, "Screws tilt", "Calibration", "SCREWS_TILT_CALCULATE", true, "SCREWS_TILT_CALCULATE", false);
        register(commands, CommandKey.Z_TILT, "Z tilt adjust", "Calibration", "Z_TILT_ADJUST", true, "Z_TILT_ADJUST", false);
        register(commands, CommandKey.QUAD_GANTRY, "Quad gantry level", "Calibration", "QUAD_GANTRY_LEVEL", true, "QUAD_GANTRY_LEVEL", false);

        this.definitions = Map.copyOf(commands);
    }

    private void register(Map<CommandKey, CommandDefinition> commands,
                          CommandKey key,
                          String label,
                          String group,
                          String script,
                          boolean requiresMacro,
                          String macroName,
                          boolean dangerous) {
        commands.put(key, new CommandDefinition(key, label, group, script.strip(), requiresMacro, macroName, dangerous));
    }

    public CommandDefinition get(CommandKey key) {
        return definitions.get(key);
    }

    public List<CommandDefinition> all() {
        return definitions.values().stream().toList();
    }
}