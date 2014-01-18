/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bpso;

import isima.solver.pso.*;
import isima.solver.pso.criterion.CellDiffCriterion;
import isima.solver.pso.criterion.Criterion;
import isima.cac.Matrix;
import isima.random.RandomManager;
import isima.solver.bpso.criterion.BCellDiffCriterion;
import isima.solver.bpso.criterion.BCriterion;
import isima.solver.bpso.decoders.BPSOPositionDecoder;
import isima.solver.bpso.decoders.BPSOStdPositionDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author onio
 */
public final class BPSOCore {
    
    private BPSOPositionDecoderWorker[]    decoders = new BPSOPositionDecoderWorker[BPSOConstants.NTHREAD_DECODE];
    private BPSOParticle[]           particles = new BPSOParticle[BPSOConstants.N_PARTICLES];
    private BPSOPosition             gbest = null;
    
    private double[]                distances = new double[BPSOConstants.N_PARTICLES * BPSOConstants.N_PARTICLES];
    private int                     iteration = 0; 
        
    private List<Integer>           toCalc = new LinkedList<Integer>();
    private Set<Integer>            calced = new TreeSet<Integer>();
    
    private List<BPSOParticle>       taken = new LinkedList<BPSOParticle>();
    
    private Object[]                toDecode = new Object[BPSOConstants.NTHREAD_DECODE];
    private Object[]                decoded = new Object[BPSOConstants.NTHREAD_DECODE];
    
    private Thread[]                decoderThreads = new Thread[BPSOConstants.NTHREAD_DECODE];
    
    private HashMap<BPSOPosition, BPSOParticle>   particlesByPosition = new HashMap<BPSOPosition, BPSOParticle>();
    
    private double                  inertia = 0.;
    
    private double                  health = 0.;
    private double                  globalHealth = 0;
    
    private final BCriterion             criterion;
    private final BPSOPositionDecoder    refDecoder;
    
    public BPSOCore(Matrix initMatrix, Matrix refMatrix) throws Exception
    {
        BPSOParticle.seedId = 0;
        BPSOPosition.seedId = 0;
        
        criterion = new BCellDiffCriterion(refMatrix);
        refDecoder = new BPSOStdPositionDecoder(criterion);
        gbest = new BPSOPosition(initMatrix, refDecoder);
        
        for (int i = 0 ; i < BPSOConstants.N_PARTICLES ; i++)
        {
            particles[i] = new BPSOParticle(initMatrix, gbest, refDecoder);
            particlesByPosition.put(particles[i].getX(), particles[i]);
        }
        
        for (int i = 0 ; i < BPSOConstants.NTHREAD_DECODE ; i++)
        {
            decoders[i] = new BPSOPositionDecoderWorker();
            toDecode[i] = new ArrayList<BPSOParticle>();
            decoded[i] = new ArrayList<BPSOParticle>();
            
            if (i != 0)
            {
                decoderThreads[i] = new Thread(decoders[i]);
                decoderThreads[i].start();
            }
        }
        
        reset();
    }
    
    public void reset()
    {
        iteration = 0;
        inertia = BPSOConstants.INERTIA_MAX;
        globalHealth = 1;
        
        
        for (int i = 0 ; i < BPSOConstants.NTHREAD_DECODE ; i++)
        {
            ((ArrayList<BPSOParticle>)toDecode[i]).clear();
            ((ArrayList<BPSOParticle>)decoded[i]).clear();
        }
        
        for (int i = 0 ; i < BPSOConstants.N_PARTICLES ; i++)
            particles[i].reset();
        
        this.gbest.reset();
    }
    
    public void step() throws Exception
    {   
        double  last_best_fitness = 0.;
        
        // 1. prepare sets for threads
        for (int i = 0 ; i < BPSOConstants.NTHREAD_DECODE ; i++)
        {
            ((ArrayList<BPSOParticle>)toDecode[i]).clear();
            
            for (int j = 0 ; j < (BPSOConstants.N_PARTICLES / BPSOConstants.NTHREAD_DECODE) ; j++)
                ((ArrayList<BPSOParticle>)toDecode[i]).add(particles[i * (BPSOConstants.N_PARTICLES / BPSOConstants.NTHREAD_DECODE) + j]);
        }
        
        // from now decoded is empty & toDecode contains 2**n sub set of particles
        
        // 2. decode particles
        
        {
            // prepare
            for (int i = 0 ; i < BPSOConstants.NTHREAD_DECODE ; i++)
                decoders[i].prepare(((List<BPSOParticle>)toDecode[i]));

            calced.clear();
            taken.clear();

            // building distances while construction is done
            // when calced list is built, every particle has been decoded
            // MAIN THREAD CALCS DISTANCES WHILE SUB THREADS DECODE PARTICLES
            while (taken.size() != (BPSOConstants.N_PARTICLES))
            {   
                for (int i = 0 ; i < BPSOConstants.NTHREAD_DECODE ; i++)
                {
                    BPSOPosition pos = null;

                    while ((pos = decoders[i].nextDecoded()) != null)
                        taken.add(particlesByPosition.get(pos));
                }
                
                decoders[0].decodeSingle();
            }
            
            //System.out.println("FINISHED : " + calced.size());
        }
        
        
        
        // calc distances
        for (int i = 0 ; i < BPSOConstants.N_PARTICLES ; i++)
            particles[i].clearNeighbors();
        
        //this.updateDistances();
        
        // 3. update p position
        BPSOPosition best = null;
        
        health = BPSOConstants.N_PARTICLES;
        last_best_fitness = this.gbest.getFitness();
        
        for (int i = 0 ; i < BPSOConstants.N_PARTICLES ; i++)
        {
            BPSOPosition cur = null;
            
            if ((    (cur = particles[i].updatePPosition()) != null)
                &&   (this.gbest.getFitness() > cur.getFitness()))
            {
                best = cur;
                this.gbest.copyContentFrom(best);
            }
            
            if (cur != null)
                health -= 1.;
        }
        
        health /= BPSOConstants.N_PARTICLES;
        globalHealth += (health * 10);
        
        //if (last_best_fitness != this.gbest.getFitness())
        //    globalHealth += 10;
        
        // 4. update gbest position
        if (best != null)
        {
            this.gbest.copyContentFrom(best);
            System.out.println(this.gbest.getAutomata().getRules().toString());
            System.out.println("New GBEST @ " + this.iteration + " - " + this.gbest.getDiff() + " AS:" + this.gbest.getAutomata().getStep());
        }
        
        // update l position
        for (int i = 0 ; i < BPSOConstants.N_PARTICLES ; i++)
            particles[i].updateLPosition(particles);
        
        
        // 5. update current positions
        this.updateXPositions();
        
        inertia -= BPSOConstants.INERTIA_DECR;
        iteration++;
    }
    
    private void updateDistances()
    {
        for (BPSOParticle from : taken)
            for (BPSOParticle to : taken)
            {
                int subindex = (BPSOConstants.N_PARTICLES * from.Id + to.Id);
                int subindex_r = (BPSOConstants.N_PARTICLES * to.Id + from.Id);
                
                if (!calced.contains(subindex))
                {
                    if (from != to)
                    {
                        distances[subindex_r] = distances[subindex] = BPSOVector.getEuclideanDistance(from.getX(), to.getX());
                        
                        from.addNeighbor(distances[subindex], to);
                        to.addNeighbor(distances[subindex], from);
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
        double  g_rnd = rm.getDoubleIn(0., 1.) * BPSOConstants.ACC_G;
        double  p_rnd = rm.getDoubleIn(0., 1.) * BPSOConstants.ACC_P;
        double  l_rnd = rm.getDoubleIn(0., 1.) * BPSOConstants.ACC_L;
        
        for (int i = 0 ; i < BPSOConstants.N_PARTICLES ; i++)
        {
            particles[i].updateCurrentPosition(inertia, 
                                                g_rnd,
                                                p_rnd,
                                                l_rnd);
            
            //System.out.println("X " + particles[i].getX().toString());
            //System.out.println("V " + particles[i].getVelo().toString());
        }
    }

    public BPSOParticle[] getParticles() {
        return particles;
    }

    public double getGlobalHealth() {
        return globalHealth;
    }

    public BPSOPosition getGbest() {
        return gbest;
    }

    public int getIteration() {
        return iteration;
    }
    
    
    
    
    
    
}
