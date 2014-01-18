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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author onio
 */
public class PSOBlockDecoder implements PSOPositionDecoder {
    
    public static final int BLOCK_SIZE = 4;
    public static final int NB_BLOCKS = 512 / BLOCK_SIZE;
    public static final int STEP_DATA_LENGTH = 100;
    
    public static final int BLOCK_DATA_START = 0;
    public static final int BLOCK_DATA_END = BLOCK_DATA_START + NB_BLOCKS;
    
    public static final int BLOCK_ORDER_START = BLOCK_DATA_END;
    public static final int BLOCK_ORDER_END = BLOCK_ORDER_START + NB_BLOCKS;
    
    public static final int BLOCK_CPL_START = BLOCK_ORDER_END;
    public static final int BLOCK_CPL_END = BLOCK_CPL_START + NB_BLOCKS;
    
    public static final int STEP_DATA_START = BLOCK_ORDER_END;
    public static final int STEP_DATA_END = STEP_DATA_START + STEP_DATA_LENGTH;
    
    public static final int N_DIMS = STEP_DATA_END;
    
    
    private transient byte[]                          ruleBlocks = new byte[BLOCK_SIZE * NB_BLOCKS];
    private transient TreeMap<Double, List<Integer>>  orders = new TreeMap<Double, List<Integer>>();
    private int                             decode_cnt = 0;
    private Criterion                       criterion = null;
    
    
    public PSOBlockDecoder(Criterion inCriterion) throws Exception {
        Arrays.fill(ruleBlocks, (byte)0);
        criterion = inCriterion.buildCopy();
    }
    
    public PSOBlockDecoder(PSOBlockDecoder inStrat) throws Exception {
        criterion = inStrat.criterion.buildCopy();
        decode_cnt = inStrat.decode_cnt;
        System.arraycopy(inStrat.ruleBlocks, 0, this.ruleBlocks, 0, 512);
    }
    
    @Override
    public PSOPositionDecoder buildCopy() throws Exception {
        return (new PSOBlockDecoder(this));
    }
    
    @Override
    public void execute(PSOPosition inPosition) throws Exception {
        
        double[]    pos = inPosition.getData();
        M2Auto      automata = inPosition.getAutomata();
        M2Rules     rules = automata.getRules();
        int         step = 0;
        
        // decode blocks
        for (int i = BLOCK_DATA_START ; i < BLOCK_DATA_END ; i++)
            decodeBlock(pos[i], i * BLOCK_SIZE);
        
        
        // calc orders
        
        orders.clear();
        for (int i = BLOCK_ORDER_START ; i < BLOCK_ORDER_END ; i++)
        {
            double  cur;
            
            if (orders.containsKey(cur = pos[i]))
                orders.get(cur).add(i - BLOCK_ORDER_START);
            else
            {
                List<Integer>   l = null;
                
                orders.put(cur, l = new LinkedList<Integer>());
                l.add(i - BLOCK_ORDER_START);
            }
        }
        
        
        // generate rules
        int rule_count = 0;
        
        for (List<Integer> ll : orders.values())
            for (Integer bid : ll)
                for (int i = 0 ; i < BLOCK_SIZE ; i++)
                    rules.set(rule_count++, ruleBlocks[bid * BLOCK_SIZE + i]);
        
        // build step
        step = decodeStep(pos);
        decode_cnt++;
        
        automata.reset();
        automata.run(step);
        criterion.calc(inPosition);
    }

    protected void decodeBlock(double inValue, int inIndex) throws Exception
    {   
        int val = (int)Math.floor(inValue 
                            / ((PSOConstants.POS_MAX - PSOConstants.POS_MIN) 
                                / (double)BLOCK_SIZE / 2));

        //System.out.printf("%x\n", val);
        
        for (int i = 0 ; i < BLOCK_SIZE ; i++)
        {
            this.ruleBlocks[inIndex + i] = (byte)((val >> i) & 1);
            //System.out.print(this.ruleBlocks[inIndex + i]);
        }
        //System.out.println();
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
        
        if (!(inDecoder instanceof PSOBlockDecoder))
            throw new Exception("not a PSOBlockDecoder given");
        
        PSOBlockDecoder bdec = (PSOBlockDecoder)inDecoder;
        criterion.copyContentFrom(bdec.criterion);
        decode_cnt = bdec.decode_cnt;
    }

    @Override
    public String toDescString() {
        return ("Block Decoder");
    }
    
}
