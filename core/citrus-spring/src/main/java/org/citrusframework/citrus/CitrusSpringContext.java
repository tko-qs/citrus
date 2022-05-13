package org.citrusframework.citrus;

import java.util.ArrayList;

import org.citrusframework.citrus.config.CitrusSpringConfig;
import org.citrusframework.citrus.container.AfterSuite;
import org.citrusframework.citrus.container.BeforeSuite;
import org.citrusframework.citrus.context.TestContextFactoryBean;
import org.citrusframework.citrus.functions.FunctionRegistry;
import org.citrusframework.citrus.log.LogModifier;
import org.citrusframework.citrus.report.MessageListeners;
import org.citrusframework.citrus.report.TestActionListeners;
import org.citrusframework.citrus.report.TestListeners;
import org.citrusframework.citrus.report.TestReporters;
import org.citrusframework.citrus.report.TestSuiteListeners;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.util.TypeConverter;
import org.citrusframework.citrus.validation.MessageValidatorRegistry;
import org.citrusframework.citrus.validation.matcher.ValidationMatcherRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Christoph Deppisch
 */
public class CitrusSpringContext extends CitrusContext {

    /** Basic Spring application context */
    private final ApplicationContext applicationContext;

    /**
     * Protected constructor using given builder to construct this instance.
     * @param builder
     */
    protected CitrusSpringContext(Builder builder) {
        super(builder);

        this.applicationContext = builder.applicationContext;
    }

    /**
     * Gets the basic Citrus Spring bean application context.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Closing Citrus and its application context.
     */
    public void close() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            if (((ConfigurableApplicationContext) applicationContext).isActive()) {
                ((ConfigurableApplicationContext) applicationContext).close();
            }
        }
    }

    /**
     * Initializing method loads Spring application context and reads bean definitions
     * such as test listeners and test context factory.
     * @return
     */
    public static CitrusSpringContext create() {
        return create(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
    }

    /**
     * Initializing method with Spring application context Java configuration class
     * that gets loaded as application context.
     * @param configClass
     * @return
     */
    public static CitrusSpringContext create(Class<? extends CitrusSpringConfig> configClass) {
        return create(new AnnotationConfigApplicationContext(configClass));
    }

    /**
     * Create new Citrus context with given Spring bean application context.
     * @param applicationContext
     * @return
     */
    public static CitrusSpringContext create(ApplicationContext applicationContext) {
        return new Builder()
                .withApplicationContext(applicationContext)
                .build();
    }

    /**
     * Spring aware Citrus context builder.
     */
    public static final class Builder extends CitrusContext.Builder {

        private ApplicationContext applicationContext;

        public Builder withApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;

            functionRegistry(applicationContext.getBean(FunctionRegistry.class));
            validationMatcherRegistry(applicationContext.getBean(ValidationMatcherRegistry.class));
            messageValidatorRegistry(applicationContext.getBean(MessageValidatorRegistry.class));
            messageListeners(applicationContext.getBean(MessageListeners.class));
            testListeners(applicationContext.getBean(TestListeners.class));
            testActionListeners(applicationContext.getBean(TestActionListeners.class));
            testReporters(applicationContext.getBean(TestReporters.class));
            testSuiteListeners(applicationContext.getBean(TestSuiteListeners.class));
            testContextFactory(applicationContext.getBean(TestContextFactoryBean.class));
            referenceResolver(applicationContext.getBean(ReferenceResolver.class));
            typeConverter(applicationContext.getBean(TypeConverter.class));
            logModifier(applicationContext.getBean(LogModifier.class));
            beforeSuite(new ArrayList<>(applicationContext.getBeansOfType(BeforeSuite.class).values()));
            afterSuite(new ArrayList<>(applicationContext.getBeansOfType(AfterSuite.class).values()));

            return this;
        }

        public CitrusSpringContext build() {
            return new CitrusSpringContext(this);
        }
    }
}
