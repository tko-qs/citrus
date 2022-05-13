package org.citrusframework.citrus.endpoint;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class EndpointComponentTest {

    @Test
    public void testLookup() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 0L);
    }
}
