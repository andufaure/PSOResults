/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.decoders.blockops;

import isima.solver.pso.PSOConstants;

/**
 *
 * @author onio
 */
public abstract class BlockOp {
    
    // inSize = 4
    public static byte decodeArg(double inArg, int inSize) {
        
        return (byte)Math.floor(inArg 
                            / ((PSOConstants.POS_MAX - PSOConstants.POS_MIN) 
                                / Math.pow(2, (double)inSize)));
    }
    
    public abstract byte execute(byte inRef, double inArg, int inSize);
}
