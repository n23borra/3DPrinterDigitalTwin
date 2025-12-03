import React, { useState, useEffect } from 'react'
import DashboardPage from './pages/DashboardPage'
import HistoryPage from './pages/HistoryPage'
import MaintenancePage from './pages/MaintenancePage'
import AdminPage from './pages/AdminPage'
import LoginPage from './pages/LoginPage'
import Navbar from './components/Navbar'
import { getMe } from './services/api/authApi'

const pages = {
    dashboard: DashboardPage,
    history: HistoryPage,
    maintenance: MaintenancePage,
    admin: AdminPage,
    login: LoginPage
}

export default function App() {
    const [page, setPage] = useState('dashboard')
    const [user, setUser] = useState(null)

    useEffect(() => {
        getMe().then(setUser).catch(() => setUser(null))
    }, [])

    const CurrentPage = pages[page]

    return (
        <div>
            <Navbar current={page} onNavigate={setPage} user={user} />
            <main style={{ padding: '1rem' }}>
                <CurrentPage onLogin={setUser} />
            </main>
        </div>
    )
}