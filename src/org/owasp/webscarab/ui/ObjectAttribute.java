/**
 * 
 */
package org.owasp.webscarab.ui;

import java.util.Comparator;

import org.springframework.util.comparator.ComparableComparator;

/**
 * @author rdawes
 *
 */
public abstract class ObjectAttribute<T> {

	public abstract Object getValue(T object);
	
	public abstract String getAttributeId ();
	
	public Comparator getComparator() {
		return new ComparableComparator();
	}
	
	public Class getAttributeClass() {
		return Object.class;
	}
	
	public boolean isAttributeEditable() {
		return false;
	}
	
	public T setAttribute(T object, Object newValue) {
		return null;
	}
	
}
