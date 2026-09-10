# Use short-lived JWTs with rotating refresh tokens

CineFlow uses fifteen-minute JWT access tokens held in browser memory and eight-hour opaque refresh tokens stored in secure HttpOnly cookies. Although server sessions would be simpler for the same-origin deployment, token refresh, rotation, revocation, and reuse detection are intentional learning goals; hashed refresh-token families are stored server-side, and authenticated STOMP connections use the in-memory access token.
