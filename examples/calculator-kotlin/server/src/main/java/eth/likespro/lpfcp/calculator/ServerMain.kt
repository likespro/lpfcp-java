package eth.likespro.lpfcp.calculator

import eth.likespro.lpfcp.ktor.Ktor.lpfcpServer

fun main() {
    // Creating a calculator object like always
    val calculator = CalculatorImpl()

    // Starting LPFCP Server, which exposes all annotated calculator functions
    lpfcpServer(processor = calculator, port = 8080)
        .start(wait = true)
}