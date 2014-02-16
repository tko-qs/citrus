/*
 * Copyright 2006-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.ws.servlet;

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.endpoint.adapter.EmptyResponseEndpointAdapter;
import com.consol.citrus.ws.WebServiceEndpoint;
import com.consol.citrus.ws.interceptor.DelegatingEndpointInterceptor;
import com.consol.citrus.ws.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

import java.util.ArrayList;
import java.util.List;

/**
 * Citrus message dispatcher servlet extends Spring's message dispatcher servlet and just
 * adds optional configuration settings for default mapping strategies, interceptors and so on.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class CitrusMessageDispatcherServlet extends MessageDispatcherServlet {
    /** Soap web server hosting the servlet */
    private WebServer webServer;

    /** Default bean names used in default configuration */
    private static final String ENDPOINT_INTERCEPTOR_BEAN_NAME = "citrusHandlerInterceptor";
    private static final String ENDPOINT_MAPPING_BEAN_NAME = "citrusEndpointMapping";
    private static final String MESSAGE_ENDPOINT_BEAN_NAME = "citrusWsEndpoint";

    /**
     * Default constructor using http server instance that
     * holds this servlet.
     * @param webServer
     */
    public CitrusMessageDispatcherServlet(WebServer webServer) {
        this.webServer = webServer;
    }

    @Override
    protected void initStrategies(ApplicationContext context) {
        super.initStrategies(context);

        configureHandlerInterceptor(context);
        configureMessageEndpoint(context);
        configurePayloadMapping(context);
    }

    protected void configurePayloadMapping(ApplicationContext context) {
        if (context.containsBean(ENDPOINT_MAPPING_BEAN_NAME)) {
            AbstractEndpointMapping endpointMapping = context.getBean(ENDPOINT_MAPPING_BEAN_NAME, AbstractEndpointMapping.class);

        }
    }

    /**
     * Post process handler interceptors.
     * @param context
     */
    protected void configureHandlerInterceptor(ApplicationContext context) {
        if (context.containsBean(ENDPOINT_INTERCEPTOR_BEAN_NAME)) {
            DelegatingEndpointInterceptor endpointInterceptor = context.getBean(ENDPOINT_INTERCEPTOR_BEAN_NAME, DelegatingEndpointInterceptor.class);
            endpointInterceptor.setInterceptors(adaptInterceptors(webServer.getInterceptors()));
        }
    }

    /**
     * Post process endpoint.
     * @param context
     */
    protected void configureMessageEndpoint(ApplicationContext context) {
        if (context.containsBean(MESSAGE_ENDPOINT_BEAN_NAME)) {
            WebServiceEndpoint messageEndpoint = context.getBean(MESSAGE_ENDPOINT_BEAN_NAME, WebServiceEndpoint.class);

            EndpointAdapter endpointAdapter = webServer.getEndpointAdapter();
            if (endpointAdapter == null) {
                messageEndpoint.setMessageHandler(new EmptyResponseEndpointAdapter());
            } else {
                messageEndpoint.setMessageHandler(endpointAdapter);
            }

            messageEndpoint.setHandleMimeHeaders(webServer.isHandleMimeHeaders());
        }
    }

    /**
     * Adapts object list to endpoint interceptors.
     * @param interceptors
     * @return
     */
    private List<EndpointInterceptor> adaptInterceptors(List<Object> interceptors) {
        List<EndpointInterceptor> endpointInterceptors = new ArrayList<EndpointInterceptor>();

        if (interceptors != null) {
            for (Object interceptor : interceptors) {
                if (interceptor instanceof EndpointInterceptor) {
                    endpointInterceptors.add((EndpointInterceptor) interceptor);
                }
            }
        }

        return endpointInterceptors;
    }
}
