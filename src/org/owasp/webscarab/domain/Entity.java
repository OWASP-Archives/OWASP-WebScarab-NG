/*
 * Entity.java
 *
 * Created on 09 March 2006, 06:05
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.owasp.webscarab.domain;

/**
 *
 * @author rdawes
 */
public interface Entity {
    
    void setId(Integer id);
    
    Integer getId();
    
    boolean isNew();
    
}
