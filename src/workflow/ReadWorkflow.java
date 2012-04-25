/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package workflow;

import com.hp.hpl.jena.sparql.core.ResultBinding;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import vivo.ResourceController;

/**
 *
 * @author dcliffsubjectStr, modelStr
 */
public class ReadWorkflow
{
    
    private ArrayList<ResultBinding> resourceList;
    private HashMap<String, URL> urlList;
    private ReadArguments rA;
    
    //tripleList for SPARQL Contruct statements TODO
    
    private ResourceController rc;

    public ReadWorkflow(String[] args)
    {
        rA = new ReadArguments(args);
        
        resourceList = new ArrayList<ResultBinding>();
        urlList = new HashMap<String, URL>();        
        
        rc = new ResourceController(rA);
    }
    
    private ArrayList<String> get3Values(String entry)
    {
        ArrayList<String> valueList = new ArrayList<String>();
        
        int startChar = entry.indexOf(": \"");
        int endchar = entry.indexOf("\",");

        valueList.add(entry.substring(startChar + 3, endchar));

        //source model
        startChar = entry.indexOf(", \"");
        endchar = entry.lastIndexOf("\",");

        valueList.add(entry.substring(startChar + 3, endchar));

        //destination model
        startChar = entry.lastIndexOf(", \"");
        endchar = entry.indexOf("\";");

        valueList.add(entry.substring(startChar + 3, endchar));
        
        return valueList;
    }
    
    private ArrayList<String> get2Values(String entry)
    {
        ArrayList<String> valueList = new ArrayList<String>();
        
        //parse line for paramaters - actionName: "url", "model"
        int startChar = entry.indexOf(": \"");
        int endchar = entry.indexOf("\",");

        valueList.add(entry.substring(startChar + 3, endchar));

        startChar = entry.indexOf(", \"");
        endchar = entry.indexOf("\";");

        valueList.add(entry.substring(startChar + 3, endchar));

        return valueList;
    }
    
    private String getValue(String entry)
    {        
        //parse line for paramaters - actionName: "model"
        int startChar = entry.indexOf(": \"");
        int endchar = entry.indexOf("\";");

        return entry.substring(startChar + 3, endchar);
    }

    public void processSteps() throws FileNotFoundException, IOException
    {          
        //open text file
        FileInputStream fstream;

        fstream = new FileInputStream(rA.getWorkflow());

        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;

        //Read File Line By Line
        while ((strLine = br.readLine()) != null)
        {            
            //skip comments
            if (!strLine.startsWith("#"))
            {
                //execute functions
                if (strLine.startsWith("LoadVClassIntoModel"))
                {
                    //parse line for paramaters - actionName: "vivoServer", "vClassID", "destinationModel"
                    ArrayList<String> valueList = get3Values(strLine);
                    rc.LoadVClassIntoModel(valueList.get(0), valueList.get(1), valueList.get(2));
                }
                else if(strLine.startsWith("LoadURLintoModel"))
                {
                    //parse line for paramaters - actionName: "url", "model"
                    ArrayList<String> valueList = get2Values(strLine);                
                    rc.LoadURLIntoModel(valueList.get(0), valueList.get(1));
                }
                if (strLine.startsWith("LoadURIsIntoModel"))
                {
                    //parse line for paramaters - actionName: "vivoServer", "subject", "destinationModel"
                    ArrayList<String> valueList = get3Values(strLine);
                    rc.LoadURIsIntoModel(urlList, resourceList, valueList.get(0), valueList.get(1), valueList.get(2));
                }
                else if (strLine.startsWith("LoadURLsIntoModel"))
                {
                    //parse line for paramaters - actionName: "subject", "model"
                    ArrayList<String> valueList = get2Values(strLine);
                    rc.LoadURLsIntoModel(urlList, resourceList, valueList.get(0), valueList.get(1), rA);
                }
                else if (strLine.startsWith("ExecuteQuery"))
                {
                    //parse line for paramaters - actionName: "SPARQLstring", "model"
                    ArrayList<String> valueList = get2Values(strLine);                    
                    rc.ExecuteQuery(valueList.get(0), valueList.get(1), resourceList);
                }
                else if (strLine.startsWith("ClearResources"))
                {
                    rc.ClearResources(resourceList);
                }
                else if (strLine.startsWith("ClearURLs"))
                {
                    rc.ClearURLs(urlList);
                }
                else if (strLine.startsWith("CreateModel"))
                {
                    //parse line for paramaters - actionName: "model"
                    rc.CreateModel(getValue(strLine));
                }
                else if (strLine.startsWith("RemoveModel"))
                {
                    //parse line for paramaters - actionName: "model"
                    rc.RemoveModel(getValue(strLine));
                }
                else if (strLine.startsWith("AddModelToModel"))
                {
                    //parse line for paramaters - actionName: "model", "model"
                    ArrayList<String> valueList = get2Values(strLine);
                    rc.AddModelToModel(valueList.get(0), valueList.get(1));
                }
                else if (strLine.startsWith("ExecuteConstruct"))
                {
                    //parse line for paramaters - actionName: "constructQuery", "sourceModel", "destinationModel"
                    ArrayList<String> valueList = get3Values(strLine);
                    rc.ExecuteConstruct(valueList.get(0), valueList.get(1), valueList.get(2));
                }
            }

        }     
    }
}