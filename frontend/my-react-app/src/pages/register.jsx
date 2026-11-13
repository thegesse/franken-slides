import { useState } from "react";
import { useNavigate, Link } from 'react-router-dom';
import { API_BASE_URL } from "../config";

export default function Register() {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    const handleregister = async (e) => {
        e.preventDefault();

        try {
            const response = await fetch(`${API_BASE_URL}/api/v1/auth/register`, {
                method: "POST",
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password}),
            });

            const text = await response.text();
            const data = text ? JSON.parse(text) : {};

            if (response.ok) {
                localStorage.setItem('user_id', data.id || data.user_id);
                localStorage.setItem('username', data.username || username);
                navigate("/dashboard");
            } else {
                alert(data.error || "Failed to sign up");
            }
        } catch (err) {
            console.error("Network or parsing error:", err);
            alert("Could not connect to the server");
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