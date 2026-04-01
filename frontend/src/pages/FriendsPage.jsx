import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../config';
import RankBadge from '../components/RankBadge';

const FriendsPage = () => {
    const [friends, setFriends] = useState([]);
    const [pending, setPending] = useState([]);
    const [newFriendId, setNewFriendId] = useState('');
    const [searchStatus, setSearchStatus] = useState(null);

    const playerId = localStorage.getItem('playerId');

    const fetchFriendsData = () => {
        if (!playerId) return;
        
        // Fetch profile to get friends array
        axios.get(`${API_BASE_URL}/api/profile/${playerId}`).then(res => {
            setFriends(res.data.friends || []);
        }).catch(err => console.error(err));

        // Fetch pending requests
        axios.get(`${API_BASE_URL}/api/friends/${playerId}/pending`).then(res => {
            setPending(res.data || []);
        }).catch(err => console.error(err));
    };

    useEffect(() => {
        fetchFriendsData();
    }, [playerId]);

    const sendRequest = (e) => {
        e.preventDefault();
        setSearchStatus(null);
        if (!newFriendId || newFriendId === playerId) return;

        axios.post(`${API_BASE_URL}/api/friends/request`, { senderId: playerId, receiverId: newFriendId })
            .then(res => {
                setSearchStatus({ type: 'success', text: 'Friend request sent!' });
                setNewFriendId('');
            })
            .catch(err => {
                setSearchStatus({ type: 'error', text: err.response?.data?.error || 'Failed to send request' });
            });
    };

    const handleAccept = (senderId) => {
        axios.post(`${API_BASE_URL}/api/friends/accept`, { senderId, receiverId: playerId })
            .then(() => fetchFriendsData())
            .catch(err => console.error(err));
    };

    const handleReject = (senderId) => {
        axios.post(`${API_BASE_URL}/api/friends/reject`, { senderId, receiverId: playerId })
            .then(() => fetchFriendsData())
            .catch(err => console.error(err));
    };

    const isOnline = (lastOnline) => {
        if (!lastOnline || lastOnline === "Unknown") return false;
        const last = new Date(lastOnline);
        const now = new Date();
        return (now - last) < 5 * 60 * 1000; // within 5 mins
    };

    return (
        <div className="friends-container">
            <div className="friends-sidebar glossy">
                <h3>Add Connection</h3>
                <form onSubmit={sendRequest} className="add-friend-form">
                    <input 
                        type="text" 
                        value={newFriendId} 
                        onChange={e => setNewFriendId(e.target.value)} 
                        placeholder="Enter Player ID (6 chars)"
                    />
                    <button type="submit" className="action-btn">Connect</button>
                </form>
                {searchStatus && (
                    <div className={`status-text ${searchStatus.type}`}>{searchStatus.text}</div>
                )}

                {pending.length > 0 && (
                    <div className="pending-section">
                        <h4>Pending Requests</h4>
                        {pending.map(p => (
                            <div key={p.playerId} className="pending-card">
                                <span>{p.displayName}</span>
                                <div className="pending-actions">
                                    <button onClick={() => handleAccept(p.playerId)} className="valid-btn">✓</button>
                                    <button onClick={() => handleReject(p.playerId)} className="danger-btn">✗</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <div className="friends-main glossy">
                <h2>Network</h2>
                {friends.length === 0 ? (
                    <p className="empty-state">Your network is empty. Add connections explicitly.</p>
                ) : (
                    <div className="friends-list">
                        {friends.map(f => (
                            <div key={f.playerId} className="friend-card">
                                <div className="friend-avatar" style={{backgroundColor: f.avatarColor}}>
                                    {f.displayName.substring(0, 2).toUpperCase()}
                                    <span className={`status-indicator ${isOnline(f.lastOnline) ? 'online' : 'offline'}`}></span>
                                </div>
                                <div className="friend-info">
                                    <h4>{f.displayName}</h4>
                                    <span>#{f.playerId}</span>
                                </div>
                                <RankBadge rank={f.rank} small={true} />
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default FriendsPage;
