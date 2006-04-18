/*
 * EditorFactory.java
 *
 * Created on 16 December 2004, 12:31
 */

package org.owasp.webscarab.ui.editors;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import java.awt.Component;

/**
 * 
 * @author rogan
 */
public class EditorFactory {

	private static Map _editors = null;

	private static ByteArrayEditor[] NONE = new ByteArrayEditor[0];

	private static Logger _logger = Logger
			.getLogger("org.owasp.webscarab.ui.swing.editors.EditorFactory");

	/** Creates a new instance of EditorFactory */
	private EditorFactory() {
	}

	private static void registerEditors() {
		String packageName = "org.owasp.webscarab.ui.editors";
		// this helps to maintain the order of the editors
		_editors = new LinkedHashMap();
		// registerEditor("multipart/form-data; .*",
		// packageName + ".MultiPartPanel");
		// registerEditor("application/x-serialized-object",
		// packageName + ".SerializedObjectPanel");
		registerEditor("image/.*", packageName + ".ImagePanel");
		registerEditor("application/x-www-form-urlencoded", packageName
				+ ".UrlEncodedPanel");
		registerEditor("text/html.*", packageName + ".HTMLPanel");
		// registerEditor("text/html.*", packageName + ".XMLPanel");
		// registerEditor("text/xml.*", packageName + ".XMLPanel");
		registerEditor("text/.*", packageName + ".TextPanel");
		registerEditor("application/x-javascript", packageName + ".TextPanel");
		registerEditor("application/x-www-form-urlencoded", packageName
				+ ".TextPanel");
		registerEditor(".*", packageName + ".HexPanel");
		// registerEditor(".*", packageName + ".CompressedTextPanel");
	}

	@SuppressWarnings("unchecked")
	public static void registerEditor(String contentType, String editorClass) {
		List list = (List) _editors.get(contentType);
		if (list == null) {
			list = new ArrayList();
			_editors.put(contentType, list);
		}
		if (list.indexOf(editorClass) < 0)
			list.add(editorClass);
	}

	@SuppressWarnings("unchecked")
	public static ByteArrayEditor[] getEditors(String contentType) {
		if (contentType == null)
			return new ByteArrayEditor[] { new HexPanel() };
		if (_editors == null)
			registerEditors();
		Iterator it = _editors.keySet().iterator();
		List editors = new ArrayList();
		while (it.hasNext()) {
			String type = (String) it.next();
			if (contentType.matches(type)) {
				List list = (List) _editors.get(type);
				Iterator it2 = list.iterator();
				while (it2.hasNext()) {
					String className = (String) it2.next();
					try {
						Object ed = Class.forName(className).newInstance();
						if (ed instanceof ByteArrayEditor
								&& ed instanceof Component) {
							editors.add(ed);
						} else {
							_logger
									.warning("Editor "
											+ className
											+ " must implement ByteArrayEditor and Component");
						}
					} catch (Exception e) {
						_logger.warning("Exception trying to instantiate "
								+ className + " : " + e);
					}
				}
			}
		}
		return (ByteArrayEditor[]) editors.toArray(NONE);
	}

}
