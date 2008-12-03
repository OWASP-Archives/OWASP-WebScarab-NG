/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms.support;

import java.lang.reflect.Array;
import java.net.URI;

import org.owasp.webscarab.domain.NamedValue;
import org.springframework.binding.value.support.DefaultValueChangeDetector;

/**
 * @author rdawes
 *
 */
public class ArrayChangeDetector extends DefaultValueChangeDetector {

	@SuppressWarnings("unchecked")
	public ArrayChangeDetector() {
		super();
		getClassesWithSafeEquals().add(NamedValue.class);
		getClassesWithSafeEquals().add(URI.class);
	}
	
	@SuppressWarnings("unchecked")
	public boolean hasValueChanged(Object oldValue, Object newValue) {
		if (oldValue == newValue) 
			return false;
		if (oldValue == null || newValue == null)
			return true;
		
		if (getClassesWithSafeEquals().contains(oldValue.getClass()))
			return super.hasValueChanged(oldValue, newValue);
		
		if (oldValue.getClass().isArray() && newValue.getClass().isArray()) {
			if (oldValue == newValue) return true; // we can't tell
			int oldLength = Array.getLength(oldValue);
			int newLength = Array.getLength(newValue);
			if (oldLength != newLength)
				return true;
			Class oldClass = oldValue.getClass().getComponentType();
			Class newClass = newValue.getClass().getComponentType();
			if (!oldClass.equals(newClass)) return true;
			if (getClassesWithSafeEquals().contains(oldClass) || oldClass.isPrimitive()) {
				for (int i=0; i<oldLength; i++) {
					Object oldObj = Array.get(oldValue, i);
					Object newObj = Array.get(newValue, i);
					if (super.hasValueChanged(oldObj, newObj))
						return true;
				}
			} else {
				return super.hasValueChanged(oldValue, newValue);
			}
			return false;
		}
		return super.hasValueChanged(oldValue, newValue);
	}

}
