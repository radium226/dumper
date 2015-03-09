package radium.dump.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import radium.dump.Dumper;
import com.google.common.base.Optional;

public class CSVDumper implements Dumper {

	final public static String EXTENSION = "csv";
	
	private PrintStream printStream;
	private int rowCount;
	private int columnCount;
	
	public CSVDumper() {
		super();
	}
	
	@Override
	public void onBegin(String objectName, OutputStream outputStream) {
		this.printStream = new PrintStream(outputStream);
		this.rowCount = 0;
	}

	@Override
	public void onIteration(ResultSet resultSet) throws SQLException {
		if (rowCount == 0) {
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			handleResultSetMetaData(resultSetMetaData);
		}

		handleResultSet(resultSet);
		rowCount++;
		printStream.flush();
	}
	
	private void handleResultSet(final ResultSet resultSet) throws SQLException {
		for (int i = 1; i <= columnCount; i++) {
			String value = Optional.fromNullable(resultSet.getString(i)).or("");
			if (value.contains(",")) {
				value = "\"" + value + "\"";
			}
			
			printStream.print(value);
			if (i < columnCount) {
				printStream.print(",");
			}
		}
		printStream.println();
	}

	private void handleResultSetMetaData(final ResultSetMetaData resultSetMetaData) throws SQLException {
		columnCount = resultSetMetaData.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			printStream.print(resultSetMetaData.getColumnLabel(i));
			if (i < columnCount) {
				printStream.print(",");
			}
		}
		printStream.println();
	}

	@Override
	public void onEnd() throws IOException {
		this.printStream.close();
	}
	
	@Override
	public String getExtension() {
		return EXTENSION;
	}

}
