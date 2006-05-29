/**
 * 
 */
package org.owasp.webscarab.domain;


/**
 * @author rdawes
 *
 */
public class Finding extends BaseEntity {

	private Integer[] conversations;
	
	private String description;
	
	public Finding(String description) {
		this(description, null);
	}
	
	public Finding(String description, Integer[] conversations) {
		this.description = description;
		this.conversations = conversations;
	}
	
	public Integer[] getConversations() {
		return this.conversations;
	}

	public String getDescription() {
		return this.description;
	}

	public void setConversations(Integer[] conversations) {
		this.conversations = conversations;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
