/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vivo;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 *
 * @author dcliff
 */
public class ModelController
{
    public Model CreateModel(ModelMaker maker, String modelName)
    {        
        return maker.createModel(modelName, false);        
    }

    public void RemoveModel(ModelMaker maker, String modelName)
    {
        maker.removeModel(modelName);
    }
}