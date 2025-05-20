/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * From https://github.com/likespro/lpfcp-java
 */

package eth.likespro.lpfcp;

import eth.likespro.commons.models.EncodableResult;
import eth.likespro.commons.network.HTTPUtils;
import eth.likespro.commons.reflection.ObjectEncoding;
import eth.likespro.commons.reflection.ReflectionUtils;

import kotlin.jvm.functions.Function2;
import org.json.JSONObject;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;

/**
 * Class LPFCPJava contains some methods from LPFCP object rewritten specifically for Java.
 */
public class LPFCPJava {
    /**
     * Creates a proxy instance of the specified interface that forwards method calls to the provided processor LPFCP URI.
     *
     * @param <Interface> Interface of the processor
     * @param interfaceClass Interface class of the processor
     * @param processorLPFCPURI A string representing the processor LPFCP endpoint URI.
     * @return A proxy instance of the specified interface.
     */
    public static <Interface> Interface getProcessor(Class<Interface> interfaceClass, String processorLPFCPURI) {
        return getProcessor(interfaceClass, URI.create(processorLPFCPURI));
    }

    /**
     * Creates a proxy instance of the specified interface that forwards method calls to the provided processor LPFCP URI.
     *
     * @param <Interface> Interface of the processor
     * @param interfaceClass Interface class of the processor
     * @param processorLPFCP A URI representing the processor LPFCP endpoint.
     *
     * @return A proxy instance of the specified interface.
     */
    public static <Interface> Interface getProcessor(Class<Interface> interfaceClass, URI processorLPFCP) {
        return getProcessor(interfaceClass, (request, returnType) -> {
            //noinspection unchecked
            return ((EncodableResult<Interface>) ObjectEncoding.INSTANCE.decodeObject(
                    HTTPUtils.INSTANCE.post(processorLPFCP, request.toString()),
                    ReflectionUtils.INSTANCE.getParametrizedType(EncodableResult.class, returnType)
            )).getOrThrow();
        });
    }

    /**
     * Creates a proxy instance of the specified interface that forwards method calls to the provided processor function.
     *
     * @param <Interface> Interface of the processor
     * @param interfaceClass Interface class of the processor
     * @param processorLPFCP A function that takes:
     * - `JSONObject` - function call request in LPFCP format;
     * - `Type` - type of value that the called function must return.
     *
     * [processorLPFCP] returns an `Any?` - value from the invoked function.
     *
     * @return A proxy instance of the specified interface.
     */
    public static <Interface> Interface getProcessor(Class<Interface> interfaceClass, Function2<JSONObject, Type, Object> processorLPFCP) {
        // noinspection unchecked
        return (Interface) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                new FunctionCallHandler(processorLPFCP)
        );
    }
}
