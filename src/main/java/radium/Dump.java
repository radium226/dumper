package radium;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import radium.arg4j.Sql2oOptionHandler;
import radium.dumper.Dumper;
import radium.dumper.Provider;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.io.Files;

public class Dump {

	final private static Logger LOGGER = LoggerFactory.getLogger(Dump.class);
	
	@Option(name = "--dumper", metaVar = "DUMPER", usage = "Output format (XLSX, CSV, etc.)", required = false)
	private Dumper.Type dumperType;
	
	@Option(name = "--provider", metaVar = "PROVIDER", usage = "Provider to use (Cursor, Query, etc)", required = false)
	private Provider.Type providerType;
	
	@Option(name = "--file", metaVar = "FILE")
	private boolean readFile = false;
	
	@Option(name = "--name", metaVar = "NAME", required = false)
	private String name;
	
	@Argument(index = 0, metaVar = "URL", usage = "JDBC URL to connect to", required = true, handler = Sql2oOptionHandler.class)
	private Sql2o sql2o;
	
	@Argument(index = 1, metaVar = "DATABASE OBJECT", usage = "Database object to fetch (Table, View, etc.)", required = true)
	private String argument;
	
	@Argument(index = 2, metaVar = "OUTPUT FILE", usage = "Output file", required = false)
	private File outputFile;
	
	public Dump() {
		super();
	}
	
	public static String selectSQL(String objectName) {
		return "SELECT * FROM " + objectName;
	}
	
	public void dump() {
		try {
			String argument = this.readFile ? Files.toString(new File(this.argument), Charsets.UTF_8) : this.argument;
			String name = MoreObjects.firstNonNull(this.name, this.argument);
			
			final Dumper dumper = Optional.fromNullable(this.dumperType).or(Dumper.Type.CSV).newDumper();
			final Provider provider = Optional.fromNullable(providerType).or(Provider.Type.OBJECT).newProvider(argument);
			if (outputFile != null) {
				try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
					dumper.onBegin(name, fileOutputStream);
				} catch (IOException e) {
					LOGGER.error("Unable to write output file");
				}
			} else {
				try {
					dumper.onBegin(name, System.out);
				} catch (IOException e) {
					LOGGER.error("Unable to write to standard output", e);
				}
			}
			
			Connection connection = sql2o.open();
		
			provider.provide(connection, new ResultSetHandler<Void>() {

				@Override
				public Void handle(ResultSet resultSet) throws SQLException {
					try {
						dumper.onIteration(resultSet);
					} catch (IOException e) {
						throw new SQLException(e); // LET'S BREAK IT! 
					}
					return null;
				}
				
			});
			connection.close();
			dumper.onEnd();
		} catch (IOException | SQLException e) {
			LOGGER.error("Something happened in the end, but it doesn't really matter", e);
		}
		
	}

	public static void main(String[] arguments) {
		Dump dump = new Dump();
		CmdLineParser parser = new CmdLineParser(dump);
		try {
			parser.parseArgument(arguments);
			dump.dump();
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
		}
	}

}
