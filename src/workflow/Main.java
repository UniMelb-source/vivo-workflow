/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package workflow;

import java.io.IOException;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author dcliff
 */
public class Main
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        PropertyConfigurator.configure("log4j.properties");

        ReadWorkflow rW = new ReadWorkflow(args);

        try
        {
            //pass fileName to ReadWorkFlow object
            rW.processSteps();
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
    }
}
