/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.owasp.webscarab.dao.BlobDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

import com.twmacinta.util.MD5;

/**
 * @author rdawes
 *
 */
public class JdbcBlobDao extends PropertiesJdbcDaoSupport implements BlobDao {

    private BlobQuery blobQuery;

    private BlobInsert blobInsert;

    private Map<String, WeakReference<byte[]>> cache;

    public JdbcBlobDao() {
        cache = new LinkedHashMap<String, WeakReference<byte[]>>(40, 0.75f, true) {
			private static final long serialVersionUID = 1L;

			/* (non-Javadoc)
             * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
             */
            @Override
            protected boolean removeEldestEntry(Entry<String, WeakReference<byte[]>> eldest) {
                return size()>50;
            }

        };
    }
    private void createTables() {
        getJdbcTemplate().execute(getProperty("blobs.createTable"));
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
                "SELECT COUNT(id) FROM blobs WHERE id = ?",
                new Object[] { key });
        return count > 0;
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.dao.BlobDao#findBlob(java.lang.String)
     */
    public byte[] findBlob(String key) {
        byte[] blob = null;
        WeakReference<byte[]> ref = cache.get(key);
        if (ref != null) {
            blob = ref.get();
            if (blob != null)
                return blob;
        }
        blob = blobQuery.getBlob(key);
        ref = new WeakReference<byte[]>(blob);
        cache.put(key, ref);
        return blob;
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.dao.BlobDao#saveBlob(java.lang.String, byte[])
     */
    public synchronized String saveBlob(byte[] blob) {
        if (blob == null || blob.length == 0)
            return null;
        MD5 md5 = new MD5();
        md5.Update(blob);
        String key = md5.asHex();
        if (!exists(key))
            blobInsert.insert(key, blob);
        return key;
    }

    private class BlobQuery extends MappingSqlQuery {

        protected BlobQuery() {
            super(getDataSource(), "SELECT blob_content FROM blobs WHERE id = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        public byte[] getBlob(String key) {
            return (byte[]) findObject(key);
        }

        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            return rs.getBytes("blob_content");
        }
    }

    private class BlobInsert extends SqlUpdate {

        protected BlobInsert() {
            super(getDataSource(),
                    "INSERT INTO blobs (id, blob_size, blob_content) VALUES (?,?,?)");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.LONGVARBINARY));
            compile();
        }

        protected void insert(String key, byte[] blob) {
            if (blob == null || blob.length == 0)
                return;
            Integer size = new Integer(blob.length);
            Object[] objs = new Object[] { key, size, blob };
            super.update(objs);
        }
    }

}
