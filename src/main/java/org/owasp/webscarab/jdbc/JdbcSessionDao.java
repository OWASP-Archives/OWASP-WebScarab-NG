/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;

import org.owasp.webscarab.dao.SessionDao;
import org.owasp.webscarab.domain.Session;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

/**
 * @author rdawes
 *
 */
public class JdbcSessionDao extends PropertiesJdbcDaoSupport implements
		SessionDao {

	private SessionsQuery sessionsQuery;
	private SessionInsert sessionInsert;
	private SessionUpdate sessionUpdate;

	protected void initDao() throws Exception {
		super.initDao();

		sessionsQuery = new SessionsQuery();
        sessionInsert = new SessionInsert();
        sessionUpdate = new SessionUpdate();

		try {
			checkTables();
		} catch (Exception e) {
			createTables();
			checkTables();
		}
	}

	protected void checkTables() {
		getSessions();
	}

	protected void createTables() {
		getJdbcTemplate().execute(getProperty("sessions.createTable"));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.owasp.webscarab.dao.SessionDao#createSession(java.lang.String)
	 */
	public Session createSession(String description) {
		Session session = new Session();
		session.setDescription(description);
		session.setDate(new Date());
		sessionInsert.insert(session);
		return session;
	}

    public void updateSession(Session session) {
        sessionUpdate.update(session);
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.owasp.webscarab.dao.SessionDao#getSessions()
	 */
	public Collection<Session> getSessions() {
		return sessionsQuery.getSessions();
	}

	private class SessionsQuery extends MappingSqlQuery {

		protected SessionsQuery() {
			super(getDataSource(), "SELECT id, description, date FROM sessions");
		}

		@SuppressWarnings("unchecked")
		public Collection<Session> getSessions() {
			return execute();
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
			Session session = new Session();
			Integer id = new Integer(rs.getInt("id"));
			session.setId(id);
			String description = rs.getString("description");
			session.setDescription(description);
			Date date = rs.getTimestamp("date");
			session.setDate(date);
			return session;
		}

	}

	private class SessionInsert extends SqlUpdate {

		protected SessionInsert() {
			super(getDataSource(),
					"INSERT INTO sessions (id, description, date) VALUES(?,?,?)");
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			compile();
		}

		protected Integer insert(Session session) {
			Object[] objs = new Object[] { null, session.getDescription(), session.getDate() };
			update(objs);
			Integer id = retrieveIdentity();
			session.setId(id);
			return id;
		}

	}

    private class SessionUpdate extends SqlUpdate {

        protected SessionUpdate() {
            super(getDataSource(),
                    "UPDATE sessions SET description = ?, date = ? WHERE id = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected void update(Session session) {
            Object[] objs = new Object[] { session.getDescription(), session.getDate(), session.getId()};
            update(objs);
        }

    }

}
