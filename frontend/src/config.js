// ─────────────────────────────────────────────
//  App-wide configuration
// ─────────────────────────────────────────────

export const config = {
  // WebSocket / STOMP endpoint (proxied to Spring Boot via vite.config.js)
  WS_ENDPOINT: import.meta.env.VITE_WS_URL || '/ws',

  // STOMP destination prefixes (must match WebSocketConfig.java)
  STOMP: {
    // Client → Server
    CREATE_ROOM:    '/app/room/create',
    JOIN_ROOM:      '/app/room/join',
    MAKE_MOVE:      '/app/game/move',
    REMATCH:        '/app/game/rematch',

    // Server → Client (subscribe)
    ROOM_TOPIC:     (roomId) => `/topic/room/${roomId}`,
    PRIVATE_QUEUE:  '/user/queue/room',
  },

  // Board
  WINNING_COMBINATIONS: [
    [0, 1, 2], [3, 4, 5], [6, 7, 8], // rows
    [0, 3, 6], [1, 4, 7], [2, 5, 8], // columns
    [0, 4, 8], [2, 4, 6],            // diagonals
  ],

  PLAYERS: { X: 'X', O: 'O' },

  GAME_STATUS: {
    WAITING:     'WAITING',
    IN_PROGRESS: 'IN_PROGRESS',
    X_WON:       'X_WON',
    O_WON:       'O_WON',
    DRAW:        'DRAW',
    ABANDONED:   'ABANDONED',
  },

  // Reconnect settings
  RECONNECT_DELAY_MS:    5000,
  MAX_RECONNECT_ATTEMPTS: 5,
}

export const API_BASE_URL = '';
export default config
