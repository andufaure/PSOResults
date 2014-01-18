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
public class PSONibbleDecoder implements PSOPositionDecoder {
    
    public static final int BLOCK_SIZE = 4;
    public static final int NB_BLOCKS = 512 / BLOCK_SIZE;
    public static final int NB_BLOCKS_ORDER = (int)Math.pow(2, BLOCK_SIZE);
    public static final int STEP_DATA_LENGTH = 100;
    
    public static final int BLOCK_DATA_START = 0;
    public static final int BLOCK_DATA_END = BLOCK_DATA_START + NB_BLOCKS;
    
    public static final int BLOCK_ORDER_START = BLOCK_DATA_END;
    public static final int BLOCK_ORDER_END = BLOCK_ORDER_START + NB_BLOCKS_ORDER;
    
    public static final int STEP_REF1 = BLOCK_ORDER_END;
    public static final int STEP_REF2 = STEP_REF1 + 1;
    
    public static final int N_DIMS = STEP_REF2 + 1;
    
    private transient byte[]                          codes = new byte[NB_BLOCKS_ORDER];
    
    private transient byte[]                          ruleBlocks = new byte[BLOCK_SIZE * NB_BLOCKS];
    private int                             decode_cnt = 0;
    private Criterion                       criterion = null;
    
    private TreeMap<Double, List<Integer> >           multimap = new TreeMap<Double, List<Integer> >();
    
    public PSONibbleDecoder(Criterion inCriterion) throws Exception {
        Arrays.fill(ruleBlocks, (byte)0);
        criterion = inCriterion.buildCopy();
    }
    
    public PSONibbleDecoder(PSONibbleDecoder inStrat) throws Exception {
        criterion = inStrat.criterion.buildCopy();
        decode_cnt = inStrat.decode_cnt;
        System.arraycopy(inStrat.ruleBlocks, 0, this.ruleBlocks, 0, 512);
    }
    
    @Override
    public PSOPositionDecoder buildCopy() throws Exception {
        return (new PSONibbleDecoder(this));
    }
    
    @Override
    public void execute(PSOPosition inPosition) throws Exception {
        
        double[]    pos = inPosition.getData();
        M2Auto      automata = inPosition.getAutomata();
        M2Rules     rules = automata.getRules();
        int         step = 0;
        
        // build step
        step = decodeStep(pos);
        //decode_cnt++;
        
        
        multimap.clear();
        // build codes table
        for (int i = BLOCK_ORDER_START ; i < BLOCK_ORDER_END ; i++)
        {
            double cur = pos[i];
            
            if (multimap.containsKey(cur))
                multimap.get(cur).add(i - BLOCK_ORDER_START);
            else
            {
                List<Integer>   ll = null;
                multimap.put(cur, ll = new LinkedList<Integer>());
                ll.add(i - BLOCK_ORDER_START);
            }
        }
        
        int k = 0;
        for (List<Integer> l : multimap.values())
        {
            for (Integer n : l)
                codes[k++] = n.byteValue();
            l.clear();
        }
        
        
        // decode blocks
        for (int i = BLOCK_DATA_START ; i < BLOCK_DATA_END ; i++)
            injectBlock(rules, pos[i], (i - BLOCK_DATA_START) * BLOCK_SIZE);
                
        //System.out.println(rules.toString());
        
        automata.reset();
        automata.run(step);
        criterion.calc(inPosition);
    }

    protected void injectBlock(M2Rules inRules, double inValue, int inIndex) throws Exception
    {   
        int index = (int)Math.floor(inValue * (NB_BLOCKS_ORDER / (PSOConstants.POS_MAX - PSOConstants.POS_MIN)));
        
        //System.out.println(index);
        
        if (index > 15)
            index = 15;
        
        byte val = codes[index];
        
        //System.out.println(val);
        
        for (int i = BLOCK_SIZE - 1 ; i > 0 ; i--)
            inRules.set(inIndex + ((BLOCK_SIZE - 1) - i), (byte)((val >> i) & 1));
        //System.out.println();
    }
    
    protected int decodeStep(double[] pos)
    {
        int step = 0;
        
        /*
        double steps_r = 0;
        
        steps_r = pos[STEP_DATA_START
                        + (decode_cnt 
                            % 
                            (STEP_DATA_END 
                            - STEP_DATA_START))];
        
        return ((int)Math.round(PSOConstants.AUTO_MIN_STEPS 
                +   steps_r * ((1. * PSOConstants.AUTO_MAX_STEPS - PSOConstants.AUTO_MIN_STEPS) 
                    /   (PSOConstants.POS_MAX - PSOConstants.POS_MIN))));
         * 
         */
        
        if (pos[STEP_REF1] < pos[STEP_REF2])
        {
            step = (int)(PSOConstants.AUTO_MIN_STEPS
                    + ((pos[STEP_REF2] - pos[STEP_REF1]) / pos[STEP_REF2])
                        * (PSOConstants.AUTO_MAX_STEPS - PSOConstants.AUTO_MIN_STEPS));
        }
        else if (pos[STEP_REF1] > pos[STEP_REF2])
        {
            step = (int)(PSOConstants.AUTO_MIN_STEPS
                    + ((pos[STEP_REF1] - pos[STEP_REF2]) / pos[STEP_REF1])
                        * (PSOConstants.AUTO_MAX_STEPS - PSOConstants.AUTO_MIN_STEPS));
        }
        else
            step = PSOConstants.AUTO_MIN_STEPS + ((PSOConstants.AUTO_MAX_STEPS - PSOConstants.AUTO_MIN_STEPS) / 2);
        
        if (step == 0)
            step++;
        
        return (step);
    }

    @Override
    public void reset() {
        decode_cnt = 0;
    }

    @Override
    public void copyContentFrom(PSOPositionDecoder inDecoder) throws Exception {
        
        if (!(inDecoder instanceof PSONibbleDecoder))
            throw new Exception("not a PSOBlockDecoder given");
        
        PSONibbleDecoder bdec = (PSONibbleDecoder)inDecoder;
        criterion.copyContentFrom(bdec.criterion);
        decode_cnt = bdec.decode_cnt;
    }

    @Override
    public String toDescString() {
        return ("Nibble Decoder");
    }
    
}
