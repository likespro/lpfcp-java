### Improvements
`core` module:
- Made JSON library (`org.json`) an `api` dependency
- Disabled types auto-boxing when working with Java primitives
- Remove `ExecutedFunctionThrowException` and replace with direct exception propagation in `LPFCP`.

`ktor` module:
- Updated Ktor to version `3.1.3`
- `Ktor.LPFCPServer` now holds the entire embedded server instead of `NettyApplicationEngine`
- Added `exceptionDetailsConfiguration` parameter to `lpfcpServer` and `lpfcp` route.
It allows specifying the details to be included in response when the exception is thrown.
This will help to hide internal server implementations from users if needed.
### Other
- Updated dependencies' versions

