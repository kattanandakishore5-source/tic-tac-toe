import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

const RegisterPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      const data = await response.json();
      
      if (response.ok) {
        setSuccess('Registration successful! Redirecting to login...');
        setTimeout(() => navigate('/login'), 1500);
      } else {
        setError(data.message || 'Registration failed');
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
        <p className="game-subtitle">REGISTER</p>
      </header>
      <div className="lobby">
        <form onSubmit={handleRegister} className="lobby-actions" style={{ flexDirection: 'column', gap: '1rem' }}>
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
          {success && <p style={{ color: '#00ff9d', fontSize: '0.8rem', letterSpacing: '0.1em' }}>{success.toUpperCase()}</p>}
          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }}>
            REGISTER
          </button>
        </form>
        <p style={{ marginTop: '2rem', fontSize: '0.8rem', letterSpacing: '0.1em' }}>
          ALREADY HAVE AN ACCOUNT? <Link to="/login" style={{ color: '#00d4ff', textDecoration: 'none' }}>LOGIN</Link>
        </p>
      </div>
    </div>
  );
};

export default RegisterPage;
