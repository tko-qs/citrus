import groovy.json.JsonSlurper
import org.citrusframework.citrus.context.TestContext
import org.citrusframework.citrus.message.Message
import org.citrusframework.citrus.validation.script.GroovyScriptExecutor

public class ValidationScript implements GroovyScriptExecutor{
    public void validate(Message receivedMessage, TestContext context){
        String payload = receivedMessage.getPayload(String.class)

        def json;
        if (payload.length()) {
            json = new JsonSlurper().parseText(payload)
        } else {
            json = "";
        }

        @SCRIPTBODY@
    }
}
