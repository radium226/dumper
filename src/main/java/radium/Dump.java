package radium;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import radium.args4j.Sql2oOptionHandler;
import radium.dump.Compressor;
import radium.dump.Dumper;
import radium.dump.Provider;
import radium.dump.impl.NoneCompressor;
import radium.dump.impl.ZIPCompressor;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.io.Files;

public class Dump {

	final private static Logger LOGGER = LoggerFactory.getLogger(Dump.class);
	final public static String DEFAULT_NAME = "unnamed";
	
	@Option(name = "--dumper", metaVar = "DUMPER", usage = "Output format (XLSX, CSV, etc.)", required = false)
	private Dumper.Type dumperType;
	
	@Option(name = "--provider", metaVar = "PROVIDER", usage = "Provider to use (Cursor, Query, etc)", required = false)
	private Provider.Type providerType;
	
	@Option(name = "--compressor", metaVar = "COMPRESSOR", required = false)
	private Compressor.Type compressorType;
	
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
			final PipedInputStream pipedInputStream = new PipedInputStream();
			final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
			
			String argument = this.readFile ? Files.toString(new File(this.argument), Charsets.UTF_8) : this.argument;
			String name = Optional.fromNullable(this.name).or(DEFAULT_NAME);
			
			final Dumper dumper = Optional.fromNullable(this.dumperType).or(Dumper.Type.CSV).newDumper();
			final Provider provider = Optional.fromNullable(providerType).or(Provider.Type.OBJECT).newProvider(argument);
			final Compressor compressor = Optional.fromNullable(compressorType).or(Compressor.Type.NONE).newCompressor(name + "." + dumper.getExtension());
			
			final OutputStream outputStream = outputFile != null ? new FileOutputStream(outputFile) : System.out;
			
			final Connection connection = sql2o.open();
			dumper.onBegin(name, pipedOutputStream);
			
			ExecutorService providerExecutor = Executors.newSingleThreadExecutor();
			Future<Void> providerFuture = providerExecutor.submit(new Callable<Void> () {

				@Override
				public Void call() throws Exception {
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
					dumper.onEnd();
					pipedOutputStream.close();
					
					return null;
				}
				
			});
			
			ExecutorService compressorExecutor = Executors.newSingleThreadExecutor();
			Future<Void> compressorFuture = compressorExecutor.submit(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					compressor.compress(pipedInputStream, outputStream);
					pipedInputStream.close();
					
					return null; 
				}
				
			});
			
			providerFuture.get();
			compressorFuture.get();
			
			connection.close();
			
			if (outputFile != null) {
				outputStream.close();
			}
			
			compressorExecutor.shutdown();
			providerExecutor.shutdown();
		} catch (Exception e) {
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
