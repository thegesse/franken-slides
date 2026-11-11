import { useEffect, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import SlideContentView from '../components/SlideContentView.jsx';

export default function PresentationPage() {
    const { state } = useLocation();
    const navigate = useNavigate();
    const slides = state?.slides ?? [];
    const dashboardState = state?.dashboardState;
    const [currentIndex, setCurrentIndex] = useState(0);
    const currentSlide = slides[currentIndex];

    useEffect(() => {
        function handleKeyDown(event) {
            if (event.key === 'ArrowRight' || event.key === ' ') {
                event.preventDefault();
                setCurrentIndex((index) => Math.min(index + 1, slides.length - 1));
            } else if (event.key === 'ArrowLeft') {
                setCurrentIndex((index) => Math.max(index - 1, 0));
            } else if (event.key === 'Escape') {
                navigate('/dashboard', { state: { dashboardState } });
            }
        }

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [dashboardState, navigate, slides.length]);

    if (slides.length === 0) return <Navigate to="/dashboard" replace />;

    return (
        <main className="presentation-page">
            <header className="presentation-toolbar">
                <div className="presentation-brand"><span className="brand-mark">F</span><span>Presentation</span></div>
                <div className="presentation-toolbar-center">
                    <span>{currentSlide.groupName}</span>
                    <span className="toolbar-divider">/</span>
                    <span>{currentSlide.title}</span>
                </div>
                <button className="button button-quiet" type="button" onClick={() => navigate('/dashboard', { state: { dashboardState } })}>Quit presentation <span aria-hidden="true">×</span></button>
            </header>

            <section className="presentation-stage" aria-live="polite" aria-label={`Slide ${currentIndex + 1} of ${slides.length}`}>
                <div className="presentation-slide">
                    <p className="presentation-kicker">{currentSlide.groupName}</p>
                    <h1>{currentSlide.title}</h1>
                    {currentSlide.description && <p className="presentation-description">{currentSlide.description}</p>}
                    <div className="presentation-content"><SlideContentView content={currentSlide.content} /></div>
                </div>
            </section>

            <footer className="presentation-controls">
                <button className="button button-secondary" type="button" onClick={() => setCurrentIndex((index) => Math.max(index - 1, 0))} disabled={currentIndex === 0}>
                    ← Previous
                </button>
                <div className="slide-progress">
                    <span>{String(currentIndex + 1).padStart(2, '0')}</span>
                    <div className="progress-track"><span style={{ width: `${((currentIndex + 1) / slides.length) * 100}%` }} /></div>
                    <span>{String(slides.length).padStart(2, '0')}</span>
                </div>
                <button className="button button-primary" type="button" onClick={() => setCurrentIndex((index) => Math.min(index + 1, slides.length - 1))} disabled={currentIndex === slides.length - 1}>
                    Next →
                </button>
            </footer>
            <p className="keyboard-hint">Use ← and → to navigate · Esc to quit</p>
        </main>
    );
}
