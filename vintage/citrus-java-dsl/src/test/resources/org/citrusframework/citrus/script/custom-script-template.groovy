import org.citrusframework.citrus.context.TestContext
import org.citrusframework.citrus.script.GroovyAction.ScriptExecutor

public class GScript implements ScriptExecutor {
    public void execute(TestContext context) {
        context.setVariable("scriptTemplateVar", "It works!")
        @SCRIPTBODY@
    }
}
