/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bpso.criterion;

import isima.cac.Matrix;
import isima.solver.bpso.BPSOPosition;

/**
 *
 * @author onio
 */
public class BCellDiffCriterion implements BCriterion {

    private Matrix  refMatrix = null;
    
    public BCellDiffCriterion(Matrix inMatrix)
    {
        refMatrix = inMatrix.buildCopy();
    }
    
    @Override
    public void calc(BPSOPosition inPosition) {
        
        int diff = Integer.MAX_VALUE;
        double fitness = Double.MAX_VALUE;
        
        try
        {
            diff = Matrix.cellDiffBetween(refMatrix, inPosition.getAutomata().getCurrentMatrix());
        }
        catch(Exception e)
        {}
        
        fitness = (1. * diff) / (refMatrix.getHeight() * refMatrix.getWidth());
        fitness = diff;
        inPosition.setDiff(diff);
        inPosition.setFitness(fitness);
    }
}
