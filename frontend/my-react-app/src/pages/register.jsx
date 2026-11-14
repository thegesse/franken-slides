import { useState } from "react";
import { useNavigate, Link } from 'react-router-dom';
import { API_BASE_URL } from "../config";
import { apiFetch, saveAuthSession } from '../api.js';

export default function Register() {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    const handleregister = async (e) => {
        e.preventDefault();

        try {
            const response = await apiFetch(`${API_BASE_URL}/api/v1/auth/register`, {
                auth: false,
                method: "POST",
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password}),
            });

            const data = await response.json().catch(() => ({}));

            if (response.ok) {
                saveAuthSession(data, username);
                navigate("/dashboard");
            } else {
                alert(data.message || data.error || "Failed to sign up");
            }
        } catch (err) {
            console.error("Network or parsing error:", err);
            alert(err.message || "Could not connect to the server");
        }
    };

    return (
        <main className="register-page">
            <section className="register-card" aria-labelledby="register-title">
                <div className="register-heading">
                    <p className="register-eyebrow">Welcome</p>
                    <h2 id="register-title">Create an account</h2>
                    <p>Start organizing your tasks in one place.</p>
                </div>

                <form className="register-form" onSubmit={handleregister}>
                    <label htmlFor="register-username">Username</label>
                    <input
                        id="register-username"
                        type="text"
                        placeholder="Choose a username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />
                    <label htmlFor="register-email">Email</label>
                    <input
                        id="register-email"
                        type="email"
                        placeholder="Enter a email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    <label htmlFor="register-password">Password</label>
                    <input
                        id="register-password"
                        type="password"
                        placeholder="Create a password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                    <button type="submit">Create account</button>
                </form>

                <p className="register-login-link">
                    Already have an account? <Link to="/">Log in</Link>
                </p>
            </section>
        </main>
    );
}
