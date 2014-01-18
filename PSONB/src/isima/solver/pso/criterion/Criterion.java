/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.criterion;

import isima.cac.Matrix;
import isima.solver.pso.PSOParticle;
import isima.solver.pso.PSOPosition;

/**
 *
 * @author onio
 */
public interface Criterion {
    
    public Matrix       getRefMatrix();
    public void         calc(PSOPosition inPosition);
    public Criterion    buildCopy() throws Exception;
    public void         copyContentFrom(Criterion inCriterion) throws Exception;
}
