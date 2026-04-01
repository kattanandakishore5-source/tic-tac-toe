import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../config';

const GameHistoryPage = () => {
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);

    const playerId = localStorage.getItem('playerId');

    useEffect(() => {
        if (!playerId) {
            setLoading(false);
            return;
        }

        axios.get(`${API_BASE_URL}/api/profile/${playerId}`)
            .then(res => {
                setHistory(res.data.gameHistory || []);
                setLoading(false);
            })
            .catch(err => {
                console.error("Error fetching history", err);
                setLoading(false);
            });
    }, [playerId]);

    if (loading) return <div className="loading">Accessing Archives...</div>;

    const renderResult = (result) => {
        switch(result) {
            case 'WIN': return <span className="result-win">VICTORY</span>;
            case 'LOSS': return <span className="result-loss">DEFEAT</span>;
            default: return <span className="result-draw">DRAW</span>;
        }
    };

    return (
        <div className="history-container glossy">
            <h2>Combat Records</h2>
            {history.length === 0 ? (
                <p className="empty-state">No combat records found in the archive.</p>
            ) : (
                <div className="table-responsive">
                    <table className="history-table">
                        <thead>
                            <tr>
                                <th>Opponent</th>
                                <th>Date</th>
                                <th>Result</th>
                                <th>Room ID</th>
                            </tr>
                        </thead>
                        <tbody>
                            {history.map((record, index) => (
                                <tr key={index}>
                                    <td className="opponent-cell">
                                        <span className="opponent-name">{record.opponentName}</span>
                                        <span className="opponent-id">#{record.opponentId}</span>
                                    </td>
                                    <td>{new Date(record.playedAt).toLocaleDateString()} {new Date(record.playedAt).toLocaleTimeString()}</td>
                                    <td>{renderResult(record.result)}</td>
                                    <td className="room-id-cell">{record.roomId}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default GameHistoryPage;
