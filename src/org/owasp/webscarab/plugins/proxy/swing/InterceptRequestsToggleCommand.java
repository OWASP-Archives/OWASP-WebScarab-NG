/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import org.springframework.richclient.command.ToggleCommand;

/**
 * @author rdawes
 *
 */
public class InterceptRequestsToggleCommand extends ToggleCommand {

	private SwingInterceptor interceptor = null;
	
	private ProxyControlBar proxyControlBar = null;
	
	public InterceptRequestsToggleCommand() {
		super("interceptRequestsCommand");
	}
	
	@Override
	protected boolean onSelection(boolean selected) {
		interceptor.setInterceptRequests(selected);
		return selected;
	}

	public void setInterceptor(SwingInterceptor interceptor) {
		this.interceptor = interceptor;
		if (interceptor != null) {
			setAuthorized(true);
			interceptor.setInterceptRequests(isSelected());
		}
	}

	public void setProxyControlBar(ProxyControlBar proxyControlBar) {
		this.proxyControlBar = proxyControlBar;
		if (this.proxyControlBar != null)
			this.proxyControlBar.setInterceptRequests(this);
	}
	
	

}
