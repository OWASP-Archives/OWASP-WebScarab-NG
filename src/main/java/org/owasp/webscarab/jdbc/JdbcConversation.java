/**
 *
 */
package org.owasp.webscarab.jdbc;

import org.owasp.webscarab.dao.BlobDao;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;

/**
 * @author rdawes
 *
 */
public class JdbcConversation extends Conversation {

    private String requestBlob;
    private String responseBlob;

    private HeadersDao headersDao;
    private BlobDao blobDao;

    /**
     * @param blobDao the blobDao to set
     */
    public void setBlobDao(BlobDao blobDao) {
        this.blobDao = blobDao;
    }
    /**
     * @param headersDao the headersDao to set
     */
    public void setHeadersDao(HeadersDao headerDao) {
        this.headersDao = headerDao;
    }
    /* (non-Javadoc)
     * @see org.owasp.webscarab.domain.Conversation#getRequestContent()
     */
    @Override
    public byte[] getRequestContent() {
        return blobDao.findBlob(requestBlob);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.domain.Conversation#getRequestHeaders()
     */
    @Override
    public NamedValue[] getRequestHeaders() {
        if (headersDao == null)
            System.exit(1);
        return headersDao.findHeaders(getId(), HeadersDao.REQUEST_HEADERS);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.domain.Conversation#getResponseContent()
     */
    @Override
    public byte[] getResponseContent() {
        return blobDao.findBlob(responseBlob);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.domain.Conversation#getResponseFooters()
     */
    @Override
    public NamedValue[] getResponseFooters() {
        return headersDao.findHeaders(getId(), HeadersDao.RESPONSE_FOOTERS);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.domain.Conversation#getResponseHeaders()
     */
    @Override
    public NamedValue[] getResponseHeaders() {
        if (headersDao == null)
            System.exit(1);
        return headersDao.findHeaders(getId(), HeadersDao.RESPONSE_HEADERS);
    }

    /**
     * @param requestBlob the requestBlob to set
     */
    public void setRequestBlob(String requestBlob) {
        this.requestBlob = requestBlob;
    }

    /**
     * @param responseBlob the responseBlob to set
     */
    public void setResponseBlob(String responseBlob) {
        this.responseBlob = responseBlob;
    }

}
