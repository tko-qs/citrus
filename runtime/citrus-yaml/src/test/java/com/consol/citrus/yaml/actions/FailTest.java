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
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class FailTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadFail() {
        YamlTestLoader testLoader = createTestLoader("classpath:com/consol/citrus/yaml/actions/fail-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "FailTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), com.consol.citrus.container.Assert.class);
        Assert.assertEquals(((com.consol.citrus.container.Assert) result.getTestAction(0)).getException(), CitrusRuntimeException.class);
        Assert.assertEquals(((com.consol.citrus.container.Assert) result.getTestAction(0)).getMessage(), "Something went wrong");
        Assert.assertEquals(((com.consol.citrus.container.Assert) result.getTestAction(0)).getActionCount(), 1L);
        Assert.assertEquals(((com.consol.citrus.container.Assert) result.getTestAction(0)).getAction().getClass(), FailAction.class);

        Assert.assertEquals(result.getTestAction(1).getClass(), com.consol.citrus.container.Assert.class);
        Assert.assertEquals(((com.consol.citrus.container.Assert) result.getTestAction(1)).getException(), CitrusRuntimeException.class);
        Assert.assertEquals(((com.consol.citrus.container.Assert) result.getTestAction(1)).getMessage(), "Something went wrong");
        Assert.assertEquals(((com.consol.citrus.container.Assert) result.getTestAction(1)).getActionCount(), 1L);
        Assert.assertEquals(((com.consol.citrus.container.Assert) result.getTestAction(1)).getAction().getClass(), FailAction.class);
    }
}
