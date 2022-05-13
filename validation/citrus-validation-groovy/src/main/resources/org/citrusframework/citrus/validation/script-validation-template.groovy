import org.citrusframework.citrus.context.TestContext
import org.citrusframework.citrus.message.Message
import org.citrusframework.citrus.validation.script.GroovyScriptExecutor

public class ValidationScript implements GroovyScriptExecutor{
    public void validate(Message receivedMessage, TestContext context){
        Map<String, Object> headers = receivedMessage.getHeaders()
        String payload = receivedMessage.getPayload(String.class)

        @SCRIPTBODY@
    }
}
