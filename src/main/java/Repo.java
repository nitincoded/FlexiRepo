import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import static spark.Spark.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by Nitin Reddy on 9/13/17.
 */
public class Repo {
    static Gson gson = new Gson();
    static Logger log = Logger.getLogger(Repo.class.getName());
    static java.util.Properties props = new java.util.Properties();
    static java.util.Properties dbinfo = new java.util.Properties();

    /**
     * Loads the logging level configuration and logger handler info
     * @throws Exception
     */
    private static void loadLoggerConfig() throws Exception {
        //An alternative to loading the config through custom code is to set the property java.util.logging.config.file using the -D VM argument

        String[] paths = new String[] {
                System.getProperty("user.dir") + "/target/classes/logging.properties",
                System.getProperty("user.dir") + "/logging.properties"
        };
        boolean isLoggingConfigFileFound = false;

        for (String iterPath : paths) {
            if (new java.io.File(iterPath).exists()) {
                LogManager.getLogManager().readConfiguration(new java.io.FileInputStream(iterPath));
                log.finest("Loaded logger config from " + iterPath);
                isLoggingConfigFileFound = true;
                break;
            }
        }

        if (!isLoggingConfigFileFound){
            log.info("No logger config file found");
        }
    }

    /**
     * Loads the configuration for the application
     * @throws Exception
     */
    private static void loadGeneralConfig() throws Exception {
        String cfgFilename = System.getProperty("user.dir") + "/" + "flexirepo.properties";
        String dbinfoFilename = System.getProperty("user.dir") + "/" + "dbinfo.properties";

        if (new java.io.File(cfgFilename).exists()) {
            log.info("Loading config file: " + cfgFilename);
            java.io.FileInputStream fis = null;

            try {
                fis = new java.io.FileInputStream(cfgFilename);
                props.load(fis);
            }
            finally {
                try { fis.close(); } catch(Exception ex) {}
            }
        } else {
            log.info("Config file doesn't exist: " + cfgFilename);
        }

        if (new java.io.File(dbinfoFilename).exists()) {
            log.info("Loading dbinfo file: " + dbinfoFilename);
            java.io.FileInputStream fis = null;

            try {
                fis = new java.io.FileInputStream(dbinfoFilename);
                dbinfo.load(fis);
            }
            finally {
                try { fis.close(); } catch(Exception ex) {}
            }
        } else {
            log.info("dbinfo file doesn't exist: " + dbinfoFilename);
        }
    }

    /**
     * Program entry point
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        loadLoggerConfig();
        loadGeneralConfig();

        int i_portNo = 2020;

        log.finest("Executing from " + Repo.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        log.finest("Working directory " + System.getProperty("user.dir"));
        log.info(String.format("API version: %d", 20170913));
        log.info(String.format("Attempting to start on port %d", i_portNo));

        port(i_portNo);

        get("/query", Repo::getExecuteQuery);
        get("/execute", Repo::getpostExecuteNonQuery);
        post("/execute", Repo::getpostExecuteNonQuery);
    }

    /**
     * Handles the GET /query route
     * @param req
     * @param res
     * @return
     * @throws Exception
     */
    public static String getExecuteQuery(Request req, Response res) throws Exception {
        return executeQuery(req.queryParams("jdbcUrl"), req.queryParams("sql"));
    }

    /**
     * Handles the POST /execute route
     * @param req
     * @param res
     * @return
     * @throws Exception
     */
    public static String getpostExecuteNonQuery(Request req, Response res) throws Exception {
        executeNonQuery(req.queryParams("jdbcUrl"), req.queryParams("sql"));
        return "OK";
    }

    /**
     * Handles the GET /execute route
     * @param jdbcUrl
     * @param sql
     * @throws Exception
     */
    private static void executeNonQuery(String jdbcUrl, String sql) throws Exception {
        Connection conn = null;
        try {
            conn = getConnectionForUrl(jdbcUrl);
            Statement st = conn.createStatement();
            st.executeQuery(sql);
//            st.close();
            conn.close();
        }
        catch (Exception ex) {
            log.warning(ex.getMessage());
            throw ex;
        }
        finally {
            try { conn.close(); } catch (Exception ex) { log.warning(ex.getMessage()); }
        }
    }

    /**
     *
     * @param jdbcUrl
     * @param sql
     * @return
     * @throws Exception
     */
    private static String executeQuery(String jdbcUrl, String sql) throws Exception {
        Connection conn = null;
        try {
            conn = getConnectionForUrl(jdbcUrl);
            Statement st = conn.createStatement();
            ResultSet rset = st.executeQuery(sql);

            HashMap<String, Object> retval = new HashMap<String, Object>();

            ArrayList<String> lstColumnNames = new ArrayList<>();
            ArrayList<HashMap<String, Object>> data = new ArrayList<>();

            while (rset.next()) {
                if (lstColumnNames.size() == 0) {
                    ResultSetMetaData meta = rset.getMetaData();
                    for (int i=0; i<meta.getColumnCount(); i++) {
                        lstColumnNames.add(meta.getColumnName(i+1));
                    }
                }

                HashMap<String, Object> row = new HashMap<>();
                for (int i=0; i<lstColumnNames.size(); i++) {
                    row.put(lstColumnNames.get(i), rset.getObject(i+1));
                }
                data.add(row);
            }

            rset.close();
            st.close();
            conn.close();

            retval.put("data", data);

            return gson.toJson(retval);
        }
        catch (Exception ex) {
            log.warning(ex.getMessage());
            throw ex;
        }
        finally {
            try { conn.close(); } catch (Exception ex) { log.warning(ex.getMessage()); }
        }
    }

    /**
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static Connection getConnectionForUrl(String url) throws Exception {
        try {
            //The .newInstance() call is for quirky JVMs

            if (url.startsWith("jdbc:sqlserver") || url.startsWith("jdbc:mssql")) {
                log.fine("Instantiating MS SQL Server JDBC driver");
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
            } else if (url.startsWith("jdbc:mysql")) {
                log.fine("Instantiating MySQL JDBC driver");
                Class.forName("com.mysql.jdbc.Driver").newInstance();
            } else if (url.startsWith("jdbc:sqlite")) {
                log.fine("Instantiating SQLite JDBC driver");
                Class.forName("org.sqlite.JDBC").newInstance();
            } else if (url.startsWith("jdbc:oracle")) {
                log.fine("Instantiating Oracle JDBC driver");
                Class.forName("oracle.jdbc.OracleDriver").newInstance();
            } else if (url.startsWith("jdbc:postgresql")) {
                log.fine("Instantiating PostgreSQL JDBC driver");
                Class.forName("org.postgresql.Driver").newInstance();
            }
        }
        catch (Exception ex) {
            log.info(ex.getMessage());
        }

        return DriverManager.getConnection(url);
    }
}

/*
jdbc:mysql://127.0.0.1/mydb?useLegacyDatetimeCode=false%26serverTimezone=Asia/Dubai%26user=root%26password=donttellanyone
SELECT * FROM chocolate

jdbc:sqlserver://127.0.0.1:1433;databaseName=mydb;user=sa;password=donttellanyone
SELECT * FROM chocolate
*/