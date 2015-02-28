package radium.dumper.impl;

import java.sql.SQLException;

import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;

import radium.dumper.Provider;

public class ObjectProvider implements Provider {

	private String objectName;
	
	public ObjectProvider(String objectName) {
		super();
		
		this.objectName = objectName;
	}
	
	@Override
	public <T> void provide(Connection connection, ResultSetHandler<T> resultSetHandler) throws SQLException {
		Provider.Type.QUERY.newProvider("SELECT * FROM " + objectName).provide(connection, resultSetHandler);
	}

}
