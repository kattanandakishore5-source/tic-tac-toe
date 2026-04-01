import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      const data = await response.json();
      
      if (response.ok) {
        if (data.token) localStorage.setItem('token', data.token);
        localStorage.setItem('username', data.username);
        if (data.playerId) localStorage.setItem('playerId', data.playerId);
        navigate('/game');
      } else {
        setError(data.message || 'Login failed');
      }
    } catch (err) {
      console.error("Auth Error:", err);
      setError('Network error');
    }
  };

  return (
    <div className="game-page">
      <header className="game-header">
        <h1 className="game-title">
          TIC<span className="title-sep">—</span>TAC<span className="title-sep">—</span>TOE
        </h1>
        <p className="game-subtitle">LOGIN</p>
      </header>
      <div className="lobby">
        <form onSubmit={handleLogin} className="lobby-actions" style={{ flexDirection: 'column', gap: '1rem' }}>
          <div className="lobby-field" style={{ width: '100%' }}>
            <label className="lobby-label">USERNAME</label>
            <input 
              className="lobby-input" 
              value={username} onChange={e => setUsername(e.target.value)} 
              required 
            />
          </div>
          <div className="lobby-field" style={{ width: '100%' }}>
            <label className="lobby-label">PASSWORD</label>
            <input 
              type="password" 
              className="lobby-input" 
              value={password} onChange={e => setPassword(e.target.value)} 
              required 
            />
          </div>
          {error && <p style={{ color: '#ff3b3b', fontSize: '0.8rem', letterSpacing: '0.1em' }}>{error.toUpperCase()}</p>}
          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }}>
            LOGIN
          </button>
        </form>
        <p style={{ marginTop: '2rem', fontSize: '0.8rem', letterSpacing: '0.1em' }}>
          DON'T HAVE AN ACCOUNT? <Link to="/register" style={{ color: '#00d4ff', textDecoration: 'none' }}>REGISTER</Link>
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
