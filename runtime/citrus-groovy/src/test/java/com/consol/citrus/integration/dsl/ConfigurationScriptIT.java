/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.integration.dsl;

import java.io.IOException;

import com.consol.citrus.TestActionRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.direct.DirectEndpoint;
import com.consol.citrus.groovy.dsl.configuration.ConfigurationScript;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import com.consol.citrus.util.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ConfigurationScriptIT extends TestNGCitrusSpringSupport {

    @Test
    @Parameters({"runner", "context"})
    @CitrusTest
    public void shouldRunConfigurationScript(@Optional @CitrusResource TestActionRunner runner,
                                       @Optional @CitrusResource TestContext context) throws IOException {
        ConfigurationScript script = new ConfigurationScript(FileUtils.readToString(new ClassPathResource("com/consol/citrus/groovy/dsl/configuration.groovy")), citrus);
        script.execute(context);

        Assert.assertTrue(context.getReferenceResolver().isResolvable("say-hello", MessageQueue.class));
        Assert.assertTrue(context.getReferenceResolver().isResolvable("say-goodbye", MessageQueue.class));
        Assert.assertTrue(context.getReferenceResolver().isResolvable("hello", DirectEndpoint.class));
        Assert.assertTrue(context.getReferenceResolver().isResolvable("goodbye", DirectEndpoint.class));

        DirectEndpoint hello = context.getReferenceResolver().resolve("hello", DirectEndpoint.class);
        Assert.assertEquals(hello.getName(), "hello");
        Assert.assertEquals(hello.getEndpointConfiguration().getQueueName(), "say-hello");

        DirectEndpoint goodbye = context.getReferenceResolver().resolve("goodbye", DirectEndpoint.class);
        Assert.assertEquals(goodbye.getName(), "goodbye");
        Assert.assertEquals(goodbye.getEndpointConfiguration().getQueueName(), "say-goodbye");
    }
}
