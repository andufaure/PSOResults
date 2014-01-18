/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.decoders.blockops;

/**
 *
 * @author onio
 */
public class BlockOpRor extends BlockOp {

    @Override
    public byte execute(byte inRef, double inArg, int inSize) {
        byte nr = decodeArg(inArg, inSize);
        
        nr %= 4;
        
        
        byte val = inRef;
        
        for (int i = 0 ; i < nr ; i++)
        {
            byte l = (byte)(val & 1);
            val <<= 1;
            val |= l >> 3;
        }
        
        return val;
    }
    
}
