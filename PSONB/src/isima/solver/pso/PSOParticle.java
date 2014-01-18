/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso;

import isima.solver.pso.decoders.PSOPositionDecoder;
import isima.cac.Matrix;
import isima.solver.pso.decoders.PSOCPPNotVNDDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author onio
 */
public final class PSOParticle {
    
    private static      int    seedId = 0;
    
    public  final       int    Id;
    
    private final       PSOPosition  xp;  // previous position
    private final       PSOPosition  x;   // current position
    private final       PSOPosition  p;   // personnal best position
    private final       PSOPosition  l;   // local best position
    private final       PSOPosition  g;   // global best position
    private final       PSOPosition  i;   // instant incr
    
    private final       PSOVector       velo = new PSOVector();
    
    private final       PSOPosition     wp;
    private final       PSOPosition     wl;
    private final       PSOPosition     wg;
    private final       PSOPosition     wi;
    
    private double                      temperature;
    
    private             int          step = -1;
    
    
    private Map<Double, LinkedList<PSOParticle> >   neighbors;
    private List<PSOParticle>                       neighborhood;
    
    
    public PSOParticle(Matrix inMatrix, PSOPosition inG, PSOPosition inWG, PSOPositionDecoder inRefDecoder) throws Exception {
        
        Id = seedId++;
        g = inG;
        wg = inWG;
        
        x = new PSOPosition(inMatrix, inRefDecoder);
        xp = new PSOPosition(inMatrix, inRefDecoder);
        
        p = new PSOPosition(inMatrix, inRefDecoder);
        l = new PSOPosition(inMatrix, inRefDecoder);
        i = new PSOPosition(inMatrix, inRefDecoder);
        
        wp = new PSOPosition(inMatrix, inRefDecoder);
        wl = new PSOPosition(inMatrix, inRefDecoder);
        wi = new PSOPosition(inMatrix, inRefDecoder);
        
        
        neighbors = new TreeMap<Double, LinkedList<PSOParticle>>();
        neighborhood = new LinkedList<PSOParticle>();
        
        
        this.reset();
    }
    
    public void reset()
    {
        step = 0;
        neighbors.clear();
        neighborhood.clear();
        x.randomize();
        xp.nullify();
        p.nullify();
        l.nullify();
        i.nullify();
        velo.nullify();
    }
    
    public PSOPosition getL() {
        return l;
    }

    public PSOPosition getP() {
        return p;
    }

    public PSOPosition getX() {
        return x;
    }
    
    public PSOPosition getI() {
        return this.i;
    }

    public PSOPosition getWg() {
        return wg;
    }

    public PSOPosition getWi() {
        return wi;
    }

    public PSOPosition getWl() {
        return wl;
    }

    public PSOPosition getWp() {
        return wp;
    }
    
    public void clearNeighbors() {
        
        neighborhood.clear();
    }
    
    public void addNeighbor(PSOParticle inParticle) {
        neighborhood.add(inParticle);
    }
    
    public void buildNeighborhoodFromDistanceSet(int inSize)
    {
        neighborhood.clear();
        
        for (LinkedList<PSOParticle> l : neighbors.values())
        {
            for (PSOParticle n : l)
            {
                neighborhood.add(n);
                if (neighborhood.size() == inSize)
                    break;
            }
            
            if (neighborhood.size() == inSize)
                break;
        }
        
        neighbors.clear();
    }
    

    
    public void updateLPosition() throws Exception {
        
        //buildNeighborhoodFromDistanceSet(PSOConstants.NEIGHBORHOOD_SIZE);
        
        PSOParticle best_neighb = neighborhood.get(0);
        
        for (PSOParticle n : neighborhood)
        {
            if (best_neighb == n)
                continue;
            else if (best_neighb.p.getFitness() > n.p.getFitness())
                best_neighb = n;
        }
        
        l.copyContentFrom(best_neighb.p);
    }
    
    public void updateWLPosition() throws Exception {
        PSOParticle best_neighb = neighborhood.get(0);
        
        for (PSOParticle n : neighborhood)
        {
            if (best_neighb == n)
                continue;
            else if (best_neighb.wp.getFitness() < n.wp.getFitness())
                best_neighb = n;
        }
        
        wl.copyContentFrom(best_neighb.wp);
    }
    
    public PSOPosition updatePPosition() throws Exception {
        
        if (x.getFitness() <= p.getFitness())
        {
            p.copyContentFrom(x);
            return p;
        }
        
        return null;
    }
    
    public PSOPosition updateWPPosition() throws Exception {
        
        if (x.getFitness() >= wp.getFitness())
        {
            wp.copyContentFrom(x);
            return wp;
        }
        
        return null;
    }
    
    public void updateIPosition() throws Exception {
        
        double  incrCoeff = Double.MAX_VALUE;
        PSOParticle best_neighb = neighborhood.get(0);
        
        for (PSOParticle n : neighborhood)
        {
            if (best_neighb == n)
                continue;
            else if (incrCoeff > n.getIncrCoefficient())
            {
                best_neighb = n;
                incrCoeff = n.getIncrCoefficient();
            }
        }
        
        i.copyContentFrom(best_neighb.getX());
    }
    
    public void updateWIPosition() throws Exception {
        
        double  incrCoeff = Double.MIN_VALUE;
        PSOParticle best_neighb = neighborhood.get(0);
        
        for (PSOParticle n : neighborhood)
        {
            if (best_neighb == n)
                continue;
            else if (incrCoeff < n.getIncrCoefficient())
            {
                best_neighb = n;
                incrCoeff = n.getIncrCoefficient();
            }
        }
        
        wi.copyContentFrom(best_neighb.getX());
    }
    
    public double   getIncrCoefficient() {
        return (x.getFitness() - xp.getFitness());
    }
    
    
    public void     updateCurrentPosition(  double inInertia,
                                            double inGFactor,
                                            double inPFactor,
                                            double inLFactor,
                                            double inIFactor) throws Exception
    {
        if (this.x.getDecoder() instanceof PSOCPPNotVNDDecoder)
        {
            recodeVelocityFromCustomPositionEncoding();
            return;
        }
        //System.out.println("COEFFICIENT INSTANT : " + (x.getFitness() - xp.getFitness()));
        
        xp.copyContentFrom(x);
        
        for (int i = 0 ; i < PSOConstants.N_DIMS ; i++)
        {
            double  xd = 0.;
            double  v = 0.;
            
            
            v =        inInertia * velo.getData()[i]
                   +   inGFactor * (g.getData()[i] - (xd = x.getData()[i]))
                   +   inPFactor * (p.getData()[i] - xd)
                   +   inLFactor * (l.getData()[i] - xd)
                   +   inIFactor * (this.i.getData()[i] - xd);
            
            if (v < -PSOConstants.VELO_MAX)
                v = -PSOConstants.VELO_MAX;
            
            if (v > PSOConstants.VELO_MAX)
                v = PSOConstants.VELO_MAX;
            
            
            
            velo.getData()[i] = v;
            
            
            
            xd += v;
            
            if (xd < PSOConstants.POS_MIN)
            {
                v = (PSOConstants.POS_MIN - xd);
                xd = PSOConstants.POS_MIN;
                v = -v;

                velo.getData()[i] = v;
            }
            
            if (xd > PSOConstants.POS_MAX)
            {
                v = (PSOConstants.POS_MAX - xd);
                xd = PSOConstants.POS_MAX;
                v = -v;
                
                velo.getData()[i] = v;
            }
            
            
            
            x.getData()[i] = xd;
        }
        
        
    }
    
    public void     updateCurrentPositionBetter(  double inInertia,
                                                  double inGFactor,
                                                  double inPFactor,
                                                  double inLFactor,
                                                  double inIFactor) throws Exception
    {
        if (this.x.getDecoder() instanceof PSOCPPNotVNDDecoder)
        {
            recodeVelocityFromCustomPositionEncoding();
            return;
        }
        //System.out.println("COEFFICIENT INSTANT : " + (x.getFitness() - xp.getFitness()));
        
        xp.copyContentFrom(x);
        
        for (int i = 0 ; i < PSOConstants.N_DIMS ; i++)
        {
            double  xd = 0.;
            double  v = 0.;
            
            
            v =        inInertia * velo.getData()[i]
                   +   inGFactor * (g.getData()[i] - (xd = x.getData()[i]))
                   +   inPFactor * (p.getData()[i] - xd)
                   +   inLFactor * (l.getData()[i] - xd)
                   +   inIFactor * (this.i.getData()[i] - xd);
            
            if (v < -PSOConstants.VELO_MAX)
                v = -PSOConstants.VELO_MAX;
            
            if (v > PSOConstants.VELO_MAX)
                v = PSOConstants.VELO_MAX;
            
            
            
            velo.getData()[i] = v;
            
            
            
            xd += v;
            
            if (xd < PSOConstants.POS_MIN)
            {
                v = (PSOConstants.POS_MIN - xd);
                xd = PSOConstants.POS_MIN;
                v = -v;

                velo.getData()[i] = v;
            }
            
            if (xd > PSOConstants.POS_MAX)
            {
                v = (PSOConstants.POS_MAX - xd);
                xd = PSOConstants.POS_MAX;
                v = -v;
                
                velo.getData()[i] = v;
            }
            
            
            
            x.getData()[i] = xd;
        }
        
        
    }
    

    public void     updateCurrentPositionWorse(  double inInertia,
                                                 double inGFactor,
                                                 double inPFactor,
                                                 double inLFactor,
                                                 double inIFactor) throws Exception
    {
        if (this.x.getDecoder() instanceof PSOCPPNotVNDDecoder)
        {
            recodeVelocityFromCustomPositionEncoding();
            return;
        }
        //System.out.println("COEFFICIENT INSTANT : " + (x.getFitness() - xp.getFitness()));
        
        xp.copyContentFrom(x);
        
        for (int i = 0 ; i < PSOConstants.N_DIMS ; i++)
        {
            double  xd = 0.;
            double  v = 0.;
            
            
            v =        inInertia * velo.getData()[i]
                   +   inGFactor * (wg.getData()[i] - (xd = x.getData()[i]))
                   +   inPFactor * (wp.getData()[i] - xd)
                   +   inLFactor * (wl.getData()[i] - xd)
                   +   inIFactor * (wi.getData()[i] - xd);
            
            if (v < -PSOConstants.VELO_MAX)
                v = -PSOConstants.VELO_MAX;
            
            if (v > PSOConstants.VELO_MAX)
                v = PSOConstants.VELO_MAX;
            
            
            
            velo.getData()[i] = v;
            
            
            
            xd += v;
            
            if (xd < PSOConstants.POS_MIN)
            {
                v = (PSOConstants.POS_MIN - xd);
                xd = PSOConstants.POS_MIN;
                v = -v;

                velo.getData()[i] = v;
            }
            
            if (xd > PSOConstants.POS_MAX)
            {
                v = (PSOConstants.POS_MAX - xd);
                xd = PSOConstants.POS_MAX;
                v = -v;
                
                velo.getData()[i] = v;
            }
            
            
            
            x.getData()[i] = xd;
        }
        
        
    }
    
    public PSOVector getVelo() {
        return velo;
    }

    private void recodeVelocityFromCustomPositionEncoding() {
        
        double  v;
        
        for (int i = 0 ; i < PSOConstants.N_DIMS ; i++)
        {
            v = x.getData()[i] - xp.getData()[i];
            
            if (v < -PSOConstants.VELO_MAX)
                v = -PSOConstants.VELO_MAX;
            
            if (v > PSOConstants.VELO_MAX)
                v = PSOConstants.VELO_MAX;
            
            velo.getData()[i] = v;
        }
    }

    public PSOPosition getXp() {
        return xp;
    }
    
    
    
}
