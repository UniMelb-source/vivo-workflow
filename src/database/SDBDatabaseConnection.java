/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package database;

import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;
import vivo.VitroJenaSDBModelMaker;
import workflow.ReadArguments;

/**
 *
 * @author dcliff
 */
public class SDBDatabaseConnection
{
    ModelMaker maker;

    public SDBDatabaseConnection(ReadArguments rA)
    {
        StoreDesc storeDesc = new StoreDesc(LayoutType.fetch("layout2/hash"), DatabaseType.fetch("MySQL"));
        BasicDataSource bds = makeBasicDataSource("com.mysql.jdbc.Driver", rA.getDBUrl(), rA.getDBUser(), rA.getDBPass());

        try
        {
            maker = new VitroJenaSDBModelMaker(storeDesc, bds);
        }
        catch(SQLException e)
        {
            //Error making the model maker
            System.out.println(e);
        }
    }

    public SDBDatabaseConnection(String dbURL, String dbUser, String dbPass)
    {
        StoreDesc storeDesc = new StoreDesc(LayoutType.fetch("layout2/hash"), DatabaseType.fetch("MySQL"));
        BasicDataSource bds = makeBasicDataSource("com.mysql.jdbc.Driver", dbURL, dbUser, dbPass);

        try
        {
            maker = new VitroJenaSDBModelMaker(storeDesc, bds);
        }
        catch(SQLException e)
        {
            //Error making the model maker
            System.out.println(e);
        }
    }

    public ModelMaker getModelMaker()
    {
        return maker;
    }

    public void closeSDBDatabaseConnection()
    {
        try
        {
            maker.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    public static BasicDataSource makeBasicDataSource(String dbDriverClassname, String jdbcUrl, String username, String password)
    {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbDriverClassname);
        ds.setUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);

        try
        {
            ds.getConnection().close();
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ds;
    }
}