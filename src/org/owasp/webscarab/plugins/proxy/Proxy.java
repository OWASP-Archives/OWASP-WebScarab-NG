/**
 * 
 */
package org.owasp.webscarab.plugins.proxy;

/**
 * @author rdawes
 * 
 */
public class Proxy {

	private Annotator annotator;

	private Listener listener;
	
	public Proxy() {
	}

	public void setListener(Listener listener) {
		this.listener = listener;
		listener.setAnnotator(getAnnotator());
		Thread t = new Thread(listener);
		t.setDaemon(true);
		t.start();
	}

	public Annotator getAnnotator() {
		return this.annotator;
	}

	public void setAnnotator(Annotator annotator) {
		this.annotator = annotator;
		if (listener != null)
			listener.setAnnotator(annotator);
	}

}
