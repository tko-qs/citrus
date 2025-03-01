/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.http.interceptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.message.RawMessage;
import com.consol.citrus.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * Simple logging interceptor writes Http request and response messages to the console.
 *
 * @author Christoph Deppisch
 * @since 1.2
 */
public class LoggingClientInterceptor implements ClientHttpRequestInterceptor {

    /** New line characters in log files */
    private static final String NEWLINE = System.getProperty("line.separator");

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(LoggingClientInterceptor.class);

    private MessageListeners messageListener;

    private final TestContextFactory contextFactory = TestContextFactory.newInstance();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
        ClientHttpRequestExecution execution) throws IOException {
        handleRequest(getRequestContent(request, new String(body)));

        ClientHttpResponse response = execution.execute(request, body);
        CachingClientHttpResponseWrapper bufferedResponse = new CachingClientHttpResponseWrapper(response);
        handleResponse(getResponseContent(bufferedResponse));

        return bufferedResponse;
    }

    /**
     * Handles request messages for logging.
     * @param request
     */
    public void handleRequest(String request) {
        if (hasMessageListeners()) {
            LOG.debug("Sending Http request message");
            messageListener.onOutboundMessage(new RawMessage(request), contextFactory.getObject());
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending Http request message:" + NEWLINE + request);
            }
        }
    }

    /**
     * Handles response messages for logging.
     * @param response
     */
    public void handleResponse(String response) {
        if (hasMessageListeners()) {
            LOG.debug("Received Http response message");
            messageListener.onInboundMessage(new RawMessage(response), contextFactory.getObject());
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received Http response message:" + NEWLINE + response);
            }
        }
    }

    /**
     * Checks if message listeners are present on this interceptor.
     * @return
     */
    public boolean hasMessageListeners() {
        return messageListener != null && !messageListener.isEmpty();
    }

    /**
     * Builds request content string from request and body.
     * @param request
     * @param body
     * @return
     */
    private String getRequestContent(HttpRequest request, String body) {
        StringBuilder builder = new StringBuilder();

        builder.append(request.getMethod());
        builder.append(" ");
        builder.append(request.getURI());
        builder.append(NEWLINE);

        appendHeaders(request.getHeaders(), builder);

        builder.append(NEWLINE);
        builder.append(body);

        return builder.toString();
    }

    /**
     * Builds response content string from response object.
     * @param response
     * @return
     * @throws IOException
     */
    private String getResponseContent(CachingClientHttpResponseWrapper response) throws IOException {
        if (response != null) {
            StringBuilder builder = new StringBuilder();

            builder.append("HTTP/1.1 "); // TODO get Http version from message
            builder.append(response.getStatusCode());
            builder.append(" ");
            builder.append(response.getStatusText());
            builder.append(NEWLINE);

            appendHeaders(response.getHeaders(), builder);

            builder.append(NEWLINE);
            builder.append(response.getBodyContent());

            return builder.toString();
        } else {
            return "";
        }
    }

    /**
     * Append Http headers to string builder.
     * @param headers
     * @param builder
     */
    private void appendHeaders(HttpHeaders headers, StringBuilder builder) {
        for (Entry<String, List<String>> headerEntry : headers.entrySet()) {
            builder.append(headerEntry.getKey());
            builder.append(":");
            builder.append(StringUtils.arrayToCommaDelimitedString(headerEntry.getValue().toArray()));
            builder.append(NEWLINE);
        }
    }

    /**
     * Response wrapper implementation of {@link ClientHttpResponse} that reads the message body
     * into memory for caching, thus allowing for multiple invocations of {@link #getBody()}.
     */
    private static final class CachingClientHttpResponseWrapper implements ClientHttpResponse {

        private final ClientHttpResponse response;

        private byte[] body;

        CachingClientHttpResponseWrapper(ClientHttpResponse response) {
            this.response = response;
        }

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return this.response.getStatusCode();
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return this.response.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return this.response.getStatusText();
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.response.getHeaders();
        }

        @Override
        public InputStream getBody() throws IOException {
            if (this.body == null) {
                if (response.getBody() != null) {
                    this.body = FileCopyUtils.copyToByteArray(response.getBody());
                } else {
                    body = new byte[] {};
                }
            }
            return new ByteArrayInputStream(this.body);
        }

        public String getBodyContent() throws IOException {
            if (this.body == null) {
                getBody();
            }

            return new String(body, StandardCharsets.UTF_8);
        }

        @Override
        public void close() {
            this.response.close();
        }
    }

    /**
     * Sets the message listener.
     * @param messageListener
     */
    public void setMessageListener(MessageListeners messageListener) {
        this.messageListener = messageListener;
    }

}
