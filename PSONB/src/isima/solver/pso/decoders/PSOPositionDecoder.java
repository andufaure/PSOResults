/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.decoders;

import isima.solver.pso.PSOParticle;
import isima.solver.pso.PSOPosition;

/**
 *
 * @author onio
 */
public interface PSOPositionDecoder {
    
    public void                 reset();
    public PSOPositionDecoder   buildCopy()                     throws Exception;
    public void                 execute(PSOPosition inPosition) throws Exception;
    public void                 copyContentFrom(PSOPositionDecoder inDecoder)   throws Exception;
    public String               toDescString();
}
