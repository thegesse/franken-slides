import { useState } from 'react';

const initialContent = '{\n  "text": ""\n}';

export default function CreateSlideForm({ onCreate }) {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [content, setContent] = useState(initialContent);
    const [error, setError] = useState('');
    const [saving, setSaving] = useState(false);

    async function handleSubmit(event) {
        event.preventDefault();
        setError('');
        let parsedContent;
        try {
            parsedContent = JSON.parse(content);
        } catch {
            setError('Slide content must be valid JSON.');
            return;
        }

        setSaving(true);
        try {
            await onCreate({ title, description, content: parsedContent });
            setTitle('');
            setDescription('');
            setContent(initialContent);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setSaving(false);
        }
    }

    return (
        <form className="slide-create-form" onSubmit={handleSubmit}>
            <div className="form-heading">
                <div>
                    <p className="eyebrow">Add to this group</p>
                    <h3>New slide</h3>
                </div>
            </div>
            {error && <p className="notice notice-error" role="alert">{error}</p>}
            <label htmlFor="slide-title">Title</label>
            <input id="slide-title" value={title} onChange={(event) => setTitle(event.target.value)} maxLength={255} required />
            <label htmlFor="slide-description">Description <span className="muted">(optional)</span></label>
            <input id="slide-description" value={description} onChange={(event) => setDescription(event.target.value)} maxLength={255} />
            <label htmlFor="slide-content">Structured content (JSON)</label>
            <textarea id="slide-content" value={content} onChange={(event) => setContent(event.target.value)} rows={5} required spellCheck="false" />
            <button className="button button-primary" type="submit" disabled={saving}>
                {saving ? 'Adding…' : 'Add slide'}
            </button>
        </form>
    );
}
