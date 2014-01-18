/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso;

import isima.solver.pso.decoders.PSOPositionDecoder;
import isima.cac.M2Auto;
import isima.cac.Matrix;

/**
 *
 * @author onio
 */
public final class PSOPosition extends PSOVector {
    
    private static int  seedId = 0;
    public final int    Id;

    private int                 diff = Integer.MAX_VALUE;
    private double              fitness = Double.MAX_VALUE;
    private M2Auto              automata = null;
    private PSOPositionDecoder  decoder = null;
    
    public PSOPosition(Matrix inMatrix, PSOPositionDecoder inDecoder) throws Exception
    {
        automata = new M2Auto(inMatrix);
        Id = seedId++;
        decoder = inDecoder.buildCopy();
    }
    
    public  void reset() 
    {
        diff = Integer.MAX_VALUE;
        fitness = Double.MAX_VALUE;
        super.randomize(PSOConstants.POS_MIN, PSOConstants.POS_MAX);
    }
    
    public M2Auto getAutomata() {
        return automata;
    }

    public double getFitness() {
        return fitness;
    }
    
    public void copyContentFrom(PSOPosition inPosition) throws Exception
    {
        super.copyContentFrom(inPosition);
        diff = inPosition.diff;
        fitness = inPosition.fitness;
        automata.copyContentFrom(inPosition.automata);
        decoder.copyContentFrom(inPosition.decoder);
    }
    
    public void     decode() throws Exception
    {
        decoder.execute(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PSOPosition other = (PSOPosition) obj;
        if (this.Id != other.Id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.Id;
        return hash;
    }
    
    public void randomize() {
        super.randomize(PSOConstants.POS_MIN, PSOConstants.POS_MAX);
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public int getDiff() {
        return diff;
    }

    public PSOPositionDecoder getDecoder() {
        return decoder;
    }
    
}
