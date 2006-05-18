/**
 * 
 */
package org.owasp.webscarab.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.owasp.webscarab.dao.BlobDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

/**
 * @author rdawes
 *
 */
public class JdbcBlobDao extends PropertiesJdbcDaoSupport implements BlobDao {

    private BlobQuery blobQuery;
    
    private BlobInsert blobInsert;
    
    private void createTables() throws SQLException {
        getJdbcTemplate().execute(getProperty("createTable.blobs"));
    }
    
    /* (non-Javadoc)
     * @see org.springframework.dao.support.DaoSupport#initDao()
     */
    @Override
    protected void initDao() throws Exception {
        super.initDao();
        
        blobQuery = new BlobQuery();
        blobInsert = new BlobInsert();
        
        try {
            findBlob("");
        } catch (Exception e) {
            createTables();
            findBlob("");
        }
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.dao.BlobDao#exists(java.lang.String)
     */
    public boolean exists(String key) {
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        int count = template.queryForInt(
                "SELECT COUNT(key) FROM blobs WHERE key = ?",
                new Object[] { key });
        return count > 0;
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.dao.BlobDao#findBlob(java.lang.String)
     */
    public byte[] findBlob(String key) {
        return blobQuery.getBlob(key);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.dao.BlobDao#saveBlob(java.lang.String, byte[])
     */
    public synchronized void saveBlob(String key, byte[] blob) {
        if (!exists(key))
            blobInsert.insert(key, blob);
    }

    private class BlobQuery extends MappingSqlQuery {

        protected BlobQuery() {
            super(getDataSource(), "SELECT blob FROM blobs WHERE key = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        public byte[] getBlob(String key) {
            return (byte[]) findObject(key);
        }

        protected Object mapRow(ResultSet rs, @SuppressWarnings("unused")
        int rownum) throws SQLException {
            return rs.getBytes("blob");
        }
    }

    private class BlobInsert extends SqlUpdate {

        protected BlobInsert() {
            super(getDataSource(),
                    "INSERT INTO blobs (key, blob) VALUES (?,?)");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.LONGVARBINARY));
            compile();
        }

        protected void insert(String key, byte[] blob) {
            if (blob == null || blob.length == 0)
                return;
            Object[] objs = new Object[] { key, blob };
            super.update(objs);
        }
    }

}
