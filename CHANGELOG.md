### Improvements
`core` module:
- Made JSON library (`org.json`) an `api` dependency
- Disabled types auto-boxing when working with Java primitives

`ktor` module:
- Updated Ktor to version `3.1.3`
- `Ktor.LPFCPServer` now holds the entire embedded server instead of `NettyApplicationEngine`
- Added `eraseStackTraces` param to `lpfcpServer` and `lpfcp` route. 
When it is set — all stack traces are automatically erased from responses to a client.
This will help to hide internal server implementations from users.
### Other
- Updated dependencies' versions

