/**
 * 
 */
package org.owasp.webscarab.dao;

import java.util.Collection;

import org.owasp.webscarab.domain.Annotation;

/**
 * @author rdawes
 *
 */
public interface AnnotationDao {

	Annotation get(Integer id);
	
	void update(Annotation annotation);
	
	void delete(Integer id);
	
	Collection<Annotation> getAll();
	
}
