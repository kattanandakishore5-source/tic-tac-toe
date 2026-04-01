import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../config';
import RankBadge from '../components/RankBadge';

const ProfilePage = () => {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const playerId = localStorage.getItem('playerId');

    useEffect(() => {
        if (!playerId) {
            setLoading(false);
            return;
        }

        axios.get(`${API_BASE_URL}/api/profile/${playerId}`)
            .then(res => {
                setProfile(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Error fetching profile", err);
                setLoading(false);
            });
    }, [playerId]);

    if (loading) return <div className="loading">Initializing Neural Link...</div>;
    if (!profile) return <div className="error-card">Failed to load profile parameters.</div>;

    const winRate = profile.totalGames > 0 
        ? ((profile.wins / profile.totalGames) * 100).toFixed(1) 
        : 0;

    const initials = profile.displayName ? profile.displayName.substring(0, 2).toUpperCase() : '??';

    return (
        <div className="profile-container">
            <div className="profile-card glossy">
                <div className="profile-header">
                    <div className="profile-avatar" style={{ backgroundColor: profile.avatarColor }}>
                        {initials}
                    </div>
                    <div className="profile-info">
                        <h2>{profile.displayName}</h2>
                        <div className="profile-id">ID: {profile.playerId}</div>
                        <RankBadge rank={profile.rank} />
                    </div>
                </div>

                <div className="stats-grid">
                    <div className="stat-box">
                        <span className="stat-value">{profile.wins}</span>
                        <span className="stat-label">WINS</span>
                    </div>
                    <div className="stat-box">
                        <span className="stat-value">{profile.losses}</span>
                        <span className="stat-label">LOSSES</span>
                    </div>
                    <div className="stat-box">
                        <span className="stat-value">{profile.draws}</span>
                        <span className="stat-label">DRAWS</span>
                    </div>
                    <div className="stat-box">
                        <span className="stat-value">{winRate}%</span>
                        <span className="stat-label">WIN RATE</span>
                    </div>
                </div>

                <div className="profile-footer">
                    <div>User Since: {new Date(profile.createdAt).toLocaleDateString()}</div>
                    <div>Last Online: {profile.lastOnline ? new Date(profile.lastOnline).toLocaleString() : 'Never'}</div>
                    <div>Last Played: {profile.lastPlayed ? new Date(profile.lastPlayed).toLocaleString() : 'Never'}</div>
                </div>
            </div>
        </div>
    );
};

export default ProfilePage;
