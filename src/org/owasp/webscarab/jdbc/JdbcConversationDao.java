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
import org.owasp.webscarab.domain.ConversationSummary;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.jdbc.VersionDao;

/**
 * @author rdawes
 * 
 */
public class JdbcConversationDao extends PropertiesJdbcDaoSupport
        implements ConversationDao {

    private UriDao uriDao;

    private VersionDao versionDao;

    private HeadersDao headersDao;

    private BlobDao blobDao;

    private ConversationIdQuery conversationIdQuery;

    private ConversationSummaryQuery conversationSummaryQuery;

    private ConversationSummaryInsert conversationSummaryInsert;

    protected JdbcConversationDao(UriDao uriDao,
            VersionDao versionDao, HeadersDao headersDao, BlobDao blobDao) {
        this.uriDao = uriDao;
        this.versionDao = versionDao;
        this.headersDao = headersDao;
        this.blobDao = blobDao;
    }

    protected void initDao() throws Exception {
        super.initDao();

        conversationIdQuery = new ConversationIdQuery();
        conversationSummaryQuery = new ConversationSummaryQuery();
        conversationSummaryInsert = new ConversationSummaryInsert();

        try {
            checkTables();
        } catch (Exception e) {
            createTables();
            checkTables();
        }
    }

    protected void checkTables() throws SQLException {
        Integer id = new Integer(0);
        getMethod("GET");
        getMessage("Ok");
        getPlugin("Proxy");
        conversationIdQuery.getConversationIds(id);
        conversationSummaryQuery.getSummary(id);
    }

    protected void createTables() throws SQLException {
        getJdbcTemplate().execute(getProperty("createTable.methods"));
        getJdbcTemplate().execute(getProperty("createTable.conversations"));
        getJdbcTemplate().execute(getProperty("createTable.messages"));
        getJdbcTemplate().execute(getProperty("createTable.versions"));
        getJdbcTemplate().execute(getProperty("createTable.plugins"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.ConversationDao#getConversationIds()
     */
    public Collection<Integer> getAllIds(Integer session) {
        return conversationIdQuery.getConversationIds(session);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.ConversationDao#getConversation(java.lang.Integer)
     */
    public Conversation get(Integer id) {
        ConversationSummary summary = getSummary(id);
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
    public ConversationSummary getSummary(Integer id) {
        return conversationSummaryQuery.getSummary(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.ConversationDao#getId(org.owasp.webscarab.Conversation)
     */
    public void update(Integer session, Conversation conversation, ConversationSummary summary) {
        Integer id = conversationSummaryInsert.insert(session, summary);
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

    private Integer getMethod(String method) {
    	String query = "SELECT id FROM methods WHERE method = ?";
    	Object[] args = new Object[] { method };
		JdbcTemplate jt = getJdbcTemplate();
		try {
			return new Integer(jt.queryForInt(query, args));
		} catch (IncorrectResultSizeDataAccessException irsdae) {
			String insert = "INSERT INTO methods (id, method) VALUES (?,?)";
			args = new Object[] { null, method };
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
			String insert = "INSERT INTO messages (id, message) VALUES (?,?)";
			args = new Object[] { null, message };
			jt.update(insert, args);
			return retrieveIdentity();
		}
    }

    private Integer getPlugin(String plugin) {
    	String query = "SELECT id FROM plugins WHERE plugin = ?";
    	Object[] args = new Object[] { plugin };
		JdbcTemplate jt = getJdbcTemplate();
		try {
			return new Integer(jt.queryForInt(query, args));
		} catch (IncorrectResultSizeDataAccessException irsdae) {
			String insert = "INSERT INTO plugins (id, plugin) VALUES (?,?)";
			args = new Object[] { null, plugin};
			jt.update(insert, args);
			return retrieveIdentity();
		}
    }

    private class ConversationIdQuery extends MappingSqlQuery {

        protected ConversationIdQuery() {
            super(getDataSource(),
                    "SELECT id FROM conversations WHERE session = ? ORDER BY id ASC");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        @SuppressWarnings("unchecked")
        public List<Integer> getConversationIds(Integer session) {
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

        protected Integer insert(Integer session, ConversationSummary summary) {
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
                    summary.getResponseContentChecksum(), 
                    getPlugin(summary.getPlugin())};
            super.update(objs);
            Integer id = retrieveIdentity();
            summary.setId(id);
            return id;
        }
    }

}
