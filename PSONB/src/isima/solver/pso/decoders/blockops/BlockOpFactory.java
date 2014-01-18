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
public class BlockOpFactory {
    
    public static final int N_OPS = 1;
    
    private static BlockOpFactory instance = null;
    
    private BlockOp[]   ops = new BlockOp[N_OPS];
    
    public static BlockOpFactory getInstance()
    {
        if (instance == null)
            instance = new BlockOpFactory();
        
        return (instance);
    }
    
    
    private BlockOpFactory()
    {
        /*
        ops[0] = new BlockOpMutat();
        ops[1] = new BlockOpNot();
        ops[2] = new BlockOpRol();
        ops[3] = new BlockOpXor();
        ops[4] = new BlockOpAnd();
        ops[5] = new BlockOpOr();
        ops[6] = new BlockOpRor();
        */
        
        /*
        ops[0] = new BlockOpXor();
        ops[1] = new BlockOpRol();
        ops[2] = new BlockOpAnd();
        ops[3] = new BlockOpOr();
        */
        
        /*
        ops[0] = new BlockOpOr();
        ops[1] = new BlockOpAnd();
        */
        ops[0] = new BlockOpMutat();
    }
    
    public BlockOp  getOp(double inArg)
    {
        int index = (int)Math.floor(inArg * (N_OPS / (PSOConstants.POS_MAX - PSOConstants.POS_MIN)));
        
        if (index >= N_OPS)
            index = N_OPS - 1;
        
        return ops[index];
    }
}
