/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * From https://github.com/likespro/lpfcp-java
 */

package eth.likespro.lpfcp

import eth.likespro.commons.reflection.ObjectEncoding.encodeObject
import org.json.JSONObject
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

/**
 * A handler for function calls that processes the request and returns the result. Used in Java/Kotlin proxies.
 *
 * @param processor A lambda function that takes a [JSONObject] request and a [Type] and returns the result of the function call.
 */
class FunctionCallHandler(val processor: (JSONObject, Type) -> Any?) : InvocationHandler {
    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        return processor(JSONObject().apply {
            put("functionName", method.name)
            put("functionArgs", JSONObject().apply {
                args?.forEachIndexed { index, arg ->
                    put("${index + 1}", arg.encodeObject())
                }
            })
        }, method.kotlinFunction!!.returnType.javaType)
    }
}