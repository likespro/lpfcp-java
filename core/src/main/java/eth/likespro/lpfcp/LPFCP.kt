/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * From https://github.com/likespro/lpfcp-java
 */

package eth.likespro.lpfcp

import eth.likespro.commons.reflection.ObjectEncoding.decodeObject
import eth.likespro.commons.models.EncodableResult
import eth.likespro.commons.network.HTTPUtils.post
import eth.likespro.commons.reflection.ReflectionUtils.boxed
import eth.likespro.commons.reflection.ReflectionUtils.getParametrizedType
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import java.net.URI
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.javaType

/**
 * The `LPFCP` object provides all core methods to make Server-side and Client-side working with LPFCP
 */
object LPFCP {
    /**
     * Annotation to mark functions as exposed for processing requests.
     * Only functions annotated with `@ExposedFunction` can be invoked by `processRequest`.
     */
    @Target(AnnotationTarget.FUNCTION)
    annotation class ExposedFunction

    /**
     * Exception thrown when the `functionName` key is missing or invalid in the request.
     * @param msg The error message describing the issue.
     */
    class IncorrectFunctionNameException(msg: String) : RuntimeException(msg)

    /**
     * Exception thrown when the `functionArgs` key is missing or invalid in the request.
     * @param msg The error message describing the issue.
     */
    class IncorrectFunctionArgsException(msg: String) : RuntimeException(msg)

    /**
     * Exception thrown when no function with the specified name and parameters is found.
     * @param msg The error message describing the issue.
     */
    class NoMatchingFunctionFoundException(msg: String) : RuntimeException(msg)

    /**
     * Exception thrown when the invoked function throws an exception during execution.
     * @param e The original exception thrown by the function.
     */
    class ExecutedFunctionThrowException(e: Throwable) : RuntimeException(e)



    /*
     * --------------------------------------------------------------
     *                         PROCESSOR SIDE
     * --------------------------------------------------------------
     */



    /**
     * Processes a request to invoke a function on the given processor object safely.
     *
     * @param request A `JSONObject` containing the function name and arguments.
     *                - `functionName`: The name of the function to invoke (String).
     *                - `functionArgs`: The arguments for the function (JSONObject).
     * @param processor The object containing the functions to be invoked.
     * @return An `EncodableResult` containing the result of the function call or an error.
     */
    fun processRequest(request: JSONObject, processor: Any): EncodableResult<Any?> {
        return try {
            EncodableResult.success(processRequestUnsafely(request, processor))
        } catch (e: Exception) {
            EncodableResult.failure(e)
        }
    }

    /**
     * Processes a request to invoke a function on the given processor object.
     *
     * @param request A `JSONObject` containing the function name and arguments.
     *                - `functionName`: The name of the function to invoke (String).
     *                - `functionArgs`: The arguments for the function (JSONObject).
     * @param processor The object containing the functions to be invoked.
     * @return A function's return value.
     *
     * @throws IncorrectFunctionNameException If the `functionName` key is missing or invalid.
     * @throws IncorrectFunctionArgsException If the `functionArgs` key is missing or invalid.
     * @throws NoMatchingFunctionFoundException If no matching function is found.
     * @throws ExecutedFunctionThrowException If the invoked function throws an exception.
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun processRequestUnsafely(request: JSONObject, processor: Any): Any? {
        val kClass = processor::class

        // Extract the function name from the request
        val functionName = try { request.getString("functionName") } catch (_: JSONException) { throw IncorrectFunctionNameException("`functionName` key not found or is not String.") }

        // Extract the function arguments from the request
        val functionArgs = try { request.getJSONObject("functionArgs") } catch (_: JSONException) { throw IncorrectFunctionArgsException("`functionArgs` key not found or is not JSON Object.") }

        // Filter functions in the processor class that match the name and are annotated with @ExposedFunction
        val functions = kClass.functions.filter { it.name == functionName && it.findAnnotation<ExposedFunction>() != null }
        functions.forEach { function ->
            val args = mutableMapOf<KParameter, Any?>()

            // Map the provided arguments to the function's parameters
            functionArgs.toMap().forEach { (argName, argValue) ->
                function.parameters.find { it.name == argName || it.index.toString() == argName }?.let { param ->
                    try {
                        args[param] = (argValue as String).decodeObject(param.type.javaType.boxed()) // We need to make .boxed() due to bug in `commons` reflection lib, which doesn't correctly handle Java primitive types
                    } catch (_: Exception) { }
                }
            }

            // Skip the function if not all passed arguments are used
            if(args.size != functionArgs.length()) {
                return@forEach
            }

            // Add the instance parameter if required
            function.instanceParameter!!.let { args[it] = processor }

            try {
                // Invoke the function with the mapped arguments
                val result = function.callBy(args)
                return result
            } catch (e: InvocationTargetException) {
                // Handle exceptions thrown by the invoked function
                throw ExecutedFunctionThrowException(e.cause!!)
            } catch (_: IllegalArgumentException) {
                // Handle argument mismatch errors
                // Skip this function and continue searching for another one
            }
        }

        // Throw an exception if no matching function is found
        throw NoMatchingFunctionFoundException("Function with specified params not found. Ensure the function has @ExposedFunction annotation.")
    }



    /*
     * --------------------------------------------------------------
     *                          INVOKER SIDE
     * --------------------------------------------------------------
     */



    /**
     * Creates a proxy instance of the specified interface that forwards method calls to the provided processor LPFCP URI.
     *
     * @param Interface template parameter - Interface of the processor
     * @param processorLPFCPURI A string representing the processor LPFCP endpoint URI.
     * @return A proxy instance of the specified interface.
     */
    inline fun <reified Interface> getProcessor(processorLPFCPURI: String) = getProcessor<Interface> (URI(processorLPFCPURI))

    /**
     * Creates a proxy instance of the specified interface that forwards method calls to the provided processor LPFCP URI.
     *
     * @param Interface template parameter - Interface of the processor
     * @param processorLPFCP A URI representing the processor LPFCP endpoint.
     * @return A proxy instance of the specified interface.
     */
    inline fun <reified Interface> getProcessor(processorLPFCP: URI) = getProcessor<Interface> { request, type ->
        (processorLPFCP.post(request.toString()).decodeObject(EncodableResult::class.java.getParametrizedType(type.boxed())) as EncodableResult<*>).getOrThrow()
    }

    /**
     * Creates a proxy instance of the specified interface that forwards method calls to the provided processor function.
     *
     * @param Interface template parameter - Interface of the processor
     * @param processorLPFCP A function that takes:
     * - `JSONObject` - function call request in LPFCP format;
     * - `Type` - type of value that the called function must return.
     *
     * [processorLPFCP] returns an `Any?` - value from the invoked function.
     * @return A proxy instance of the specified interface.
     */
    inline fun <reified Interface> getProcessor(noinline processorLPFCP: (JSONObject, Type) -> Any?): Interface {
        return Proxy.newProxyInstance(
            Interface::class.java.classLoader,
            arrayOf(Interface::class.java),
            FunctionCallHandler(processorLPFCP)
        ) as Interface
    }
}