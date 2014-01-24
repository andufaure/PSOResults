/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso;

import isima.solver.pso.criterion.CellDiffCriterion;
import isima.solver.pso.criterion.Criterion;
import isima.solver.pso.decoders.PSOPositionDecoder;
import isima.solver.pso.decoders.PSOBlockDecoder;
import isima.cac.Matrix;
import isima.random.RandomManager;
import isima.solver.pso.criterion.CDLE2Criterion;
import isima.solver.pso.criterion.CDLECriterion;
import isima.solver.pso.decoders.PSOBlockOpDecoder;
import isima.solver.pso.decoders.PSOBlockOpSearchDecoder;
import isima.solver.pso.decoders.PSOCPP2Decoder;
import isima.solver.pso.decoders.PSOCPPDecoder;
import isima.solver.pso.decoders.PSOCPPNotDecoder;
import isima.solver.pso.decoders.PSOCPPNotVNDDecoder;
import isima.solver.pso.decoders.PSONibbleDecoder;
import isima.solver.pso.decoders.PSONibbleRolDecoder;
import isima.solver.pso.mutators.PSOCPPMutator;
import isima.solver.pso.mutators.PSOCPPVNDMutator;
import isima.solver.pso.mutators.PSOMutator;
import isima.solver.pso.mutators.PSONullMutator;
import isima.solver.pso.mutators.PSOStdMutator;
import isima.solver.pso.neighborhood.PSOCircularNeighborhood;
import isima.solver.pso.neighborhood.PSOEDistNeighborhood;
import isima.solver.pso.neighborhood.PSOFixedNeighborhood;
import isima.solver.pso.neighborhood.PSONeighborhood;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.lang.Thread;

/**
 *
 * @author onio
 */
public final class PSOCore {
    
    private PSOPositionDecoderWorker[]      decoders = new PSOPositionDecoderWorker[PSOConstants.NTHREAD_DECODE];
    private PSOParticle[]                   particles = new PSOParticle[PSOConstants.N_PARTICLES];
    private PSOPosition                     gbest = null;
    private PSOPosition                     wgbest = null;
    
    private double[]                distances = new double[PSOConstants.N_PARTICLES * PSOConstants.N_PARTICLES];
    private int                     iteration = 0; 
        
    private List<Integer>           toCalc = new LinkedList<Integer>();
    private Set<Integer>            calced = new TreeSet<Integer>();
    
    private List<PSOParticle>       taken = new LinkedList<PSOParticle>();
    
    private Object[]                toDecode = new Object[PSOConstants.NTHREAD_DECODE];
    private Object[]                decoded = new Object[PSOConstants.NTHREAD_DECODE];
    
    private Thread[]                decoderThreads = new Thread[PSOConstants.NTHREAD_DECODE];
    
    private HashMap<PSOPosition, PSOParticle>   particlesByPosition = new HashMap<PSOPosition, PSOParticle>();
    
    private double                  inertia = 0.;
    private double                  health = 0.;    
    
    private PSOMutator              mutator = null;
    private PSONeighborhood         neighborhood = null;
    
    private final Criterion             criterion;
    private final PSOPositionDecoder    refDecoder;
    
    private long                        startTime;
    
    public PSOCore(Matrix initMatrix, Matrix refMatrix) throws Exception
    {
        criterion = new CellDiffCriterion(refMatrix);
        refDecoder = new PSOCPPNotDecoder(criterion);
        
        mutator = new PSOStdMutator();
        //mutator = new PSOCPPVNDMutator(initMatrix, refMatrix);
        neighborhood = new PSOFixedNeighborhood();
        
        System.out.println(refDecoder.toDescString());
        
        gbest = new PSOPosition(initMatrix, refDecoder);
        wgbest = new PSOPosition(initMatrix, refDecoder);
        
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
        {
            //if ((i % 2) == 0)
            //    particles[i] = new PSOParticle(initMatrix, gbest, speDecoder);
            //else
            particles[i] = new PSOParticle(initMatrix, gbest, wgbest, refDecoder);
            
            particlesByPosition.put(particles[i].getX(), particles[i]);
        }
        
        for (int i = 0 ; i < PSOConstants.NTHREAD_DECODE ; i++)
        {
            decoders[i] = new PSOPositionDecoderWorker();
            toDecode[i] = new ArrayList<PSOParticle>();
            decoded[i] = new ArrayList<PSOParticle>();
        }
        
        reset();
    }
    
    public void reset()
    {
        iteration = 0;
        inertia = PSOConstants.INERTIA_MAX;
        gbest.reset();
        
        for (int i = 0 ; i < PSOConstants.NTHREAD_DECODE ; i++)
        {
            ((ArrayList<PSOParticle>)toDecode[i]).clear();
            ((ArrayList<PSOParticle>)decoded[i]).clear();
        }
        
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
            particles[i].reset();
        
        startTime = System.nanoTime();
        mutator.reset();
        neighborhood.reset();
        neighborhood.init(particles);
    }
    
    public void finish()
    {
        for (int i = 0 ; i < PSOConstants.NTHREAD_DECODE ; i++)
        {            
            if (i != 0)
            {
                decoders[i].stop();
            }
        }
    }
    
    public void step() throws Exception
    {        
        
        //System.out.println("PSO GLOBAL STEP ---------------------------");
        
        // 1. prepare sets for threads
        for (int i = 0 ; i < PSOConstants.NTHREAD_DECODE ; i++)
        {
            ((ArrayList<PSOParticle>)toDecode[i]).clear();
            
            for (int j = 0 ; j < (PSOConstants.N_PARTICLES / PSOConstants.NTHREAD_DECODE) ; j++)
                ((ArrayList<PSOParticle>)toDecode[i]).add(particles[i * (PSOConstants.N_PARTICLES / PSOConstants.NTHREAD_DECODE) + j]);
        }
        
        // from now decoded is empty & toDecode contains 2**n sub set of particles
        
        // 2. decode particles
        
        {
            
            // prepare
            calced.clear();
            taken.clear();
            
            for (int i = 0 ; i < PSOConstants.NTHREAD_DECODE ; i++)
                decoders[i].prepare(((List<PSOParticle>)toDecode[i]));
            
            for (int i = 0 ; i < PSOConstants.NTHREAD_DECODE ; i++)
            {
                decoderThreads[i] = new Thread(decoders[i], "");
                decoderThreads[i].start();
            }
            
            
            for (int i = 0 ; i < PSOConstants.NTHREAD_DECODE ; i++)
            {
                decoderThreads[i].join();
                decoderThreads[i] = null;
            }        
            

            for (int i = 0 ; i < PSOConstants.NTHREAD_DECODE ; i++)
            {
                PSOPosition pos = null;

                while ((pos = decoders[i].nextDecoded()) != null)
                    taken.add(particlesByPosition.get(pos));
            }
        }
        
        
        
        // calc distances
        /*for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
            particles[i].clearNeighbors();
        
        this.updateDistances();
        */
        
        // 3. update special positions (p, l) according to fits
        PSOPosition best = null;
        
        
        health = 0;
        
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
        {
            PSOPosition cur = null;
            
            if ((    (cur = particles[i].updatePPosition()) != null)
                &&   (this.gbest.getFitness() > cur.getFitness()))
            {
                best = cur;
                this.gbest.copyContentFrom(cur);
            }
            
            if (cur != null)
                health += 1;
        }
        
        health /= PSOConstants.N_PARTICLES;
        
        
        // 4. update gbest position
        if (best != null)
        {
            //this.gbest.copyContentFrom(best);
            System.out.println("#");
            System.out.print("It:" + this.iteration + ":Diff:" + this.gbest.getDiff() 
                            + ":PSOStep:" + this.gbest.getAutomata().getStep() + ":Time:" + (double)((1. * System.nanoTime() - startTime) / 1000000000) + ":s");
            System.out.print(":AutomataStep:" + this.gbest.getAutomata().getStep());
            System.out.println(":Transition:" + this.gbest.getAutomata().getRules().toString());
            
            this.gbest.getAutomata().getCurrentMatrix().print();
            health = 1.;
        }
        
        // calc neighborhoods
        neighborhood.calc(health, particles);
        
        // calc L & I
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
        {
            particles[i].updateIPosition();
            particles[i].updateLPosition();
        }
        
        if (mutator.enabled(health, iteration))
        {
            //System.out.println("MUTATION TRIGGERED");
            mutator.mutat(health, particles);
            if (mutator.doUpdatePos())
                this.updateXPositions();
        }
        else
            this.updateXPositions();
        
        iteration++;
        inertia -= PSOConstants.INERTIA_DECR;
    }
    
    private void updateDistances()
    {
        calced.clear();
        
        for (PSOParticle from : taken)
            for (PSOParticle to : taken)
            {
                int subindex = (PSOConstants.N_PARTICLES * from.Id + to.Id);
                int subindex_r = (PSOConstants.N_PARTICLES * to.Id + from.Id);
                
                if (!calced.contains(subindex))
                {
                    if (from != to)
                    {
                        distances[subindex_r] = distances[subindex] = PSOVector.getEuclideanDistance(from.getX(), to.getX());
                        
                        //from.addNeighbor(distances[subindex], to);
                        //to.addNeighbor(distances[subindex], from);
                        calced.add(subindex_r);
                    }
                    
                    calced.add(subindex);
                }
            }
        
        //System.out.println(calced.size());
    }
    
    private void    updateXPositions() throws Exception
    {
        RandomManager   rm = RandomManager.getInstance();
        double  g_rnd = rm.getDoubleIn(0., 1.) * PSOConstants.ACC_G;
        double  p_rnd = rm.getDoubleIn(0., 1.) * PSOConstants.ACC_P;
        double  l_rnd = rm.getDoubleIn(0., 1.) * PSOConstants.ACC_L;
        double  i_rnd = rm.getDoubleIn(0., 1.) * PSOConstants.ACC_I;
        
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
        {
            particles[i].updateCurrentPosition( inertia, 
                                                g_rnd,
                                                p_rnd,
                                                l_rnd, i_rnd);
            
            //System.out.println(particles[i].getX().toString());
        }
        
        for (int i = 0 ; i < PSOConstants.N_PARTICLES ; i++)
            for (int j = 0 ; j < PSOConstants.N_PARTICLES ; j++)
            {
                if (i == j)
                    continue;
                
                double[]  ppos_i = particles[i].getXp().getData();
                double[]  ppos_j = particles[i].getXp().getData();
                double[]  pos_i = particles[i].getX().getData();
                double[]  pos_j = particles[j].getX().getData();
                double[]  velo_i = particles[i].getVelo().getData();
                double[]  velo_j = particles[j].getVelo().getData();
                
                int k = 0;
                
                for (k = 0 ; k < PSOConstants.N_DIMS ; k++)
                {
                    if (Math.abs((pos_i[k] - pos_j[k])) > 0.00001)
                        break;
                    
                }
                
                if (k == PSOConstants.N_DIMS)
                {
                    //System.out.println("COLLISION TRIGGERED");
                    for (int l = 0 ; l < PSOConstants.N_DIMS ; l++)
                    {
                        velo_i[l] = -PSOConstants.VELO_MAX;
                        velo_j[l] = PSOConstants.VELO_MAX;


                        pos_i[l] = ppos_i[l] + velo_i[l];
                        pos_j[l] = ppos_j[l] + velo_j[l];

                        if (pos_i[l] >= PSOConstants.POS_MAX)
                            pos_i[l] = PSOConstants.POS_MAX;

                        if (pos_i[l] <= PSOConstants.POS_MIN)
                            pos_i[l] = PSOConstants.POS_MIN;


                        if (pos_j[l] >= PSOConstants.POS_MAX)
                            pos_j[l] = PSOConstants.POS_MAX;

                        if (pos_j[l] <= PSOConstants.POS_MIN)
                            pos_j[l] = PSOConstants.POS_MIN;
                    }
                }
                
            }
        
    }
}
