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
public class PSOCircularNeighborhood implements PSONeighborhood {

    public static final boolean FIXED = true;
    public static final int     NEIGHBORHOOD_SIZE = PSOConstants.NEIGHBORHOOD_SIZE;
    
    
    // == toutes les TRANSITION iterations de la PSO, le voisinage est décalé vers la droite
    //public static final int     TRANSITION = PSOConstants.NEIGHBORHOOD_SIZE;
    public static final int     TRANSITION = 13;
    
    
    public int                  it_cnt = 0;
    public int                  neighb_cnt = 1;
    public int                  transition = TRANSITION;
    
    
    @Override
    public void reset() {
        it_cnt = 0;
        neighb_cnt = 1;
        transition = TRANSITION;
    }

    @Override
    public void init(PSOParticle[] inParticles) {
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
        {
            inParticles[i].clearNeighbors();
            for (int j = 0 ; j < NEIGHBORHOOD_SIZE ; j++)
            {
                inParticles[i].addNeighbor(inParticles[(i + j + neighb_cnt) % PSOConstants.N_PARTICLES]);
            }
        }
    }

    @Override
    public void calc(double inHealth, PSOParticle[] inParticles) {
        
        if (FIXED)
        {
            it_cnt++;
            return;
        }
        
        
        if (it_cnt != 0 && ((it_cnt % transition)) == 0)
        //if (it_cnt > TRANSITION)
        {
            neighb_cnt++;
            //neighb_cnt = neighb_cnt + ((inHealth < 0.5) ? (-1) : 1);
            
            //if (neighb_cnt < 0)
            //    neighb_cnt = (NEIGHBORHOOD_SIZE - 1);
            
            for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
            {
                inParticles[i].clearNeighbors();
                for (int j = 0 ; j < NEIGHBORHOOD_SIZE ; j++)
                {
                    if (i == ((i + j + neighb_cnt) % PSOConstants.N_PARTICLES))
                        continue;
                    
                    inParticles[i].addNeighbor(inParticles[(i + j + neighb_cnt) % PSOConstants.N_PARTICLES]);
                }
            }
            
            //transition *= 1.61803398875;
        }
        
        it_cnt++;
    }
    
    
}
