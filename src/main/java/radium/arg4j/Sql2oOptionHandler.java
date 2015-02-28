package radium.arg4j;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

import radium.oracle.TNSNames;
import com.google.common.base.Strings;

public class Sql2oOptionHandler extends OneArgumentOptionHandler<Sql2o> {

	final private static Logger LOGGER = LoggerFactory.getLogger(Sql2oOptionHandler.class);
	
	final public static String TNS_ADMIN = "TNS_ADMIN";
	final public static String ORACLE_LOGON_REGEX = "^([a-zA-Z0-9_]+)/(.+?)@([a-zA-Z0-9_.]+)$";
	
	public Sql2oOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Sql2o> setter) {
		super(parser, option, setter);
	}

	@Override
	public String getDefaultMetaVariable() {
		return "URL";
	}

	@Override
	protected Sql2o parse(String argument) throws NumberFormatException, CmdLineException {
		String url = null; 
		String user = null; 
		String password = null; 
		Matcher oracleLogonMatcher = Pattern.compile(ORACLE_LOGON_REGEX).matcher(argument);
		if (oracleLogonMatcher.matches()) {
			LOGGER.info("{} is an Oracle Logon string", argument);
			try {
				String tnsAdmin = System.getenv(TNS_ADMIN);
				if (Strings.isNullOrEmpty(tnsAdmin)) {
					tnsAdmin = TNSNames.expandServices("C:/Softwares/Oracle/Database/Client/11g/product/11.2.0.3/client_1/network/admin");
					System.setProperty("oracle.net.tns_admin", tnsAdmin);
				}
			} catch (IOException e) {
				LOGGER.warn("Unable to expend the aliases of the tnsnames.ora", e);
			}
			
			user = oracleLogonMatcher.group(1);
			password = oracleLogonMatcher.group(2);
			String alias = oracleLogonMatcher.group(3);
			url = "jdbc:oracle:thin:@" + alias;
		} else {
			url = argument;
		}
		
		Sql2o sql2o = new Sql2o(url, user, password);
		return sql2o;
	}
	
}
