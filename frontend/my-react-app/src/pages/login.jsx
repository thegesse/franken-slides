import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { API_BASE_URL } from "../config.js"

export default function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();

        try {
            const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            const text = await response.text();
            const data = text ? JSON.parse(text) : {};

            if (response.ok) {
                //TODO when token validation added, store function here
                localStorage.setItem('user_id', data.id || data.user_id);
                localStorage.setItem('username', data.username || username);

                navigate('/dashboard');
            } else {
                alert(data.error || 'Invalid credentials');
            }
        } catch (err) {
            console.error('Login error:', err);
            alert('Could not connect to server');
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