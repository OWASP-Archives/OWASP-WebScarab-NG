/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import org.springframework.richclient.form.Form;

/**
 * @author rdawes
 *
 */
public interface ContentForm extends Form {

	boolean canHandle(String contentType);
	
}
