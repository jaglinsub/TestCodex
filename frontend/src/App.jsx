import { useEffect, useState } from 'react'

const API = 'http://localhost:8080/api'

export default function App() {
  const [username, setUsername] = useState('demo')
  const [password, setPassword] = useState('password123')
  const [code, setCode] = useState('')
  const [status, setStatus] = useState('Not authenticated')
  const [needsMfa, setNeedsMfa] = useState(false)

  const checkSession = async () => {
    const res = await fetch(`${API}/me`, { credentials: 'include' })
    const data = await res.json()
    setStatus(data.authenticated ? `Signed in as ${data.username}` : 'Not authenticated')
  }

  useEffect(() => {
    checkSession()
  }, [])

  const login = async () => {
    const res = await fetch(`${API}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ username, password })
    })
    const data = await res.json()
    setStatus(data.message)
    setNeedsMfa(res.ok)
  }

  const verifyMfa = async () => {
    const res = await fetch(`${API}/auth/mfa/verify`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ username, code })
    })
    const data = await res.json()
    setStatus(data.message)
    if (res.ok) {
      setNeedsMfa(false)
      checkSession()
    }
  }

  const logout = async () => {
    await fetch(`${API}/auth/logout`, { method: 'POST', credentials: 'include' })
    setStatus('Logged out')
    setNeedsMfa(false)
  }

  return (
    <main className="container">
      <h1>Home</h1>
      <p>{status}</p>

      <section className="card">
        <h2>Login + MFA</h2>
        <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="username" />
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="password" />
        <button onClick={login}>Login</button>

        {needsMfa && (
          <>
            <input value={code} onChange={(e) => setCode(e.target.value)} placeholder="MFA Code" />
            <button onClick={verifyMfa}>Verify MFA</button>
          </>
        )}
      </section>

      <section className="card">
        <h2>Single Sign-On</h2>
        <a className="button" href="http://localhost:8080/oauth2/authorization/google">Continue with Google</a>
      </section>

      <button onClick={logout}>Logout</button>
    </main>
  )
}
