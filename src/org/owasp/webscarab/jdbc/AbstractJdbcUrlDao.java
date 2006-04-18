/**
 * 
 */
package org.owasp.webscarab.jdbc;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.owasp.webscarab.dao.UrlDao;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

/**
 * @author rdawes
 * 
 */
public abstract class AbstractJdbcUrlDao extends AbstractJdbcIdentityDaoSupport implements
        UrlDao {

    private UrlQuery urlQuery;

    private IdQuery idQuery;

    private UrlInsert urlInsert;

    protected void initDao() throws Exception {
        super.initDao();

        urlQuery = new UrlQuery();
        idQuery = new IdQuery();
        urlInsert = new UrlInsert();

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
     * @see org.owasp.webscarab.dao.UrlDao#getId(java.net.URL)
     */
    public Integer findUrlId(URL url) {
        if (url == null)
            throw new NullPointerException("Can't search for a NULL URL");
        Integer id = idQuery.query(url);
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.dao.UrlDao#getUrl(java.lang.Integer)
     */
    public URL findUrl(Integer id) {
        if (id == null)
            return null;
        return urlQuery.getUrl(id);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.dao.UrlDao#saveUrl(java.net.URL)
     */
    public Integer saveUrl(URL url) {
        return urlInsert.insert(url);
    }

    protected void checkTables() throws SQLException {
        try {
            findUrlId(new URL("http://localhost/"));
        } catch (MalformedURLException mue) {}
        findUrl(new Integer(0));
    }

    protected abstract void createTables() throws SQLException;

    private class UrlInsert extends SqlUpdate {

        protected UrlInsert() {
            super(getDataSource(), "INSERT INTO urls (id, url) VALUES(?,?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        protected Integer insert(URL url) {
            Object[] objs = new Object[] { null, url.toString() };
            update(objs);
            Integer id = retrieveIdentity();
            return id;
        }

    }

    private class UrlQuery extends MappingSqlQuery {
        public UrlQuery() {
            super(getDataSource(), "SELECT url FROM urls WHERE id = ?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public URL getUrl(Integer id) {
            return (URL) findObject(id);
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
            String url = rs.getString("url");
            try {
                return new URL(url);
            } catch (MalformedURLException mue) {
                throw new RuntimeException("Malformed URL: '" + url + "'");
            }
        }
    }

    private class IdQuery extends MappingSqlQuery {
        public IdQuery() {
            super(getDataSource(), "SELECT id FROM urls WHERE url = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        public Integer query(URL url) {
            List results = execute(new Object[] { url.toString() });
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
