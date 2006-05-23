/**
 * 
 */
package org.owasp.webscarab.jdbc;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.owasp.webscarab.dao.UriDao;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

/**
 * @author rdawes
 * 
 */
public class JdbcUriDao extends PropertiesJdbcDaoSupport implements
        UriDao {

    private UriQuery uriQuery;

    private IdQuery idQuery;

    private UriInsert uriInsert;

    protected void initDao() throws Exception {
        super.initDao();

        uriQuery = new UriQuery();
        idQuery = new IdQuery();
        uriInsert = new UriInsert();

        try {
            checkTables();
        } catch (Exception e) {
            createTables();
            checkTables();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.UriDao#getId(java.net.URI)
     */
    public Integer findUriId(URI uri) {
        if (uri == null)
            throw new NullPointerException("Can't search for a NULL URI");
        Integer id = idQuery.query(uri);
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.UriDao#getUri(java.lang.Integer)
     */
    public URI findUri(Integer id) {
        if (id == null)
            return null;
        return uriQuery.getUri(id);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.dao.UriDao#saveUri(java.net.URI)
     */
    public Integer saveUri(URI uri) {
        return uriInsert.insert(uri);
    }

    protected void checkTables() throws SQLException {
        try {
            findUriId(new URI("http://localhost/"));
        } catch (URISyntaxException use) {}
        findUri(new Integer(0));
    }

    private void createTables() throws SQLException {
        getJdbcTemplate().execute(getProperty("createTable.uris"));
    }

    private class UriInsert extends SqlUpdate {

        protected UriInsert() {
            super(getDataSource(), "INSERT INTO uris (id, uri) VALUES(?,?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.LONGVARCHAR));
            compile();
        }

        protected Integer insert(URI uri) {
            Object[] objs = new Object[] { null, uri.toString() };
            update(objs);
            Integer id = retrieveIdentity();
            return id;
        }

    }

    private class UriQuery extends MappingSqlQuery {
        public UriQuery() {
            super(getDataSource(), "SELECT uri FROM uris WHERE id = ?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public URI getUri(Integer id) {
            return (URI) findObject(id);
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
            String uri = rs.getString("uri");
            try {
                return new URI(uri);
            } catch (URISyntaxException use) {
                throw new RuntimeException("URI Syntax Exception: '" + uri + "'");
            }
        }
    }

    private class IdQuery extends MappingSqlQuery {
        public IdQuery() {
            super(getDataSource(), "SELECT id FROM uris WHERE uri = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        public Integer query(URI uri) {
            List results = execute(new Object[] { uri.toString() });
            if (results.size() == 0)
                return null;
            return (Integer) results.get(0);
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
            return new Integer(rs.getInt("id"));
        }
    }

}
