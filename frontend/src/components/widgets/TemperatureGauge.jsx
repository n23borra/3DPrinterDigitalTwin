import React from 'react'

export default function TemperatureGauge({ label, current = 0, target = 0 }) {
    const color = current > target + 5 ? 'red' : '#0b7'
    return (
        <div style={{ marginBottom: '0.5rem' }}>
            <strong>{label}</strong>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>Actuelle: {current ?? '-'}°C</span>
                <span>Cible: {target ?? '-'}°C</span>
            </div>
            <div style={{ background: '#eee', height: '10px', borderRadius: '4px' }}>
                <div style={{ width: `${Math.min(100, (current || 0) / 3)}%`, background: color, height: '100%', borderRadius: '4px' }} />
            </div>
        </div>
    )
}