import Square from './Square'
import '../styles/board.css'

/**
 * 3×3 game board.
 *
 * Props:
 *   squares        — string[9]  ('X' | 'O' | null)
 *   onSquareClick  — (index: number) => void
 *   winningSquares — number[] | null   (indices of the winning line)
 *   currentPlayer  — 'X' | 'O'
 *   gameOver       — boolean
 */
const Board = ({ squares, onSquareClick, winningSquares, currentPlayer, gameOver }) => {
  const winSet = new Set(winningSquares || [])
  const hasWinner = winSet.size > 0

  return (
    <div className="board-wrapper">
      <div className="board-grid" role="grid" aria-label="Tic Tac Toe board">
        {squares.map((value, index) => (
          <Square
            key={index}
            value={value}
            onClick={() => onSquareClick(index)}
            isWinning={winSet.has(index)}
            isDimmed={hasWinner && !winSet.has(index)}
            currentPlayer={currentPlayer}
            disabled={gameOver}
          />
        ))}
      </div>
    </div>
  )
}

export default Board
