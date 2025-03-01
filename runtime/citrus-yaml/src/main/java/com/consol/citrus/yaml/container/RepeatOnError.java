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

package com.consol.citrus.yaml.container;

import java.util.List;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.container.RepeatOnErrorUntilTrue;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.yaml.TestActions;

/**
 * @author Christoph Deppisch
 */
public class RepeatOnError implements TestActionBuilder<RepeatOnErrorUntilTrue>, ReferenceResolverAware {

    private final RepeatOnErrorUntilTrue.Builder builder = new RepeatOnErrorUntilTrue.Builder();

    private ReferenceResolver referenceResolver;

    @Override
    public RepeatOnErrorUntilTrue build() {
        builder.getActions().stream()
                .filter(action -> action instanceof ReferenceResolverAware)
                .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));

        return builder.build();
    }

    public void setDescription(String value) {
        builder.description(value);
    }

    public void setCondition(String condition) {
        builder.condition(condition);
    }

    public void setUntil(String condition) {
        builder.until(condition);
    }

    public void setAutoSleep(long milliseconds) {
        builder.autoSleep(milliseconds);
    }

    public void setIndex(String index) {
        builder.index(index);
    }

    public void setStartsWith(int value) {
        builder.startsWith(value);
    }

    public void setActions(List<TestActions> actions) {
        builder.actions(actions.stream().map(TestActions::get).toArray(TestActionBuilder<?>[]::new));
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
