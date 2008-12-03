/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.owasp.webscarab.dao.FindingDao;
import org.owasp.webscarab.domain.Finding;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author rdawes
 *
 */
public class JdbcFindingDao extends PropertiesJdbcDaoSupport implements
		FindingDao {

	private void createTables() {
		getJdbcTemplate().execute(getProperty("findings.createTable"));
		getJdbcTemplate().execute(getProperty("findings_conversations.createTable"));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.dao.support.DaoSupport#initDao()
	 */
	@Override
	protected void initDao() throws Exception {
		super.initDao();

		try {
			get(new Integer(0));
			getConversations(new Integer(0));
		} catch (Exception e) {
			createTables();
			get(new Integer(0));
			getConversations(new Integer(0));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.owasp.webscarab.dao.FindingDao#get(java.lang.Integer)
	 */
	public Finding get(Integer id) {
		JdbcTemplate jt = getJdbcTemplate();
		String query = "SELECT id, finding FROM findings WHERE id = ?";
		Object[] args = new Object[] { id };
		RowMapper rowMapper = new FindingRowMapper();
		return (Finding) jt.queryForObject(query, args, rowMapper);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.owasp.webscarab.dao.FindingDao#update(org.owasp.webscarab.Finding)
	 */
	public void update(Finding finding) {
		JdbcTemplate jt = getJdbcTemplate();
		String statement = "INSERT INTO findings (description, id) VALUES (?,?)";
		if (finding.getId() != null)
			statement = "UPDATE findings SET description = ? WHERE id = ?";
		Object[] args = new Object[] { finding.getDescription(),
				finding.getId() };
		jt.update(statement, args);
		if (finding.getId() == null) {
			finding.setId(retrieveIdentity());
		} else {
			deleteConversations(finding.getId());
		}
		insertConversations(finding.getId(), finding.getConversations());
	}

	public void delete(Integer id) {
		JdbcTemplate jt = getJdbcTemplate();
		String statement = "DELETE FROM findings WHERE id = ?";
		Object[] args = new Object[] { id };
		int rows = jt.update(statement, args);
		if (rows > 0) { // did the finding exist at all?
			statement = "DELETE FROM findings_conversations WHERE finding_id = ?";
			jt.update(statement, args);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.owasp.webscarab.dao.FindingDao#getAll()
	 */
	@SuppressWarnings("unchecked")
	public Collection<Finding> getAll() {
		JdbcTemplate jt = getJdbcTemplate();
		String query = "SELECT id, description FROM findings";
		RowMapper rowMapper = new FindingRowMapper();
		Collection<Finding> findings = jt.query(query, rowMapper);
		Iterator<Finding> it = findings.iterator();
		while (it.hasNext()) {
			Finding finding = it.next();
			finding.setConversations(getConversations(finding.getId()));
		}
		return findings;
	}

	@SuppressWarnings("unchecked")
	private Integer[] getConversations(Integer id) {
		JdbcTemplate jt = getJdbcTemplate();
		String query = "SELECT conversation_id FROM findings_conversations WHERE finding_id = ?";
		Object[] args = new Object[] { id };
		RowMapper rowMapper = new ConversationRowMapper();
		Collection<Integer> conversations = jt.query(query, args, rowMapper);
		return conversations.toArray(new Integer[conversations.size()]);
	}

	private void insertConversations(final Integer finding, final Integer[] conversations) {
		JdbcTemplate jt = getJdbcTemplate();
		String query = "INSERT INTO findings_conversations (finding_id, conversation_id) VALUES (?,?)";
		BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			public int getBatchSize() { return conversations.length; }
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setInt(1, finding.intValue());
				ps.setInt(2, conversations[i].intValue());
			}
		};
		jt.batchUpdate(query, bpss);
	}

	private int deleteConversations(Integer id) {
		JdbcTemplate jt = getJdbcTemplate();
		String statement = "DELETE FROM findings_conversations WHERE finding_id = ?";
		Object[] args = new Object[] { id };
		return jt.update(statement, args);
	}

	private class FindingRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int index) throws SQLException {
			Finding finding = new Finding(rs.getString("description"));
			finding.setId(new Integer(rs.getInt("id")));
			return finding;
		}
	}

	private class ConversationRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int index) throws SQLException {
			return new Integer(rs.getInt("conversation_id"));
		}
	}

}
