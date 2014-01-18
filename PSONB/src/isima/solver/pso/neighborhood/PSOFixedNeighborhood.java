/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.neighborhood;

import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOParticle;

/**
 *
 * @author onio
 */
public class PSOFixedNeighborhood implements PSONeighborhood {
    
    public static final int     NEIGHBORHOOD_SIZE = 5;
    
    
    @Override
    public void reset() {
    }

    @Override
    public void init(PSOParticle[] inParticles) {
        
        int     rest;
        int     nsize;
        int     ring = 0;
        
        nsize = NEIGHBORHOOD_SIZE;
        while ((rest = (PSOConstants.N_PARTICLES % nsize)) != 0)
            nsize--;
        
        if (nsize != NEIGHBORHOOD_SIZE)
            System.out.println("WARNING : using NEIGHBORHOOD_SIZE=" + nsize);
        
        ring = (PSOConstants.N_PARTICLES / nsize);
        
        for (int i = 0 ; i < ring ; i++)
        {
            for (int j = 0 ; j < nsize ; j++)
            {
                inParticles[i * nsize + j].clearNeighbors();
                System.out.print(i * nsize + j + " :");
                for (int k = 0 ; k < nsize ; k++)
                {
                    if (j == k)
                        continue;
                    
                    System.out.print(i * nsize + k + ",");
                    inParticles[i * nsize + j].addNeighbor(inParticles[i * nsize + k]);
                }
                System.out.println();
            }
        }
        
    }

    @Override
    public void calc(double inHealth, PSOParticle[] inParticles) {
    }
}
