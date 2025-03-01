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

package com.consol.citrus.integration;

import com.consol.citrus.annotations.CitrusTestSource;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.spi.BindToRegistry;
import com.consol.citrus.testng.TestNGCitrusSupport;
import com.consol.citrus.validation.TextEqualsMessageValidator;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.1
 */
@Test
public class YamlTestLoaderIT extends TestNGCitrusSupport {

    @BindToRegistry
    TextEqualsMessageValidator textEqualsMessageValidator = new TextEqualsMessageValidator();

    @CitrusTestSource(type = TestLoader.YAML, name = { "sample-test" })
    public void YamlTestLoader_1_IT() {}

    @CitrusTestSource(type = TestLoader.YAML, name = { "echo-test", "sleep-test" }, packageName = "com.consol.citrus.yaml.actions")
    public void YamlTestLoader_2_IT() {}

    @CitrusTestSource(type = TestLoader.YAML, packageScan = "com.consol.citrus.integration")
    public void YamlTestLoader_3_IT() {}

    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:com/consol/citrus/yaml/actions/create-variables-test.yaml" })
    public void YamlTestLoader_4_IT() {}
}
