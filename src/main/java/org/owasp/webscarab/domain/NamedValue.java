/*
 * NamedValue.java
 *
 * Created on 09 March 2006, 06:08
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.owasp.webscarab.domain;

import java.util.LinkedList;
import java.util.List;


/**
 * 
 * @author rdawes
 */
public class NamedValue extends BaseEntity {

	public final static String PROPERTY_NAME = "name";

	public final static String PROPERTY_VALUE = "value";

	private String name;

	private String value;

	/** Creates a new instance of NamedValue */
	public NamedValue(@SuppressWarnings("hiding")
	String name, @SuppressWarnings("hiding")
	String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return this.name;
	}

	public String getValue() {
		return this.value;
	}

	public static NamedValue[] copy(NamedValue[] headers) {
		if (headers == null || headers.length == 0)
			return headers;
		NamedValue[] copy = new NamedValue[headers.length];
		System.arraycopy(headers, 0, copy, 0, headers.length);
		return copy;
	}

	public String toString() {
		return getName() + ": " + getValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NamedValue))
			return false;
		NamedValue that = (NamedValue) obj;
		if (this.getId() != null && that.getId() != null) {
			if (!this.getId().equals(that.getId()))
				return false;
		} else if (this.getId() != that.getId())
			return false;
		if (this.getName() != null && that.getName() != null) {
			if (!this.getName().equals(that.getName()))
				return false;
		} else if (this.getName() != that.getName())
			return false;
		if (this.getValue() != null && that.getValue() != null) {
			if (!this.getValue().equals(that.getValue()))
				return false;
		} else if (this.getValue() != that.getValue())
			return false;
		return true;
	}

	@SuppressWarnings("unchecked")
	public static NamedValue[] find(String name, NamedValue[] headers) {
		if (headers == null || headers.length == 0)
			return headers;
		List found = new LinkedList();
		for (int i = 0; i < headers.length; i++)
			if (headers[i].getName().equalsIgnoreCase(name))
				found.add(headers[i]);
		return (NamedValue[]) found.toArray(new NamedValue[found.size()]);
	}

	public static NamedValue findOne(String name, NamedValue[] nv) {
		NamedValue[] found = find(name, nv);
		if (found == null || found.length == 0) return null;
		if (found.length == 1) return found[0];
		throw new IllegalStateException("More than one result for '" + name + "'");
	}
	
	public static NamedValue[] add(NamedValue[] headers, NamedValue header) {
		if (headers == null || headers.length == 0)
			return new NamedValue[] { header };
		NamedValue[] result = new NamedValue[headers.length + 1];
		System.arraycopy(headers, 0, result, 0, headers.length);
		result[result.length - 1] = header;
		return result;
	}

	@SuppressWarnings("unchecked")
	public static NamedValue[] delete(String name, NamedValue[] headers) {
		if (headers == null || headers.length == 0)
			return headers;
		List left = new LinkedList();
		for (int i = 0; i < headers.length; i++)
			if (!headers[i].getName().equalsIgnoreCase(name))
				left.add(headers[i]);
		return (NamedValue[]) left.toArray(new NamedValue[left.size()]);
	}

	public static NamedValue[] parse(String string, String pairSeparator,
			String nvSeparator) {
		if (string == null)
			return null;
		String[] pairs = string.split(pairSeparator);
		NamedValue[] values = new NamedValue[pairs.length];
		for (int i = 0; i < pairs.length; i++) {
			String[] nv = pairs[i].split(nvSeparator, 2);
			if (nv.length == 2) {
				values[i] = new NamedValue(nv[0], nv[1]);
			} else if (nv.length == 1 && !"".equals(nv[0])) {
				values[i] = new NamedValue(nv[0], null);
			} else 
				throw new ArrayIndexOutOfBoundsException(pairs[i]
						+ " did not contain '" + nvSeparator + "'");
		}
		return values;
	}
	
	public static String join(NamedValue[] nv, String pairSeparator, String nvSeparator) {
		if (nv == null || nv.length == 0) 
			return null;
		StringBuilder string = new StringBuilder();
		for (int i=0; i<nv.length; i++) {
			if (i>0) string.append(pairSeparator);
			string.append(nv[i].getName()).append(nvSeparator);
			string.append(nv[i].getValue());
		}
		return string.toString();
	}
	
	public static String get(String name, NamedValue[] nv) {
		if (nv == null) return null;
		for (int i=0; i<nv.length; i++)
			if (name.equalsIgnoreCase(nv[i].getName())) 
				return nv[i].getValue();
		return null;
	}
	
	public static NamedValue[] set(String name, String value, NamedValue[] nv) {
		NamedValue[] existing = find(name, nv);
		NamedValue newNv = new NamedValue(name, value);
		if (existing == null)
			return add(nv, newNv);
		if (existing.length > 1) {
			return add(delete(name, nv), newNv);
		}
		NamedValue[] result = new NamedValue[nv.length];
		for (int i=0; i<nv.length; i++) {
			if (nv[i].getName().equals(name)) {
				result[i] = newNv;
			} else {
				result[i] = nv[i];
			}
		}
		return result;
	}
	
}
