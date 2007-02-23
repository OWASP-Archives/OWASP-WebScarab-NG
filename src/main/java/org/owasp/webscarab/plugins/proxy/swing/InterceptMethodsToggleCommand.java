/**
 *
 */
package org.owasp.webscarab.plugins.proxy.swing;

import java.util.Arrays;
import java.util.List;

import org.owasp.webscarab.util.HttpMethodUtils;
import org.springframework.richclient.command.ToggleCommand;

/**
 * @author rdawes
 *
 */
public class InterceptMethodsToggleCommand extends ToggleCommand {

    private SwingInterceptor interceptor;

    private List<String> interceptRequestMethods = Arrays.asList(HttpMethodUtils.SUPPORTED_METHODS);

    public void setSwingInterceptor(SwingInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public void setInterceptRequestMethods(List<String> methods) {
        this.interceptRequestMethods = methods;
    }

    /* (non-Javadoc)
     * @see org.springframework.richclient.command.ToggleCommand#onSelection()
     */
    @Override
    protected void onSelection() {
        interceptor.setInterceptRequestMethods(interceptRequestMethods);
    }

}
