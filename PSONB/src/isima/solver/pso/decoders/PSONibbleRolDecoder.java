/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.decoders;

import isima.cac.M2Auto;
import isima.cac.M2Rules;
import isima.random.RandomManager;
import isima.solver.pso.criterion.Criterion;
import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOPosition;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author onio
 */
public class PSONibbleRolDecoder implements PSOPositionDecoder {
    
    public static final int BLOCK_SIZE = 4;
    public static final int NB_BLOCKS = 512 / BLOCK_SIZE;
    public static final int NB_BLOCKS_ORDER = (int)Math.pow(2, BLOCK_SIZE);
    public static final int STEP_DATA_LENGTH = 100;
    
    public static final int BLOCK_DATA_START = 0;                                       // 
    public static final int BLOCK_DATA_END = BLOCK_DATA_START + NB_BLOCKS;              //
    
    public static final int BLOCK_ORDER_START = BLOCK_DATA_END;                         // 16
    public static final int BLOCK_ORDER_END = BLOCK_ORDER_START + NB_BLOCKS_ORDER;      // 16
        
    public static final int BLOCK_ROT_VALUE_REF = BLOCK_ORDER_END;
    public static final int BLOCK_ROT_VALUE = BLOCK_ROT_VALUE_REF + 1;                          // 4
    
    public static final int STEP_REF1 = BLOCK_ROT_VALUE + 1;
    public static final int STEP_REF2 = STEP_REF1 + 1;
    
    public static final int N_DIMS = STEP_REF2 + 1;
    
    public static final int LOCAL_SEARCH_IT_MAX = 0;
    
    private static String   initBlock = null;
    
    private transient byte[]                          codes = new byte[NB_BLOCKS_ORDER];
    
    private transient byte[]                          ruleBlocks = new byte[BLOCK_SIZE * NB_BLOCKS];
    private int                             decode_cnt = 0;
    private Criterion                       criterion = null;
    
    private TreeMap<Double, List<Integer> >           multimap = new TreeMap<Double, List<Integer> >();
    
    private BigInteger                      ruleConcat = null;//new BigInteger(new String("FFFFFFFFFFFFFFFFFFFFFFFFFFFF"), 16);
    private BigInteger                      ruleConcatTmp = null;
    private BigInteger                      tmp = null;
    private BigInteger                      zero = null;
    private BigInteger                      one = null;
    
    public PSONibbleRolDecoder(Criterion inCriterion) throws Exception {
        Arrays.fill(ruleBlocks, (byte)0);
        criterion = inCriterion.buildCopy();
        
        if (initBlock == null)
        {
            initBlock = new String("");
            
            for (int i = 0 ; i < 65 ; i++)
                initBlock += "ff";
        }
        
        ruleConcat = new BigInteger(initBlock, 0x10);
        ruleConcatTmp = ruleConcat.xor(ruleConcat);
        one = new BigInteger(initBlock, 0x10);
        tmp = ruleConcat.xor(ruleConcat);
        zero = ruleConcat.xor(ruleConcat);
        
//        tmp.not();
//        zero.not();
        //System.out.println(tmp.bitLength());
    }
    
    public PSONibbleRolDecoder(PSONibbleRolDecoder inStrat) throws Exception {
        criterion = inStrat.criterion.buildCopy();
        decode_cnt = inStrat.decode_cnt;
        System.arraycopy(inStrat.ruleBlocks, 0, this.ruleBlocks, 0, 512);
        ruleConcat = new BigInteger(initBlock, 0x10);
        one = new BigInteger(initBlock, 0x10);
        ruleConcatTmp = ruleConcat.xor(ruleConcat);
        tmp = ruleConcat.xor(ruleConcat);
        zero = ruleConcat.xor(ruleConcat);
    }
    
    @Override
    public PSOPositionDecoder buildCopy() throws Exception {
        return (new PSONibbleRolDecoder(this));
    }
    
    @Override
    public void execute(PSOPosition inPosition) throws Exception {
        
        double[]    pos = inPosition.getData();
        M2Auto      automata = inPosition.getAutomata();
        M2Rules     rules = automata.getRules();
        int         step = 0;
        double      bestfit;
        double      fitTmp;
        
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
        
        ruleConcat = ruleConcat.and(zero);
        //System.out.println("START RULE CONCAT : " + ruleConcat.toString(2));
        
        // decode blocks
        for (int i = BLOCK_DATA_START ; i < BLOCK_DATA_END ; i++)
            injectBlockInRuleConcat(pos[i], i - BLOCK_DATA_START);
        
        
        //rotRuleConcat(0, 4, pos[BLOCK_ROT_VALUE]);
        rotRuleConcat(0, decodeRotRefValue(pos[BLOCK_ROT_VALUE_REF]), pos[BLOCK_ROT_VALUE]);
        
        
        injectRuleConcat(rules);
        automata.reset();
        automata.run(step);
        criterion.calc(inPosition);
        /*
        ruleConcatTmp = ruleConcatTmp.xor(ruleConcatTmp);
        ruleConcatTmp = ruleConcatTmp.or(ruleConcat);
        fitTmp = inPosition.getFitness();
        
        for (int i = 0 ; i < LOCAL_SEARCH_IT_MAX ; i++)
        {
            for (int j = 0 ; j < 2 ; j++)
            {
                if (i == 0)
                    continue;
                
                notRuleConcat();
                injectRuleConcat(rules);
                automata.reset();
                automata.run(step);
                criterion.calc(inPosition);
                
                if (inPosition.getFitness() < fitTmp)
                {
                    fitTmp = inPosition.getFitness();
                    ruleConcatTmp = ruleConcatTmp.xor(ruleConcatTmp);
                    ruleConcatTmp = ruleConcatTmp.or(ruleConcat);
                }
            }
            
            rotRuleConcat(0, 4, 1);
        }
        
        ruleConcat = ruleConcat.xor(ruleConcat);
        ruleConcat = ruleConcat.or(ruleConcatTmp);
        injectRuleConcat(rules);
        automata.reset();
        automata.run(step);
        criterion.calc(inPosition);
        */
        //System.out.println(rules.toString());
    }
    
    protected void notRuleConcat()
    {
        ruleConcat = ruleConcat.xor(one);
    }
    
    protected void injectBlockInRuleConcat(double inValue, int inBlockCnt) throws Exception
    {
        int index = (int)Math.floor(inValue * (NB_BLOCKS_ORDER / (PSOConstants.POS_MAX - PSOConstants.POS_MIN)));
        
        //System.out.println(index);
        
        if (index > 15)
            index = 15;
        
        byte val = codes[index];
        
        tmp = tmp.xor(tmp);
        
        
        for (int i = 0 ; i < 4 ; i++)
        {
            if ((((val & 0xf) >> i) & 1) == 1)
                tmp = tmp.setBit(i);
        }
        
        ruleConcat = ruleConcat.shiftLeft(4);
        ruleConcat = ruleConcat.or(tmp);
    }
    
    protected void rotRuleConcat(double inRotType, int inRefValue, double inValue) throws Exception
    {
        int rot = (int)Math.floor(inValue * (2 / (PSOConstants.POS_MAX - PSOConstants.POS_MIN)));
        
        if (rot > 1)
            rot = 1;
        
        if (rot == 0)
            rolRuleConcat(inRefValue, inValue);
        else
            rorRuleConcat(inRefValue, inValue);
    }
    
    protected void rolRuleConcat(int inRefValue, double inValue) throws Exception
    {
        int rol = (int)Math.floor(inValue * (inRefValue / (PSOConstants.POS_MAX - PSOConstants.POS_MIN)));
        
        if (rol > (inRefValue - 1))
            rol = (inRefValue - 1);
        
        if (rol == 0)
            return;
        
        for (int i = 0 ; i < rol ; i++)
        {
            tmp = tmp.xor(tmp);
            
            if (ruleConcat.testBit(511))
            {
                tmp = tmp.setBit(0);
                ruleConcat = ruleConcat.shiftLeft(1);
                ruleConcat = ruleConcat.or(tmp);
            }
            else
                ruleConcat = ruleConcat.shiftLeft(1);
            
            ruleConcat.clearBit(512);
        }
    }
    
    protected void rorRuleConcat(int inRefValue, double inValue) throws Exception
    {
        int ror = (int)Math.floor(inValue * (inRefValue / (PSOConstants.POS_MAX - PSOConstants.POS_MIN)));
        
        if (ror > (inRefValue - 1))
            ror = (inRefValue - 1);
        
        if (ror == 0)
            return;
        
        for (int i = 0 ; i < ror ; i++)
        {
            tmp = tmp.xor(tmp);
            
            if (ruleConcat.testBit(0))
            {
                tmp = tmp.setBit(511);
                ruleConcat = ruleConcat.shiftRight(1);
                ruleConcat = ruleConcat.or(tmp);
            }
            else
                ruleConcat = ruleConcat.shiftRight(1);
            
            //ruleConcat.clearBit(512);
        }
    }
    
    protected int decodeRotRefValue(double inRefValue)
    {
        int val = (int)Math.floor(inRefValue * (512 / (PSOConstants.POS_MAX - PSOConstants.POS_MIN)));
        
        if (val > 511)
            val = 511;
        
        return val;
    }
    
    protected void injectRuleConcat(M2Rules inRules) throws Exception
    {
        byte    val = 0;
        
        for (int i = 0 ; i < 512 ; i++)
        {
            val = (byte)((ruleConcat.testBit(511 - i)) ? 1 : 0);
            inRules.set(i, val);
        }
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
        
        if (step < PSOConstants.AUTO_MIN_STEPS)
            step = PSOConstants.AUTO_MIN_STEPS;
        
        if (step > PSOConstants.AUTO_MAX_STEPS)
            step = PSOConstants.AUTO_MAX_STEPS;
        
        return (step);
    }

    @Override
    public void reset() {
        decode_cnt = 0;
    }

    @Override
    public void copyContentFrom(PSOPositionDecoder inDecoder) throws Exception {
        
        if (!(inDecoder instanceof PSONibbleRolDecoder))
            throw new Exception("not a PSOBlockDecoder given");
        
        PSONibbleRolDecoder bdec = (PSONibbleRolDecoder)inDecoder;
        criterion.copyContentFrom(bdec.criterion);
        decode_cnt = bdec.decode_cnt;
    }

    @Override
    public String toDescString() {
        return ("Nibble ROL Decoder");
    }
    
}
