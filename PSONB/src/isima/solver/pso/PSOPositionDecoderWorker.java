/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author onio
 */
public class PSOPositionDecoderWorker implements Runnable {

    private final LinkedList<PSOPosition>   toDecode = new LinkedList<PSOPosition>();
    private final LinkedList<PSOPosition>   decoded = new LinkedList<PSOPosition>();
    private boolean finished = false;
    
    
    public PSOPositionDecoderWorker()
    {
        super();
    }
    
    public void prepare(List<PSOParticle> inParticles) throws Exception {
        
        if (inParticles == null)
            throw new Exception("inPositions given is null");
        
        synchronized (this.toDecode)
        {
            toDecode.clear();
        
            for (PSOParticle p : inParticles)
                toDecode.add(p.getX());
        }
    }
    
    
    @Override
    public void run() {
        
        try
        {
            while (!finished)
            {
                decoded.clear();

                while (this.toDecode.size() != 0)
                    decodeSingle();
                
                synchronized(this.toDecode)
                {
                    this.toDecode.clear();
                }
                
                Thread.sleep(35);
            }
            
            //toDecode.clear();
        }
        catch(Exception e)
        {
            System.err.println("sub thread exception : " + e.toString());
            e.printStackTrace(System.err);
        }
    }
    
    public synchronized void stop()
    {
        finished = true;
    }
    
    public void decodeSingle() throws Exception
    {
        if (this.toDecode.size() == 0)
            return;
        
        PSOPosition pos = this.toDecode.getFirst();
       
        pos.decode();
        
        synchronized(this.toDecode)
        {
            toDecode.removeFirst();
        }
        
        synchronized (this.decoded)
        {
            decoded.add(pos);
        }
    }
    
    public synchronized PSOPosition nextDecoded()
    {
        PSOPosition d = null;
        
        synchronized(this.decoded)
        {
            if (this.decoded.size() == 0)
                d = null;
            else
            {
                d = this.decoded.getFirst();
                this.decoded.removeFirst();
            }
        }
        
        return (d);
    }
}
