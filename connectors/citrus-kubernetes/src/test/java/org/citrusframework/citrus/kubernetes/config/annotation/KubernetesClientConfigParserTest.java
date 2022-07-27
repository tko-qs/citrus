/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.citrus.kubernetes.config.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.citrus.annotations.CitrusAnnotations;
import org.citrusframework.citrus.annotations.CitrusEndpoint;
import org.citrusframework.citrus.config.annotation.AnnotationConfigParser;
import org.citrusframework.citrus.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.citrus.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.citrus.http.config.annotation.HttpClientConfigParser;
import org.citrusframework.citrus.http.config.annotation.HttpServerConfigParser;
import org.citrusframework.citrus.kubernetes.client.KubernetesClient;
import org.citrusframework.citrus.kubernetes.message.KubernetesMessageConverter;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class KubernetesClientConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "k8sClient1")
    @KubernetesClientConfig()
    private KubernetesClient client1;

    @CitrusEndpoint
    @KubernetesClientConfig(url = "http://localhost:8443",
            version="v1",
            username="user",
            password="s!cr!t",
            namespace="user_namespace",
            messageConverter="messageConverter",
            objectMapper="objectMapper")
    private KubernetesClient client2;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private KubernetesMessageConverter messageConverter;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", KubernetesMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("objectMapper", ObjectMapper.class)).thenReturn(objectMapper);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testKubernetesClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st client
        Assert.assertNotNull(client1.getClient());

        // 2nd client
        Assert.assertNotNull(client2.getClient());
        Assert.assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl(), "http://localhost:8443/");
        Assert.assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion(), "v1");
        Assert.assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getUsername(), "user");
        Assert.assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getPassword(), "s!cr!t");
        Assert.assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getNamespace(), "user_namespace");
        Assert.assertEquals(client2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(client2.getEndpointConfiguration().getObjectMapper(), objectMapper);
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 6L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("http.client"));
        Assert.assertEquals(validators.get("http.client").getClass(), HttpClientConfigParser.class);
        Assert.assertNotNull(validators.get("http.server"));
        Assert.assertEquals(validators.get("http.server").getClass(), HttpServerConfigParser.class);
        Assert.assertNotNull(validators.get("k8s.client"));
        Assert.assertEquals(validators.get("k8s.client").getClass(), KubernetesClientConfigParser.class);
        Assert.assertNotNull(validators.get("kubernetes.client"));
        Assert.assertEquals(validators.get("kubernetes.client").getClass(), KubernetesClientConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("k8s.client").isPresent());
    }
}
