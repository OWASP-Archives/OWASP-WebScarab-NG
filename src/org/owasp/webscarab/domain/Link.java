/**
 * 
 */
package org.owasp.webscarab.domain;

import java.net.URI;


/**
 * @author rdawes
 *
 */
public class Link extends BaseEntity {

	private URI uri;
	private Integer conversation;
	
	public Link(URI uri, Integer conversation) {
		this.uri = uri;
		this.conversation = conversation;
	}

	/**
	 * @return the uri
	 */
	public URI getUri() {
		return this.uri;
	}

	/**
	 * @return the conversation that this URI was found in
	 */
	public Integer getConversation() {
		return this.conversation;
	}

}
