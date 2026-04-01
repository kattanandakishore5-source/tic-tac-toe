import { config } from '../config'

const { GAME_STATUS } = config

/**
 * Displays scoreboard + current game status message.
 *
 * Props:
 *   gameState      — full GameState object from server (or null)
 *   mySymbol       — 'X' | 'O' | null  (this client's symbol)
 *   scores         — { X: number, O: number, draws: number }
 *   connectionStatus — 'connecting' | 'connected' | 'disconnected'
 *   roomId         — string | null
 */
const GameStatus = ({ gameState, mySymbol, scores, connectionStatus, roomId }) => {
  // ── Status text ────────────────────────────────────────────────────

  const getStatusText = () => {
    if (connectionStatus === 'connecting')    return 'Connecting…'
    if (connectionStatus === 'disconnected')  return 'Disconnected — trying to reconnect'
    if (!gameState) return 'Create or join a room to play'

    switch (gameState.status) {
      case GAME_STATUS.WAITING:
        return `Room ${roomId} — share the code with a friend!`
      case GAME_STATUS.IN_PROGRESS: {
        const isMyTurn = gameState.currentTurn === mySymbol
        return isMyTurn
          ? `Your turn (${mySymbol})`
          : `Opponent's turn (${gameState.currentTurn})`
      }
      case GAME_STATUS.X_WON:
        return mySymbol === 'X' ? '🏆 You win!' : '😞 You lose'
      case GAME_STATUS.O_WON:
        return mySymbol === 'O' ? '🏆 You win!' : '😞 You lose'
      case GAME_STATUS.DRAW:
        return "🤝 It's a draw!"
      case GAME_STATUS.ABANDONED:
        return '👋 Opponent disconnected'
      default:
        return gameState.message || '—'
    }
  }

  const getStatusColor = () => {
    if (!gameState) return 'status-neutral'
    switch (gameState.status) {
      case GAME_STATUS.X_WON:     return 'status-x'
      case GAME_STATUS.O_WON:     return 'status-o'
      case GAME_STATUS.DRAW:
      case GAME_STATUS.ABANDONED: return 'status-neutral'
      case GAME_STATUS.IN_PROGRESS:
        return gameState.currentTurn === 'X' ? 'status-x' : 'status-o'
      default:                    return 'status-neutral'
    }
  }

  // ── Player names ───────────────────────────────────────────────────
  const xName = gameState?.playerX?.name || 'X'
  const oName = gameState?.playerO?.name || 'O'

  return (
    <div className="game-status-wrapper">

      {/* Scoreboard */}
      <div className="scoreboard">
        <div className={`score-item${mySymbol === 'X' ? ' score-mine' : ''}`}>
          <span className="score-symbol score-symbol-x">X</span>
          <span className="score-name">{xName}</span>
          <span className="score-value">{scores.X}</span>
        </div>

        <div className="score-center">
          <span className="score-draws-num">{scores.draws}</span>
          <span className="score-draws-label">draws</span>
        </div>

        <div className={`score-item score-item-right${mySymbol === 'O' ? ' score-mine' : ''}`}>
          <span className="score-value">{scores.O}</span>
          <span className="score-name">{oName}</span>
          <span className="score-symbol score-symbol-o">O</span>
        </div>
      </div>

      {/* Status bar */}
      <div className={`status-bar ${getStatusColor()}`}>
        <span className="status-text">{getStatusText()}</span>
      </div>

    </div>
  )
}

export default GameStatus
