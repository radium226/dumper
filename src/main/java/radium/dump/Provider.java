package radium.dump;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;

import radium.dump.impl.CursorProvider;
import radium.dump.impl.ObjectProvider;
import radium.dump.impl.QueryProvider;

public interface Provider {
	
	public enum Type {

		CURSOR(CursorProvider.class), QUERY(QueryProvider.class), OBJECT(ObjectProvider.class);
		
		final private static Logger LOGGER = LoggerFactory.getLogger(Type.class);
		
		private Class<? extends Provider> providerClass;
		
		private Type(Class<? extends Provider> providerClass) {
			this.providerClass = providerClass;
		}
		
		public Class<? extends Provider> getProviderClass() {
			return providerClass;
		}
		
		@SuppressWarnings("unchecked")
		public <T extends Provider> T newProvider(Object... arguments) {
			try {
				return (T) providerClass.getConstructors()[0].newInstance(arguments);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				LOGGER.error("Unable to instanciate dumper", e);
				return null; 
			}
		}
	}
	
	<T> void provide(Connection connection, ResultSetHandler<T> resultSetHandler) throws SQLException;
	
}
