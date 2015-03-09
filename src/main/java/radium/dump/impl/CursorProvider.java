package radium.dump.impl;

/* import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet; */
import java.sql.SQLException;

/* import oracle.jdbc.OracleTypes; */

import org.sql2o.ResultSetHandler; 

import radium.dump.Provider;

public class CursorProvider implements Provider {

	private String sql;
	
	public CursorProvider(String sql) {
		super();
		
		this.sql = sql;
	}
	
	@Override
	public <T> void provide(org.sql2o.Connection connection, ResultSetHandler<T> resultSetHandler) throws SQLException {
		/* CallableStatement callableStatement = jdbc(connection).prepareCall(sql);
		callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
		callableStatement.execute();
		ResultSet resultSet = (ResultSet) callableStatement.getObject(1);
		while (resultSet.next()) {
			resultSetHandler.handle(resultSet);
		}
		resultSet.close();
		callableStatement.close(); 
	}
	
	private static Connection jdbc(org.sql2o.Connection connection) {
		return connection.getJdbcConnection(); */
	}

}
