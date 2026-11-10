import { useEffect, useState } from 'react';
import { API_BASE_URL } from '../../config.js';
import ThemeList from '../../components/ThemeList.jsx';

export default function ThemeSection({ userId }) {
    const [themes, setThemes] = useState([]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [newTitle, setNewTitle] = useState('');

    const themeBaseApi = `${API_BASE_URL}/api/v1/users/${userId}/themes`;

    useEffect(() => {
        if (userId == null) {
            setThemes([]);
            setLoading(false);
            return;
        }

        const controller = new AbortController();

        async function loadThemes() {
            setLoading(true);
            setError('');
            try {
                const response = await fetch(themeBaseApi, { signal: controller.signal });
                if (!response.ok) {
                    throw new Error(`Could not load themes (${response.status})`);
                }
                const page = await response.json();
                setThemes(page.content);
            } catch (e) {
                if (e.name !== 'AbortError') setError(e.message);
            } finally {
                if (!controller.signal.aborted) setLoading(false);
            }
        }

        loadThemes();
        return () => controller.abort();
    }, [themeBaseApi, userId]);

    async function createTheme(event) {
        event.preventDefault();
        setError('');
        try {
            const response = await fetch(themeBaseApi, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title: newTitle })
            });

            if (!response.ok) {
                throw new Error(`Could not create theme (${response.status})`);
            }
            const createdTheme = await response.json();
            setThemes((current) => [...current, createdTheme]);
            setNewTitle('');
            setShowCreateForm(false);
        } catch (e) {
            setError(e.message);
        }
    }

    async function updateTheme(themeId, newTitle) {
        setError('');
        try {
            const response = await fetch(`${themeBaseApi}/${themeId}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ newTitle })
            });
            if (!response.ok) {
                throw new Error(`Could not modify theme (${response.status})`);
            }
            const updatedTheme = await response.json();
            setThemes((current) => current.map((theme) =>
                theme.id === themeId ? updatedTheme : theme
            ));
            return updatedTheme;
        } catch (e) {
            setError(e.message);
            throw e;
        }
    }

    async function deleteTheme(themeId) {
        setError('');
        try {
            const response = await fetch(`${themeBaseApi}/${themeId}`, {
                method: 'DELETE'
            });
            if (!response.ok) {
                throw new Error(`Could not delete theme (${response.status})`);
            }
            setThemes((current) => current.filter((theme) => theme.id !== themeId));
        } catch (e) {
            setError(e.message);
        }
    }

    return (
        <section>
            {error && <p role="alert">{error}</p>}
            {loading ? (
                <p>Loading themes…</p>
            ) : (
                <ThemeList
                    themes={themes}
                    onDelete={deleteTheme}
                    onRename={updateTheme}
                    onAdd={() => setShowCreateForm(true)}
                />
            )}

            {showCreateForm && (
                <form onSubmit={createTheme}>
                    <label htmlFor="new-theme-title">Theme name</label>
                    <input
                        id="new-theme-title"
                        value={newTitle}
                        onChange={(event) => setNewTitle(event.target.value)}
                        maxLength={255}
                        required
                    />
                    <button type="submit">Create theme</button>
                    <button type="button" onClick={() => setShowCreateForm(false)}>
                        Cancel
                    </button>
                </form>
            )}
        </section>
    );
}
