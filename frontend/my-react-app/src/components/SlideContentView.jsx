function renderBlock(block, index) {
    if (block == null) return null;
    if (typeof block === 'string' || typeof block === 'number') return <p key={index}>{block}</p>;
    if (Array.isArray(block)) return block.map((item, itemIndex) => renderBlock(item, itemIndex));

    const text = block.text ?? block.content;
    switch (block.type) {
        case 'heading':
        case 'title':
            return <h2 key={index}>{text}</h2>;
        case 'list':
        case 'bullets':
            return (
                <ul key={index}>
                    {(block.items ?? []).map((item, itemIndex) => <li key={itemIndex}>{typeof item === 'string' ? item : item.text}</li>)}
                </ul>
            );
        case 'quote':
            return <blockquote key={index}>{text}</blockquote>;
        case 'paragraph':
        case 'text':
            return <p key={index}>{text}</p>;
        default:
            if (typeof text === 'string' || typeof text === 'number') return <p key={index}>{text}</p>;
            if (Array.isArray(block.blocks)) return block.blocks.map((item, itemIndex) => renderBlock(item, itemIndex));
            return <pre key={index}>{JSON.stringify(block, null, 2)}</pre>;
    }
}

export default function SlideContentView({ content }) {
    if (content == null) return <p className="muted">No content</p>;
    if (Array.isArray(content)) return content.map((block, index) => renderBlock(block, index));
    if (typeof content === 'object') {
        if (Array.isArray(content.blocks)) return content.blocks.map((block, index) => renderBlock(block, index));
        if (typeof content.text === 'string') return <p>{content.text}</p>;
    }
    if (typeof content === 'string' || typeof content === 'number') return <p>{content}</p>;
    return <pre>{JSON.stringify(content, null, 2)}</pre>;
}
