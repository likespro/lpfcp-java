package eth.likespro.lpfcp.calculator

import eth.likespro.lpfcp.LPFCP

fun main() {
    // Creating a calculator object from Calculator interface with LPFCP
    val calculator: Calculator = LPFCP.getProcessor<Calculator>(
        "http://localhost:8080/lpfcp" // If the calculator server is hosted on the same machine
    )

    // Call Calculator functions as the calculator was just a regular implementation
    println(calculator.add(1, 2))       // -> 3
    println(calculator.subtract(5, 3))  // -> 2
}