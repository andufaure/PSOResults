/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.decoders;

import isima.cac.M2Auto;
import isima.cac.M2Rules;
import isima.solver.pso.criterion.Criterion;
import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOPosition;
import isima.solver.pso.decoders.blockops.BlockOpFactory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author onio
 */
public class PSOBlockOpDecoder implements PSOPositionDecoder {
    
    public static final int BLOCK_SIZE = 4;
    public static final int BLOCKOP_LENGTH = 2;
    
    public static final int NB_BLOCKOPS = (512 / BLOCK_SIZE);
    
    public static final int STEP_DATA_LENGTH = 100;
    
    public static final int BLOCK_SEED = 0;
    public static final int BLOCKOP_START = 1;
    public static final int BLOCKOP_END = BLOCKOP_START + NB_BLOCKOPS * BLOCKOP_LENGTH;
    
    public static final int STEP_DATA_START = BLOCKOP_END;
    public static final int STEP_DATA_END = STEP_DATA_START + STEP_DATA_LENGTH;
    
    public static final int N_DIMS = STEP_DATA_END;
    
    public static final BlockOpFactory  opFactory = BlockOpFactory.getInstance();
    
    private transient byte[]                          ruleBlocks = new byte[512];
    private transient TreeMap<Double, List<Integer>>  orders = new TreeMap<Double, List<Integer>>();
    private int                             decode_cnt = 0;
    private Criterion                       criterion = null;
    
    
    public PSOBlockOpDecoder(Criterion inCriterion) throws Exception {
        Arrays.fill(ruleBlocks, (byte)0);
        criterion = inCriterion.buildCopy();
    }
    
    public PSOBlockOpDecoder(PSOBlockOpDecoder inStrat) throws Exception {
        criterion = inStrat.criterion.buildCopy();
        decode_cnt = inStrat.decode_cnt;
        System.arraycopy(inStrat.ruleBlocks, 0, this.ruleBlocks, 0, 512);
    }
    
    @Override
    public PSOPositionDecoder buildCopy() throws Exception {
        return (new PSOBlockOpDecoder(this));
    }
    
    @Override
    public void execute(PSOPosition inPosition) throws Exception {
        
        double[]    pos = inPosition.getData();
        M2Auto      automata = inPosition.getAutomata();
        M2Rules     rules = automata.getRules();
        int         step = 0;
        byte        seed = 0;
        byte        block = 0;
        double      op;
        double      arg;
        
        // decode seed
        seed = decodeSeedBlock(pos[BLOCK_SEED]);
        
        for (int i = 0 ; i < NB_BLOCKOPS ; i++)
        {
            op = pos[BLOCKOP_START + i * BLOCKOP_LENGTH];
            arg = pos[BLOCKOP_START + (i * BLOCKOP_LENGTH) + 1];
            
            block = this.opFactory.getOp(op).execute(seed, arg, BLOCK_SIZE);
            this.injectBlock(rules, block, i * BLOCK_SIZE);
        }
        
        step = this.decodeStep(pos);
        automata.reset();
        automata.run(step);
        criterion.calc(inPosition);
    }

    protected byte decodeSeedBlock(double inValue) throws Exception
    {   
        return ((byte)Math.floor(inValue 
                            / ((PSOConstants.POS_MAX - PSOConstants.POS_MIN) 
                                / (double)BLOCK_SIZE / 2)));
    }
    
    protected void injectBlock(M2Rules inRules, byte inBlock, int inIndex) throws Exception
    {
        for (int i = BLOCK_SIZE - 1 ; i > 0 ; i--)
            inRules.set(inIndex + ((BLOCK_SIZE - 1) - i), (byte)((inBlock >> i) & 1));
    }
    
    protected int decodeStep(double[] pos)
    {
        double steps_r = 0;
        
        steps_r = pos[STEP_DATA_START
                        + (decode_cnt 
                            % 
                            (STEP_DATA_END 
                            - STEP_DATA_START))];
        
        return ((int)Math.round(PSOConstants.AUTO_MIN_STEPS 
                +   steps_r * ((1. * PSOConstants.AUTO_MAX_STEPS - PSOConstants.AUTO_MIN_STEPS) 
                    /   (PSOConstants.POS_MAX - PSOConstants.POS_MIN))));
    }

    @Override
    public void reset() {
        decode_cnt = 0;
    }

    @Override
    public void copyContentFrom(PSOPositionDecoder inDecoder) throws Exception {
        
        if (!(inDecoder instanceof PSOBlockOpDecoder))
            throw new Exception("not a PSOBlockDecoder given");
        
        PSOBlockOpDecoder bdec = (PSOBlockOpDecoder)inDecoder;
        criterion.copyContentFrom(bdec.criterion);
        decode_cnt = bdec.decode_cnt;
    }

    @Override
    public String toDescString() {
        return ("Block Op Decoder");
    }
    
}
