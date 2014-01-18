/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.incr.vnd.criterion;

import isima.cac.M2Rules;
import isima.cac.Matrix;

/**
 *
 * @author onio
 */
public interface VNDCriterion {
    
    public Matrix           getInitMatrix();
    public Matrix           getRefMatrix();
    public double           calc(M2Rules inRules, int inSteps) throws Exception;
    public VNDCriterion     buildCopy() throws Exception;
    public void             copyContentFrom(VNDCriterion inCriterion) throws Exception;
    public double           getMaxFitness();
}
