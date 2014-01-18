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
public interface PSOMutator {
    
    void        reset();
    boolean     enabled(double inHealth, int inIteration);
    void        mutat(double inHealth, PSOParticle[] inParticles) throws Exception;
    boolean     doUpdatePos();
}
