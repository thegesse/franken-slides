import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { API_BASE_URL } from '../../config.js';
import CreateSlideForm from '../../components/CreateSlideForm.jsx';
import GroupList from '../../components/GroupList.jsx';
import SlideList from '../../components/SlideList.jsx';
import ThemeSection from './ThemePage.jsx';

async function getAllPages(url, signal) {
    const results = [];
    let pageNumber = 0;
    let hasNext = true;

    while (hasNext) {
        const separator = url.includes('?') ? '&' : '?';
        const response = await fetch(`${url}${separator}page=${pageNumber}&size=100`, { signal });
        if (!response.ok) {
            throw new Error(`Request failed (${response.status})`);
        }

        const page = await response.json();
        results.push(...page.content);
        hasNext = !page.last;
        pageNumber += 1;
    }

    return results;
}

export default function Dashboard() {
    const userId = localStorage.getItem('user_id');
    const username = localStorage.getItem('username') || 'Presenter';
    const navigate = useNavigate();
    const location = useLocation();
    const restoredState = location.state?.dashboardState;
    const [selectedThemeId, setSelectedThemeId] = useState(restoredState?.selectedThemeId ?? null);
    const [groups, setGroups] = useState([]);
    const [groupsThemeId, setGroupsThemeId] = useState(null);
    const [selectedGroupId, setSelectedGroupId] = useState(restoredState?.selectedGroupId ?? null);
    const [slides, setSlides] = useState([]);
    const [slidesGroupId, setSlidesGroupId] = useState(null);
    const [presentationGroups, setPresentationGroups] = useState(restoredState?.presentationGroups ?? []);
    const [groupName, setGroupName] = useState('');
    const [showGroupForm, setShowGroupForm] = useState(false);
    const loadingGroups = selectedThemeId != null && groupsThemeId !== selectedThemeId;
    const loadingSlides = selectedGroupId != null && slidesGroupId !== selectedGroupId;
    const [error, setError] = useState('');

    useEffect(() => {
        if (!selectedThemeId || !userId) return undefined;

        const controller = new AbortController();
        async function loadGroups() {
            try {
                const groupUrl = `${API_BASE_URL}/api/v1/users/${userId}/themes/${selectedThemeId}/groups`;
                setGroups(await getAllPages(groupUrl, controller.signal));
                setGroupsThemeId(selectedThemeId);
                setError('');
            } catch (requestError) {
                if (requestError.name !== 'AbortError') {
                    setGroups([]);
                    setGroupsThemeId(selectedThemeId);
                    setError(`Could not load groups: ${requestError.message}`);
                }
            }
        }

        loadGroups();
        return () => controller.abort();
    }, [selectedThemeId, userId]);

    useEffect(() => {
        if (!selectedThemeId || !selectedGroupId || !userId) return undefined;

        const controller = new AbortController();
        async function loadSlides() {
            try {
                const slideUrl = `${API_BASE_URL}/api/v1/users/${userId}/themes/${selectedThemeId}/groups/${selectedGroupId}/slides`;
                setSlides(await getAllPages(slideUrl, controller.signal));
                setSlidesGroupId(selectedGroupId);
                setError('');
            } catch (requestError) {
                if (requestError.name !== 'AbortError') {
                    setSlides([]);
                    setSlidesGroupId(selectedGroupId);
                    setError(`Could not load slides: ${requestError.message}`);
                }
            }
        }

        loadSlides();
        return () => controller.abort();
    }, [selectedThemeId, selectedGroupId, userId]);

    async function createGroup(event) {
        event.preventDefault();
        setError('');
        try {
            const response = await fetch(
                `${API_BASE_URL}/api/v1/users/${userId}/themes/${selectedThemeId}/groups`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name: groupName })
                }
            );
            if (!response.ok) throw new Error(`Could not create group (${response.status})`);

            const createdGroup = await response.json();
            setGroups((current) => [...current, createdGroup]);
            setGroupName('');
            setShowGroupForm(false);
            setSelectedGroupId(createdGroup.id);
        } catch (requestError) {
            setError(requestError.message);
        }
    }

    async function deleteGroup(group) {
        setError('');
        try {
            const response = await fetch(
                `${API_BASE_URL}/api/v1/users/${userId}/themes/${group.themeId}/groups/${group.id}`,
                { method: 'DELETE' }
            );
            if (!response.ok) throw new Error(`Could not delete group (${response.status})`);

            setGroups((current) => current.filter((item) => item.id !== group.id));
            setPresentationGroups((current) => current.filter((item) => item.id !== group.id));
            if (selectedGroupId === group.id) {
                setSelectedGroupId(null);
                setSlides([]);
            }
        } catch (requestError) {
            setError(requestError.message);
        }
    }

    async function createSlide(slideInput) {
        setError('');
        const response = await fetch(
            `${API_BASE_URL}/api/v1/users/${userId}/themes/${selectedThemeId}/groups/${selectedGroupId}/slides`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(slideInput)
            }
        );
        if (!response.ok) throw new Error(`Could not create slide (${response.status})`);
        const createdSlide = await response.json();
        setSlides((current) => [...current, createdSlide]);
    }

    async function deleteSlide(slideId) {
        setError('');
        try {
            const response = await fetch(
                `${API_BASE_URL}/api/v1/users/${userId}/themes/${selectedThemeId}/groups/${selectedGroupId}/slides/${slideId}`,
                { method: 'DELETE' }
            );
            if (!response.ok) throw new Error(`Could not delete slide (${response.status})`);
            setSlides((current) => current.filter((slide) => slide.id !== slideId));
        } catch (requestError) {
            setError(requestError.message);
        }
    }

    function togglePresentationGroup(group) {
        setPresentationGroups((current) => current.some((item) => item.id === group.id)
            ? current.filter((item) => item.id !== group.id)
            : [...current, group]);
    }

    function selectTheme(themeId) {
        if (themeId !== selectedThemeId) {
            setSelectedGroupId(null);
            setSlides([]);
        }
        setSelectedThemeId(themeId);
    }

    function removeThemeFromPresentation(themeId) {
        setPresentationGroups((current) => current.filter((group) => group.themeId !== themeId));
    }

    function movePresentationGroup(index, direction) {
        setPresentationGroups((current) => {
            const targetIndex = index + direction;
            if (targetIndex < 0 || targetIndex >= current.length) return current;
            const reordered = [...current];
            [reordered[index], reordered[targetIndex]] = [reordered[targetIndex], reordered[index]];
            return reordered;
        });
    }

    async function startPresentation() {
        if (presentationGroups.length === 0) {
            setError('Select at least one group for the presentation.');
            return;
        }

        setError('');
        try {
            const slidesByGroup = await Promise.all(presentationGroups.map(async (group) => {
                const url = `${API_BASE_URL}/api/v1/users/${userId}/themes/${group.themeId}/groups/${group.id}/slides`;
                return getAllPages(url);
            }));
            const presentationSlides = presentationGroups.flatMap((group, index) =>
                slidesByGroup[index].map((slide) => ({ ...slide, groupName: group.name }))
            );
            if (presentationSlides.length === 0) {
                setError('The selected groups do not contain any slides yet.');
                return;
            }
            navigate('/presentation', {
                state: {
                    slides: presentationSlides,
                    dashboardState: { selectedThemeId, selectedGroupId, presentationGroups }
                }
            });
        } catch (requestError) {
            setError(`Could not start presentation: ${requestError.message}`);
        }
    }

    function logout() {
        localStorage.removeItem('user_id');
        localStorage.removeItem('username');
        navigate('/');
    }

    return (
        <main className="dashboard-page">
            <header className="dashboard-header">
                <a className="brand-mark" href="/dashboard" aria-label="Frankenslides dashboard">F</a>
                <div className="dashboard-heading">
                    <p className="eyebrow">Workspace</p>
                    <h1>Your slide library</h1>
                </div>
                <div className="dashboard-user">
                    <span>Hi, {username}</span>
                    <button className="button button-quiet" type="button" onClick={logout}>Log out</button>
                </div>
            </header>

            {error && <p className="notice notice-error" role="alert">{error}</p>}

            <div className="dashboard-grid">
                <aside className="library-panel panel">
                    <div className="panel-heading">
                        <div>
                            <p className="eyebrow">Organize</p>
                            <h2>Themes</h2>
                        </div>
                    </div>
                    <ThemeSection
                        userId={userId}
                        selectedThemeId={selectedThemeId}
                        onThemeSelect={selectTheme}
                        onThemeDeleted={removeThemeFromPresentation}
                    />
                </aside>

                <section className="workspace-panel panel" aria-label="Theme and group workspace">
                    {!selectedThemeId ? (
                        <div className="empty-state">
                            <span className="empty-icon">✳</span>
                            <h2>Choose a theme to begin</h2>
                            <p>Your groups and slides will appear here.</p>
                        </div>
                    ) : (
                        <>
                            <div className="panel-heading">
                                <div>
                                    <p className="eyebrow">Theme contents</p>
                                    <h2>Slide groups</h2>
                                </div>
                                <button className="button button-secondary" type="button" onClick={() => setShowGroupForm((open) => !open)}>
                                    {showGroupForm ? 'Cancel' : '+ New group'}
                                </button>
                            </div>

                            {showGroupForm && (
                                <form className="inline-create-form" onSubmit={createGroup}>
                                    <label htmlFor="group-name">Group name</label>
                                    <div className="form-row">
                                        <input id="group-name" value={groupName} onChange={(event) => setGroupName(event.target.value)} maxLength={255} required />
                                        <button className="button button-primary" type="submit">Create group</button>
                                    </div>
                                </form>
                            )}

                            {loadingGroups ? <p className="muted">Loading groups…</p> : (
                                <GroupList
                                    groups={groups}
                                    selectedGroupId={selectedGroupId}
                                    presentationGroupIds={presentationGroups.map((group) => group.id)}
                                    onSelect={setSelectedGroupId}
                                    onTogglePresentation={togglePresentationGroup}
                                    onDelete={deleteGroup}
                                />
                            )}

                            {selectedGroupId && (
                                <section className="slides-editor" aria-label="Selected group slides">
                                    <div className="panel-heading slides-heading">
                                        <div>
                                            <p className="eyebrow">Selected group</p>
                                            <h2>{groups.find((group) => group.id === selectedGroupId)?.name}</h2>
                                        </div>
                                        <span className="count-pill">{slides.length} slides</span>
                                    </div>
                                    <CreateSlideForm onCreate={createSlide} />
                                    {loadingSlides ? <p className="muted">Loading slides…</p> : (
                                        <SlideList slides={slides} onDelete={deleteSlide} />
                                    )}
                                </section>
                            )}
                        </>
                    )}
                </section>

                <aside className="presentation-panel panel">
                    <div className="panel-heading">
                        <div>
                            <p className="eyebrow">Playback order</p>
                            <h2>Presentation</h2>
                        </div>
                        <span className="count-pill">{presentationGroups.length} groups</span>
                    </div>
                    {presentationGroups.length === 0 ? (
                        <p className="muted presentation-hint">Select groups from your themes to build a presentation.</p>
                    ) : (
                        <ol className="presentation-group-list">
                            {presentationGroups.map((group, index) => (
                                <li key={group.id}>
                                    <span className="order-number">{index + 1}</span>
                                    <span className="presentation-group-name">{group.name}</span>
                                    <div className="order-controls">
                                        <button type="button" aria-label={`Move ${group.name} up`} disabled={index === 0} onClick={() => movePresentationGroup(index, -1)}>↑</button>
                                        <button type="button" aria-label={`Move ${group.name} down`} disabled={index === presentationGroups.length - 1} onClick={() => movePresentationGroup(index, 1)}>↓</button>
                                        <button type="button" aria-label={`Remove ${group.name}`} onClick={() => togglePresentationGroup(group)}>×</button>
                                    </div>
                                </li>
                            ))}
                        </ol>
                    )}
                    <button className="button button-primary start-button" type="button" onClick={startPresentation} disabled={presentationGroups.length === 0}>
                        Start presentation <span aria-hidden="true">→</span>
                    </button>
                </aside>
            </div>
        </main>
    );
}
