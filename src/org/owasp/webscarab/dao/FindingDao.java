/**
 * 
 */
package org.owasp.webscarab.dao;

import java.util.Collection;

import org.owasp.webscarab.domain.Finding;

/**
 * @author rdawes
 *
 */
public interface FindingDao {

	Finding get(Integer id);
	
	void update(Finding finding);
	
	void delete(Integer id);
	
	Collection<Finding> getAll();
	
}
