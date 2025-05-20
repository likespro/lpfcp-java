package eth.likespro.lpfcp.ktor;

import eth.likespro.lpfcp.LPFCP;
import eth.likespro.lpfcp.LPFCPJava;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KtorTestsJava {
    interface Calculator {
        String hello();
        int add(int a, int b);
        String add(String a, String b);
        int multiply(int a, int b);
        int divide(int a, int b);
        Integer divideSafely(int a, int b);
    }

    public static class CalculatorImpl implements Calculator {
        @Override @LPFCP.ExposedFunction
        public String hello() {
            return "Hello, World!";
        }

        @Override @LPFCP.ExposedFunction
        public int add(int a, int b) {
            return a + b;
        }

        @Override @LPFCP.ExposedFunction
        public String add(String a, String b) {
            return a + b;
        }

        @Override
        public int multiply(int a, int b) {
            return a * b;
        }

        @Override @LPFCP.ExposedFunction
        public int divide(int a, int b) {
            return a / b;
        }

        @Override @LPFCP.ExposedFunction
        public Integer divideSafely(int a, int b) {
            if (b == 0) return null;
            return a / b;
        }
    }

    public static CalculatorImpl calculator = new CalculatorImpl();
    public static Ktor.LPFCPServer server;

    @BeforeAll static void setupKtorServer() {
        server = Ktor.INSTANCE.lpfcpServer(calculator, 8080).start(false);
    }

    @AfterAll static void teardownKtorServer() {
        server.stop(0, 0);
    }



    /*
     * --------------------------------------------------------------
     *                        KTOR SERVER SIDE
     * --------------------------------------------------------------
     */



    @Test public void ktorServer_withValidFunctionWithNoArgsAndNoArgs_returnsSuccess() throws URISyntaxException {
        Calculator processor = LPFCPJava.getProcessor(Calculator.class, new URI("http://localhost:8080/lpfcp"));
        String response = processor.hello();
        assertEquals("Hello, World!", response);
    }

    @Test public void ktorServer_withValidFunctionAndArgs_returnsSuccess() {
        Calculator processor = LPFCPJava.getProcessor(Calculator.class, "http://localhost:8080/lpfcp");
        Integer response = processor.add(3, 5);
        assertEquals(8, response);
    }
}
