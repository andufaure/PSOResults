/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bga;

import isima.cac.Matrix;
import isima.solver.bpso.BPSO;
import isima.solver.bpso.BPSOParticle;
import java.math.BigInteger;

/**
 *
 * @author onio
 */
public class BGAIndividual {
    
    private     BPSO                        bpso = null;
    private     double                      fitness = Double.MAX_VALUE;
    private     BGAIndividualEvaluator      evaluator = null;
    private     BigInteger                  curSequence = null;
    
    public BGAIndividual(Matrix inInitMatrix, Matrix inRefMatrix, BigInteger inInitSequence, BGAIndividualEvaluator inEvaluator) throws Exception
    {
        bpso = new BPSO(inInitMatrix, inRefMatrix, inInitSequence);
        evaluator = inEvaluator;
        reset(inInitSequence);
    }
    
    public void reset(BigInteger inInitSequence)
    {
        bpso.reset(inInitSequence);
        fitness = Double.MAX_VALUE;
    }

    public void live(int n_steps) throws Exception
    {
        BPSOParticle[]  particles = null;
        
        for (int i = 0 ; i < n_steps ; i++)
            bpso.getCore().step();
        
        curSequence = bpso.getInitSequence().and(bpso.getInitSequence());
        particles = bpso.getCore().getParticles();
        for (int i = 0 ; i < particles.length ; i++)
        {
            byte pos[] = particles[i].getX().getData();
            for (int j = 0 ; j < pos.length ; j++)
            {
                if (pos[j] == 1)
                    curSequence = curSequence.setBit(i * pos.length +j);
                else
                    curSequence = curSequence.clearBit(i * pos.length +j);
            }
            //particles[i].getVelo().nullify();
        }
    }
    
    public void evaluate()
    {
        evaluator.evaluate(this);
        
    }
    
    public BPSO getBpso() {
        return bpso;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public BigInteger getCurSequence() {
        return curSequence;
    }
    
    
}
