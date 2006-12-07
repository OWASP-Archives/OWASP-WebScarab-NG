/**
 *
 */
package org.owasp.webscarab.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.owasp.webscarab.dao.AnnotationDao;
import org.owasp.webscarab.domain.Annotation;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author rdawes
 *
 */
public class JdbcAnnotationDao extends PropertiesJdbcDaoSupport implements
		AnnotationDao {

	private void createTables() {
		getJdbcTemplate().execute(getProperty("annotations.createTable"));
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
		} catch (Exception e) {
			createTables();
			get(new Integer(0));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.owasp.webscarab.dao.AnnotationDao#get(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public Annotation get(Integer id) {
		JdbcTemplate jt = getJdbcTemplate();
		String query = "SELECT id, annotation FROM annotations WHERE id = ?";
		Object[] args = new Object[] { id };
		RowMapper rowMapper = new AnnotationRowMapper();
		Collection<Annotation> results = jt.query(query, args, rowMapper);
		if (results.size() == 0) return null;
		if (results.size() > 1) throw new IncorrectResultSizeDataAccessException(1, results.size());
		return results.iterator().next();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.owasp.webscarab.dao.AnnotationDao#update(org.owasp.webscarab.Annotation)
	 */
	public void update(Annotation annotation) {
		JdbcTemplate jt = getJdbcTemplate();
		String statement = "UPDATE annotations SET annotation = ? WHERE id = ?";
		Object[] args = new Object[] { annotation.getAnnotation(),
				annotation.getId() };
		if (jt.update(statement, args) == 0) {
			statement = "INSERT INTO annotations (annotation, id) VALUES (?,?)";
			jt.update(statement, args);
		}
	}

	public void delete(Integer id) {
		JdbcTemplate jt = getJdbcTemplate();
		String statement = "DELETE FROM annotations WHERE id = ?";
		Object[] args = new Object[] { id };
		jt.update(statement, args);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.owasp.webscarab.dao.AnnotationDao#getAll()
	 */
	@SuppressWarnings("unchecked")
	public Collection<Annotation> getAll() {
		JdbcTemplate jt = getJdbcTemplate();
		String query = "SELECT id, annotation FROM annotations";
		RowMapper rowMapper = new AnnotationRowMapper();
		return jt.query(query, rowMapper);
	}

	private class AnnotationRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int index) throws SQLException {
			Annotation annotation = new Annotation();
			annotation.setAnnotation(rs.getString("annotation"));
			annotation.setId(new Integer(rs.getInt("id")));
			return annotation;
		}
	}

}
