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
public class BlockOpMutat extends BlockOp {

    // 0b0000
    
    // 0b1000
    // 0b0100
    // 0b0010
    // 0b0001
    
    // 0b1100
    // 0b0110
    // 0b0101
    // 0b1010
    // 0b1001
    // 0b0011
    
    // 0b1110
    // 0b0111
    // 0b1011
    // 0b1101
    
    // 0b1111
    private int  sizes[] = new int[]{1, 4, 6, 4, 1};
    private int  data[][] = new int[][]{
        { 0 },
        { 8, 4, 2, 1 },
        { 12, 6, 5, 10, 9, 3 },
        { 14, 7, 11, 13 },
        { 15 }
    };
    
    public BlockOpMutat()
    {}
    
    @Override
    public byte execute(byte inRef, double inArg, int inSize) {
        
        double ent = Math.floor(inArg);
        double dec = inArg - ent;
        
        int index_mutat = (int)Math.floor(ent * (5 / (Math.floor(PSOConstants.POS_MAX) - Math.floor(PSOConstants.POS_MIN))));

        if (index_mutat > 4)
            index_mutat = 4;
        int subindex = (int)Math.floor(dec * sizes[index_mutat]);
        
        return (byte)((inRef ^ data[index_mutat][subindex]) & 15);
    }
    
    
    
}
