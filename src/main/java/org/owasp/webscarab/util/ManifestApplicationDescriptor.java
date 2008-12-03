/**
 * 
 */
package org.owasp.webscarab.util;

import org.springframework.richclient.application.ApplicationDescriptor;
import org.springframework.richclient.core.LabeledObjectSupport;

/**
 * @author rdawes
 *
 */
public class ManifestApplicationDescriptor extends LabeledObjectSupport implements ApplicationDescriptor  {

	private String version;
	
	public ManifestApplicationDescriptor(Class<?> target) {
		super();
		this.version = target.getPackage().getImplementationVersion();
		if (this.version == null) 
			this.version = "Non-packaged version";
	}

	public String getBuildId() {
		return null;
	}

	public String getVersion() {
		return this.version;
	}
	
	
}
