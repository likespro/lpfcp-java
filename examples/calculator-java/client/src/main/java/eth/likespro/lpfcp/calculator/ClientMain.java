package eth.likespro.lpfcp.calculator;

import eth.likespro.lpfcp.LPFCPJava;

public class ClientMain {
    public static void main(String[] args) {
        // Creating a calculator object from Calculator interface with LPFCP
        Calculator calculator = LPFCPJava.getProcessor(
                Calculator.class,
                "http://localhost:8080/lpfcp" // If the calculator server is hosted on the same machine
        );

        // Call Calculator functions as the calculator was just a regular implementation
        System.out.println(calculator.add(1, 2));       // -> 3
        System.out.println((calculator.subtract(5, 3)));  // -> 2
    }
}
