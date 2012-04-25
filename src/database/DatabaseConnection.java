/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

/**
 *
 * @author dcliff
 */
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.rdf.model.*;
import workflow.ReadArguments;

public class DatabaseConnection
{

    ModelMaker maker;
    IDBConnection conn;

    public DatabaseConnection(ReadArguments rA)
    {
        //move these details out into a properties file!
        String className = "com.mysql.jdbc.Driver";             // path of driver class

        try
        {
            Class.forName(className);                           // Load the Driver

            String DB_URL = rA.getDBUrl();                      // URL of database - vivo???
            String DB_USER = rA.getDBUser();                    // database user id
            String DB_PASSWD = rA.getDBPass();                  // database password
            String DB = "MySQL";                                // database type

            // Create database connection
            conn = new DBConnection(DB_URL, DB_USER, DB_PASSWD, DB);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        maker = ModelFactory.createModelRDBMaker(conn);
    }

    public DatabaseConnection(String dbURL, String dbUser, String dbPass)
    {
        //move these details out into a properties file!
        String className = "com.mysql.jdbc.Driver";             // path of driver class

        try
        {
            Class.forName(className);                           // Load the Driver

            String DB_URL = dbURL;
            String DB_USER = dbUser;
            String DB_PASSWD = dbPass;
            String DB = "MySQL";                                // database type

            // Create database connection
            conn = new DBConnection(DB_URL, DB_USER, DB_PASSWD, DB);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        maker = ModelFactory.createModelRDBMaker(conn);
    }

    public ModelMaker getModelMaker()
    {
        return maker;
    }

    public void closeDatabaseConnection()
    {
        try
        {
            maker.close();
            conn.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
