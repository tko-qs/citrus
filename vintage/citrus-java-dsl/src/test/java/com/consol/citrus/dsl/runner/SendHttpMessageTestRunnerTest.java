/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.runner;

import jakarta.servlet.http.Cookie;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.http.message.HttpMessageBuilder;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.dsl.UnitTestSupport;
import org.apache.http.entity.ContentType;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendHttpMessageTestRunnerTest extends UnitTestSupport {

    private HttpClient httpClient = Mockito.mock(HttpClient.class);
    private Producer messageProducer = Mockito.mock(Producer.class);

    @Test
    public void testFork() {
        reset(httpClient, messageProducer);
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "Foo");
            return null;
        }).doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "Bar");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                http(builder -> builder.client(httpClient)
                        .send()
                        .get()
                        .messageType(MessageType.PLAINTEXT)
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                        .header("additional", "additionalValue"));

                http(builder -> builder.client(httpClient)
                        .send()
                        .post()
                        .message(new DefaultMessage("Bar").setHeader("operation", "bar"))
                        .messageType(MessageType.PLAINTEXT)
                        .fork(true));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)(test.getActions().get(0)));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(String.class), "Foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 4L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE), MessageType.PLAINTEXT.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("additional"), "additionalValue");

        Assert.assertFalse(action.isForkMode());

        action = ((SendMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "send");

        messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);
        Assert.assertEquals(messageBuilder.getMessage().getPayload(String.class), "Bar");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 3L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE), MessageType.PLAINTEXT.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.POST.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "bar");

        Assert.assertTrue(action.isForkMode());
    }

    @Test
    public void testMessageObjectOverride() {
        reset(httpClient, messageProducer);
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "Foo");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                http(builder -> builder.client(httpClient)
                        .send()
                        .get()
                        .messageType(MessageType.PLAINTEXT)
                        .contentType(ContentType.APPLICATION_JSON.getMimeType())
                        .cookie(new Cookie("Foo", "123456"))
                        .message(new HttpMessage("Foo")
                                .cookie(new Cookie("Bar", "987654"))
                                .setHeader("operation", "foo"))
                        .header("additional", "additionalValue"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)(test.getActions().get(0)));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(String.class), "Foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 7L);
        Assert.assertNotEquals(messageBuilder.buildMessageHeaders(context).get(MessageHeaders.MESSAGE_TYPE), MessageType.PLAINTEXT);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("Content-Type"), ContentType.APPLICATION_JSON.getMimeType());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("additional"), "additionalValue");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_COOKIE_PREFIX + "Foo"), "Foo=123456");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_COOKIE_PREFIX + "Bar"), "Bar=987654");
        Assert.assertEquals(((HttpMessage) messageBuilder.getMessage()).getCookies().size(), 2L);
        Assert.assertTrue(((HttpMessage) messageBuilder.getMessage()).getCookies().stream().anyMatch(cookie -> cookie.getName().equals("Foo")));
        Assert.assertTrue(((HttpMessage) messageBuilder.getMessage()).getCookies().stream().anyMatch(cookie -> cookie.getName().equals("Bar")));
    }

    @Test
    public void testHttpMethod() {
        reset(httpClient, messageProducer);
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                http((builder -> builder.client(httpClient)
                        .send()
                        .get()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), HttpMethod.GET.name());
    }

    @Test
    public void testHttpRequestUriAndPath() {
        reset(httpClient, messageProducer);
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/");
            Assert.assertEquals(message.getHeader(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/test");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                http(builder -> builder.client(httpClient)
                        .send()
                        .get("/test")
                        .uri("http://localhost:8080/")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 4L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), "GET");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_URI), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.REQUEST_PATH_HEADER_NAME), "/test");
    }

    @Test
    public void testHttpRequestUriAndQueryParams() {
        reset(httpClient, messageProducer);
        when(httpClient.createProducer()).thenReturn(messageProducer);
        when(httpClient.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertEquals(message.getHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/");
            Assert.assertEquals(message.getHeader(EndpointUriResolver.QUERY_PARAM_HEADER_NAME), "param1=value1,param2=value2");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                http(builder -> builder.client(httpClient)
                        .send()
                        .get()
                        .uri("http://localhost:8080/")
                        .queryParam("param1", "value1")
                        .queryParam("param2", "value2")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), HttpMessageBuilder.class);

        HttpMessageBuilder messageBuilder = (HttpMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 5L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_METHOD), "GET");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_REQUEST_URI), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(HttpMessageHeaders.HTTP_QUERY_PARAMS), "param1=value1,param2=value2");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME), "http://localhost:8080/");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get(EndpointUriResolver.QUERY_PARAM_HEADER_NAME), "param1=value1,param2=value2");
    }

}
