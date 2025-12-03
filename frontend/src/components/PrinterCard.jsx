import React from 'react'
import TemperatureGauge from './widgets/TemperatureGauge'
import ProgressBar from './widgets/ProgressBar'

export default function PrinterCard({ printer }) {
    return (
        <div style={{ border: '1px solid #ddd', borderRadius: '8px', padding: '1rem', width: '260px' }}>
            <h3>{printer.name}</h3>
            <p>Type: {printer.type}</p>
            <p>Status: {printer.status}</p>
            <TemperatureGauge label="Lit" current={printer.bedTemp} target={printer.targetBed} />
            <TemperatureGauge label="Buse" current={printer.nozzleTemp} target={printer.targetNozzle} />
            <ProgressBar value={printer.progress || 0} />
        </div>
    )
}