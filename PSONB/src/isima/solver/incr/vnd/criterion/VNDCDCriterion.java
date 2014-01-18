/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.incr.vnd.criterion;

import isima.cac.M2Auto;
import isima.cac.M2Rules;
import isima.cac.Matrix;

/**
 *
 * @author onio
 */
public class VNDCDCriterion implements VNDCriterion {

    private M2Auto      automata;
    private Matrix      refMatrix;
    private Matrix      initMatrix;
    
    public VNDCDCriterion(Matrix inInitMatrix, Matrix inRefMatrix) throws Exception
    {
        initMatrix = inInitMatrix.buildCopy();
        refMatrix = inRefMatrix.buildCopy();
        automata = new M2Auto(inInitMatrix);
    }
    
    public VNDCDCriterion(VNDCDCriterion inCriterion) throws Exception
    {
        initMatrix = inCriterion.initMatrix.buildCopy();
        refMatrix = inCriterion.refMatrix.buildCopy();
        automata = new M2Auto(initMatrix);
    }
    
    
    @Override
    public Matrix getRefMatrix() {
        return (refMatrix);
    }

    @Override
    public Matrix getInitMatrix() {
        return (initMatrix);
    }

    @Override
    public VNDCriterion buildCopy() throws Exception {
        return new VNDCDCriterion(this);
    }

    @Override
    public void copyContentFrom(VNDCriterion inCriterion) throws Exception {
        
        VNDCDCriterion  cdCriterion = (VNDCDCriterion)inCriterion;
        
        initMatrix.copyContentFrom(cdCriterion.initMatrix);
        refMatrix.copyContentFrom(cdCriterion.refMatrix);
        automata.copyContentFrom(cdCriterion.automata);
    }

    @Override
    public double calc(M2Rules inRules, int inSteps) throws Exception {
        
        automata.reset(inRules);
        automata.run(inSteps);
        return (Matrix.cellDiffBetween(refMatrix, automata.getCurrentMatrix()));
    }

    @Override
    public double getMaxFitness() {
        return (refMatrix.getHeight() * refMatrix.getWidth());
    }
}
