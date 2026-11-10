import { useState, useEffect } from 'react';
import { API_BASE_URL } from "../../config.js";
import ThemeList from '../../components/ThemeCard';

export default function ThemeSection({ userId }) {
    const [themes, setThemes] = useState([]);
    const [error, setError] = useState('');

    const themeBaseApi = `${API_BASE_URL}/api/v1/users/${userId}/themes`

    async function loadThemes() {
            try {
                const response = await fetch(`${themeBaseApi}`);
                if (!response.ok) {
                    throw new Error(`Could not load themes (${response.status})`);
                }
                const page = await response.json();
                setThemes(page.content);
            }catch(e) {
                setError(e.message);
            }
        }
        async function createTheme(title) {
            try {
                const response = await fetch (`${themeBaseApi}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json'},
                    body: JSON.stringify({ title })
                });
                
                if(!response.ok) {
                    throw new Error(`Could not create Theme (${response.status})`);
                }
            }catch(e) {
                console.error('Create theme error', e);
            }
        }
        async function updateTheme(themeId, newTitle) {
            try {
                const response = await fetch(`${themeBaseApi}/${themeId}`, {
                    method: 'PATCH',
                    headers: { 'Content-type': 'application/json' },
                    body: JSON.stringify({themeId, newTitle})
                });
                if(!response.ok) {
                    throw new Error(`Could not modify Theme (${response.status})`);
                }
            } catch(e) {
                console.error('Update theme error', e);
            }
        }
        async function deleteTheme(themeId) {
            try {
                const response = await fetch(`${themeBaseApi}/${themeId}`, {
                    method: 'DELETE'
                });
                if(!response.ok) {
                    throw new Error(`Could not delete Theme (${response.status})`);
                }
            }catch(e) {
                    console.error('Delete theme error', e);
            }
        }

    useEffect(() => {
        loadThemes();
    }, [userId]);

    if (error) return <p role="alert">{error}</p>
    return (
        <ThemeList themes={themes} />
    )
}