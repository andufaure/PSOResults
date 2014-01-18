/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bpso;

import isima.cac.Matrix;
import isima.solver.bpso.decoders.BPSOPositionDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author onio
 */
public final class BPSOParticle {
    
    public static       int    seedId = 0;
    
    public  final       int    Id;
    
    private final       BPSOPosition  xp;  // previous position
    private final       BPSOPosition  x;   // current position
    private final       BPSOPosition  p;   // personnal best position
    private final       BPSOPosition  l;   // local best position
    private final       BPSOPosition  g;   // global best position
    
    private final       BPSOVector    velo = new BPSOVector();
    
    private             int           step = -1;
    
    
    private Map<Double, LinkedList<BPSOParticle> >   neighbors;
    private List<Integer>                            neighborhood;
    
    
    public BPSOParticle(    Matrix inMatrix, 
                            BPSOPosition inG, 
                            BPSOPositionDecoder inRefDecoder) throws Exception {
        
        Id = seedId++;
        g = inG;
        x = new BPSOPosition(inMatrix, inRefDecoder);
        p = new BPSOPosition(inMatrix, inRefDecoder);
        l = new BPSOPosition(inMatrix, inRefDecoder);
        xp = new BPSOPosition(inMatrix, inRefDecoder);
        neighbors = new TreeMap<Double, LinkedList<BPSOParticle>>();
        neighborhood = new LinkedList<Integer>();
        
        
        this.reset();
    }
    
    public void reset()
    {
        step = 0;
        neighbors.clear();
        neighborhood.clear();
        
        for (int i = 0 ; i < BPSOConstants.NEIGHBORHOOD_SIZE ; i++)
        {            
            //System.out.println("PARTICLE " + Id);
            //System.out.println(((Id + (i << 1)) - 1 + BPSOConstants.N_PARTICLES) % BPSOConstants.N_PARTICLES);
            //System.out.println(((Id + (i << 1) + 1 ) - 1 + BPSOConstants.N_PARTICLES) % BPSOConstants.N_PARTICLES);
            //neighborhood.add(((Id + (i << 1)) - 1 + BPSOConstants.N_PARTICLES) % BPSOConstants.N_PARTICLES);
            //neighborhood.add(((Id + ((i << 1) + 1)) + 1 + BPSOConstants.N_PARTICLES) % BPSOConstants.N_PARTICLES);
            //k++;
            
            neighborhood.add((Id + i + 1) % BPSOConstants.N_PARTICLES);
        }
        
        x.randomize();
        xp.reset();
        p.reset();
        l.reset();
        velo.nullify();
    }
    
    public BPSOPosition getL() {
        return l;
    }

    public BPSOPosition getP() {
        return p;
    }

    public BPSOPosition getX() {
        return x;
    }

    public BPSOVector getVelo() {
        return velo;
    }
    
    
    
    public void clearNeighbors() {
        
        for (List<BPSOParticle> ln : neighbors.values())
            ln.clear();
        
        neighbors.clear();
    }
    
    public void addNeighbor(double inDistance, BPSOParticle inParticle) {
        
        if (neighbors.containsKey(inDistance))
            neighbors.get(inDistance).add(inParticle);
        else
        {
            LinkedList<BPSOParticle> ll = null;
            neighbors.put(inDistance, (ll = new LinkedList<BPSOParticle>()));
            ll.add(inParticle);
        }
    }
    
    public BPSOPosition updatePPosition() throws Exception {
        
        if (x.getFitness() < p.getFitness())
        {
            p.copyContentFrom(x);
            return p;
        }
        
        return null;
    }
    
    public void updateLPosition(BPSOParticle particles[]) throws Exception {
        
        BPSOParticle    best_neighb = null;
        int             k = 0;
        do
        {
            best_neighb = particles[neighborhood.get(k)];
            k++;
        }
        while (best_neighb == this);
        
                    
        for (Integer i : neighborhood)
        {
            //System.out.println(i);
            BPSOParticle n = particles[i];
            
            if (n == this)
                continue;
            
            if (best_neighb == n)
                continue;
            else if (best_neighb.p.getFitness() > n.p.getFitness())
                best_neighb = n;
        }
        
        l.copyContentFrom(best_neighb.p);
    }
    
    public void     updateCurrentPosition(  double inInertia,
                                            double inGFactor,
                                            double inPFactor,
                                            double inLFactor) throws Exception
    {
        xp.copyContentFrom(x);
        
        for (int i = 0 ; i < BPSOConstants.N_DIMS ; i++)
        {
            byte xd = x.getData()[i];
            byte v = velo.getData()[i];
            
            byte icompo = 0;
            byte gcompo = 0;
            byte pcompo = 0;
            byte lcompo = 0;
            
            icompo = v;
            icompo &= 1;
            
            gcompo = (byte)(~(g.getData()[i] ^ (xd & 1)) & 1);
            //gcompo &= 1;
            gcompo = (byte)Math.round(inGFactor * gcompo);
            //gcompo &= 1;
            
            if (gcompo > 1)
                gcompo = 1;
            
            pcompo = (byte)(~(p.getData()[i] ^ (xd & 1)) & 1);
            //pcompo &= 1;
            pcompo = (byte)Math.round(inPFactor * pcompo);
            //pcompo &= 1;
            
            if (pcompo > 1)
                pcompo = 1;
            
            lcompo = (byte)(~(l.getData()[i] ^ (xd & 1)) & 1);
            //lcompo &= 1;
            lcompo = (byte)Math.round(inLFactor * lcompo);
            //lcompo &= 1;
            
            if (lcompo > 1)
                lcompo = 1;
            
            
            v = (byte)(  (icompo ^ gcompo)
                       ^ (icompo ^ pcompo)
                       ^ (icompo ^ lcompo)
                       ^ (gcompo ^ lcompo)
                       ^ (gcompo ^ pcompo)
                       ^ (lcompo ^ pcompo));
            
            
            v = (byte)(icompo ^ gcompo ^ pcompo ^ lcompo);
            v &= 1;
            xd ^= v;
            xd &= 1;
            
            velo.getData()[i] = v;
            x.getData()[i] = xd;
        }
    }
}
