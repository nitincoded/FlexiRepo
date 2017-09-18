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

    public static void main(String args[]) throws Exception {
        //Either load it here, or force the user to set the property java.util.logging.config.file using the -D VM argument
        String[] paths = new String[] {
                System.getProperty("user.dir") + "/target/classes/logging.properties",
                "logging.properties"
        };
        for (String iterPath : paths) {
            if (new java.io.File(iterPath).exists()) {
                LogManager.getLogManager().readConfiguration(new java.io.FileInputStream(iterPath));
                log.finest("Loaded logger config from " + iterPath);
                break;
            } else {
//                log.finest("Default logger config");
            }
        }

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

    public static String getExecuteQuery(Request req, Response res) throws Exception {
        return executeQuery(req.queryParams("jdbcUrl"), req.queryParams("sql"));
    }
    public static String getpostExecuteNonQuery(Request req, Response res) throws Exception {
        executeNonQuery(req.queryParams("jdbcUrl"), req.queryParams("sql"));
        return "OK";
    }

    public static void executeNonQuery(String jdbcUrl, String sql) throws Exception {
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

    public static String executeQuery(String jdbcUrl, String sql) throws Exception {
        Connection conn = null;
        try {
            conn = getConnectionForUrl(jdbcUrl);
            Statement st = conn.createStatement();
            ResultSet rset = st.executeQuery(sql);

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

            return gson.toJson(data);
        }
        catch (Exception ex) {
            log.warning(ex.getMessage());
            throw ex;
        }
        finally {
            try { conn.close(); } catch (Exception ex) { log.warning(ex.getMessage()); }
        }
    }

    public static Connection getConnectionForUrl(String url) throws Exception {
        if (url.startsWith("jdbc:sqlserver") || url.startsWith("jdbc:mssql")) {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
        } else if (url.startsWith("jdbc:mysql")) {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
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