/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.mutators;

import isima.solver.pso.PSOParticle;

/**
 *
 * @author onio
 */
public class PSONullMutator implements PSOMutator {

    @Override
    public void reset() {
    }
    
    @Override
    public void mutat(double inHealth, PSOParticle[] inParticles) {
    }

    @Override
    public boolean doUpdatePos() {
        return (true);
    }

    @Override
    public boolean enabled(double inHealth, int inIteration) {
        return (false);
    }
    
}
