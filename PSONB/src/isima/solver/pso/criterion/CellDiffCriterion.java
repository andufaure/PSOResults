/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.criterion;

import isima.cac.Matrix;
import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOPosition;

/**
 *
 * @author onio
 */
public class CellDiffCriterion implements Criterion {

    private Matrix  refMatrix = null;
    
    public CellDiffCriterion(Matrix inMatrix)
    {
        refMatrix = inMatrix.buildCopy();
    }
    
    @Override
    public void calc(PSOPosition inPosition) {
        
        int diff = Integer.MAX_VALUE;
        double fitness = Double.MAX_VALUE;
        
        try
        {
            diff = Matrix.cellDiffBetween(refMatrix, inPosition.getAutomata().getCurrentMatrix());
        }
        catch(Exception e)
        {}
        
        //fitness = diff + ((inPosition.getAutomata().getStep() / PSOConstants.AUTO_MAX_STEPS) * 0.1);
        inPosition.setDiff(diff);
        inPosition.setFitness(diff);
    }

    @Override
    public Criterion buildCopy() throws Exception {
        return new CellDiffCriterion(this.refMatrix);
    }

    @Override
    public void copyContentFrom(Criterion inCriterion) throws Exception {
        refMatrix.copyContentFrom(((CellDiffCriterion)inCriterion).refMatrix);
    }

    @Override
    public Matrix getRefMatrix() {
        return (refMatrix);
    }
    
    

}
