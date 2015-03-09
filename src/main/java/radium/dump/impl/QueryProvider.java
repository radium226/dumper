package radium.dump.impl;

import java.sql.SQLException;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;

import radium.dump.Provider;

public class QueryProvider implements Provider {

	private String sql;
	
	public QueryProvider(String sql) {
		super();
		
		this.sql = sql;
	}
	
	@Override
	public <T> void provide(Connection connection, ResultSetHandler<T> resultSetHandler) throws SQLException {
		Query query = connection.createQuery(sql);
		query.executeAndFetch(resultSetHandler);
		query.close();
	}

}
