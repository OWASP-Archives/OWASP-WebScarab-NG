/**
 *
 */
package org.owasp.webscarab.domain;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author rdawes
 *
 */
public class SessionFactory implements FactoryBean {

    private Session session;

    @SuppressWarnings("unchecked")
	public Class getObjectType() {
        return Session.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public Object getObject() throws Exception {
        if (session == null)
            throw new NullPointerException("Session has not yet been created");
        return session;
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

}
