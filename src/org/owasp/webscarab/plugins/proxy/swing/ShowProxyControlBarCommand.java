/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import org.springframework.richclient.command.ToggleCommand;

/**
 * @author rdawes
 *
 */
public class ShowProxyControlBarCommand extends ToggleCommand {

	private ProxyControlBar proxyControlBar;
	
	public ShowProxyControlBarCommand() {
		super("showProxyControlBarCommand");
	}
	
	@Override
	protected void onDeselection() {
		proxyControlBar.setVisible(false);
	}

	@Override
	protected void onSelection() {
		proxyControlBar.setVisible(true);
	}

	@Override
	public boolean isEnabled() {
		return proxyControlBar != null && super.isEnabled();
	}

	public ProxyControlBar getProxyControlBar() {
		return this.proxyControlBar;
	}

	public void setProxyControlBar(ProxyControlBar proxyControlBar) {
		this.proxyControlBar = proxyControlBar;
		this.proxyControlBar.setShowProxyControlBarCommand(this);
	}

}
