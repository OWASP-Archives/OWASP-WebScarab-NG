/**
 * 
 */
package org.owasp.webscarab.ui.rcp;

import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.support.DefaultApplicationWindow;

/**
 * @author rdawes
 *
 */
public class DescriptorApplicationWindow extends DefaultApplicationWindow {

	public void showPage(PageDescriptor pageDescriptor) {
        if (pageDescriptor == null) throw new IllegalArgumentException("pageDescriptor == null");

        if (getPage() == null || !getPage().getId().equals(pageDescriptor.getId())) {
            showPage(createPage(pageDescriptor));
        }
        else {
            // asking for the same page, so ignore
        }
	}
	
}
