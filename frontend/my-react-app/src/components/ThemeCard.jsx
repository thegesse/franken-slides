function ThemeCard({ theme }) {
    return (
        <article>
            <h2>{theme.name}</h2>
        </article>
    );
}

export default function ThemeList({ themes }) {
    return (
        <div>
            {themes.map((theme) => (
                <ThemeCard key={theme.id} theme={theme} />
            ))}
        </div>
    );
}