import React from 'react'

export default function ProgressBar({ value }) {
    return (
        <div style={{ background: '#eee', height: '12px', borderRadius: '6px' }}>
            <div
                style={{ width: `${value}%`, background: '#0080ff', height: '100%', borderRadius: '6px', transition: 'width 0.3s' }}
            />
            <small>{value.toFixed(0)}%</small>
        </div>
    )
}