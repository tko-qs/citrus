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

package com.consol.citrus.zookeeper.config.xml;

import java.util.Map;

import com.consol.citrus.testng.AbstractActionParserTest;
import com.consol.citrus.zookeeper.actions.ZooExecuteAction;
import com.consol.citrus.zookeeper.client.ZooClient;
import com.consol.citrus.zookeeper.command.Create;
import com.consol.citrus.zookeeper.command.Info;
import org.springframework.beans.factory.BeanCreationException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ZooExecuteActionParserTest extends AbstractActionParserTest<ZooExecuteAction> {

    @Test
    public void testZookeeperExecuteActionParser() {
        assertActionCount(2);
        assertActionClassAndName(ZooExecuteAction.class, "zookeeper-execute");

        ZooExecuteAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), Info.class);
        Assert.assertEquals(action.getZookeeperClient(), beanDefinitionContext.getBean("myZookeeperClient", ZooClient.class));
        Assert.assertEquals(action.getCommand().getParameters().size(), 0);
        Assert.assertEquals(action.getExpectedCommandResult(), "{a:\"some thing\"}");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), Create.class);
        Assert.assertEquals(action.getZookeeperClient(), beanDefinitionContext.getBean("myZookeeperClient", ZooClient.class));
        Assert.assertEquals(action.getCommand().getParameters().size(), 4);
        assertParametersContainValue(action.getCommand().getParameters(), "path", "/some-path");
        assertParametersContainValue(action.getCommand().getParameters(), "mode", "PERSISTENT");
        assertParametersContainValue(action.getCommand().getParameters(), "acl", "OPEN_ACL_UNSAFE");
        assertParametersContainValue(action.getCommand().getParameters(), "data", "more data");
        Assert.assertEquals(action.getExpectedCommandResult(), "{b:\"some thing\"}");
    }

    private void assertParametersContainValue(Map<String, Object> parameters, String key, String value) {
        Assert.assertTrue(parameters.containsKey(key));
        Assert.assertEquals(parameters.get(key), value);
    }

    @Test(expectedExceptions = BeanCreationException.class)
    public void testZookeeperExecuteActionParserFailed() {
        createApplicationContext("failed");
    }
}
