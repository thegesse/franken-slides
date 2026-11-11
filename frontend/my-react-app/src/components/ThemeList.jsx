import { useState } from 'react';

function ThemeCard({ theme, selected, onSelect, onDelete, onRename }) {
    const [editing, setEditing] = useState(false);
    const [newTitle, setNewTitle] = useState(theme.name);

    async function handleRename(event) {
        event.preventDefault();
        try {
            await onRename(theme.id, newTitle);
            setEditing(false);
        } catch {
            // The parent displays the request error and keeps the form open.
        }
    }

    return (
        <article className={`theme-card${selected ? ' is-selected' : ''}`}>
            <button className="theme-select" type="button" aria-expanded={selected} onClick={() => onSelect(selected ? null : theme.id)}>
                <span className="theme-select-icon">{selected ? '▾' : '▸'}</span>
                <span>{theme.name}</span>
            </button>
            <div className="theme-actions">
                <button className="icon-button" type="button" aria-label={`Rename ${theme.name}`} onClick={() => { setNewTitle(theme.name); setEditing(true); }}>✎</button>
                <button className="icon-button danger-text" type="button" aria-label={`Delete ${theme.name}`} onClick={() => onDelete(theme.id)}>×</button>
            </div>
            {editing && (
                <form className="theme-rename-form" onSubmit={handleRename}>
                    <label htmlFor={`theme-title-${theme.id}`}>Theme name</label>
                    <input id={`theme-title-${theme.id}`} value={newTitle} onChange={(event) => setNewTitle(event.target.value)} maxLength={255} required />
                    <div className="form-row">
                        <button className="button button-primary" type="submit">Save</button>
                        <button className="button button-quiet" type="button" onClick={() => setEditing(false)}>Cancel</button>
                    </div>
                </form>
            )}
        </article>
    );
}

export default function ThemeList({ themes, selectedThemeId, onSelect, onDelete, onRename, onAdd }) {
    return (
        <div className="theme-list">
            {themes.map((theme) => (
                <ThemeCard
                    key={theme.id}
                    theme={theme}
                    selected={theme.id === selectedThemeId}
                    onSelect={onSelect}
                    onDelete={onDelete}
                    onRename={onRename}
                />
            ))}
            <button className="add-theme-button" type="button" onClick={onAdd}>
                <span aria-hidden="true">+</span> Add theme
            </button>
        </div>
    );
}
