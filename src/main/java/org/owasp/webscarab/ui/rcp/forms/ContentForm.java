/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import org.springframework.richclient.form.Form;

/**
 * @author rdawes
 *
 */
public interface ContentForm extends Form {

	boolean canHandle(String contentType);
	
}
