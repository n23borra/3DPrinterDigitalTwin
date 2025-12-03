import React, { useEffect, useState } from 'react'
import { fetchHistory, listPrinters } from '../services/api/printerApi'

export default function HistoryPage() {
    const [printers, setPrinters] = useState([])
    const [selected, setSelected] = useState(null)
    const [history, setHistory] = useState([])

    useEffect(() => {
        listPrinters().then(data => {
            setPrinters(data)
            if (data.length) {
                setSelected(data[0].id)
            }
        })
    }, [])

    useEffect(() => {
        if (selected) {
            fetchHistory(selected).then(setHistory)
        }
    }, [selected])

    return (
        <div>
            <h2>Historique</h2>
            <select onChange={e => setSelected(e.target.value)} value={selected || ''}>
                {printers.map(p => (
                    <option key={p.id} value={p.id}>{p.name}</option>
                ))}
            </select>
            <table>
                <thead>
                <tr>
                    <th>Timestamp</th>
                    <th>Lit</th>
                    <th>Buse</th>
                    <th>Progression</th>
                </tr>
                </thead>
                <tbody>
                {history.map(s => (
                    <tr key={s.id}>
                        <td>{new Date(s.timestamp).toLocaleString()}</td>
                        <td>{s.bedTemp}</td>
                        <td>{s.nozzleTemp}</td>
                        <td>{(s.progress || 0).toFixed(0)}%</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    )
}