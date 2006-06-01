/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import org.owasp.webscarab.plugins.proxy.Proxy;
import org.springframework.beans.BeansException;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.richclient.command.support.ApplicationWindowAwareCommand;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 * 
 */
public class ProxyConfigCommand extends ApplicationWindowAwareCommand implements
		ApplicationContextAware {

	private ApplicationContext applicationContext;

	private Proxy proxy;

	public ProxyConfigCommand() {
		super("proxyConfigCommand");
	}

	@Override
	protected void doExecuteCommand() {
		final HierarchicalFormModel model = FormModelHelper
				.createCompoundFormModel(getProxy());
		Form form = new ProxyConfigForm(model);
		DialogPage page = new FormBackedDialogPage(form);
		TitledPageApplicationDialog dialog = new TitledPageApplicationDialog(
				page, getParentWindowControl()) {

			@Override
			protected boolean onFinish() {
				try {
					model.commit();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}

		};
		dialog.showDialog();
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	private Proxy getProxy() {
		if (proxy == null)
			proxy = (Proxy) applicationContext.getBean("proxy");
		return proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

}
