/**
 *
 */
package org.owasp.webscarab.plugins.proxy.swing;

import javax.swing.JWindow;

import org.springframework.richclient.command.ToggleCommand;

/**
 * @author rdawes
 *
 */
public class ShowProxyControlBarToggleCommand extends ToggleCommand {

	private ProxyControlBar proxyControlBar;

	public ShowProxyControlBarToggleCommand() {
	}

	public ProxyControlBar getProxyControlBar() {
		return this.proxyControlBar;
	}

	public void setProxyControlBar(ProxyControlBar proxyControlBar) {
		this.proxyControlBar = proxyControlBar;
	}

	/* (non-Javadoc)
	 * @see org.springframework.richclient.command.ToggleCommand#onSelection(boolean)
	 */
	@Override
	protected boolean onSelection(boolean selected) {
		JWindow window = proxyControlBar.getControl();
		window.setVisible(selected);
		return selected;
	}

}
