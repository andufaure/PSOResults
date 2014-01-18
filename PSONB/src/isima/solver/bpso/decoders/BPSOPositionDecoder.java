/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bpso.decoders;

import isima.solver.bpso.BPSOPosition;

/**
 *
 * @author onio
 */
public interface BPSOPositionDecoder {
    
    public void                 reset();
    public BPSOPositionDecoder  buildCopy()                      throws Exception;
    public void                 execute(BPSOPosition inPosition) throws Exception;
    public void                 copyContentFrom(BPSOPositionDecoder inDecoder)   throws Exception;
}
