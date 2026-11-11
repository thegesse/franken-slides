export default function GroupList({ groups, selectedGroupId, presentationGroupIds, onSelect, onTogglePresentation, onDelete }) {
    if (groups.length === 0) {
        return <p className="muted group-empty">No groups in this theme yet. Create one to add slides.</p>;
    }

    return (
        <div className="group-list">
            {groups.map((group) => {
                const included = presentationGroupIds.includes(group.id);
                return (
                    <article className={`group-card${selectedGroupId === group.id ? ' is-selected' : ''}`} key={group.id}>
                        <button className="group-select" type="button" onClick={() => onSelect(group.id)}>
                            <span className="group-glyph">▤</span>
                            <span>{group.name}</span>
                        </button>
                        <label className="include-group">
                            <input type="checkbox" checked={included} onChange={() => onTogglePresentation(group)} />
                            <span>Present</span>
                        </label>
                        <button className="icon-button danger-text" type="button" aria-label={`Delete ${group.name}`} onClick={() => onDelete(group)}>×</button>
                    </article>
                );
            })}
        </div>
    );
}
