import React from 'react';

const RankBadge = ({ rank, small = false }) => {
    if (!rank) return null;

    const rankColors = {
        'BRONZE': '#cd7f32',
        'SILVER': '#c0c0c0',
        'GOLD': '#ffd700',
        'PLATINUM': '#e5e4e2',
        'DIAMOND': '#b9f2ff'
    };

    const color = rankColors[rank] || '#ffffff';

    return (
        <span 
            className={`rank-badge ${small ? 'rank-badge-sm' : 'rank-badge-lg'}`}
            style={{ borderColor: color, color: color, boxShadow: `0 0 10px ${color}33` }}
        >
            {rank}
        </span>
    );
};

export default RankBadge;
