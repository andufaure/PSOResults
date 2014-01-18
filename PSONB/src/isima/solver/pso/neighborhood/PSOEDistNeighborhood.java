/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.neighborhood;

import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOParticle;
import isima.solver.pso.PSOVector;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author onio
 */
public class PSOEDistNeighborhood implements PSONeighborhood {

    public static final int NEIGHBORHOOD_SIZE = 5;
    
    private double[]                            distances = new double[PSOConstants.N_PARTICLES * PSOConstants.N_PARTICLES];
    private List<Map<Double, List<Integer> > >  distancesByParticle = new ArrayList<Map<Double, List<Integer>>>();
    
    private Set<Integer>    calced = new TreeSet<Integer>();
    
    public PSOEDistNeighborhood()
    {
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
            distancesByParticle.add(new TreeMap<Double, List<Integer>>());
    }
    
    
    private void resetDistancesByParticle()
    {
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
            distancesByParticle.get(i).clear();
    }
    
    
    private void add(int from, double distance, int to)
    {
        Map<Double, List<Integer> > multimap = distancesByParticle.get(from);
        
        if (!multimap.containsKey(distance))
            multimap.put(distance, new LinkedList<Integer>());
        
        multimap.get(distance).add(to);
    }   
    
    private void updateDistances(PSOParticle[] inParticles)
    {
        calced.clear();
        resetDistancesByParticle();
        
        for (int i = 0 ; i < inParticles.length ; i++)
            for (int j = 0 ; j < inParticles.length ; j++)
            {
                PSOParticle from = inParticles[i];
                PSOParticle to = inParticles[j];
                
                int subindex = (PSOConstants.N_PARTICLES * from.Id + to.Id);
                int subindex_r = (PSOConstants.N_PARTICLES * to.Id + from.Id);
                
                if (!calced.contains(subindex))
                {
                    if (i != j)
                    {
                        distances[subindex_r] = distances[subindex] = PSOVector.getEuclideanDistance(from.getX(), to.getX());
                        
                        this.add(from.Id, distances[subindex], to.Id);
                        this.add(to.Id, distances[subindex], from.Id);
                        calced.add(subindex_r);
                    }
                    calced.add(subindex);
                }
            }
        
        //System.out.println(calced.size());
    }

    @Override
    public void reset() {
    }
    
    @Override
    public void init(PSOParticle[] inParticles) {
    }
    
        
    @Override
    public void calc(double inHealth, PSOParticle[] inParticles) {
        
        
        this.updateDistances(inParticles);
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
        {
            Map<Double, List<Integer> > multimap = distancesByParticle.get(i);
            int                         neighb_count = 0;
            
            inParticles[i].clearNeighbors();
            for (List<Integer> l : multimap.values())
            {
                for (Integer to : l)
                {
                    if (neighb_count >= NEIGHBORHOOD_SIZE)
                        break;
                    
                    inParticles[i].addNeighbor(inParticles[to]);
                    neighb_count++;
                }
                
                if (neighb_count >= NEIGHBORHOOD_SIZE)
                    break;
            }
            
            multimap.clear();
        }
        
    }
    
}
