package radium.dump.impl;

import java.sql.SQLException;
import java.util.Map;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;

import radium.dump.Provider;

public class QueryProvider implements Provider {

	private String sql;
	
	public QueryProvider(String sql) {
		super();
		
		this.sql = removeSemicolon(sql);
	}
	
	@Override
	public <T> void provide(Connection connection, Map<String, String> variables, ResultSetHandler<T> resultSetHandler) throws SQLException {
		Query query = connection.createQuery(sql);
		for (Map.Entry<String, String> variable : variables.entrySet()) {
			query = query.addParameter(variable.getKey(), variable.getValue());
		}
		query.executeAndFetch(resultSetHandler);
		query.close();
	}
	
	public static String removeSemicolon(String sql) {
		sql = sql.trim();
		char lastChar = sql.charAt(sql.length() - 1);
		if (lastChar == ';') {
			sql = sql.substring(0, sql.length() - 1);
		}
		return sql;
	}

}
