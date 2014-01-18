/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.decoders.blockops;

/**
 *
 * @author onio
 */
public class BlockOpNot extends BlockOp {

    @Override
    public byte execute(byte inRef, double inArg, int inSize) {
        return (byte)(~inRef & 15);
    }
    
    
    
}
