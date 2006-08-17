/**
 * 
 */
package org.owasp.webscarab.domain;


/**
 * @author rdawes
 *
 */
public class Annotation extends BaseEntity {
	
	public final static String PROPERTY_ANNOTATION = "annotation";
	
	private String annotation;

	public String getAnnotation() {
		return this.annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

}
