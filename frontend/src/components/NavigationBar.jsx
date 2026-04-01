import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { API_BASE_URL } from '../config';
import RankBadge from './RankBadge';

const NavigationBar = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [profile, setProfile] = useState(null);

    const playerId = localStorage.getItem('playerId');
    const username = localStorage.getItem('username');

    useEffect(() => {
        if (playerId) {
            axios.get(`${API_BASE_URL}/api/profile/${playerId}`)
                .then(res => setProfile(res.data))
                .catch(err => console.error("Could not fetch profile", err));
        }
    }, [playerId, location.pathname]); // re-fetch on navigation

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('playerId');
        localStorage.removeItem('username');
        navigate('/login');
    };

    return (
        <nav className="navbar">
            <div className="nav-brand">T3RMINAL</div>
            <div className="nav-links">
                <Link to="/game" className={location.pathname === '/game' ? 'active' : ''}>Play</Link>
                <Link to="/profile" className={location.pathname === '/profile' ? 'active' : ''}>Profile</Link>
                <Link to="/friends" className={location.pathname === '/friends' ? 'active' : ''}>Friends</Link>
                <Link to="/history" className={location.pathname === '/history' ? 'active' : ''}>History</Link>
            </div>
            <div className="nav-user">
                {profile ? (
                    <>
                        <span className="nav-username">{profile.displayName}</span>
                        <RankBadge rank={profile.rank} small={true} />
                    </>
                ) : (
                    <span className="nav-username">{username}</span>
                )}
                <button className="logout-btn" onClick={handleLogout}>Logout</button>
            </div>
        </nav>
    );
};

export default NavigationBar;
