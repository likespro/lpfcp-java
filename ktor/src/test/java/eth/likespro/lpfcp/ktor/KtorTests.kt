package eth.likespro.lpfcp.ktor

import eth.likespro.commons.models.WrappedException
import eth.likespro.lpfcp.LPFCP
import eth.likespro.lpfcp.ktor.Ktor.lpfcpServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

class KtorTests {
    interface Calculator {
        fun hello(): String
        fun add(a: Int, b: Int): Int
        fun add(a: String = "1", b: String = "2"): String
        fun multiply(a: Int, b: Int): Int
        fun divide(a: Int, b: Int): Int
        fun divideSafely(a: Int, b: Int): Int?
    }

    companion object {
        val calculator = object : Calculator {
            @LPFCP.ExposedFunction
            override fun hello(): String = "Hello, World!"

            @LPFCP.ExposedFunction
            override fun add(a: Int, b: Int): Int = a + b

            @LPFCP.ExposedFunction
            override fun add(a: String, b: String): String = a + b

            override fun multiply(a: Int, b: Int) = a * b

            @LPFCP.ExposedFunction
            override fun divide(a: Int, b: Int) = a / b

            @LPFCP.ExposedFunction
            override fun divideSafely(a: Int, b: Int): Int? = if (b == 0) null else a / b
        }

        var server: Ktor.LPFCPServer? = null

        @JvmStatic
        @BeforeAll fun setupKtorServer() {
            server = lpfcpServer(calculator).start(wait = false)
        }

        @JvmStatic
        @AfterAll fun teardownKtorServer() {
            server?.stop(0, 0)
        }
    }



    /*
     * --------------------------------------------------------------
     *                        KTOR SERVER SIDE
     * --------------------------------------------------------------
     */



    @Test fun ktorServer_withValidFunctionWithNoArgsAndNoArgs_returnsSuccess() {
        val processor = LPFCP.getProcessor<Calculator>(URI("http://localhost:8080/lpfcp"))
        val response = processor.hello()
        assertEquals("Hello, World!", response)
    }

    @Test fun ktorServer_withValidFunctionAndArgs_returnsSuccess() {
        val processor = LPFCP.getProcessor<Calculator>("http://localhost:8080/lpfcp")
        val response = processor.add(3, 5)
        assertEquals(8, response)
    }

    @Test fun getProcessor_withLambda_proceedsRequest_withValidFunctionReturningNullAndArgs_returnsSuccess() {
        val processor = LPFCP.getProcessor<Calculator>("http://localhost:8080/lpfcp")
        val response = processor.divideSafely(3, 0)
        assertEquals(null, response)
    }

    @Test fun getProcessor_withLambda_proceedsRequest_withValidOverloadedFunctionAndArgs_returnsSuccess() {
        val processor = LPFCP.getProcessor<Calculator>("http://localhost:8080/lpfcp")
        val response = processor.add("3", "5")
        assertEquals("35", response)
    }

    @Test fun getProcessor_withLambda_proceedsRequest_withValidFunctionWithDefaultArgsAndArgs_returnsSuccess() {
        val processor = LPFCP.getProcessor<Calculator>("http://localhost:8080/lpfcp")
        val response = processor.add(b = "5")
        assertEquals("15", response)
    }

    @Test fun getProcessor_withLambda_proceedsRequest_withValidFunctionWithDefaultArgsAndArgsInReverseOrder_returnsSuccess() {
        val processor = LPFCP.getProcessor<Calculator>("http://localhost:8080/lpfcp")
        val response = processor.add(b = "5", a = "4")
        assertEquals("45", response)
    }

    @Test fun getProcessor_withLambda_proceedsRequest_withFunctionThrowingException_returnsExecutedFunctionThrowException() {
        val processor = LPFCP.getProcessor<Calculator>("http://localhost:8080/lpfcp")
        assertEquals(
            LPFCP.ExecutedFunctionThrowException::class.java,
            assertThrows<WrappedException.Exception> { processor.divide(5, 0) }.wrappedException.exceptionClass
        )
    }
}