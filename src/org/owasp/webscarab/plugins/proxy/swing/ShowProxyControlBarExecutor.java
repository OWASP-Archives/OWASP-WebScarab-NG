/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import javax.swing.JWindow;

import org.springframework.richclient.command.ActionCommandExecutor;

/**
 * @author rdawes
 *
 */
public class ShowProxyControlBarExecutor implements ActionCommandExecutor {

	private ProxyControlBar proxyControlBar;
	
	public ShowProxyControlBarExecutor() {
	}
	
	public ProxyControlBar getProxyControlBar() {
		return this.proxyControlBar;
	}

	public void setProxyControlBar(ProxyControlBar proxyControlBar) {
		this.proxyControlBar = proxyControlBar;
	}

	public void execute() {
		JWindow window = proxyControlBar.getControl();
		window.setVisible(!window.isVisible());
	}

}
