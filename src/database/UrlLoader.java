/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import vivo.ModelController;

/**
 *
 * @author dcliff
 */
public class UrlLoader implements Runnable
{
    private String URLString, modelName;
    private ModelController mc;
    private ModelMaker maker;
    private DatabaseConnection dC;
    //private SDBDatabaseConnection dC;
    private int iterationNumber;

    public UrlLoader(String URLStringStr, String modelNameStr, String dbUrlStr, String dbUserStr, String dbPassStr, int i)
    {
        URLString = URLStringStr;
        modelName = modelNameStr;
        mc = new ModelController();
        dC = new DatabaseConnection(dbUrlStr, dbUserStr, dbPassStr);
        iterationNumber = i;
        this.maker = dC.getModelMaker();
    }

    @Override public void run()
    {
        int exceptionCount = 0;        

        while(exceptionCount < 3)
        {
            try
            {
                attemptRead();
                break;
            }
            catch(Exception e)
            {
                exceptionCount++;
                try
                {
                    Thread.sleep(5000);
                }
                catch(InterruptedException iE)
                {
                    //do nothing
                }

                if(exceptionCount == 3)
                {
                    //We need informative error writing here
                    try
                    {
                        FileWriter fstream = new FileWriter(this.hashCode() + "-" +System.currentTimeMillis() + "-error.txt");
                        BufferedWriter errorLog = new BufferedWriter(fstream);
                        errorLog.write("Error with URL: " + URLString + " = " + e);
                        errorLog.close();
                    }
                    catch(IOException f)
                    {
                        System.out.println("Can't access error log: " + f);
                    }
                    break;
                }
            }
        }

        dC.closeDatabaseConnection();
    }

    private void attemptRead() throws Exception
    {
        Model model = mc.CreateModel(maker, modelName);
        //Use URL to get RDF and load into the model

        System.out.println(modelName + ", " + URLString);

        URL mrwLink = new URL(URLString);        
        URLConnection uCon = mrwLink.openConnection();
        model.read(uCon.getInputStream(), null);
        System.out.printf(iterationNumber + "\tdone\r");
    }
}