import React, { useEffect, useState } from 'react'
import { listPrinters } from '../services/api/printerApi'
import PrinterCard from '../components/PrinterCard'
import AlertList from '../components/AlertList'
import api from '../services/api/apiClient'

export default function DashboardPage() {
    const [printers, setPrinters] = useState([])
    const [alerts, setAlerts] = useState([])

    useEffect(() => {
        listPrinters().then(setPrinters)
        api.get('/alerts').then(({ data }) => setAlerts(data))
    }, [])

    return (
        <div>
            <h2>Dashboard</h2>
            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                {printers.map(p => (
                    <PrinterCard key={p.id} printer={p} />
                ))}
            </div>
            <AlertList alerts={alerts} />
        </div>
    )
}