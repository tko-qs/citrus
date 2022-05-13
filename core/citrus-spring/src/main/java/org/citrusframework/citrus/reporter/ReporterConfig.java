package org.citrusframework.citrus.reporter;

import org.citrusframework.citrus.report.HtmlReporter;
import org.citrusframework.citrus.report.JUnitReporter;
import org.citrusframework.citrus.report.LoggingReporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class ReporterConfig {

    @Bean
    public LoggingReporter loggingReporter() {
        return new LoggingReporter();
    }

    @Bean
    public HtmlReporter htmlReporter() {
        return new HtmlReporter();
    }

    @Bean
    public JUnitReporter junitReporter() {
        return new JUnitReporter();
    }

    @Bean
    public TestReportersFactory testReporters() {
        return new TestReportersFactory();
    }
}
