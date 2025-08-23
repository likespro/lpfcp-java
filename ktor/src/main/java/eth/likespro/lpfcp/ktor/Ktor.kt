/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * From https://github.com/likespro/lpfcp-java
 */

package eth.likespro.lpfcp.ktor

import eth.likespro.commons.models.WrappedException
import eth.likespro.commons.reflection.ObjectEncoding.encodeObject
import eth.likespro.lpfcp.LPFCP.ExposedFunction
import eth.likespro.lpfcp.LPFCP.processRequest
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.json.JSONObject

object Ktor {
    /**
     * A class-wrapper that manages the lifecycle of a Netty-based server engine.
     *
     * @property engine The Netty application engine instance used to handle server operations.
     */
    class LPFCPServer(val embeddedServer: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>) {
        /**
         * Starts the Netty application engine.
         *
         * @param wait Specifies whether to block the current thread until the engine stops. Defaults to true.
         * @return The instance of the started NettyApplicationEngine.
         */
        fun start(wait: Boolean): LPFCPServer = this.apply { embeddedServer.start(wait) }

        /**
         * Stops the Netty application engine with the specified grace period and timeout.
         *
         * @param gracePeriodMillis The time in milliseconds to wait for ongoing requests to complete before forcefully stopping. Defaults to 500 milliseconds.
         * @param timeoutMillis The maximum time in milliseconds to wait for the engine to stop. Defaults to 1500 milliseconds.
         */
        fun stop(gracePeriodMillis: Long = 500, timeoutMillis: Long = 1500) = embeddedServer.engine.stop(gracePeriodMillis, timeoutMillis)
    }

    /**
     * Creates an embedded Ktor server to handle LPFCP requests.
     * Has only one route - `/lpfcp` - endpoint for LPFCP protocol.
     *
     * @param processor The object containing the functions to be invoked with [ExposedFunction] annotation.
     * @param port The port on which the server will listen to (default is `8080`).
     * @param exceptionDetailsConfiguration The configuration for the exception details to be included in the response
     * (default is [WrappedException.DetailsConfiguration.INCLUDE_ALL]).
     */
    fun lpfcpServer(processor: Any, port: Int = 8080, exceptionDetailsConfiguration: WrappedException.DetailsConfiguration = WrappedException.DetailsConfiguration.INCLUDE_ALL) = LPFCPServer(embeddedServer(Netty, port) {
        routing {
            lpfcp(processor, exceptionDetailsConfiguration = exceptionDetailsConfiguration)
        }
    })

    /**
     * Ktor route to handle LPFCP requests.
     *
     * @param processor The object containing the functions to be invoked with [ExposedFunction] annotation.
     * @param path The path for the LPFCP endpoint (default is "/lpfcp").
     * @param exceptionDetailsConfiguration The configuration for the exception details to be included in the response
     * (default is [WrappedException.DetailsConfiguration.INCLUDE_ALL]).
     */
    fun Route.lpfcp(processor: Any, path: String = "/lpfcp", exceptionDetailsConfiguration: WrappedException.DetailsConfiguration = WrappedException.DetailsConfiguration.INCLUDE_ALL) {
        post(path) {
            val request = JSONObject(call.receiveText())
            call.respond(processRequest(request, processor)
                    .applyExceptionDetailsConfiguration(exceptionDetailsConfiguration)
                    .encodeObject())
        }
    }
}