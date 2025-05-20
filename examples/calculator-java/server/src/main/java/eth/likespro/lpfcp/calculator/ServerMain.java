package eth.likespro.lpfcp.calculator;

import eth.likespro.lpfcp.LPFCPJava;
import eth.likespro.lpfcp.ktor.Ktor;

public class ServerMain {
    public static void main(String[] args) {
        // Creating a calculator object like always
        Calculator calculator = new CalculatorImpl();

        // Starting LPFCP Server, which exposes all annotated calculator functions
        Ktor.INSTANCE.lpfcpServer(calculator, 8080)
                .start(true);
    }
}
