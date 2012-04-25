/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vivo;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import database.SDBDatabaseConnection;
import database.UrlLoader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import workflow.ReadArguments;

/**
 *
 * @author dcliff
 */
public class ResourceController
{
    ModelController mc;
    ReadArguments rA;

    public ResourceController(ReadArguments readArumentObject)
    {
        rA = readArumentObject;
        mc = new ModelController();        
    }

    public void ExecuteQuery(String queryString, String modelName, ArrayList<ResultBinding> resourceList)
    {
        SDBDatabaseConnection dc = new SDBDatabaseConnection(rA);
        ModelMaker maker = dc.getModelMaker();

        System.out.println("Executing Query on " + modelName + "...");
        Model model = mc.CreateModel(maker, modelName);
        Query query = QueryFactory.create(queryString);
        // Execute the query and obtain results
        QueryExecution qe = QueryExecutionFactory.create(query, model);

        ResultSet results = qe.execSelect();        
        for (Iterator iter = results; iter.hasNext();)
        {            
            ResultBinding res = (ResultBinding) iter.next();
            resourceList.add(res);
        }

        dc.closeSDBDatabaseConnection();
    }

    public void ExecuteConstruct(String queryString, String sourceModel, String destinationModel)
    {
        SDBDatabaseConnection dc = new SDBDatabaseConnection(rA);
        ModelMaker maker = dc.getModelMaker();

        System.out.println("Executing Construct on " + sourceModel);
        Model model = mc.CreateModel(maker, sourceModel);
        Query query = QueryFactory.create(queryString);
        // Execute the query and obtain results
        QueryExecution qe = QueryExecutionFactory.create(query, model);

        System.out.println("Putting statements from construct into " + sourceModel);
        Model destModel = mc.CreateModel(maker, destinationModel);
        qe.execConstruct(destModel);

        dc.closeSDBDatabaseConnection();
    }

    public void ExtractURLbySubject(HashMap<String, URL> urlList, ArrayList<ResultBinding> resourceList, String subject)
    {
        System.out.println("Extracting " + subject + " URLs...");
        int i = 0;
        for (ResultBinding obj : resourceList)
        {
            try
            {
                URL mrwLink = new URL((obj.get(subject).toString()));
                urlList.put(subject + i, mrwLink);
            }
            catch (MalformedURLException e)
            {
                //not a valid URL
                System.out.println("ExtractURLbySubject MalformedURLException error: " + e);
            }

            i++;
        }
    }

    public void LoadURLsIntoModel(HashMap<String, URL> urlList, ArrayList<ResultBinding> resourceList, String subject, String modelName, ReadArguments rA)
    {
        ExecutorService executor = Executors.newFixedThreadPool(rA.getThreadCount());

        ExtractURLbySubject(urlList, resourceList, subject);

        System.out.println("Loading " + urlList.size() + " " + subject + " URLs into model " + modelName + "\n");
        //Model model = mc.CreateModel(maker, modelName);
        Iterator iterator = urlList.entrySet().iterator();        

        int i = 0;

        while (iterator.hasNext())
        {
            //this is where we can multi thread to make this application faster
            Map.Entry pairs = (Map.Entry)iterator.next();
            if (((String)pairs.getKey()).startsWith(subject))
            {
                //Use URL to get RDF and load into the model
                try
                {
                    Runnable worker = new UrlLoader(((URL)pairs.getValue()).toString(), modelName, rA.getDBUrl(), rA.getDBUser(), rA.getDBPass(), i);
                    executor.execute(worker);
                    i++;
                }
                catch(Exception e)
                {                    
                    try
                    {
                        FileWriter fstream = new FileWriter(this.hashCode() + "-" +System.currentTimeMillis() + "-error.txt");
                        BufferedWriter errorLog = new BufferedWriter(fstream);
                        errorLog.write("LoadURLsIntoModel Exception error for " + ((URL)pairs.getValue()).toString() + ": " + e + "\n");
                        errorLog.close();
                    }
                    catch(IOException f)
                    {
                        System.out.println("Can't access error log: " + f);
                    }
                    continue;
                }
            }
        }        

        // This will make the executor accept no new threads
		// and finish all existing threads in the queue
		executor.shutdown();
		// Wait until all threads are finished
		while (!executor.isTerminated())
        {
            
		}        
        System.out.println("\nFinished all threads.");
    }

    public void LoadURIsIntoModel(HashMap<String, URL> urlList, ArrayList<ResultBinding> resourceList, String vivoServer, String subject, String modelName)
    {
        SDBDatabaseConnection dc = new SDBDatabaseConnection(rA);
        ModelMaker maker = dc.getModelMaker();
        Model model = mc.CreateModel(maker, modelName);

        ExtractURLbySubject(urlList, resourceList, subject);

        System.out.println("Loading " + urlList.size() + " " + subject + " URIs into model " + modelName + " from server " + vivoServer + "\n");
        //Model model = mc.CreateModel(maker, modelName);
        Iterator iterator = urlList.entrySet().iterator();

        int i = 0;

        while (iterator.hasNext())
        {
            //this is where we can multi thread to make this application faster
            Map.Entry pairs = (Map.Entry)iterator.next();
            if (((String)pairs.getKey()).startsWith(subject))
            {
                //Use URL to get RDF and load into the model
                try
                {
                    String URIstr = vivoServer + "individual?uri=" + URLEncoder.encode(((URL)pairs.getValue()).toString(), "UTF-8") + "&format=rdfxml";

                    //System.out.println("Loading URL into model "  + modelName + "...");
                    
                    //Use URL to get RDF and load into the model
                    try
                    {
                        URL mrwLink = new URL(URIstr);
                        URLConnection uCon = mrwLink.openConnection();
                        model.read(uCon.getInputStream(), null);

                        mrwLink = null;
                        uCon = null;
                    }
                    catch(IOException e)
                    {
                        //Internet connection failed
                        System.out.println("LoadURLIntoModel IOException error: " + e);
                    }

                    i++;                    
                    
                    System.out.printf(i + "\tdone\r");
                }
                catch(Exception e)
                {
                    System.out.println(e);
                }
            }
            pairs.setValue(null);
            pairs = null;            
        }

        model.close();
        dc.closeSDBDatabaseConnection();
    }

    public void LoadVClassIntoModel(String vivoServer, String vClassID, String modelName) throws UnsupportedEncodingException, MalformedURLException, IOException
    {
        SDBDatabaseConnection dc = new SDBDatabaseConnection(rA);
        ModelMaker maker = dc.getModelMaker();
        Model model = mc.CreateModel(maker, modelName);

        System.out.println("Extracting URIs matching vClassID " + vClassID + "...");

        String vClassURL = vivoServer + "listrdf?vclass=" + URLEncoder.encode(vClassID, "UTF-8");
        Model tempModel = ModelFactory.createDefaultModel();

        URL vivoLink = new URL(vClassURL);
        URLConnection uCon = vivoLink.openConnection();
        tempModel.read(uCon.getInputStream(), null);

        // list the statements in the Model
        StmtIterator iter = tempModel.listStatements();
        StmtIterator tmp = tempModel.listStatements();

        int statementListSize = tmp.toList().size();
        System.out.println("Loading " + statementListSize + " URIs into model...");

        int iterationNumber = 0;

        long totalTime = 0;
        long avgTime = 0;

        while (iter.hasNext())
        {
            try
            {
                final long startTime = System.currentTimeMillis();
                final long endTime;
                
                System.out.printf(iterationNumber + "\tdone at an avg time of " + avgTime + "ms per individual\r");
                iterationNumber += 1;
                Statement stmt      = iter.nextStatement();  // get next statement
                Resource  subject   = stmt.getSubject();     // get the subject
                String URLString = vivoServer + "individual?uri=" + URLEncoder.encode(subject.toString(), "UTF-8") + "&format=rdfxml";
                vivoLink = new URL(URLString);
                uCon = vivoLink.openConnection();
                model.read(uCon.getInputStream(), null);
                
                endTime = System.currentTimeMillis();
                final long duration = endTime - startTime;

                totalTime = totalTime + duration;
                avgTime = totalTime / iterationNumber;
            }
            catch(Exception e)
            {
                try
                {
                    Thread.sleep(4000);
                    
                    final long startTime = System.currentTimeMillis();
                    final long endTime;

                    System.out.printf(iterationNumber + "\tdone at an avg time of " + avgTime + "ms per individual\r");
                    iterationNumber += 1;
                    Statement stmt      = iter.nextStatement();  // get next statement
                    Resource  subject   = stmt.getSubject();     // get the subject
                    String URLString = vivoServer + "individual?uri=" + URLEncoder.encode(subject.toString(), "UTF-8") + "&format=rdfxml";
                    vivoLink = new URL(URLString);
                    uCon = vivoLink.openConnection();
                    model.read(uCon.getInputStream(), null);

                    endTime = System.currentTimeMillis();
                    final long duration = endTime - startTime;

                    totalTime = totalTime + duration;
                    avgTime = totalTime / iterationNumber;
                }
                catch(Exception error)
                {
                    //
                }
            }
        }

        dc.closeSDBDatabaseConnection();
        System.out.println(vClassID + " vClassID completed in a time of " + totalTime + "ms");
    }

    public void LoadURLIntoModel(String URLString, String modelName)
    {
        SDBDatabaseConnection dc = new SDBDatabaseConnection(rA);
        ModelMaker maker = dc.getModelMaker();

        //System.out.println("Loading URL into model "  + modelName + "...");
        Model model = mc.CreateModel(maker, modelName);
        //Use URL to get RDF and load into the model
        try
        {
            URL mrwLink = new URL(URLString);
            URLConnection uCon = mrwLink.openConnection();
            model.read(uCon.getInputStream(), null);
        }
        catch(IOException e)
        {
            //Internet connection failed
            System.out.println("LoadURLIntoModel IOException error: " + e);
        }

        dc.closeSDBDatabaseConnection();
    }

    public void ClearResources(ArrayList<ResultBinding> resourceList)
    {
        resourceList.clear();
    }

    public void ClearURLs(HashMap<String, URL> urlList)
    {
        urlList.clear();
    }

    public void CreateModel(String modelName)
    {
        SDBDatabaseConnection dc = new SDBDatabaseConnection(rA);
        ModelMaker maker = dc.getModelMaker();

        mc.CreateModel(maker, modelName);

        dc.closeSDBDatabaseConnection();
    }

    public void RemoveModel(String modelName)
    {
        System.out.println("Removing model " + modelName);
        SDBDatabaseConnection dc = new SDBDatabaseConnection(rA);
        ModelMaker maker = dc.getModelMaker();

        mc.RemoveModel(maker, modelName);

        dc.closeSDBDatabaseConnection();
    }

    public void AddModelToModel(String modelName, String addModelName)
    {
        SDBDatabaseConnection dc = new SDBDatabaseConnection(rA);
        ModelMaker maker = dc.getModelMaker();
        
        Model model = mc.CreateModel(maker, modelName);
        Model addModel = mc.CreateModel(maker, addModelName);

        model.add(addModel);

        dc.closeSDBDatabaseConnection();
    }

}
