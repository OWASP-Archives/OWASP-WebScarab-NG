/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.swing.JComponent;

import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.util.CharsetUtils;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 *
 */
public abstract class AbstractContentForm extends AbstractForm implements ContentForm {

	private String contentPropertyName;
	
	private ValueModel headerValueModel, contentValueModel;
	
	private boolean updating = false;
	
	private ContentListener contentListener = null;
	
	public AbstractContentForm(FormModel model, String formId, String headerPropertyName, String contentPropertyName) {
		super(model, formId);
		this.contentPropertyName = contentPropertyName;
		headerValueModel = getValueModel(headerPropertyName);
		contentValueModel = getValueModel(contentPropertyName);
	}
	
	protected boolean isReadOnly() {
		return getFormModel().getFieldMetadata(contentPropertyName).isReadOnly();
	}
	
	public final JComponent createFormControl() {
		JComponent c = createContentFormControl();
		if (contentListener == null) {
			contentListener = new ContentListener();
			contentValueModel.addValueChangeListener(contentListener);
			c.addComponentListener(contentListener);
		}
		return c;
	}
	
	protected abstract JComponent createContentFormControl();
	
	protected abstract void clearContentFormControl();
	
	protected abstract void updateContentFormControl();
	
	protected boolean isUpdating() {
		return updating;
	}
	
	protected String getContentType() {
		NamedValue[] headers = (NamedValue[]) headerValueModel.getValue();
		NamedValue[] ct = NamedValue.find("Content-Type", headers);
		if (ct == null || ct.length == 0) return null;
		return ct[0].getValue();
	}
	
	protected String getDeclaredCharacterSet() {
		String ct = getContentType();
		if (ct == null) return null;
		int semi = ct.indexOf(";");
		if (semi < 0) return null;
		return ct.substring(semi).trim();
	}
	
	protected String getDetectedCharacterSet() {
		byte[] content = getContent();
		if (content == null || content.length == 0) return null;
		return CharsetUtils.getCharset(content);
	}
	
	protected byte[] getContent() {
		return (byte[]) contentValueModel.getValue();
	}
	
	protected void setContent(byte[] content) {
		contentValueModel.setValueSilently(content, contentListener);
	}
	
	protected String getContentAsString() {
		byte[] content = getContent();
		if (content == null || content.length == 0) return null;
		String cs = getDeclaredCharacterSet();
		if (cs == null) 
			cs = getDetectedCharacterSet();
		if (cs != null) {
			try {
				return new String(content, cs);
			} catch (Exception e) {}
		}
		return new String(content);
	}
	
	protected void setContent(String content) throws UnsupportedEncodingException {
		if (content == null || content.length() == 0) {
			setContent((byte[]) null);
			return;
		}
		String cs = getDeclaredCharacterSet();
		if (cs == null) 
			cs = getDetectedCharacterSet();
		if (cs == null)
			cs = "ISO-8859-1";
		setContent(content.getBytes(cs));
	}
	
	protected InputStream getContentAsStream() {
		byte[] content = getContent();
		if (content == null || content.length == 0) return null;
		return new ByteArrayInputStream(content);
	}
	
	protected Reader getContentAsReader(String charSet) {
		InputStream content = getContentAsStream();
		if (content == null) return null;
		if (charSet != null) {
			try {
				return new InputStreamReader(content, charSet);
			} catch (Exception e) {}
		}
		return new InputStreamReader(content);
	}
	
	protected Reader getContentAsReader() {
		String cs = getDeclaredCharacterSet();
		if (cs == null) 
			cs = getDetectedCharacterSet();
		return getContentAsReader(cs);
	}
	
	private class ContentListener extends ComponentAdapter implements PropertyChangeListener {

		private boolean upToDate = false;
		
		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			upToDate = false;
			if (getControl().isShowing()) {
				updating = true;
				if (canHandle(getContentType())) {
					updateContentFormControl();
				} else {
					clearContentFormControl();
				}
				updating = false;
				upToDate = true;
			} else {
				updating = true;
				clearContentFormControl();
				updating = false;
			}
		}
		
		public void componentShown(ComponentEvent e) {
			if (!upToDate) {
				updating = true;
				updateContentFormControl();
				updating = false;
				upToDate = true;
			}
		}

	}
	
}
