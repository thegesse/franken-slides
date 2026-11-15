import { useRef, useState } from 'react';

const initialContent = '{\n  "text": ""\n}';

export default function CreateSlideForm({ onCreate }) {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [content, setContent] = useState(initialContent);
    const [imageFile, setImageFile] = useState(null);
    const [imageAlt, setImageAlt] = useState('');
    const imageInputRef = useRef(null);
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
            await onCreate({ title, description, content: parsedContent, imageFile, imageAlt });
            setTitle('');
            setDescription('');
            setContent(initialContent);
            setImageFile(null);
            setImageAlt('');
            if (imageInputRef.current) imageInputRef.current.value = '';
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
            <label htmlFor="slide-image">Image <span className="muted">(optional)</span></label>
            <input
                id="slide-image"
                ref={imageInputRef}
                type="file"
                accept="image/jpeg,image/png,image/webp"
                onChange={(event) => setImageFile(event.target.files?.[0] ?? null)}
            />
            {imageFile && (
                <>
                    <p className="muted selected-image-name">Selected: {imageFile.name}</p>
                    <label htmlFor="slide-image-alt">Image description <span className="muted">(optional)</span></label>
                    <input
                        id="slide-image-alt"
                        value={imageAlt}
                        onChange={(event) => setImageAlt(event.target.value)}
                        maxLength={255}
                        placeholder={title || 'Describe the image'}
                    />
                </>
            )}
            <button className="button button-primary" type="submit" disabled={saving}>
                {saving ? 'Adding…' : 'Add slide'}
            </button>
        </form>
    );
}
