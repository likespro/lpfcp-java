package eth.likespro.lpfcp.calculator;

import eth.likespro.lpfcp.LPFCP;

public class CalculatorImpl implements Calculator {
    @Override @LPFCP.ExposedFunction
    public int add(int a, int b) {
        return a + b;
    }

    @Override @LPFCP.ExposedFunction
    public int subtract(int a, int b) {
        return a - b;
    }
}
