import React, { useState } from 'react'
import { login } from '../services/api/authApi'

export default function LoginPage({ onLogin }) {
    const [email, setEmail] = useState('admin@example.com')
    const [password, setPassword] = useState('password')
    const [error, setError] = useState(null)

    const submit = async e => {
        e.preventDefault()
        try {
            const data = await login({ email, password })
            onLogin(data)
            setError(null)
        } catch (err) {
            setError('Login failed')
        }
    }

    return (
        <form onSubmit={submit} style={{ maxWidth: '320px' }}>
            <h2>Connexion</h2>
            {error && <p style={{ color: 'red' }}>{error}</p>}
            <label>Email</label>
            <input value={email} onChange={e => setEmail(e.target.value)} style={{ width: '100%' }} />
            <label>Mot de passe</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} style={{ width: '100%' }} />
            <button type="submit" style={{ marginTop: '1rem' }}>Se connecter</button>
        </form>
    )
}