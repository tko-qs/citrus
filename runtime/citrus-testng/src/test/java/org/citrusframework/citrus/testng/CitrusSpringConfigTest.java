package org.citrusframework.citrus.testng;

import org.citrusframework.citrus.CitrusSpringSettings;
import org.citrusframework.citrus.context.SpringBeanReferenceResolver;
import org.citrusframework.citrus.context.TestContextFactoryBean;
import org.citrusframework.citrus.endpoint.DefaultEndpointFactory;
import org.citrusframework.citrus.endpoint.EndpointFactory;
import org.citrusframework.citrus.functions.DefaultFunctionLibrary;
import org.citrusframework.citrus.functions.FunctionLibrary;
import org.citrusframework.citrus.functions.FunctionRegistry;
import org.citrusframework.citrus.log.DefaultLogModifier;
import org.citrusframework.citrus.log.LogModifier;
import org.citrusframework.citrus.message.MessageProcessor;
import org.citrusframework.citrus.message.MessageProcessors;
import org.citrusframework.citrus.report.*;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.citrus.util.SpringBeanTypeConverter;
import org.citrusframework.citrus.util.TypeConverter;
import org.citrusframework.citrus.validation.DefaultMessageHeaderValidator;
import org.citrusframework.citrus.validation.MessageValidator;
import org.citrusframework.citrus.validation.MessageValidatorRegistry;
import org.citrusframework.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import org.citrusframework.citrus.validation.matcher.ValidationMatcherLibrary;
import org.citrusframework.citrus.validation.matcher.ValidationMatcherRegistry;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = CitrusSpringConfigTest.CustomConfig.class)
public class CitrusSpringConfigTest extends TestNGCitrusSpringSupport {

    static {
        System.setProperty(CitrusSpringSettings.DEFAULT_APPLICATION_CONTEXT_PROPERTY, "classpath:citrus-unit-context.xml");
    }

    @Autowired
    private TestReporters testReporters;

    @Autowired
    private TestListeners testListeners;

    @Autowired
    private TestSuiteListeners testSuiteListeners;

    @Autowired
    private TestActionListeners testActionListeners;

    @Autowired
    private MessageListeners messageListeners;

    @Autowired
    private MessageProcessors messageProcessors;

    @Autowired
    private FunctionRegistry functionRegistry;

    @Autowired
    private ValidationMatcherRegistry validationMatcherRegistry;

    @Autowired
    private MessageValidatorRegistry messageValidatorRegistry;

    @Autowired
    private EndpointFactory endpointFactory;

    @Autowired
    private ReferenceResolver referenceResolver;

    @Autowired
    private TypeConverter typeConverter;

    @Autowired
    private LogModifier logModifier;

    @Autowired
    private TestContextFactoryBean testContextFactory;

    @Autowired
    private LoggingReporter loggingReporter;

    @Test
    public void verifySpringConfig() {
        Assert.assertEquals(testReporters.getTestReporters().size(), 4);
        Assert.assertTrue(testReporters.getTestReporters().stream().anyMatch(CustomConfig.reporter::equals));
        Assert.assertTrue(testReporters.getTestReporters().stream().anyMatch(loggingReporter::equals));
        Assert.assertTrue(testReporters.getTestReporters().stream().anyMatch(HtmlReporter.class::isInstance));
        Assert.assertTrue(testReporters.getTestReporters().stream().anyMatch(JUnitReporter.class::isInstance));

        Assert.assertEquals(testListeners.getTestListeners().size(), 5);
        Assert.assertTrue(testListeners.getTestListeners().stream().anyMatch(CustomConfig.testListener::equals));
        Assert.assertTrue(testListeners.getTestListeners().stream().anyMatch(loggingReporter::equals));
        Assert.assertTrue(testListeners.getTestListeners().stream().anyMatch(HtmlReporter.class::isInstance));
        Assert.assertTrue(testListeners.getTestListeners().stream().anyMatch(FailureStackTestListener.class::isInstance));
        Assert.assertTrue(testListeners.getTestListeners().stream().anyMatch(TestReporters.class::isInstance));

        Assert.assertEquals(testSuiteListeners.getTestSuiteListeners().size(), 3);
        Assert.assertTrue(testSuiteListeners.getTestSuiteListeners().stream().anyMatch(CustomConfig.testSuiteListener::equals));
        Assert.assertTrue(testSuiteListeners.getTestSuiteListeners().stream().anyMatch(loggingReporter::equals));
        Assert.assertTrue(testSuiteListeners.getTestSuiteListeners().stream().anyMatch(TestReporters.class::isInstance));

        Assert.assertEquals(testActionListeners.getTestActionListeners().size(), 2);
        Assert.assertTrue(testActionListeners.getTestActionListeners().stream().anyMatch(CustomConfig.testActionListener::equals));
        Assert.assertTrue(testActionListeners.getTestActionListeners().stream().anyMatch(loggingReporter::equals));

        Assert.assertEquals(messageListeners.getMessageListener().size(), 2);
        Assert.assertTrue(messageListeners.getMessageListener().stream().anyMatch(CustomConfig.messageListener::equals));
        Assert.assertTrue(messageListeners.getMessageListener().stream().anyMatch(loggingReporter::equals));

        Assert.assertEquals(messageProcessors.getMessageProcessors().size(), 1);
        Assert.assertTrue(messageProcessors.getMessageProcessors().stream().anyMatch(CustomConfig.messageProcessor::equals));

        Assert.assertEquals(functionRegistry.getFunctionLibraries().size(), 2);
        Assert.assertTrue(functionRegistry.getFunctionLibraries().stream().anyMatch(CustomConfig.functionLibrary::equals));
        Assert.assertTrue(functionRegistry.getFunctionLibraries().stream().anyMatch(DefaultFunctionLibrary.class::isInstance));

        Assert.assertEquals(validationMatcherRegistry.getValidationMatcherLibraries().size(), 2);
        Assert.assertTrue(validationMatcherRegistry.getValidationMatcherLibraries().stream().anyMatch(CustomConfig.validationMatcherLibrary::equals));
        Assert.assertTrue(validationMatcherRegistry.getValidationMatcherLibraries().stream().anyMatch(DefaultValidationMatcherLibrary.class::isInstance));

        Assert.assertEquals(messageValidatorRegistry.getMessageValidators().size(), 2);
        Assert.assertTrue(messageValidatorRegistry.getMessageValidators().values().stream().anyMatch(CustomConfig.messageValidator::equals));
        Assert.assertTrue(messageValidatorRegistry.getMessageValidators().values().stream().anyMatch(DefaultMessageHeaderValidator.class::isInstance));

        Assert.assertEquals(endpointFactory.getClass(), DefaultEndpointFactory.class);

        Assert.assertEquals(referenceResolver.getClass(), SpringBeanReferenceResolver.class);
        Assert.assertEquals(typeConverter.getClass(), SpringBeanTypeConverter.class);
        Assert.assertEquals(logModifier.getClass(), DefaultLogModifier.class);

        Assert.assertEquals(testContextFactory.getTestListeners(), testListeners);
        Assert.assertEquals(testContextFactory.getMessageListeners(), messageListeners);
        Assert.assertEquals(testContextFactory.getMessageProcessors(), messageProcessors);
        Assert.assertEquals(testContextFactory.getFunctionRegistry(), functionRegistry);
        Assert.assertEquals(testContextFactory.getValidationMatcherRegistry(), validationMatcherRegistry);
        Assert.assertEquals(testContextFactory.getMessageValidatorRegistry(), messageValidatorRegistry);
        Assert.assertEquals(testContextFactory.getEndpointFactory(), endpointFactory);
        Assert.assertEquals(testContextFactory.getReferenceResolver(), referenceResolver);
        Assert.assertEquals(testContextFactory.getTypeConverter(), typeConverter);
        Assert.assertEquals(testContextFactory.getLogModifier(), logModifier);
    }

    @Configuration
    static class CustomConfig {
        static TestReporter reporter = Mockito.mock(TestReporter.class);
        static TestListener testListener = Mockito.mock(TestListener.class);
        static TestSuiteListener testSuiteListener = Mockito.mock(TestSuiteListener.class);
        static TestActionListener testActionListener = Mockito.mock(TestActionListener.class);
        static MessageListener messageListener = Mockito.mock(MessageListener.class);
        static MessageProcessor messageProcessor = Mockito.mock(MessageProcessor.class);

        static FunctionLibrary functionLibrary = Mockito.mock(FunctionLibrary.class);
        static ValidationMatcherLibrary validationMatcherLibrary = Mockito.mock(ValidationMatcherLibrary.class);
        static MessageValidator<?> messageValidator = Mockito.mock(MessageValidator.class);

        @Bean
        public TestReporter reporter() {
            return reporter;
        }

        @Bean
        public TestListener testListener() {
            return testListener;
        }

        @Bean
        public TestSuiteListener testSuiteListener() {
            return testSuiteListener;
        }

        @Bean
        public TestActionListener testActionListener() {
            return testActionListener;
        }

        @Bean
        public MessageListener messageListener() {
            return messageListener;
        }

        @Bean
        public MessageProcessor messageProcessor() {
            return messageProcessor;
        }

        @Bean
        public FunctionLibrary functionLibrary() {
            return functionLibrary;
        }

        @Bean
        public ValidationMatcherLibrary validationMatcherLibrary() {
            return validationMatcherLibrary;
        }

        @Bean
        public MessageValidator<?> messageValidator() {
            return messageValidator;
        }
    }
}
