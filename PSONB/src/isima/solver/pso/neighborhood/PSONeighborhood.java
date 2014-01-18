/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.neighborhood;

import isima.solver.pso.PSOParticle;

/**
 *
 * @author onio
 */
public interface PSONeighborhood {
    
    public void     reset();
    public void     init(PSOParticle[] inParticles);
    public void     calc(double inHealth, PSOParticle[] inParticles);
}
