/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.junit.jupiter;

import java.lang.reflect.Method;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusContext;
import com.consol.citrus.CitrusInstanceManager;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.TestResult;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * JUnit5 extension adding {@link TestCaseRunner} support as well as Citrus annotation based resource injection
 * and lifecycle management such as before/after suite.
 *
 * Extension resolves method parameter of type {@link com.consol.citrus.context.TestContext}, {@link TestCaseRunner}
 * or {@link com.consol.citrus.TestActionRunner} and injects endpoints and resources coming from Citrus Spring application context that
 * is automatically loaded at suite start up. After suite automatically includes Citrus report generation.
 *
 * Extension is based on Citrus Xml test extension that also allows to load test cases from external Spring configuration files.
 *
 * @author Christoph Deppisch
 */
public class CitrusExtension implements BeforeAllCallback, InvocationInterceptor,
        AfterTestExecutionCallback, ParameterResolver, TestInstancePostProcessor, TestExecutionExceptionHandler, AfterEachCallback, AfterAllCallback {

    /** Test suite name */
    private static final String SUITE_NAME = "citrus-junit5-suite";

    private static boolean beforeSuite = true;
    private static boolean afterSuite = true;

    /**
     * {@link ExtensionContext.Namespace} in which Citrus related objects are stored keyed by test class.
     */
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(CitrusExtension.class);

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        if (CitrusExtensionHelper.requiresCitrus(extensionContext)) {
            CitrusExtensionHelper.setCitrus(CitrusInstanceManager.getOrDefault(), extensionContext);
        }

        if (beforeSuite) {
            beforeSuite = false;
      // Assertion: If the beforeAll callback is called for a test, the annotated tags are currently
      // included by the groups filter of surefire / failsafe or no specific filter is defined and
      // all groups / tags will run anyway.
      final String[] tags = extensionContext.getTags().toArray(new String[0]);
      extensionContext.getTestClass().map(Class::getName).or(() ->
          extensionContext.getTestMethod().map(method -> method.getDeclaringClass().getName()+":"+method.getName())
      ).ifPresentOrElse(suiteName ->
              CitrusExtensionHelper
                  .getCitrus(extensionContext)
                  .beforeSuite(suiteName, tags),
          () -> CitrusExtensionHelper
              .getCitrus(extensionContext)
              .beforeSuite(SUITE_NAME, tags)
      );
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (afterSuite) {
            afterSuite = false;
      // Assertion: If the afterAll callback is called for a test, the annotated tags are currently
      // included by the groups filter of surefire / failsafe or no specific filter is defined and
      // all groups / tags did run anyway.
      final String[] tags = extensionContext.getTags().toArray(new String[0]);
      extensionContext.getTestClass().map(Class::getName).or(() ->
          extensionContext.getTestMethod().map(meth -> meth.getDeclaringClass().getName()+":"+meth.getName())
      ).ifPresentOrElse(suiteName ->
              CitrusExtensionHelper
                  .getCitrus(extensionContext)
                  .afterSuite(suiteName, tags),
          () -> CitrusExtensionHelper
              .getCitrus(extensionContext)
              .afterSuite(SUITE_NAME, tags)
      );
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        TestCase testCase = CitrusExtensionHelper.getTestCase(extensionContext);
        if (testCase.getTestResult() == null || testCase.getTestResult().isSuccess()) {
            testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), throwable));
        }

        throw throwable;
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        extensionContext.getExecutionException()
                .ifPresent(e -> {
                    TestCase testCase = CitrusExtensionHelper.getTestCase(extensionContext);
                    testCase.setTestResult(TestResult.failed(testCase.getName(), testCase.getTestClass().getName(), e));
                });

        extensionContext.getRoot().getStore(NAMESPACE).remove(CitrusExtensionHelper.getBaseKey(extensionContext) + TestContext.class.getSimpleName());
        extensionContext.getRoot().getStore(NAMESPACE).remove(CitrusExtensionHelper.getBaseKey(extensionContext) + TestCase.class.getSimpleName());

        Object testInstance = extensionContext.getRequiredTestInstance();
        if (testInstance instanceof TestListener) {
            ((TestListener) testInstance).after(CitrusExtensionHelper.getCitrus(extensionContext).getCitrusContext());
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) {
        Object testInstance = extensionContext.getRequiredTestInstance();
        Citrus citrus = CitrusExtensionHelper.getCitrus(extensionContext);
        TestContext context = CitrusExtensionHelper.getTestContext(extensionContext);

        TestCaseRunner testRunner = CitrusExtensionHelper.getTestRunner(extensionContext);
        CitrusAnnotations.injectAll(testInstance, citrus, context);
        CitrusAnnotations.injectTestRunner(testInstance, testRunner);

        if (testInstance instanceof TestListener) {
            ((TestListener) testInstance).before(citrus.getCitrusContext());
        }

        TestLoader testLoader = CitrusExtensionHelper.getTestLoader(extensionContext);
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, testRunner);

        testLoader.doWithTestCase(t -> {
            try {
                invocation.proceed();
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new CitrusRuntimeException("Test failed", e);
            }
        });

        testLoader.load();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        extensionContext.getRoot().getStore(CitrusExtension.NAMESPACE)
                .remove(CitrusExtensionHelper.getBaseKey(extensionContext) + TestCaseRunner.class.getSimpleName());
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        CitrusAnnotations.injectCitrusFramework(testInstance, CitrusExtensionHelper.getCitrus(extensionContext));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().isAnnotationPresent(CitrusResource.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return CitrusExtensionHelper.resolveParameter(parameterContext, extensionContext);
    }

    /**
     * Listener able to perform changes on Citrus context before/after a test.
     */
    public interface TestListener {

        /**
         * Runs tasks on given Citrus context before test.
         */
        default void before(CitrusContext context) {
        }

        /**
         * Runs tasks on given Citrus context after test.
         */
        default void after(CitrusContext context) {
        }
    }
}
