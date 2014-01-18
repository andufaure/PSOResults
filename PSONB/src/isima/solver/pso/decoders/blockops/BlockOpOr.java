/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.decoders.blockops;

/**
 *
 * @author onio
 */
public class BlockOpOr extends BlockOp {

    @Override
    public byte execute(byte inRef, double inArg, int inSize) {
        //System.out.println("OR OP : " + decodeArg(inArg, inSize));
        
        return (byte)(inRef | decodeArg(inArg, inSize));
    }

    
}
