package org.citrusframework.citrus.config.annotation;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class AnnotationConfigParserTest {

    @Test
    public void testLookup() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 0L);
    }

}
