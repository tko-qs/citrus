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

package com.consol.citrus.jms.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.jms.message.JmsMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.correlation.CorrelationManager;
import com.consol.citrus.message.correlation.PollingCorrelationManager;
import com.consol.citrus.messaging.ReplyProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import jakarta.jms.*;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsSyncConsumer extends JmsConsumer implements ReplyProducer {

    /** Map of reply destinations */
    private CorrelationManager<Destination> correlationManager;

    /** Endpoint configuration */
    private final JmsSyncEndpointConfiguration endpointConfiguration;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmsSyncConsumer.class);

    /**
     * Default constructor using endpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public JmsSyncConsumer(String name, JmsSyncEndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply jms destination not set up yet");
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message receivedMessage = super.receive(selector, context, timeout);

        JmsMessage jmsMessage;
        if (receivedMessage instanceof JmsMessage) {
            jmsMessage = (JmsMessage) receivedMessage;
        } else {
            jmsMessage = new JmsMessage(receivedMessage);
        }

        saveReplyDestination(jmsMessage, context);

        return jmsMessage;
    }

    @Override
    public void send(final Message message, final TestContext context) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = correlationManager.getCorrelationKey(correlationKeyName, context);
        Destination replyDestination = correlationManager.find(correlationKey, endpointConfiguration.getTimeout());
        Assert.notNull(replyDestination, "Failed to find JMS reply destination for message correlation key: '" + correlationKey + "'");

        if (log.isDebugEnabled()) {
            log.debug("Sending JMS message to destination: '" + endpointConfiguration.getDestinationName(replyDestination) + "'");
        }

        endpointConfiguration.getJmsTemplate().send(replyDestination, session -> {
            jakarta.jms.Message jmsMessage = endpointConfiguration.getMessageConverter().createJmsMessage(message, session, endpointConfiguration, context);
            endpointConfiguration.getMessageConverter().convertOutbound(jmsMessage, message, endpointConfiguration, context);
            return jmsMessage;
        });

        context.onOutboundMessage(message);

        log.info("Message was sent to JMS destination: '" + endpointConfiguration.getDestinationName(replyDestination) + "'");
    }

    /**
     * Store the reply destination either straight forward or with a given
     * message correlation key.
     *
     * @param jmsMessage
     * @param context
     */
    public void saveReplyDestination(JmsMessage jmsMessage, TestContext context) {
        if (jmsMessage.getReplyTo() != null) {
            String correlationKeyName = endpointConfiguration.getCorrelator().getCorrelationKeyName(getName());
            String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(jmsMessage);
            correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);
            correlationManager.store(correlationKey, jmsMessage.getReplyTo());
        }  else {
            log.warn("Unable to retrieve reply to destination for message \n" +
                    jmsMessage + "\n - no reply to destination found in message headers!");
        }
    }

    /**
     * Gets the correlation manager.
     * @return
     */
    public CorrelationManager<Destination> getCorrelationManager() {
        return correlationManager;
    }

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<Destination> correlationManager) {
        this.correlationManager = correlationManager;
    }

}
