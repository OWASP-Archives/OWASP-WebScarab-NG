/**
 * 
 */
package org.owasp.webscarab.ui.rcp;

import java.util.Comparator;

import org.springframework.util.comparator.ComparableComparator;
import org.springframework.util.comparator.NullSafeComparator;

/**
 * @author rdawes
 *
 */
public abstract class ObjectAttribute<T> {

	public abstract Object getValue(T object);
	
	public abstract String getAttributeId ();
	
	public Comparator<?> getComparator() {
		if (Comparable.class.isAssignableFrom(getAttributeClass()))
			return new NullSafeComparator(new ComparableComparator(), true);
		return null;
	}
	
	public Class<?> getAttributeClass() {
		return Object.class;
	}
	
	public boolean isAttributeEditable() {
		return false;
	}
	
	public T setAttribute(T object, Object newValue) {
		return null;
	}
	
}
