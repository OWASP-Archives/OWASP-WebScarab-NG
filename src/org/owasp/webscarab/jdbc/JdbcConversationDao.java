/**
 * 
 */
package org.owasp.webscarab.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.ConversationSummary;
import org.owasp.webscarab.NamedValue;
import org.owasp.webscarab.dao.BlobDao;
import org.owasp.webscarab.dao.ConversationDao;
import org.owasp.webscarab.dao.UriDao;
import org.owasp.webscarab.jdbc.VersionDao;

/**
 * @author rdawes
 * 
 */
public class JdbcConversationDao extends PropertiesJdbcDaoSupport
        implements ConversationDao {

    private Integer session;

    private UriDao uriDao;

    private VersionDao versionDao;

    private HeadersDao headersDao;

    private BlobDao blobDao;

    private MethodQuery methodQuery;

    private MethodInsert methodInsert;

    private MessageQuery messageQuery;

    private MessageInsert messageInsert;

    private ConversationIdQuery conversationIdQuery;

    private ConversationSummaryQuery conversationSummaryQuery;

    private ConversationSummaryInsert conversationSummaryInsert;

    private DescriptionQuery descriptionQuery;

    private DescriptionInsert descriptionInsert;

    protected JdbcConversationDao(Integer session, UriDao uriDao,
            VersionDao versionDao, HeadersDao headersDao, BlobDao blobDao) {
        this.session = session;
        this.uriDao = uriDao;
        this.versionDao = versionDao;
        this.headersDao = headersDao;
        this.blobDao = blobDao;
    }

    protected void initDao() throws Exception {
        super.initDao();

        methodQuery = new MethodQuery();
        methodInsert = new MethodInsert();
        messageQuery = new MessageQuery();
        messageInsert = new MessageInsert();
        conversationIdQuery = new ConversationIdQuery();
        conversationSummaryQuery = new ConversationSummaryQuery();
        conversationSummaryInsert = new ConversationSummaryInsert();
        descriptionQuery = new DescriptionQuery();
        descriptionInsert = new DescriptionInsert();

        try {
            checkTables();
        } catch (Exception e) {
            createTables();
            checkTables();
        }
    }

    protected void checkTables() throws SQLException {
        Integer id = new Integer(0);
        methodQuery.getId("");
        messageQuery.getId("");
        conversationIdQuery.getConversationIds();
        conversationSummaryQuery.getSummary(id);
        descriptionQuery.getDescription(id);
    }

    protected void createTables() throws SQLException {
        getJdbcTemplate().execute(getProperty("createTable.methods"));
        getJdbcTemplate().execute(getProperty("createTable.conversations"));
        getJdbcTemplate().execute(getProperty("createTable.messages"));
        getJdbcTemplate().execute(getProperty("createTable.versions"));
        getJdbcTemplate().execute(getProperty("createTable.descriptions"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.ConversationDao#getConversationIds()
     */
    public Collection<Integer> getConversationIds() {
        return conversationIdQuery.getConversationIds();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.ConversationDao#getConversation(java.lang.Integer)
     */
    public Conversation getConversation(Integer id) {
        ConversationSummary summary = getConversationSummary(id);
        if (summary == null)
            return null;
        Conversation conversation = new Conversation();
        conversation.setId(summary.getId());
        conversation.setRequestMethod(summary.getRequestMethod());
        conversation.setRequestUri(summary.getRequestUri());
        conversation.setRequestVersion(summary.getRequestVersion());
        conversation.setRequestHeaders(headersDao.findHeaders(id,
                HeadersDao.REQUEST_HEADERS));
        if (summary.getRequestContentChecksum() != null) {
            String key = summary.getRequestContentChecksum();
            byte[] content = blobDao.findBlob(key);
            conversation.setRequestContent(content);
        }
        conversation.setResponseVersion(summary.getResponseVersion());
        conversation.setResponseStatus(summary.getResponseStatus());
        conversation.setResponseMessage(summary.getResponseMessage());
        conversation.setResponseHeaders(headersDao.findHeaders(id,
                HeadersDao.RESPONSE_HEADERS));
        if (summary.getResponseContentChecksum() != null) {
            String key = summary.getResponseContentChecksum();
            byte[] content = blobDao.findBlob(key);
            conversation.setResponseContent(content);
        }
        conversation.setResponseFooters(headersDao.findHeaders(id,
                HeadersDao.RESPONSE_FOOTERS));
        return conversation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.ConversationDao#getConversationSummary(java.lang.Integer)
     */
    public ConversationSummary getConversationSummary(Integer id) {
        return conversationSummaryQuery.getSummary(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.ConversationDao#getId(org.owasp.webscarab.Conversation)
     */
    public void getId(Conversation conversation, ConversationSummary summary) {
        Integer id = conversationSummaryInsert.insert(summary);
        conversation.setId(id);
        NamedValue[] nv = conversation.getRequestHeaders();
        headersDao.saveHeaders(id, HeadersDao.REQUEST_HEADERS, nv);
        blobDao.saveBlob(summary.getRequestContentChecksum(), conversation
                .getRequestContent());
        nv = conversation.getResponseHeaders();
        headersDao.saveHeaders(id, HeadersDao.RESPONSE_HEADERS, nv);
        blobDao.saveBlob(summary.getResponseContentChecksum(), conversation
                .getResponseContent());
        nv = conversation.getResponseFooters();
        headersDao.saveHeaders(id, HeadersDao.RESPONSE_FOOTERS, nv);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.ConversationDao#getConversationDescription(java.lang.Integer)
     */
    public String getConversationDescription(Integer id) {
        return descriptionQuery.getDescription(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.ConversationDao#updateConversationDescription(java.lang.Integer,
     *      java.lang.String)
     */
    public void updateConversationDescription(Integer id, String description) {
        descriptionInsert.insert(id, description);
    }

    private Integer getMethod(String method) {
        Integer id = methodQuery.getId(method);
        if (id != null)
            return id;
        return methodInsert.insert(method);
    }

    private Integer getMessage(String message) {
        Integer id = messageQuery.getId(message);
        if (id != null)
            return id;
        return messageInsert.insert(message);
    }

    private class ConversationIdQuery extends MappingSqlQuery {

        protected ConversationIdQuery() {
            super(getDataSource(),
                    "SELECT id FROM conversations WHERE session = ? ORDER BY id ASC");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        @SuppressWarnings("unchecked")
        public List<Integer> getConversationIds() {
            return execute(session);
        }

        protected Object mapRow(ResultSet rs, @SuppressWarnings("unused")
        int rownum) throws SQLException {
            return new Integer(rs.getInt("id"));
        }
    }

    private class ConversationSummaryQuery extends MappingSqlQuery {

        public ConversationSummaryQuery() {
            super(
                    getDataSource(),
                    "SELECT "
                            + "id, date, methods.method as request_method, request_uri, request_version, "
                            + "request_content_checksum, response_version, "
                            + "response_status, messages.message as response_message, response_content_checksum, plugin "
                            + "FROM conversations, methods, messages "
                            + "WHERE methods.id = conversations.request_method "
                            + "AND messages.id = conversations.response_message "
                            + "AND conversations.id = ?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public ConversationSummary getSummary(Integer id) {
            return (ConversationSummary) findObject(new Object[] { id });
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.jdbc.object.MappingSqlQuery#mapRow(java.sql.ResultSet,
         *      int)
         */
        @Override
        protected Object mapRow(ResultSet rs, @SuppressWarnings("unused")
        int rownum) throws SQLException {
            ConversationSummary summary = new ConversationSummary();
            summary.setId(new Integer(rs.getInt("id")));
            summary.setDate(rs.getTimestamp("date"));
            summary.setRequestMethod(rs.getString("request_method"));
            summary.setRequestUri(uriDao.findUri(new Integer(rs
                    .getInt("request_uri"))));
            Integer version = new Integer(rs.getInt("request_version"));
            summary.setRequestVersion(versionDao.getVersion(version));
            version = new Integer(rs.getInt("response_version"));
            summary.setResponseVersion(versionDao.getVersion(version));
            summary.setRequestContentChecksum(rs
                    .getString("request_content_checksum"));
            summary.setResponseStatus(rs.getString("response_status"));
            summary.setResponseMessage(rs.getString("response_message"));
            summary.setResponseContentChecksum(rs
                    .getString("response_content_checksum"));
            return summary;
        }
    }

    private class ConversationSummaryInsert extends SqlUpdate {

        protected ConversationSummaryInsert() {
            super(
                    getDataSource(),
                    "INSERT INTO conversations ("
                            + "session, date, request_method, request_uri, request_version, request_content_checksum, "
                            + "response_version, response_status, response_message, response_content_checksum, plugin) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Integer insert(ConversationSummary summary) {
            Integer uriId = uriDao.findUriId(summary.getRequestUri());
            if (uriId == null)
                uriId = uriDao.saveUri(summary.getRequestUri());
            Object[] objs = new Object[] { session, summary.getDate(),
                    getMethod(summary.getRequestMethod()), uriId,
                    versionDao.getId(summary.getRequestVersion()),
                    summary.getRequestContentChecksum(),
                    versionDao.getId(summary.getResponseVersion()),
                    summary.getResponseStatus(),
                    getMessage(summary.getResponseMessage()),
                    summary.getResponseContentChecksum(), new Integer(0) };
            super.update(objs);
            Integer id = retrieveIdentity();
            summary.setId(id);
            return id;
        }
    }

    private class MethodQuery extends MappingSqlQuery {

        protected MethodQuery() {
            super(getDataSource(), "SELECT id FROM methods WHERE method = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        public Integer getId(String method) {
            return (Integer) findObject(method);
        }

        protected Object mapRow(ResultSet rs, @SuppressWarnings("unused")
        int rownum) throws SQLException {
            return new Integer(rs.getInt("id"));
        }
    }

    private class MethodInsert extends SqlUpdate {

        protected MethodInsert() {
            super(getDataSource(),
                    "INSERT INTO methods (id, method) VALUES (?,?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        protected Integer insert(String method) {
            Object[] objs = new Object[] { null, method };
            super.update(objs);
            return retrieveIdentity();
        }
    }

    private class MessageQuery extends MappingSqlQuery {

        protected MessageQuery() {
            super(getDataSource(), "SELECT id FROM messages WHERE message = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        public Integer getId(String message) {
            return (Integer) findObject(message);
        }

        protected Object mapRow(ResultSet rs, @SuppressWarnings("unused")
        int rownum) throws SQLException {
            return new Integer(rs.getInt("id"));
        }
    }

    private class MessageInsert extends SqlUpdate {

        protected MessageInsert() {
            super(getDataSource(),
                    "INSERT INTO messages (id, message) VALUES (?,?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        protected Integer insert(String message) {
            Object[] objs = new Object[] { null, message };
            super.update(objs);
            return retrieveIdentity();
        }
    }

    private class DescriptionQuery extends MappingSqlQuery {

        protected DescriptionQuery() {
            super(getDataSource(),
                    "SELECT description FROM descriptions WHERE id = ?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public String getDescription(Integer conversation) {
            return (String) findObject(conversation);
        }

        protected Object mapRow(ResultSet rs, @SuppressWarnings("unused")
        int rownum) throws SQLException {
            return rs.getString("description");
        }
    }

    private class DescriptionInsert extends SqlUpdate {

        protected DescriptionInsert() {
            super(getDataSource(),
                    "INSERT INTO descriptions (id, description) VALUES (?,?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        protected Integer insert(Integer conversation, String description) {
            Object[] objs = new Object[] { conversation, description };
            super.update(objs);
            return retrieveIdentity();
        }
    }

}
