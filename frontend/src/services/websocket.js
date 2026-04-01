import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { config } from '../config'

/**
 * WebSocket / STOMP service — singleton wrapper around @stomp/stompjs Client.
 *
 * Usage:
 *   wsService.connect()
 *   wsService.on('connected',   () => { ... })
 *   wsService.on('state',       (gameState) => { ... })
 *   wsService.on('error',       (msg) => { ... })
 *   wsService.on('disconnected',() => { ... })
 *
 *   wsService.createRoom('Alice')
 *   wsService.joinRoom('ABCD1234', 'Bob')
 *   wsService.makeMove(roomId, position, symbol)
 *   wsService.requestRematch(roomId)
 *   wsService.disconnect()
 */
class WebSocketService {
  constructor() {
    this._client          = null
    this._listeners       = {}        // event → [callback]
    this._roomSub         = null      // current /topic/room/X subscription
    this._privateSub      = null      // /user/queue/room subscription
    this._reconnectCount  = 0
    this._connected       = false
  }

  // ── Connection ──────────────────────────────────────────────────────

  connect() {
    if (this._client && this._connected) return

    this._client = new Client({
      // Use SockJS factory so the library handles WS + HTTP fallbacks
      webSocketFactory: () => new SockJS(config.WS_ENDPOINT),

      reconnectDelay: config.RECONNECT_DELAY_MS,

      onConnect: () => {
        this._connected = true
        this._reconnectCount = 0
        console.log('[WS] Connected')

        // Subscribe to private reply channel
        this._privateSub = this._client.subscribe(
          config.STOMP.PRIVATE_QUEUE,
          (frame) => this._handleFrame(frame)
        )

        this._emit('connected')
      },

      onDisconnect: () => {
        this._connected = false
        console.log('[WS] Disconnected')
        this._emit('disconnected')
      },

      onStompError: (frame) => {
        console.error('[WS] STOMP error:', frame)
        this._emit('error', frame.headers?.message || 'STOMP error')
      },

      onWebSocketError: (event) => {
        console.error('[WS] WebSocket error:', event)
        this._emit('error', 'WebSocket connection error')
      },
    })

    this._client.activate()
  }

  disconnect() {
    this._roomSub?.unsubscribe()
    this._privateSub?.unsubscribe()
    this._client?.deactivate()
    this._client    = null
    this._connected = false
    this._listeners = {}
    console.log('[WS] Manually disconnected')
  }

  isConnected() {
    return this._connected && this._client?.connected
  }

  // ── STOMP send helpers ──────────────────────────────────────────────

  createRoom(playerName, playerId) {
    this._send(config.STOMP.CREATE_ROOM, { name: playerName, playerId })
  }

  joinRoom(roomId, playerName, playerId) {
    this._send(config.STOMP.JOIN_ROOM, { roomId: roomId.toUpperCase(), name: playerName, playerId })
  }

  makeMove(roomId, position, symbol) {
    this._send(config.STOMP.MAKE_MOVE, { roomId, position, symbol })
  }

  requestRematch(roomId) {
    this._send(config.STOMP.REMATCH, { roomId })
  }

  // ── Room topic subscription ─────────────────────────────────────────

  /**
   * Subscribe (or re-subscribe) to /topic/room/{roomId}.
   * Automatically unsubscribes from the previous room.
   */
  subscribeToRoom(roomId) {
    this._roomSub?.unsubscribe()
    const dest = config.STOMP.ROOM_TOPIC(roomId)
    this._roomSub = this._client.subscribe(dest, (frame) => this._handleFrame(frame))
    console.log('[WS] Subscribed to', dest)
  }

  // ── Event emitter ───────────────────────────────────────────────────

  /**
   * Register a listener for a named event.
   * Returns an unsubscribe function.
   *
   * Events: 'connected' | 'disconnected' | 'state' | 'error'
   */
  on(event, callback) {
    if (!this._listeners[event]) this._listeners[event] = []
    this._listeners[event].push(callback)
    return () => this.off(event, callback)
  }

  off(event, callback) {
    if (!this._listeners[event]) return
    this._listeners[event] = this._listeners[event].filter((cb) => cb !== callback)
  }

  // ── Private ─────────────────────────────────────────────────────────

  _send(destination, body) {
    if (!this.isConnected()) {
      console.warn('[WS] Not connected — cannot send to', destination)
      return
    }
    this._client.publish({ destination, body: JSON.stringify(body) })
  }

  _handleFrame(frame) {
    try {
      const data = JSON.parse(frame.body)

      if (data.error) {
        this._emit('error', data.error)
        return
      }

      // Emit raw state; GamePage decides what changed
      this._emit('state', data)
    } catch (err) {
      console.error('[WS] Failed to parse frame:', err)
    }
  }

  _emit(event, data) {
    ;(this._listeners[event] || []).forEach((cb) => {
      try { cb(data) } catch (e) { console.error('[WS] Listener error:', e) }
    })
  }
}

// Singleton export
const wsService = new WebSocketService()
export default wsService
