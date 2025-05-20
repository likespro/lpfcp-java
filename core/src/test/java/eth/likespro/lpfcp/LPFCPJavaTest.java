package eth.likespro.lpfcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LPFCPJavaTest {
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

    @Test void getProcessor_withLambda_proceedsRequest_withValidFunctionWithNoArgsAndNoArgs_returnsSuccess() {
        Calculator processor = LPFCPJava.getProcessor(Calculator.class, (request, returnType) -> LPFCP.INSTANCE.processRequest(request, calculator).getOrThrow());
        String result = processor.hello();
        assertEquals("Hello, World!", result);
    }
}