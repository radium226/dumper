package radium.dump.impl;

import java.sql.SQLException;
import java.util.Map;

import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;

import radium.dump.Provider;

public class ObjectProvider implements Provider {

	private String objectName;
	
	public ObjectProvider(String objectName) {
		super();
		
		this.objectName = objectName;
	}
	
	@Override
	public <T> void provide(Connection connection, Map<String, String> variables, ResultSetHandler<T> resultSetHandler) throws SQLException {
		Provider.Type.QUERY.newProvider("SELECT * FROM " + objectName).provide(connection, variables, resultSetHandler);
	}

}
