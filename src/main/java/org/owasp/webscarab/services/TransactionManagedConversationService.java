/**
 *
 */
package org.owasp.webscarab.services;

import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.Session;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author rdawes
 *
 */
public class TransactionManagedConversationService extends ConversationService {

    private PlatformTransactionManager transactionManager;

    private DefaultTransactionDefinition def = new DefaultTransactionDefinition(
            TransactionDefinition.PROPAGATION_REQUIRED
                    | TransactionDefinition.ISOLATION_READ_UNCOMMITTED);

    public TransactionManagedConversationService() {

    }

    public void addConversation(Session session, Conversation conversation) {
        TransactionStatus status = getTransactionManager().getTransaction(def);
        try {
            super.addConversation(session, conversation);
        } catch (Exception t) {
            getTransactionManager().rollback(status);
            return;
        }
        getTransactionManager().commit(status);
    }

    public void updateAnnotation(Annotation annotation) {
        TransactionStatus status = getTransactionManager().getTransaction(def);
        try {
            super.updateAnnotation(annotation);
        } catch (Exception t) {
            getTransactionManager().rollback(status);
            return;
        }
        getTransactionManager().commit(status);
    }

    /**
     * @return the transactionManager
     */
    public PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    /**
     * @param transactionManager
     *            the transactionManager to set
     */
    public void setTransactionManager(
            PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

}
