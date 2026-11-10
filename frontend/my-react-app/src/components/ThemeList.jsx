import { useState } from 'react';

function ThemeCard({ theme, onDelete, onRename }) {
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
        <article>
            {editing ? (
                <form onSubmit={handleRename}>
                    <label htmlFor={`theme-title-${theme.id}`}>Theme name</label>
                    <input
                        id={`theme-title-${theme.id}`}
                        value={newTitle}
                        onChange={(event) => setNewTitle(event.target.value)}
                        maxLength={255}
                        required
                    />
                    <button type="submit">Save</button>
                    <button type="button" onClick={() => {
                        setNewTitle(theme.name);
                        setEditing(false);
                    }}>
                        Cancel
                    </button>
                </form>
            ) : (
                <>
                    <h2>{theme.name}</h2>
                    <button type="button" onClick={() => setEditing(true)}>
                        Rename
                    </button>
                    <button type="button" onClick={() => onDelete(theme.id)}>
                        Delete
                    </button>
                </>
            )}
        </article>
    );
}

export default function ThemeList({ themes, onDelete, onRename, onAdd }) {
    return (
        <section>
            {themes.map((theme) => (
                <ThemeCard
                    key={theme.id}
                    theme={theme}
                    onDelete={onDelete}
                    onRename={onRename}
                />
            ))}
            <button type="button" onClick={onAdd} aria-label="Add theme">
                +
            </button>
        </section>
    );
}
