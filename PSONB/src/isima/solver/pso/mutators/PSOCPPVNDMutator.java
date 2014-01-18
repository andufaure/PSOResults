/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.mutators;

import isima.cac.M2Rules;
import isima.cac.Matrix;
import isima.random.RandomManager;
import isima.solver.incr.vnd.VND;
import isima.solver.incr.vnd.criterion.VNDCDCriterion;
import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOParticle;

/**
 *
 * @author onio
 */
public class PSOCPPVNDMutator implements PSOMutator {

    private int     unhealthy_it = 0;
    private int     mutat_time = (PSOConstants.NEIGHBORHOOD_SIZE / 2);
    private int     n_mutats = 0;
    private int     PSOCPPDecoder;
    private VND     vnd;
    private Matrix  init;
    private Matrix  ref;
    
    public PSOCPPVNDMutator(Matrix inInit, Matrix inRef) throws Exception
    {
        init = inInit.buildCopy();
        ref = inRef.buildCopy();
        vnd = new VND(inInit, inRef, new M2Rules(), 1, new VNDCDCriterion(inInit, inRef));
    }
    
    
    @Override
    public void reset() {
    }

    @Override
    public void mutat(double inHealth, PSOParticle[] inParticles) throws Exception {
        
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
        {
            if (((i + n_mutats) % PSOConstants.NEIGHBORHOOD_SIZE) == 0)
            {
                double[]    pos = null;
                double[]    velo = null;
                
                //inParticles[i].getX().randomize();
                vnd.execute(init, ref, inParticles[i].getX().getAutomata().getRules(), inParticles[i].getX().getAutomata().getStep());
                velo = inParticles[i].getVelo().getData();
                
                pos = inParticles[i].getX().getData();
                inParticles[i].getXp().copyContentFrom(inParticles[i].getX());
                
                inParticles[i].getP().reset();
                
                pos[513] = RandomManager.getInstance().getDoubleIn(PSOConstants.POS_MIN, PSOConstants.POS_MAX);
                
                for (int j = 0 ; j < 512 ; j++)
                {    
                    pos[j] = RandomManager.getInstance().getDoubleIn(vnd.getRules().get(j) * pos[513], ((vnd.getRules().get(j) + 1) * pos[513] - 0.01));
                    velo[j] = pos[j] - inParticles[i].getXp().getData()[j];
                }
                
                velo[513] = pos[513] -  inParticles[i].getXp().getData()[513];
            }
            else
            {
                /*
                inParticles[i].getXp().copyContentFrom(inParticles[i].getX());
                
                inParticles[i].getP().reset();
                
                inParticles[i].getX().getData()[513] = RandomManager.getInstance().getDoubleIn(PSOConstants.POS_MIN, PSOConstants.POS_MAX);
                inParticles[i].getX().getData()[514] = RandomManager.getInstance().getDoubleIn(PSOConstants.POS_MIN, PSOConstants.POS_MAX);
                
                
                inParticles[i].getVelo().getData()[513] = inParticles[i].getX().getData()[513] -  inParticles[i].getXp().getData()[513];
                inParticles[i].getVelo().getData()[514] = inParticles[i].getX().getData()[514] -  inParticles[i].getXp().getData()[514];
                */
                //inParticles[i].getX().nullify();
            }
            n_mutats++;
        }
        mutat_time *= 1.6;
        unhealthy_it = 0;
    }

    @Override
    public boolean doUpdatePos() {
        return (false);
    }

    @Override
    public boolean enabled(double inHealth, int inIteration) {

        if (inHealth <= 0.5)
            unhealthy_it++;
        
        return ((unhealthy_it > mutat_time));
    }
}
