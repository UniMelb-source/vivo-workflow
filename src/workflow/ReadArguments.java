/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package workflow;

/**
 *
 * @author dcliff
 */
public class ReadArguments
{
    private String dbUrl, dbUser, dbPass, workflow;
    private int threadCount;

    public ReadArguments(String[] arguments)
    {
        int i = 0;

        for(String s : arguments)
        {
            if(s.equalsIgnoreCase("-dbURL"))
            {
                dbUrl = arguments[i + 1];
            }
            else if(s.equalsIgnoreCase("-dbUser"))
            {
                dbUser = arguments[i + 1];
            }
            else if(s.equalsIgnoreCase("-dbPass"))
            {
                dbPass = arguments[i + 1];
            }
            else if(s.equalsIgnoreCase("-workflow"))
            {
                workflow = arguments[i + 1];
            }
            else if(s.equalsIgnoreCase("-threadCount"))
            {
                threadCount = Integer.parseInt((arguments[i + 1]).trim());
            }
            
            i++;
        }
    }

    public String getDBUrl()
    {
        return dbUrl;
    }
    
    public String getDBUser()
    {
        return dbUser;
    }
    
    public String getDBPass()
    {
        return dbPass;
    }
    
    public String getWorkflow()
    {
        return workflow;
    }

    public int getThreadCount()
    {
        return threadCount;
    }

}
