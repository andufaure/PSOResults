/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bpso;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author onio
 */
public class BPSOPositionDecoderWorker implements Runnable {

    private final LinkedList<BPSOPosition>   toDecode = new LinkedList<BPSOPosition>();
    private final LinkedList<BPSOPosition>   decoded = new LinkedList<BPSOPosition>();
    
    public BPSOPositionDecoderWorker()
    {
    }
    
    public void prepare(List<BPSOParticle> inParticles) throws Exception {
        
        if (inParticles == null)
            throw new Exception("inPositions given is null");
        
        synchronized (this.toDecode)
        {
            toDecode.clear();
        
            for (BPSOParticle p : inParticles)
                toDecode.add(p.getX());
        }
    }
    
    
    @Override
    public void run() {
        
        try
        {
            while (true)
            {
                decoded.clear();

                while (this.toDecode.size() != 0)
                    decodeSingle();
                
                synchronized(this.toDecode)
                {
                    this.toDecode.clear();
                }
                
                Thread.sleep(50);
            }
            
            //toDecode.clear();
        }
        catch(Exception e)
        {
            System.err.println("sub thread exception : " + e.toString());
            e.printStackTrace(System.err);
        }
        
    }
    
    public void decodeSingle() throws Exception
    {
        if (this.toDecode.size() == 0)
            return;
        
        BPSOPosition pos = this.toDecode.getFirst();
       
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
    
    public synchronized BPSOPosition nextDecoded()
    {
        BPSOPosition d = null;
        
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
