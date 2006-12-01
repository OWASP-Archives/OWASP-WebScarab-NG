/**
 *
 */
package org.owasp.webscarab.domain;

import java.util.Date;

/**
 * @author rdawes
 *
 */
public class Session extends BaseEntity {

	private String description;
	private Date date;

	public Session() {
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String name) {
		this.description = name;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return this.date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date startDate) {
		this.date = startDate;
	}

}
