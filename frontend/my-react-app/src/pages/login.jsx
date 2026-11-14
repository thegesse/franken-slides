import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { API_BASE_URL } from "../config.js";
import { apiFetch, saveAuthSession } from '../api.js';

export default function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();

        try {
            const response = await apiFetch(`${API_BASE_URL}/api/v1/auth/login`, {
                auth: false,
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            const data = await response.json().catch(() => ({}));

            if (response.ok) {
                saveAuthSession(data, username);
                navigate('/dashboard');
            } else {
                alert(data.message || data.error || 'Invalid credentials');
            }
        } catch (err) {
            console.error('Login error:', err);
            alert(err.message || 'Could not connect to server');
        }
    };

    return (
        <main className="login-page">
            <section className="login-card" aria-labelledby="login-title">
                <div className="login-heading">
                    <p className="login-eyebrow">Welcome back</p>
                    <h2 id="login-title">Log in to your account</h2>
                    <p>Pick up where you left off.</p>
                </div>

                <form className="login-form" onSubmit={handleLogin}>
                    <label htmlFor="login-username">Username</label>
                    <input id="login-username" type="text" placeholder="Enter your username" value={username} onChange={(e) => setUsername(e.target.value)} />
                    <label htmlFor="login-password">Password</label>
                    <input id="login-password" type="password" placeholder="Enter your password" value={password} onChange={(e) => setPassword(e.target.value)} />
                    <button type="submit">Log in</button>
                </form>

                <p className="login-signup-link">New here? <Link to="/signup">Create an account</Link></p>
            </section>
        </main>
    );
}
