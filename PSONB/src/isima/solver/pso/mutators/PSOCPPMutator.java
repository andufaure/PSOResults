/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.mutators;

import isima.random.RandomManager;
import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOParticle;

/**
 *
 * @author onio
 */
public class PSOCPPMutator implements PSOMutator {

    private int     unhealthy_it = 0;
    private int     mutat_time = 0;
    private int     PSOCPPDecoder;
    
    @Override
    public void reset() {
    }

    @Override
    public void mutat(double inHealth, PSOParticle[] inParticles) {
        
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
        {
            double[] pos = inParticles[i].getX().getData();
            
            for (int j = 0 ; j < 512 ; j++)
            {
                if (RandomManager.getInstance().getDoubleIn(0, 1.) < 0.3)
                    pos[j] = RandomManager.getInstance().getDoubleIn(PSOConstants.POS_MIN, PSOConstants.POS_MAX);
            }
        }
        mutat_time++;
        unhealthy_it = 0;
    }

    @Override
    public boolean doUpdatePos() {
        return (true);
    }

    @Override
    public boolean enabled(double inHealth, int inIteration) {

        if (inHealth <= 0.3)
            unhealthy_it++;
        
        return ((unhealthy_it > 20 * mutat_time));
    }
}
