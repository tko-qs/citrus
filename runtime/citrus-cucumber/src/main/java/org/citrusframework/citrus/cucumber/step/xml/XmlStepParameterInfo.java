package org.citrusframework.citrus.cucumber.step.xml;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.TypeResolver;

import java.lang.reflect.Type;

/**
 * @author Christoph Deppisch
 */
public class XmlStepParameterInfo implements ParameterInfo {

    private final Type type;

    public XmlStepParameterInfo(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isTransposed() {
        return false;
    }

    @Override
    public TypeResolver getTypeResolver() {
        return () -> type;
    }
}
