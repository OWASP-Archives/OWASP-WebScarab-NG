/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.owasp.webscarab.dao.BlobDao;
import org.owasp.webscarab.dao.ConversationDao;
import org.owasp.webscarab.dao.UriDao;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.domain.Session;
import org.owasp.webscarab.jdbc.VersionDao;

/**
 * @author rdawes
 *
 */
public class JdbcConversationDao extends PropertiesJdbcDaoSupport implements
        ConversationDao {

    private UriDao uriDao;

    private VersionDao versionDao;

    private HeadersDao headersDao;

    private BlobDao blobDao;

    private ConversationIdQuery conversationIdQuery;

    private ConversationQuery conversationQuery;

    private ConversationInsert conversationInsert;

    protected JdbcConversationDao(UriDao uriDao, VersionDao versionDao,
            HeadersDao headersDao, BlobDao blobDao) {
        this.uriDao = uriDao;
        this.versionDao = versionDao;
        this.headersDao = headersDao;
        this.blobDao = blobDao;
    }

    protected void initDao() throws Exception {
        super.initDao();

        conversationIdQuery = new ConversationIdQuery();
        conversationQuery = new ConversationQuery();
        conversationInsert = new ConversationInsert();

        try {
            checkTables();
        } catch (Exception e) {
            createTables();
            checkTables();
        }
    }

    protected void checkTables() {
        Integer id = new Integer(0);
        Session session = new Session();
        session.setId(0);
        getMethod("GET");
        getMessage("Ok");
        getSource("Proxy");
        conversationIdQuery.getConversationIds(session);
        conversationQuery.get(id);
    }

    protected void createTables() {
        getJdbcTemplate().execute(getProperty("methods.createTable"));
        getJdbcTemplate().execute(getProperty("conversations.createTable"));
        getJdbcTemplate().execute(getProperty("messages.createTable"));
        getJdbcTemplate().execute(getProperty("versions.createTable"));
        getJdbcTemplate().execute(getProperty("sources.createTable"));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.owasp.webscarab.dao.ConversationDao#getConversationIds()
     */
    public Collection<Integer> getAllIds(Session session) {
        return conversationIdQuery.getConversationIds(session);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.owasp.webscarab.dao.ConversationDao#getConversation(java.lang.Integer)
     */
    public Conversation get(Integer id) {
        return conversationQuery.get(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.owasp.webscarab.dao.ConversationDao#getId(org.owasp.webscarab.Conversation)
     */
    public Conversation add(Session session, Conversation conversation) {
        return conversationInsert.insert(session.getId(), conversation);
    }

    private Integer getMethod(String method) {
        String query = "SELECT id FROM methods WHERE method = ?";
        Object[] args = new Object[] { method };
        JdbcTemplate jt = getJdbcTemplate();
        try {
            return new Integer(jt.queryForInt(query, args));
        } catch (IncorrectResultSizeDataAccessException irsdae) {
            String insert = "INSERT INTO methods (method) VALUES (?)";
            args = new Object[] { method };
            jt.update(insert, args);
            return retrieveIdentity();
        }
    }

    private Integer getMessage(String message) {
        String query = "SELECT id FROM messages WHERE message = ?";
        Object[] args = new Object[] { message };
        JdbcTemplate jt = getJdbcTemplate();
        try {
            return new Integer(jt.queryForInt(query, args));
        } catch (IncorrectResultSizeDataAccessException irsdae) {
            String insert = "INSERT INTO messages (message) VALUES (?)";
            args = new Object[] { message };
            jt.update(insert, args);
            return retrieveIdentity();
        }
    }

    private Integer getSource(String source) {
        String query = "SELECT id FROM sources WHERE source = ?";
        Object[] args = new Object[] { source };
        JdbcTemplate jt = getJdbcTemplate();
        try {
            return new Integer(jt.queryForInt(query, args));
        } catch (IncorrectResultSizeDataAccessException irsdae) {
            String insert = "INSERT INTO sources (source) VALUES (?)";
            args = new Object[] { source };
            jt.update(insert, args);
            return retrieveIdentity();
        }
    }

    private class ConversationIdQuery extends MappingSqlQuery {

        protected ConversationIdQuery() {
            super(getDataSource(),
                    "SELECT id FROM conversations WHERE session_id = ? ORDER BY id ASC");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        @SuppressWarnings("unchecked")
        public List<Integer> getConversationIds(Session session) {
            return execute(session.getId());
        }

        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            return new Integer(rs.getInt("id"));
        }
    }

    private class ConversationQuery extends MappingSqlQuery {

        public ConversationQuery() {
            super(
                    getDataSource(),
                    "SELECT "
                            + "conversations.id, request_date, source_id, methods.method as request_method, request_uri_id, request_version_id, "
                            + "request_content_id, response_version_id, response_status, "
                            + "messages.message as response_message, response_content_id "
                            + "FROM conversations, methods, messages "
                            + "WHERE methods.id = conversations.request_method_id "
                            + "AND messages.id = conversations.response_message_id "
                            + "AND conversations.id = ?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public Conversation get(Integer id) {
            return (Conversation) findObject(new Object[] { id });
        }

        /*
         * (non-Javadoc)
         *
         * @see org.springframework.jdbc.object.MappingSqlQuery#mapRow(java.sql.ResultSet,
         *      int)
         */
        @Override
        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            JdbcConversation conversation = new JdbcConversation(headersDao, blobDao);
            conversation.setId(new Integer(rs.getInt("id")));
            conversation.setDate(rs.getTimestamp("request_date"));
            conversation.setRequestMethod(rs.getString("request_method"));
            Integer uri = new Integer(rs.getInt("request_uri_id"));
            conversation.setRequestUri(uriDao.findUri(uri));
            Integer version = new Integer(rs.getInt("request_version_id"));
            conversation.setRequestVersion(versionDao.getVersion(version));
            version = new Integer(rs.getInt("response_version_id"));
            conversation.setResponseVersion(versionDao.getVersion(version));
            conversation.setResponseStatus(rs.getString("response_status"));
            conversation.setResponseMessage(rs.getString("response_message"));
            conversation.setRequestBlob(rs.getString("request_content_id"));
            conversation.setResponseBlob(rs.getString("response_content_id"));
            return conversation;
        }
    }

    private class ConversationInsert extends SqlUpdate {

        protected ConversationInsert() {
            super(
                    getDataSource(),
                    "INSERT INTO conversations ("
                            + "session_id, source_id, request_date, request_method_id, request_uri_id, request_version_id, request_content_id, "
                            + "response_version_id, response_status, response_message_id, response_content_id) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            declareParameter(new SqlParameter(Types.INTEGER));
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
            compile();
        }

        protected JdbcConversation insert(Integer session, Conversation conversation) {
            Integer uriId;
            synchronized (uriDao) {
                uriId = uriDao.findUriId(conversation.getRequestUri());
                if (uriId == null)
                    uriId = uriDao.saveUri(conversation.getRequestUri());
            }
            String requestContentKey = blobDao.saveBlob(conversation.getRequestContent());
            String responseContentKey = blobDao.saveBlob(conversation.getResponseContent());
            Object[] objs = new Object[] { session,
                    getSource(conversation.getSource()), conversation.getDate(),
                    getMethod(conversation.getRequestMethod()), uriId,
                    versionDao.getId(conversation.getRequestVersion()),
                    requestContentKey,
                    versionDao.getId(conversation.getResponseVersion()),
                    conversation.getResponseStatus(),
                    getMessage(conversation.getResponseMessage()),
                    responseContentKey };
            super.update(objs);
            Integer id = retrieveIdentity();
            conversation.setId(id);
            NamedValue[] nv = conversation.getRequestHeaders();
            headersDao.saveHeaders(id, HeadersDao.REQUEST_HEADERS, nv);
            nv = conversation.getResponseHeaders();
            headersDao.saveHeaders(id, HeadersDao.RESPONSE_HEADERS, nv);
            nv = conversation.getResponseFooters();
            headersDao.saveHeaders(id, HeadersDao.RESPONSE_FOOTERS, nv);
            JdbcConversation c = new JdbcConversation(headersDao, blobDao);
            c.setId(conversation.getId());
            c.setSource(conversation.getSource());
            c.setDate(conversation.getDate());
            c.setRequestMethod(conversation.getRequestMethod());
            c.setRequestUri(conversation.getRequestUri());
            c.setRequestVersion(conversation.getRequestVersion());
            c.setRequestBlob(requestContentKey);
            c.setResponseVersion(conversation.getResponseVersion());
            c.setResponseStatus(conversation.getResponseStatus());
            c.setResponseMessage(conversation.getResponseMessage());
            c.setResponseBlob(responseContentKey);
            return c;
        }
    }

}
