import { useEffect, useState } from 'react';
import { apiFetch } from '../api.js';

export default function SlideImage({ image, src, fallbackAlt = 'Slide image', editable = false, onUpload, onRemove }) {
    const [objectUrl, setObjectUrl] = useState('');
    const [alt, setAlt] = useState(image?.alt ?? fallbackAlt);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        setAlt(image?.alt ?? fallbackAlt);
    }, [image?.id, image?.alt, fallbackAlt]);

    useEffect(() => {
        let active = true;
        let loadedUrl = '';
        setObjectUrl('');
        setError('');

        if (!image || !src) return undefined;

        apiFetch(src)
            .then(async (response) => {
                if (!response.ok) throw new Error(`Could not load image (${response.status})`);
                return response.blob();
            })
            .then((blob) => {
                loadedUrl = URL.createObjectURL(blob);
                if (active) setObjectUrl(loadedUrl);
                else URL.revokeObjectURL(loadedUrl);
            })
            .catch((requestError) => {
                if (active) setError(requestError.message);
            });

        return () => {
            active = false;
            if (loadedUrl) URL.revokeObjectURL(loadedUrl);
        };
    }, [image?.id, src]);

    async function handleFileChange(event) {
        const file = event.target.files?.[0];
        event.target.value = '';
        if (!file) return;

        setBusy(true);
        setError('');
        try {
            await onUpload(file, alt || fallbackAlt);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setBusy(false);
        }
    }

    async function handleRemove() {
        setBusy(true);
        setError('');
        try {
            await onRemove();
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setBusy(false);
        }
    }

    if (!editable && !image) return null;

    return (
        <div className={`slide-image ${editable ? 'slide-image-editable' : ''}`}>
            {objectUrl ? (
                <img className="slide-image-preview" src={objectUrl} alt={image?.alt || fallbackAlt} />
            ) : editable ? (
                <div className="slide-image-placeholder">No image attached</div>
            ) : null}

            {editable && (
                <div className="slide-image-tools">
                    <label className="button button-secondary image-picker">
                        {busy ? 'Working…' : image ? 'Replace image' : 'Choose image'}
                        <input
                            type="file"
                            accept="image/jpeg,image/png,image/webp"
                            onChange={handleFileChange}
                            disabled={busy}
                            aria-label={`Choose image for ${fallbackAlt}`}
                        />
                    </label>
                    <input
                        className="image-alt-input"
                        aria-label="Image alternative text"
                        placeholder="Describe the image"
                        value={alt}
                        onChange={(event) => setAlt(event.target.value)}
                        maxLength={255}
                        disabled={busy}
                    />
                    {image && (
                        <button className="button button-quiet danger-text" type="button" onClick={handleRemove} disabled={busy}>
                            Remove image
                        </button>
                    )}
                </div>
            )}
            {error && <p className="notice notice-error image-error" role="alert">{error}</p>}
        </div>
    );
}
