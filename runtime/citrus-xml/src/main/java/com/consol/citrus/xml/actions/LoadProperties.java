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

package com.consol.citrus.xml.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.LoadPropertiesAction;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "load")
public class LoadProperties implements TestActionBuilder<LoadPropertiesAction> {

    private final LoadPropertiesAction.Builder builder = new LoadPropertiesAction.Builder();

    @XmlElement
    public LoadProperties setProperties(Properties properties) {
        builder.filePath(properties.file);
        return this;
    }

    @Override
    public LoadPropertiesAction build() {
        return builder.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Properties {

        @XmlAttribute(name = "file", required = true)
        protected String file;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }
    }
}
