/**
 *
 */
package org.owasp.webscarab.domain;

import org.owasp.webscarab.util.HttpMethodUtils;
import org.springframework.rules.Rules;
import org.springframework.rules.support.DefaultRulesSource;

/**
 * @author rdawes
 *
 */
public class WebScarabValidationRulesSource extends DefaultRulesSource {

    public WebScarabValidationRulesSource() {
        super();
        addRules(createConversationRules());
    }

    private Rules createConversationRules() {
        return new Rules(Conversation.class) {
            protected void initRules() {
                add(Conversation.PROPERTY_REQUEST_METHOD, required());
                add(Conversation.PROPERTY_REQUEST_METHOD,
                            inGroup(HttpMethodUtils.SUPPORTED_METHODS)
                );
                add(Conversation.PROPERTY_REQUEST_URI, required());
                // add(Conversation.PROPERTY_REQUEST_URI, new Constraint() {
                // public boolean test(Object object) {
                // if (object == null)
                // return false;
                // if (!(object instanceof URI))
                // return false;
                // URI uri = (URI) object;
                // return uri.getScheme().startsWith("http")
                // && uri.getHost() != null;
                // }
                // });
                add(Conversation.PROPERTY_REQUEST_VERSION, required());
                add(Conversation.PROPERTY_REQUEST_VERSION,
                        ifTrue(present(), or(eq("HTTP/1.0"),
                        eq("HTTP/1.1"))));
                // add(Conversation.PROPERTY_REQUEST_CONTENT, any( new
                // Constraint[] {
                // and()
                // }));
                add(Conversation.PROPERTY_RESPONSE_VERSION,
                        ifTrue(present(), or(eq("HTTP/1.0"),
                        eq("HTTP/1.1"))));
            }
        };
    }
}
