/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.lang.ref.WeakReference;

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

    WeakReference<NamedValue[]> requestHeaders = new WeakReference<NamedValue[]>(null);
    WeakReference<NamedValue[]> responseHeaders = new WeakReference<NamedValue[]>(null);
    WeakReference<byte[]> requestContent = new WeakReference<byte[]>(null);
    WeakReference<byte[]> responseContent = new WeakReference<byte[]>(null);

    public JdbcConversation(HeadersDao headersDao, BlobDao blobDao) {
        if (headersDao == null || blobDao == null)
            throw new NullPointerException("Headers or Blob DAO is null");
        this.headersDao = headersDao;
        this.blobDao = blobDao;
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.domain.Conversation#getRequestContent()
     */
    @Override
    public byte[] getRequestContent() {
        if (requestContent == null) return null;
        byte[] content = requestContent.get();
        if (content != null) return content;
        content = blobDao.findBlob(requestBlob);
        if (content == null) {
            requestContent = null;
        } else {
            requestContent = new WeakReference<byte[]>(content);
        }
        return content;
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.domain.Conversation#getRequestHeaders()
     */
    @Override
    public NamedValue[] getRequestHeaders() {
        if (headersDao == null) {
            throw new NullPointerException("HeadersDAO is null in conversation " + getId());
        }
        if (requestHeaders == null) return null;
        NamedValue[] headers = requestHeaders.get();
        if (headers != null) return headers;
        headers = headersDao.findHeaders(getId(), HeadersDao.REQUEST_HEADERS);
        if (headers == null) {
            requestHeaders = null;
        } else {
            requestHeaders = new WeakReference<NamedValue[]>(headers);
        }
        return headers;
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.domain.Conversation#getResponseContent()
     */
    @Override
    public byte[] getResponseContent() {
        if (responseContent == null) return null;
        byte[] content = responseContent.get();
        if (content != null) return content;
        content = blobDao.findBlob(responseBlob);
        if (content == null) {
            responseContent = null;
        } else {
            responseContent = new WeakReference<byte[]>(content);
        }
        return content;
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
        if (headersDao == null) {
            throw new NullPointerException("HeadersDAO is null in conversation " + getId());
        }
        if (responseHeaders == null) return null;
        NamedValue[] headers = responseHeaders.get();
        if (headers != null) return headers;
        headers = headersDao.findHeaders(getId(), HeadersDao.RESPONSE_HEADERS);
        if (headers == null) {
            responseHeaders = null;
        } else {
            responseHeaders = new WeakReference<NamedValue[]>(headers);
        }
        return headers;
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
