/**
 *
 */
package org.owasp.webscarab.plugins.proxy.swing;

import org.springframework.richclient.command.ToggleCommand;

/**
 * @author rdawes
 *
 */
public class InterceptResponseCommand extends ToggleCommand {

    private SwingInterceptor swingInterceptor;

    /**
     * @param swingInterceptor the swingInterceptor to set
     */
    public void setSwingInterceptor(SwingInterceptor swingInterceptor) {
        this.swingInterceptor = swingInterceptor;
    }

    /* (non-Javadoc)
     * @see org.springframework.richclient.command.ToggleCommand#onDeselection()
     */
    @Override
    protected void onDeselection() {
        swingInterceptor.setInterceptResponses(false);
    }

    /* (non-Javadoc)
     * @see org.springframework.richclient.command.ToggleCommand#onSelection()
     */
    @Override
    protected void onSelection() {
        swingInterceptor.setInterceptResponses(true);
    }

}
