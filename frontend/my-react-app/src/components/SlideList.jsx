import SlideContentView from './SlideContentView.jsx';

export default function SlideList({ slides, onDelete }) {
    if (slides.length === 0) {
        return <p className="muted slide-empty">No slides in this group yet. Use the form above to add the first one.</p>;
    }

    return (
        <div className="slide-list">
            {slides.map((slide, index) => (
                <article className="slide-card" key={slide.id}>
                    <div className="slide-card-heading">
                        <span className="slide-index">{String(index + 1).padStart(2, '0')}</span>
                        <div>
                            <h3>{slide.title}</h3>
                            {slide.description && <p className="muted">{slide.description}</p>}
                        </div>
                        <button className="icon-button danger-text" type="button" aria-label={`Delete slide ${slide.title}`} onClick={() => onDelete(slide.id)}>×</button>
                    </div>
                    <div className="slide-content-preview"><SlideContentView content={slide.content} /></div>
                </article>
            ))}
        </div>
    );
}
