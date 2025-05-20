package eth.likespro.lpfcp.calculator

import eth.likespro.lpfcp.LPFCP

class CalculatorImpl: Calculator {
    @LPFCP.ExposedFunction
    override fun add(a: Int, b: Int): Int = a + b

    @LPFCP.ExposedFunction
    override fun subtract(a: Int, b: Int): Int = a - b
}