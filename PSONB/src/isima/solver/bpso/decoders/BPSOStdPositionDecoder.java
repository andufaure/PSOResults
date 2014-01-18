/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bpso.decoders;

import isima.cac.M2Auto;
import isima.cac.M2Rules;
import isima.solver.bpso.BPSOConstants;
import isima.solver.bpso.BPSOPosition;
import isima.solver.bpso.criterion.BCriterion;
import isima.solver.pso.criterion.Criterion;

/**
 *
 * @author onio
 */
public class BPSOStdPositionDecoder implements BPSOPositionDecoder {

    private BCriterion   criterion = null;
    
    public BPSOStdPositionDecoder(BCriterion in)
    {
        criterion = in;
    }
    
    @Override
    public void reset() {
    }

    @Override
    public BPSOPositionDecoder buildCopy() throws Exception {
        return new BPSOStdPositionDecoder(criterion);
    }

    @Override
    public void execute(BPSOPosition inPosition) throws Exception {
        
        M2Auto  automata = inPosition.getAutomata();
        M2Rules rules = automata.getRules();
        byte[]  pos = inPosition.getData();
        int     step = BPSOConstants.AUTO_MIN_STEPS; 
        
        double  fit = 0;
        
        for (int i = BPSOConstants.INDEX_RULE_DATA_BEGIN ; i < BPSOConstants.INDEX_RULE_DATA_END ; i++)
            rules.set(i - BPSOConstants.INDEX_RULE_DATA_BEGIN, pos[i]);
        
        for (int i = BPSOConstants.INDEX_STEPS_DATA_BEGIN ; i < BPSOConstants.INDEX_STEPS_DATA_END ; i++)
            step += pos[i];
        
        
        //System.out.println(step + " " + rules.toString());
        
        automata.reset();
        automata.run(step);
        criterion.calc(inPosition);
        
        fit = inPosition.getFitness();
        
        for (int i = BPSOConstants.INDEX_RULE_DATA_BEGIN ; i < BPSOConstants.INDEX_RULE_DATA_END ; i++)
            rules.set(i - BPSOConstants.INDEX_RULE_DATA_BEGIN, (byte)((pos[i] ^ 1) & 1));
        
        for (int i = BPSOConstants.INDEX_STEPS_DATA_BEGIN ; i < BPSOConstants.INDEX_STEPS_DATA_END ; i++)
            step += pos[i];
        
        automata.reset();
        automata.run(step);
        criterion.calc(inPosition);
        
        if (fit > inPosition.getFitness())
        {
            for (int i = BPSOConstants.INDEX_RULE_DATA_BEGIN ; i < BPSOConstants.INDEX_RULE_DATA_END ; i++)
                pos[i] = (byte)((pos[i] ^ 1) & 1);
        }
        else
        {
            for (int i = BPSOConstants.INDEX_RULE_DATA_BEGIN ; i < BPSOConstants.INDEX_RULE_DATA_END ; i++)
                rules.set(i - BPSOConstants.INDEX_RULE_DATA_BEGIN, pos[i]);
            
            automata.reset();
            automata.run(step);
            criterion.calc(inPosition);
        }
    }

    @Override
    public void copyContentFrom(BPSOPositionDecoder inDecoder) throws Exception {
    }
    
}
