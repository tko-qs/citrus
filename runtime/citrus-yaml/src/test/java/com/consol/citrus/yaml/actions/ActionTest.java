/*
 * Copyright 2022 the original author or authors.
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

package com.consol.citrus.yaml.actions;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 */
public class ActionTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadAction() {
        YamlTestLoader testLoader = createTestLoader("classpath:com/consol/citrus/yaml/actions/action-test.yaml");

        EchoAction action = echo().build();

        context.getReferenceResolver().bind("echoBuilder", echo().message("Citrus rocks!"));
        context.getReferenceResolver().bind("echo", action);

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "ActionTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction) result.getTestAction(0)).getMessage(), "Citrus rocks!");

        Assert.assertEquals(result.getTestAction(1).getClass(), EchoAction.class);
        Assert.assertEquals(result.getTestAction(1), action);
    }
}
