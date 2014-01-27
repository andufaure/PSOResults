/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.mutators;

import isima.cac.M2Rules;
import isima.random.RandomManager;
import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOParticle;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Reproduction mutator
 * ONLY WITH CPP DECODER
 * ONLY WITH FIXED NEIGHBORHOOD => must be adapted for circular
 * @author onio
 */
public class PSOReprMutator implements PSOMutator {

    private int unhealthy_it = 0;
    private int mutat_time = 0;
    
    @Override
    public void reset() {
    }

    @Override
    public boolean enabled(double inHealth, int inIteration) {
        
        if (inHealth <= 0.3)
            unhealthy_it++;
        else
            unhealthy_it = 0;
        
        return ((unhealthy_it > (20 * mutat_time)));
    }

    @Override
    public void mutat(double inHealth, PSOParticle[] inParticles) throws Exception {
        // for each sub swarm (neighborhood)
        // take the best, take the worst
        
        int subSwarmsCount = PSOConstants.N_PARTICLES / (PSOConstants.NEIGHBORHOOD_SIZE + 1);
        Map<Double, PSOParticle>    particleByFitness;
        particleByFitness = new HashMap<Double, PSOParticle>();
        
        
        
        for (int i = 0 ; i < subSwarmsCount ; i++)
        {
            particleByFitness.clear();
            for (int j = 0 ; j < (PSOConstants.NEIGHBORHOOD_SIZE + 1) ; j++)
            {
                PSOParticle par = inParticles[i * (PSOConstants.NEIGHBORHOOD_SIZE + 1) + j];
                particleByFitness.put(par.getX().getFitness(), par);
            }
            performReproduce(particleByFitness);
        }
        
        //mutat_time++;
    }
    
    private void    performReproduce(Map<Double, PSOParticle> inParticleByFitness) throws Exception
    {
        List<PSOParticle> lpars = new LinkedList<PSOParticle>(inParticleByFitness.values());
        PSOParticle       best = null;
        PSOParticle       worst = null;
        M2Rules           best_rules = null;
        M2Rules           worst_rules = null;
        
        best = lpars.get(RandomManager.getInstance().getIntIn(0, ((lpars.size() - 1)) / 2));
        worst = lpars.get(RandomManager.getInstance().getIntIn(((lpars.size() - 1) / 2) + 1, (lpars.size() - 1)));
        
        best_rules = best.getX().getAutomata().getRules();
        worst_rules = best.getX().getAutomata().getRules();
        
        //worst.getXp().copyContentFrom(worst.getX());
        
        for (int i = 0 ; i < 512 ; i++)
        {
            if (worst_rules.get(i) != best_rules.get(i))
            {
                worst_rules.set(i, (RandomManager.getInstance().getIntIn(0, 1) == 0) 
                                    ? worst.getP().getAutomata().getRules().get(i) 
                                    : best.getP().getAutomata().getRules().get(i));
                worst.getX().getData()[i] = 
                        RandomManager.getInstance().getDoubleIn(worst_rules.get(i) * worst.getX().getData()[513], ((worst_rules.get(i) + 1) * worst.getX().getData()[513] - 0.01));
                //worst.getVelo().getData()[i] = worst.getX().getData()[i] - worst.getXp().getData()[i];
            }
        }
        
        
    }

    @Override
    public boolean doUpdatePos() {
        return true;
    }
    
}
