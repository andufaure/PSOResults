/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.criterion;

import isima.cac.M2Auto;
import isima.cac.Matrix;
import isima.random.RandomManager;
import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOPosition;

/**
 *
 * @author onio
 */
public class CDLECriterion implements Criterion {
    
    public static final int N_STEPS_EVAL = 19;
    
    private M2Auto  refAutomata = null;
    private M2Auto  curAutomata = null;
    private Matrix  refMatrix = null;
    private Matrix  curMatrix = null;
    
    private int     rol = 0;
    private int     record_diff = Integer.MAX_VALUE;
    private int[]   diffByStep = new int[N_STEPS_EVAL];
    
    public CDLECriterion(Matrix inMatrix) throws Exception
    {
        refMatrix = inMatrix.buildCopy();
        curMatrix = inMatrix.buildCopy();
        refAutomata = new M2Auto(refMatrix);
        curAutomata = new M2Auto(refMatrix);
    }
    
    @Override
    public void calc(PSOPosition inPosition) {
        
        int max_diff = refMatrix.getWidth() * refMatrix.getHeight();
        
        int diff = Integer.MAX_VALUE;
        double fitness = Double.MAX_VALUE;
        
        int steps = inPosition.getAutomata().getStep();
        
        try
        {
            curMatrix.copyContentFrom(inPosition.getAutomata().getCurrentMatrix());
            diff = Matrix.cellDiffBetween(refMatrix, curMatrix);
                        
            if (diff != 0)
            {
                if (record_diff > diff)
                {
                    rol = 0;
                    record_diff = diff;
                }
                else if (record_diff < diff)
                {
                    rol = 0;
                }
                else
                {
                    rol = 1;
                }
                
                fitness = diff;
                refAutomata.reset(refMatrix);
                refAutomata.reset(inPosition.getAutomata().getRules());
                curAutomata.reset(curMatrix);
                curAutomata.reset(inPosition.getAutomata().getRules());
                
                //refAutomata.getRules().rol((int)rol);
                //curAutomata.getRules().rol((int)rol); 
                
                double tmp_fitness = 1;
                //fitness = 0;
                for (int i = 0 ; i < N_STEPS_EVAL ; i++)
                {
                    int tmp_diff;
                    
                    //fitness *= (max_diff + 1);
                    //tmp_fitness *= (max_diff + 1);
                    refAutomata.step();
                    curAutomata.step();
                    refAutomata.getRules().completeMutat();
                    refAutomata.getRules().rol(1);
                    curAutomata.getRules().completeMutat();
                    curAutomata.getRules().rol(1);                  
                    
                    if ((rol) != 0)
                        tmp_diff = max_diff - Matrix.cellDiffBetween(refAutomata.getCurrentMatrix(), curAutomata.getCurrentMatrix());
                    else
                        tmp_diff = Matrix.cellDiffBetween(refAutomata.getCurrentMatrix(), curAutomata.getCurrentMatrix());

                    diffByStep[i] = tmp_diff;
                    
                    //refAutomata.getCurrentMatrix().completeMutat();
                    //curAutomata.getCurrentMatrix().completeMutat();

                }

                if (rol != 0)
                {
                    for (int i = (N_STEPS_EVAL - 1) ; i >= 0 ; i--)
                    {
                        fitness *= (max_diff + 1);
                        fitness += diffByStep[i];
                    }
                }
                else
                {
                    for (int i = 0 ; i < N_STEPS_EVAL ; i++)
                    {
                        fitness *= (max_diff + 1);
                        fitness += diffByStep[i];
                    }
                }
                
                //System.out.println(fitness);
                //fitness *= (max_diff + 1);
                //fitness -= tmp_fitness;
            }
            else
                fitness = 0;
            
            
        }
        catch(Exception e)
        {}
        
        //fitness = diff + ((inPosition.getAutomata().getStep() / PSOConstants.AUTO_MAX_STEPS) * 0.1);
        inPosition.setDiff(diff);
        inPosition.setFitness(fitness);
    }

    @Override
    public Criterion buildCopy() throws Exception {
        return new CDLECriterion(refMatrix);
    }

    @Override
    public void copyContentFrom(Criterion inCriterion) throws Exception {
        refMatrix.copyContentFrom(((CDLECriterion)inCriterion).refMatrix);
        curMatrix.copyContentFrom(((CDLECriterion)inCriterion).curMatrix);
        refAutomata.copyContentFrom(((CDLECriterion)inCriterion).refAutomata);
        curAutomata.copyContentFrom(((CDLECriterion)inCriterion).curAutomata);
        record_diff = ((CDLECriterion)inCriterion).record_diff;
        rol = ((CDLECriterion)inCriterion).rol;
    }

    @Override
    public Matrix getRefMatrix() {
        return (refMatrix);
    }
}
