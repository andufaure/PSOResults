/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.decoders;

import isima.cac.M2Auto;
import isima.cac.M2Rules;
import isima.cac.Matrix;
import isima.random.RandomManager;
import isima.solver.incr.vnd.VND;
import isima.solver.incr.vnd.criterion.VNDCDCriterion;
import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOPosition;
import isima.solver.pso.criterion.Criterion;

/**
 *
 * @author onio
 */
public class PSOCPPNotVNDDecoder implements PSOPositionDecoder {
    
    public static final int RULE_DATA_BEGIN = 0;
    public static final int RULE_DATA_END = RULE_DATA_BEGIN + 512;
    
    public static final int STEP_REF1 = RULE_DATA_END;
    public static final int RULE_REF = STEP_REF1 + 1;
    public static final int STEP_REF2 = RULE_REF + 1;
    public static final int N_DIMS = STEP_REF2 + 1;
    
    private int     decode_cnt = 0;
    
    private final   Criterion   criterion;
    private final   VND         vnd;
    
    public PSOCPPNotVNDDecoder(Matrix inInitMatrix, Criterion inCriterion) throws Exception
    {
        criterion = inCriterion.buildCopy();
        vnd = new VND(inInitMatrix, criterion.getRefMatrix(), new M2Rules(), 12, new VNDCDCriterion(inInitMatrix, criterion.getRefMatrix()));
    }
    
    @Override
    public void reset() {
        //decode_cnt = 0;
    }

    @Override
    public PSOPositionDecoder buildCopy() throws Exception {
        return new PSOCPPNotVNDDecoder(vnd.getInitMatrix(), this.criterion);
    }

    @Override
    public void execute(PSOPosition inPosition) throws Exception {
        
        M2Auto      automata = inPosition.getAutomata();
        M2Rules     rules = inPosition.getAutomata().getRules();
        double[]    pos = inPosition.getData();
        byte        rule = 0;
        int         step = 0;
        double      fitness = 0.;
        
        
        //  CPP CODE
        //  double      base = (ppPosition[ppPosition.size() - 2] + 1.0 - 0.) / AlphaSize;
        double      rule_ref = (pos[RULE_REF] + 1.0) / 2.;
        
        // DECODING RULES
        for (int i = RULE_DATA_BEGIN ; i < RULE_DATA_END ; i++)
        {
            
            //  CPP CODE
            //    j = ppPosition[i] / base;
            //    if (j >= AlphaSize)
            //        j = (AlphaSize - 1);
            //    ppSolution.getRules()[i].Value() = j;
            
            rule = (byte)Math.floor(pos[i] / rule_ref);
            
            if (rule >= 2)
                rule = 1;
            
            rules.set(i, rule);
        }
        
        // DECODING STEPS
        // CPP CODE
//        if (ppPosition[ppPosition.size() - 3] < ppPosition[ppPosition.size() - 1])
//        {
//            ppSolution.getSteps() = ppParam.pspAutomataMinSteps 
//                                            + ((ppPosition[ppPosition.size() - 1] - ppPosition[ppPosition.size() - 3]) / ppPosition[ppPosition.size() - 1]) 
//                                                    * ((ppParam.pspAutomataMaxSteps - ppParam.pspAutomataMinSteps));
//        }
//        else if (ppPosition[ppPosition.size() - 3] > ppPosition[ppPosition.size() - 1])
//        {
//            ppSolution.getSteps() = ppParam.pspAutomataMinSteps 
//                                                    + ((ppPosition[ppPosition.size() - 3] - ppPosition[ppPosition.size() - 1]) / ppPosition[ppPosition.size() - 3]) 
//                                                            * ((ppParam.pspAutomataMaxSteps - ppParam.pspAutomataMinSteps));
//        }
//        else
//            ppSolution.getSteps() = ppParam.pspAutomataMinSteps + ((ppParam.pspAutomataMaxSteps - ppParam.pspAutomataMinSteps) / 2);
//
//        if (ppSolution.getSteps() == 0)
//            ppSolution.getSteps()++;
        
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
        
        
        // evaluation
        if ((decode_cnt % 6) == 0)
        {           
            System.out.print("vnd... ");
            vnd.execute(automata.getInitMatrix(), criterion.getRefMatrix(), automata.getRules(), step);
            automata.reset(vnd.getRules());
            automata.run(step);
            criterion.calc(inPosition);
            
            System.out.println(" diff=" + inPosition.getDiff());
            
            double      base = (pos[RULE_REF] + 1) / 2;
            
            // recode from solution
            /*
            for (int i = 0 ; i < 512 ; i++)
            {
                pos[i] = RandomManager.getInstance().getDoubleIn(automata.getRules().get(i) * base, (automata.getRules().get(i) + 1) * base);
                if (pos[i] > PSOConstants.POS_MAX)
                    pos[i] = PSOConstants.POS_MAX;
                
                if (pos[i] < PSOConstants.POS_MIN)
                    pos[i] = PSOConstants.POS_MIN;
            }
             * 
             */
            //System.out.println(" end!");
        }
        else
        {
            automata.reset();
            automata.run(step);
            criterion.calc(inPosition);


            fitness = inPosition.getFitness();

            automata.getRules().completeMutat();
            automata.reset();
            automata.run(step);

            criterion.calc(inPosition);

            if (fitness > inPosition.getFitness())
            {

            }
            else
            {
                automata.getRules().completeMutat();
                automata.reset();
                automata.run(step);
                criterion.calc(inPosition);
            }
        }
        
        decode_cnt++;
    }

    @Override
    public void copyContentFrom(PSOPositionDecoder inDecoder) throws Exception {
        return;
        //criterion.copyContentFrom(((PSOCPPNotVNDDecoder)inDecoder).criterion);
    }

    @Override
    public String toDescString() {
        return ("CPP FULL VND + NOT DECODER");
    }
}
