/*
 * Copyright 2021 the original author or authors.
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

package com.consol.citrus.yaml;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.common.DefaultTestLoader;
import com.consol.citrus.common.TestSourceAware;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.yaml.actions.YamlTestActionBuilder;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Loads test case from given YAML source.
 * @author Christoph Deppisch
 */
public class YamlTestLoader extends DefaultTestLoader implements TestSourceAware {

    private String source;

    private final Yaml yaml;

    /**
     * Default constructor.
     */
    public YamlTestLoader() {
        Constructor constructor = new Constructor(YamlTestCase.class, new LoaderOptions());

        Map<String, TestActionBuilder<?>> builders = YamlTestActionBuilder.lookup();
        if (!builders.isEmpty()) {
            TypeDescription actions = new TypeDescription(TestActions.class);
            for (Map.Entry<String, TestActionBuilder<?>> builder : builders.entrySet()) {
                actions.substituteProperty(builder.getKey(), builder.getValue().getClass(), "getAction", "setAction", TestActionBuilder.class);
            }
            constructor.addTypeDescription(actions);
        }

        yaml = new Yaml(constructor);
    }

    /**
     * Constructor with context file and parent application context field.
     * @param testClass
     * @param testName
     * @param packageName
     */
    public YamlTestLoader(Class<?> testClass, String testName, String packageName) {
        this();

        this.testClass = testClass;
        this.testName = testName;
        this.packageName = packageName;
    }

    @Override
    public void doLoad() {
        Resource yamlSource = FileUtils.getFileResource(getSource());

        try {
            YamlTestCase tc = yaml.load(FileUtils.readToString(yamlSource));
            testCase = tc.getTestCase();
            if (runner instanceof DefaultTestCaseRunner) {
                ((DefaultTestCaseRunner) runner).setTestCase(testCase);
            }

            testCase.getActionBuilders().stream()
                    .filter(action -> ReferenceResolverAware.class.isAssignableFrom(action.getClass()))
                    .map(ReferenceResolverAware.class::cast)
                    .forEach(action -> action.setReferenceResolver(context.getReferenceResolver()));

            configurer.forEach(handler -> handler.accept(testCase));
            citrus.run(testCase, context);
            handler.forEach(handler -> handler.accept(testCase));
        } catch (IOException e) {
            throw citrusContext.getTestContextFactory().getObject()
                    .handleError(testName, packageName, "Failed to load YAML test with name '" + testName + "'", e);
        }
    }

    /**
     * Gets custom Spring application context file for the YAML test case. If not set creates default
     * context file path from testName and packageName.
     * @return
     */
    public String getSource() {
        if (StringUtils.hasText(source)) {
            return source;
        } else {
            return ResourceUtils.CLASSPATH_URL_PREFIX + packageName.replace('.', File.separatorChar) +
                    File.separator + testName + ".yaml";
        }
    }

    /**
     * Sets custom Spring application context file for YAML test case.
     * @param source
     */
    @Override
    public void setSource(String source) {
        this.source = source;
    }
}
