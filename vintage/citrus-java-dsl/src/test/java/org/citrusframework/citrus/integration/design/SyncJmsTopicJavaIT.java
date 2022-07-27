/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.citrus.integration.design;

import org.citrusframework.citrus.dsl.testng.TestNGCitrusTestDesigner;
import org.citrusframework.citrus.annotations.CitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class SyncJmsTopicJavaIT extends TestNGCitrusTestDesigner {

    @CitrusTest
    public void syncJmsTopic() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        parallel().actions(
            sequential().actions(
                sleep(2000L),
                send("syncJmsTopicEndpoint")
                    .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                   "<MessageId>${messageId}</MessageId>" +
                                   "<CorrelationId>${correlationId}</CorrelationId>" +
                                   "<User>${user}</User>" +
                                   "<Text>Hello TestFramework</Text>" +
                               "</HelloRequest>")
                    .header("Operation", "sayHello")
                    .header("CorrelationId", "${correlationId}")
            ),
            sequential().actions(
                parallel().actions(
                    receive("syncJmsTopicSubscriberEndpoint")
                        .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                   "<MessageId>${messageId}</MessageId>" +
                                   "<CorrelationId>${correlationId}</CorrelationId>" +
                                   "<User>${user}</User>" +
                                   "<Text>Hello TestFramework</Text>" +
                               "</HelloRequest>")
                        .header("Operation", "sayHello")
                        .header("CorrelationId", "${correlationId}"),
                    sequential().actions(
                        sleep().milliseconds(500L),
                        receive("syncJmsTopicSubscriberEndpoint")
                            .payload("<HelloRequest xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                       "<MessageId>${messageId}</MessageId>" +
                                       "<CorrelationId>${correlationId}</CorrelationId>" +
                                       "<User>${user}</User>" +
                                       "<Text>Hello TestFramework</Text>" +
                                   "</HelloRequest>")
                            .header("Operation", "sayHello")
                            .header("CorrelationId", "${correlationId}")
                    )
                ),
                send("syncJmsTopicSubscriberEndpoint")
                    .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                    "<MessageId>${messageId}</MessageId>" +
                                    "<CorrelationId>${correlationId}</CorrelationId>" +
                                    "<User>HelloService</User>" +
                                    "<Text>Hello ${user}</Text>" +
                                "</HelloResponse>")
                    .header("Operation", "sayHello")
                    .header("CorrelationId", "${correlationId}")
            )
        );

        receive("syncJmsTopicEndpoint")
            .payload("<HelloResponse xmlns=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                                    "<MessageId>${messageId}</MessageId>" +
                                    "<CorrelationId>${correlationId}</CorrelationId>" +
                                    "<User>HelloService</User>" +
                                    "<Text>Hello ${user}</Text>" +
                                "</HelloResponse>")
            .header("Operation", "sayHello")
            .header("CorrelationId", "${correlationId}");
    }
}
