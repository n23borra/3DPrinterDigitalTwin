import React from 'react'

export default function AlertList({ alerts }) {
    return (
        <div>
            <h4>Alertes</h4>
            <ul>
                {alerts.map(alert => (
                    <li key={alert.id}>
                        <strong>{alert.severity}</strong> - {alert.message} ({new Date(alert.createdAt).toLocaleString()})
                    </li>
                ))}
            </ul>
        </div>
    )
}