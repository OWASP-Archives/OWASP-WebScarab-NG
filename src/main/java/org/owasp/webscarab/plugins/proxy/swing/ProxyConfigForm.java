/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import javax.swing.JComponent;

import org.owasp.webscarab.plugins.proxy.ListenerConfiguration;
import org.owasp.webscarab.plugins.proxy.Proxy;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.richclient.form.AbstractDetailForm;
import org.springframework.richclient.form.AbstractTableMasterForm;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 * @author rdawes
 * 
 */
public class ProxyConfigForm extends AbstractTableMasterForm {

	private static final String FORM_ID = "proxyConfigForm";
	private static final String DETAIL_FORM_ID = "proxyConfigForm";

	public ProxyConfigForm(HierarchicalFormModel model) {
		super(model, Proxy.PROPERTY_LISTENERS, FORM_ID,
				ListenerConfiguration.class);
		setConfirmDelete(false);
	}

	@Override
    protected AbstractDetailForm createDetailForm(HierarchicalFormModel parentFormModel,
            ValueModel valueModel, ObservableList observableList) {
        return new AbstractDetailForm(parentFormModel, DETAIL_FORM_ID, valueModel, observableList) {

            protected JComponent createFormControl() {
                TableFormBuilder builder = new TableFormBuilder(getBindingFactory());
                builder.add( ListenerConfiguration.PROPERTY_HOSTNAME);
                builder.row();
                builder.add( ListenerConfiguration.PROPERTY_PORT);
                builder.row();
                builder.add( ListenerConfiguration.PROPERTY_BASE);
                builder.row();
                updateControlsForState();
                return builder.getForm();
            }            
        };
    }

	@Override
	protected String[] getColumnPropertyNames() {
		return new String[] { ListenerConfiguration.PROPERTY_HOSTNAME, ListenerConfiguration.PROPERTY_PORT, ListenerConfiguration.PROPERTY_BASE};
	}

}
