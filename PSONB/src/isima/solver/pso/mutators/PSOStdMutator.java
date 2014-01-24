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
public class PSOStdMutator implements PSOMutator {

    private int     unhealthy_it = 0;
    private int     mutat_time = 1;
    
    @Override
    public void reset() {
    }

    @Override
    public void mutat(double inHealth, PSOParticle[] inParticles) {
        
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
        {
            if (RandomManager.getInstance().getDoubleIn(0, 1) < 0.1)
            {
                inParticles[i].getX().randomize();
                inParticles[i].getVelo().nullify();
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
        else
            unhealthy_it = 0;
        
        return ((unhealthy_it > 10 * mutat_time));
    }
}
