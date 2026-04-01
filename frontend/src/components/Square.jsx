import { memo } from 'react'
import '../styles/board.css'

/**
 * A single board cell.
 *
 * Props:
 *   value          — 'X' | 'O' | null
 *   onClick        — callback when clicked
 *   isWinning      — highlight this cell as part of the winning line
 *   isDimmed       — grey-out non-winning cells after game ends
 *   currentPlayer  — 'X' | 'O' — used for ghost preview
 *   disabled       — prevent interaction
 */
const Square = ({ value, onClick, isWinning, isDimmed, currentPlayer, disabled }) => {
  const classNames = [
    'square',
    isWinning ? 'winning' : '',
    isDimmed  ? 'dimmed'  : '',
  ].filter(Boolean).join(' ')

  return (
    <button
      className={classNames}
      onClick={onClick}
      disabled={disabled || !!value}
      aria-label={value ? `${value} is here` : `Empty — click to place ${currentPlayer}`}
    >
      {/* Ghost preview shown on hover when cell is empty */}
      {!value && currentPlayer && (
        <span className={`square-ghost ghost-${currentPlayer.toLowerCase()}`}>
          {currentPlayer}
        </span>
      )}

      {/* Placed mark */}
      {value && (
        <span
          key={value}               // re-mount triggers CSS animation
          className={`square-mark mark-${value.toLowerCase()}`}
        >
          {value}
        </span>
      )}
    </button>
  )
}

export default memo(Square)
