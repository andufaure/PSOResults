/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bpso;

import isima.cac.Matrix;
import isima.solver.pso.PSOCore;
import java.math.BigInteger;

/**
 *
 * @author onio
 */
public class BPSO {
    
    private BigInteger  initSequence = null;
    private BPSOCore    core = null;
    
    public BPSO(Matrix inInitMatrix, Matrix inRefMatrix, BigInteger inInitSequence) throws Exception
    {
        core = new BPSOCore(inInitMatrix, inRefMatrix);
        initSequence = inInitSequence;
    }
    
    public void reset()
    {
        BPSOParticle[] particles = null;
        
        core.reset();
        //core.getGbest().reset();
        particles = core.getParticles();
        for (int i = 0 ; i < particles.length ; i++)
        {
            byte pos[] = particles[i].getX().getData();
            for (int j = 0 ; j < pos.length ; j++)
            {
                if (initSequence.testBit(i * pos.length +j))
                    pos[j] = 1;
                else
                    pos[j] = 0;
            }
            particles[i].getVelo().nullify();
        }
    }
    
    public void reset(BigInteger inInitSequence)
    {
        initSequence = inInitSequence;
        reset();
    }
    
    public BPSOCore getCore() {
        return core;
    }

    public BigInteger getInitSequence() {
        return initSequence;
    }

    public void setInitSequence(BigInteger initSequence) {
        this.initSequence = initSequence;
    }
}
