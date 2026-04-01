import { useState, useEffect, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import Board from '../components/Board'
import GameStatus from '../components/GameStatus'
import wsService from '../services/websocket'
import { config } from '../config'

const { GAME_STATUS } = config
const EMPTY_BOARD = Array(9).fill(null)

const GamePage = () => {
  const navigate = useNavigate()

  // ── Connection ────────────────────────────────────────────────────
  const [connectionStatus, setConnectionStatus] = useState('disconnected')

  // ── Identity ──────────────────────────────────────────────────────
  const [playerName, setPlayerName] = useState(localStorage.getItem('username') || '')
  const [mySymbol, setMySymbol] = useState(null)

  useEffect(() => {
    if (!localStorage.getItem('username')) {
      navigate('/login');
    }
  }, [navigate]);

  // ── Room ──────────────────────────────────────────────────────────
  const [roomId, setRoomId] = useState(null)
  const [roomIdInput, setRoomIdInput] = useState('')

  // ── Game state (from server) ──────────────────────────────────────
  const [gameState, setGameState] = useState(null)

  // ── Local scores ──────────────────────────────────────────────────
  const [scores, setScores] = useState({ X: 0, O: 0, draws: 0 })
  const prevStatusRef = useRef(null)

  // ── Symbol lock — set once and never changed ──────────────────────
  const symbolLockedRef = useRef(false)

  // ── Derived board / UI ────────────────────────────────────────────
  const board = gameState?.board || EMPTY_BOARD
  const winningLine = gameState?.winningLine || null
  const currentTurn = gameState?.currentTurn || 'X'
  const status = gameState?.status || null
  const gameOver =
    status &&
    status !== GAME_STATUS.WAITING &&
    status !== GAME_STATUS.IN_PROGRESS
  const isMyTurn =
    !gameOver &&
    currentTurn === mySymbol &&
    status === GAME_STATUS.IN_PROGRESS

  // ── WebSocket setup ───────────────────────────────────────────────
  useEffect(() => {
    setConnectionStatus('connecting')
    wsService.connect()

    const offConnected = wsService.on('connected', () =>
      setConnectionStatus('connected')
    )
    const offDisconnected = wsService.on('disconnected', () =>
      setConnectionStatus('disconnected')
    )
    const offError = wsService.on('error', (msg) =>
      console.error('[WS Error]', msg)
    )
    const offState = wsService.on('state', handleServerState)

    return () => {
      offConnected()
      offDisconnected()
      offError()
      offState()
      wsService.disconnect()
    }
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  // ── Handle incoming game state ────────────────────────────────────
  const handleServerState = useCallback((data) => {
    // Determine and lock my symbol from the FIRST private queue message only
    if (!symbolLockedRef.current) {
      let assignedSymbol = null

      if (data.playerX && !data.playerO) {
        // Only X exists — I just created the room — I am X
        assignedSymbol = 'X'
      } else if (data.playerX && data.playerO) {
        // Both players exist — I just joined — I am O
        assignedSymbol = 'O'
      }

      if (assignedSymbol) {
        setMySymbol(assignedSymbol)
        symbolLockedRef.current = true
        console.log('[Game] My symbol assigned:', assignedSymbol)
      }
    }

    // Subscribe to room broadcast topic if this is a new room
    if (data.roomId) {
      setRoomId((prev) => {
        if (prev !== data.roomId) {
          wsService.subscribeToRoom(data.roomId)
        }
        return data.roomId
      })
    }

    setGameState(data)
  }, [])

  // ── Score tracking ────────────────────────────────────────────────
  useEffect(() => {
    if (!status) return
    const prev = prevStatusRef.current
    if (status === prev) return
    prevStatusRef.current = status

    if (status === GAME_STATUS.X_WON)
      setScores((s) => ({ ...s, X: s.X + 1 }))
    if (status === GAME_STATUS.O_WON)
      setScores((s) => ({ ...s, O: s.O + 1 }))
    if (status === GAME_STATUS.DRAW)
      setScores((s) => ({ ...s, draws: s.draws + 1 }))
  }, [status])

  // ── Actions ───────────────────────────────────────────────────────

  const handleCreateRoom = () => {
    const name = playerName.trim() || 'Player'
    const playerId = localStorage.getItem('playerId')
    symbolLockedRef.current = false
    setMySymbol(null)
    wsService.createRoom(name, playerId)
  }

  const handleJoinRoom = () => {
    const name = playerName.trim() || 'Player'
    const playerId = localStorage.getItem('playerId')
    const id = roomIdInput.trim().toUpperCase()
    if (!id) return
    symbolLockedRef.current = false
    setMySymbol(null)
    wsService.joinRoom(id, name, playerId)
  }

  const handleSquareClick = (index) => {
    if (!isMyTurn || board[index]) return
    wsService.makeMove(roomId, index, mySymbol)
  }

  const handleRematch = () => {
    wsService.requestRematch(roomId)
    prevStatusRef.current = null
  }

  const handleLeave = () => {
    symbolLockedRef.current = false
    wsService.disconnect()
    setGameState(null)
    setRoomId(null)
    setMySymbol(null)
    setScores({ X: 0, O: 0, draws: 0 })
    prevStatusRef.current = null
    setConnectionStatus('connecting')
    setTimeout(() => wsService.connect(), 100)
  }

  // ── Render ────────────────────────────────────────────────────────
  const inRoom = !!roomId && !!gameState

  return (
    <div className="game-page">

      {/* Header */}
      <header className="game-header" style={{ position: 'relative' }}>
        <button 
          className="btn btn-ghost" 
          style={{ position: 'absolute', top: '-1rem', right: '-1rem', padding: '0.5rem', fontSize: '0.7rem' }}
          onClick={() => {
            handleLeave();
            localStorage.clear();
            navigate('/login');
          }}
        >
          LOGOUT
        </button>
        <h1 className="game-title">
          TIC<span className="title-sep">—</span>TAC
          <span className="title-sep">—</span>TOE
        </h1>
        <p className="game-subtitle">MULTIPLAYER · REAL-TIME</p>
      </header>

      {/* Lobby */}
      {!inRoom && (
        <div className="lobby">
          <div className="lobby-field">
            <label className="lobby-label" style={{textAlign: 'center', color: '#00d4ff'}}>
              WELCOME, {playerName.toUpperCase()}
            </label>
          </div>

          <div className="lobby-actions" style={{ marginTop: '1rem' }}>
            <button
              className="btn btn-primary"
              onClick={handleCreateRoom}
              disabled={connectionStatus !== 'connected'}
            >
              CREATE ROOM
            </button>

            <div className="lobby-divider">OR</div>

            <div className="lobby-join-row">
              <input
                className="lobby-input lobby-input-code"
                value={roomIdInput}
                onChange={(e) =>
                  setRoomIdInput(e.target.value.toUpperCase())
                }
                placeholder="ROOM CODE"
                maxLength={8}
                onKeyDown={(e) => e.key === 'Enter' && handleJoinRoom()}
              />
              <button
                className="btn btn-secondary"
                onClick={handleJoinRoom}
                disabled={
                  connectionStatus !== 'connected' || !roomIdInput.trim()
                }
              >
                JOIN
              </button>
            </div>
          </div>

          <p className={`conn-badge conn-${connectionStatus}`}>
            {connectionStatus === 'connected' && '● Connected'}
            {connectionStatus === 'connecting' && '○ Connecting…'}
            {connectionStatus === 'disconnected' && '✕ Disconnected'}
          </p>
        </div>
      )}

      {/* In-room view */}
      {inRoom && (
        <>
          {/* Debug indicator — remove in production */}
          <p style={{
            fontSize: '0.65rem',
            color: mySymbol === 'X' ? '#ff3b3b' : '#00d4ff',
            letterSpacing: '0.2em',
            textTransform: 'uppercase'
          }}>
            YOU ARE PLAYING AS: {mySymbol || '…'}
          </p>

          <GameStatus
            gameState={gameState}
            mySymbol={mySymbol}
            scores={scores}
            connectionStatus={connectionStatus}
            roomId={roomId}
          />

          <Board
            squares={board}
            onSquareClick={handleSquareClick}
            winningSquares={winningLine}
            currentPlayer={isMyTurn ? mySymbol : null}
            gameOver={!isMyTurn || gameOver}
          />

          <div className="game-actions">
            {gameOver && (
              <button className="btn btn-primary" onClick={handleRematch}>
                REMATCH
              </button>
            )}
            <button className="btn btn-ghost" onClick={handleLeave}>
              LEAVE ROOM
            </button>
          </div>

          <p className="room-badge">ROOM · {roomId}</p>
        </>
      )}
    </div>
  )
}

export default GamePage
