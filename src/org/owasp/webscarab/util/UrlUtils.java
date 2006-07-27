/*
 * URLUtils.java
 *
 * Created on 21 February 2006, 12:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.owasp.webscarab.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 
 * @author rdawes
 */
public class UrlUtils {

	/** Creates a new instance of URLUtils */
	private UrlUtils() {
		super();
	}

	public static URL getParent(URL url) {
		if (!url.getProtocol().startsWith("http"))
			return null;
		try {
			boolean parent = false;

			String s = url.toString();
			int q = s.indexOf('?');
			if (q > -1) {
				s = s.substring(0, q);
				parent = true;
			}
			int f = s.indexOf(';');
			if (f > -1) {
				s = s.substring(0, f);
				parent = true;
			}
			if (parent)
				return new URL(s);
			int sl = s.lastIndexOf('/');
			// if the url ends in /, cut of the last component
			if (sl == s.length() - 1) {
				sl = s.lastIndexOf('/', s.length() - 2);
			}
			s = s.substring(0, sl + 1);
			sl = s.lastIndexOf('/');
			// if the last slash is part of the "://", there is no parent
			if (sl == 6 || sl == 7)
				return null;
			return new URL(s);
		} catch (MalformedURLException mue) {
			return null;
		}
	}

	public static URI getParent(URI uri) {
		if (!uri.getScheme().startsWith("http"))
			return null;
		try {
			boolean parent = false;

			String s = uri.toString();
			int q = s.indexOf('?');
			if (q > -1) {
				s = s.substring(0, q);
				parent = true;
			}
			int f = s.indexOf(';');
			if (f > -1) {
				s = s.substring(0, f);
				parent = true;
			}
			if (parent)
				return new URI(s);
			int sl = s.lastIndexOf('/');
			// if the url ends in /, cut of the last component
			if (sl == s.length() - 1) {
				sl = s.lastIndexOf('/', s.length() - 2);
			}
			s = s.substring(0, sl + 1);
			sl = s.lastIndexOf('/');
			// if the last slash is part of the "://", there is no parent
			if (sl == 6 || sl == 7)
				return null;
			return new URI(s);
		} catch (URISyntaxException use) {
			use.printStackTrace();
			return null;
		}
	}

	public static String getSchemeHostPort(URI uri) {
		StringBuilder sb = new StringBuilder();
		int port = uri.getPort();
		String scheme = uri.getScheme();
		if (port == -1) {
			if (scheme.equals("http"))
				port = 80;
			if (scheme.equals("https"))
				port = 443;
		}
		sb.append(scheme).append("://");
		sb.append(uri.getHost()).append(":");
		sb.append(port);
		return sb.toString();
	}

	public static URI relativeUri(URI base, String relative) throws URISyntaxException {
		if (base == null || relative.toLowerCase().startsWith("http://")
				|| relative.toLowerCase().startsWith("https://")) {
			return new URI(relative);
		} else {
			String dir = base.getPath();
			if (! dir.endsWith("/")) 
				dir = dir.substring(0, dir.lastIndexOf("/") + 1); // include the last "/"
			while (relative.startsWith(".")) {
				if (relative.startsWith("./")) {
					relative = relative.substring(2); // "./" is meaningless
				} else if (relative.startsWith("../")) {
					relative = relative.substring(3);
					if (dir.endsWith("/") && dir.length() > 1)
						dir = dir.substring(0, dir.lastIndexOf("/", dir.length() - 2) + 1);
				}
			}
			if (relative.startsWith("/")) {
				dir = "";
			}
			relative = dir + relative;
			int q = relative.indexOf("?");
			int f = relative.lastIndexOf("#");
			String path, query = null, fragment = null;
			if (q > -1) {
				path = relative.substring(0, q);
				if (f > -1) {
					query = relative.substring(q+1, f);
					fragment = relative.substring(f+1);
				} else {
					query = relative.substring(q+1);
				}
			} else if (f > -1) {
				path = relative.substring(0, f);
				fragment = relative.substring(f+1);
			} else {
				path = relative;
			}
			return new URI(base.getScheme(), base.getAuthority(), path, query, fragment);
		}
	}
}
