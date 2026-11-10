import { useState, useEffect } from 'react';
import { API_BASE_URL } from "../../config.js";
import ThemeList from '../../components/ThemeCard';

export default function ThemeSection({ userId }) {
    const [themes, setThemes] = useState([]);
    const [error, setError] = useState('');

    useEffect(() => {
        async function loadThemes() {
            try {
                const response = await fetch(`${API_BASE_URL}/api/v1/users/${userId}/themes`);
                if (!response.ok) {
                    throw new Error(`Could not load themes (${response.status})`);
                }
                const page = await response.json();
                setThemes(page.content);
            }catch(e) {
                setError(e.message);
            }
        }
        loadThemes();
    }, [userId]);

    if (error) return <p role="alert">{error}</p>
    return (
        <ThemeList themes={themes} />
    )
}