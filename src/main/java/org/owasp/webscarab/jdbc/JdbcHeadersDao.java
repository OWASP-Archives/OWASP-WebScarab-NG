/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.owasp.webscarab.domain.NamedValue;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

/**
 * @author rdawes
 *
 */
public class JdbcHeadersDao extends
        PropertiesJdbcDaoSupport implements HeadersDao {

    private HeadersQuery headersQuery;

    private HeaderInsert headersInsert;

    private NamedValueDao namedValueDao;

    private Map<Key, WeakReference<NamedValue[]>> cache;

    public JdbcHeadersDao(NamedValueDao namedValueDao) {
        this.namedValueDao = namedValueDao;
        cache = new LinkedHashMap<Key, WeakReference<NamedValue[]>>(40, 0.75f, true) {

            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			/* (non-Javadoc)
             * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
             */
            @Override
            protected boolean removeEldestEntry(Entry<Key, WeakReference<NamedValue[]>> eldest) {
                return size()>50;
            }

        };
    }

    private void createTables() {
        getJdbcTemplate().execute(getProperty("headers.createTable"));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.dao.support.DaoSupport#initDao()
     */
    @Override
    protected void initDao() throws Exception {
        super.initDao();

        headersQuery = new HeadersQuery();
        headersInsert = new HeaderInsert();

        try {
            findHeaders(new Integer(0), REQUEST_HEADERS);
        } catch (Exception e) {
            createTables();
            findHeaders(new Integer(0), REQUEST_HEADERS);
        }
    }

    public NamedValue[] findHeaders(Integer conversation, Integer type) {
        Key key = new Key(conversation, type);
        WeakReference<NamedValue[]> ref = cache.get(key);
        NamedValue[] headers;
        if (ref != null) {
            headers = ref.get();
            if (headers != null)
                return headers;
        }
        Integer[] namedValueIds = headersQuery.getHeaders(conversation, type);
        headers = new NamedValue[namedValueIds.length];
        for (int i = 0; i < namedValueIds.length; i++)
            headers[i] = namedValueDao.findNamedValue(namedValueIds[i]);
        ref = new WeakReference<NamedValue[]>(headers);
        cache.put(key, ref);
        return headers;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.owasp.webscarab.jdbc.HeadersDao#saveHeaders(java.lang.Integer,
     *      java.lang.Integer, org.owasp.webscarab.NamedValue[])
     */
    public void saveHeaders(Integer conversation, Integer type,
            NamedValue[] headers) {
        if (headers == null)
            return;
        for (int i = 0; i < headers.length; i++) {
            Integer id = headers[i].getId();
            if (id == null) {
                id = namedValueDao.findNamedValueId(headers[i]);
                if (id == null)
                    id = namedValueDao.saveNamedValue(headers[i]);
            }
            headersInsert.insert(conversation, type, id);
        }
    }

    private class HeadersQuery extends MappingSqlQuery {

        public HeadersQuery() {
            super(getDataSource(), "SELECT named_value " + "FROM headers "
                    + "WHERE headers.conversation_id = ? "
                    + "AND headers.type = ? " + "ORDER BY id ASC");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        @SuppressWarnings("unchecked")
        public Integer[] getHeaders(Integer conversation, Integer type) {
            List results = execute(new Object[] { conversation, type });
            return (Integer[]) results.toArray(new Integer[results.size()]);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.springframework.jdbc.object.MappingSqlQuery#mapRow(java.sql.ResultSet,
         *      int)
         */
        @Override
        protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
            return new Integer(rs.getInt("named_value"));
        }
    }

    private class HeaderInsert extends SqlUpdate {

        protected HeaderInsert() {
            super(getDataSource(),
                    "INSERT INTO headers (conversation_id, type, named_value) VALUES (?,?,?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected void insert(Integer conversation, Integer type,
                Integer headerId) {
            Object[] objs = new Object[] { conversation, type, headerId };
            super.update(objs);
        }
    }

    private static class Key {
        private Integer id;
        private Integer type;

        public Key(Integer id, Integer type) {
            this.id = id;
            this.type = type;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) return false;
            Key that = (Key) obj;
            return (this.id.equals(that.id) && this.type.equals(that.type));
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return id.hashCode() | type.hashCode();
        }

    }
}
