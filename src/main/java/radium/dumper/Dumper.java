package radium.dumper;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import radium.dumper.impl.CSVDumper;
import radium.dumper.impl.ExcelDumper;

public interface Dumper {
	
	public enum Type {

		XLSX(ExcelDumper.class), CSV(CSVDumper.class);

		final public static Logger LOGGER = LoggerFactory
				.getLogger(Type.class);

		private Class<? extends Dumper> dumperClass;

		private Type(Class<? extends Dumper> dumperClass) {
			this.dumperClass = dumperClass;
		}

		public Class<? extends Dumper> getDumperClass() {
			return dumperClass;
		}

		@SuppressWarnings("unchecked")
		public <T extends Dumper> T newDumper() {
			try {
				return (T) dumperClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				LOGGER.error("Unable to instanciate dumper", e);
				return null;
			}
		}

	}

	void onBegin(String objectName, OutputStream outputStream) throws IOException;

	void onIteration(ResultSet resultSet) throws SQLException, IOException;

	void onEnd() throws IOException;

}
