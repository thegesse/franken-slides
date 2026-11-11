import { useEffect, useState } from 'react';
import { API_BASE_URL } from '../../config.js';
import ThemeList from '../../components/ThemeList.jsx';

async function getAllThemes(url, signal) {
    const themes = [];
    let pageNumber = 0;
    let hasNext = true;

    while (hasNext) {
        const response = await fetch(`${url}?page=${pageNumber}&size=100`, { signal });
        if (!response.ok) throw new Error(`Could not load themes (${response.status})`);
        const page = await response.json();
        themes.push(...page.content);
        hasNext = !page.last;
        pageNumber += 1;
    }
    return themes;
}

export default function ThemeSection({ userId, selectedThemeId, onThemeSelect, onThemeDeleted }) {
    const [themes, setThemes] = useState([]);
    const [error, setError] = useState('');
    const [loadedUserId, setLoadedUserId] = useState(null);
    const loading = userId != null && loadedUserId !== userId;
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [newTitle, setNewTitle] = useState('');

    const themeBaseApi = `${API_BASE_URL}/api/v1/users/${userId}/themes`;

    useEffect(() => {
        if (userId == null) {
            return undefined;
        }

        const controller = new AbortController();
        async function loadThemes() {
            try {
                setThemes(await getAllThemes(themeBaseApi, controller.signal));
                setError('');
            } catch (requestError) {
                if (requestError.name !== 'AbortError') {
                    setError(requestError.message);
                    setThemes([]);
                }
            } finally {
                if (!controller.signal.aborted) setLoadedUserId(userId);
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
            if (!response.ok) throw new Error(`Could not create theme (${response.status})`);
            const createdTheme = await response.json();
            setThemes((current) => [...current, createdTheme]);
            setNewTitle('');
            setShowCreateForm(false);
            onThemeSelect(createdTheme.id);
        } catch (requestError) {
            setError(requestError.message);
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
            if (!response.ok) throw new Error(`Could not rename theme (${response.status})`);
            const updatedTheme = await response.json();
            setThemes((current) => current.map((theme) => theme.id === themeId ? updatedTheme : theme));
            return updatedTheme;
        } catch (requestError) {
            setError(requestError.message);
            throw requestError;
        }
    }

    async function deleteTheme(themeId) {
        setError('');
        try {
            const response = await fetch(`${themeBaseApi}/${themeId}`, { method: 'DELETE' });
            if (!response.ok) throw new Error(`Could not delete theme (${response.status})`);
            setThemes((current) => current.filter((theme) => theme.id !== themeId));
            onThemeDeleted(themeId);
            if (selectedThemeId === themeId) onThemeSelect(null);
        } catch (requestError) {
            setError(requestError.message);
        }
    }

    return (
        <div className="theme-section">
            {error && <p className="notice notice-error" role="alert">{error}</p>}
            {loading ? <p className="muted">Loading themes…</p> : (
                <ThemeList
                    themes={themes}
                    selectedThemeId={selectedThemeId}
                    onSelect={onThemeSelect}
                    onDelete={deleteTheme}
                    onRename={updateTheme}
                    onAdd={() => setShowCreateForm(true)}
                />
            )}
            {showCreateForm && (
                <form className="theme-create-form" onSubmit={createTheme}>
                    <label htmlFor="new-theme-title">New theme name</label>
                    <input id="new-theme-title" value={newTitle} onChange={(event) => setNewTitle(event.target.value)} maxLength={255} required autoFocus />
                    <div className="form-row">
                        <button className="button button-primary" type="submit">Create theme</button>
                        <button className="button button-quiet" type="button" onClick={() => setShowCreateForm(false)}>Cancel</button>
                    </div>
                </form>
            )}
        </div>
    );
}
