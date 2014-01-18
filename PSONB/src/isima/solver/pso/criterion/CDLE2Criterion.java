/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.criterion;

import isima.cac.M2Auto;
import isima.cac.Matrix;
import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOPosition;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author onio
 */
public class CDLE2Criterion implements Criterion {
    
    public static final int N_STEPS_EVAL = 5;
    
    private M2Auto  refAutomata = null;
    private M2Auto  curAutomata = null;
    private Matrix  refMatrix = null;
    private Matrix  curMatrix = null;
    
    private Map<BigDecimal, Set<Integer> >  refMultimap;
    private Map<BigDecimal, Set<Integer> >  curMultimap;
    
    public CDLE2Criterion(Matrix inMatrix) throws Exception
    {
        refMatrix = inMatrix.buildCopy();
        curMatrix = inMatrix.buildCopy();
        refAutomata = new M2Auto(refMatrix);
        curAutomata = new M2Auto(refMatrix);
        refMultimap = new TreeMap<BigDecimal, Set<Integer> >();
        curMultimap = new TreeMap<BigDecimal, Set<Integer> >();
    }
    
    @Override
    public void calc(PSOPosition inPosition) {
        
        int max_diff = refMatrix.getWidth() * refMatrix.getHeight();
        
        int diff = Integer.MAX_VALUE;
        double fitness = Double.MAX_VALUE;
        
        int steps = inPosition.getAutomata().getStep();
        
        try
        {
            curMatrix.copyContentFrom(inPosition.getAutomata().getCurrentMatrix());
            diff = Matrix.cellDiffBetween(refMatrix, curMatrix);
            if (diff != 0)
            {
                //fitness = diff;
                refAutomata.reset(refMatrix);
                refAutomata.reset(inPosition.getAutomata().getRules());
                refAutomata.activateDynRuleWeightsCalc();
                curAutomata.reset(curMatrix);
                curAutomata.reset(inPosition.getAutomata().getRules());
                curAutomata.activateDynRuleWeightsCalc();
                //fitness = 0;
                
                refAutomata.run(N_STEPS_EVAL);
                curAutomata.run(N_STEPS_EVAL);
                //fitness *= Math.pow((max_diff + 1), (N_STEPS_EVAL + 1)) * 513;
                
                /*
                BigDecimal  bfitness = new BigDecimal(0);
                BigDecimal  bdiff = new BigDecimal(diff);
                
                refMultimap.clear();
                curMultimap.clear();
                
                for (int i = 0 ; i < 512 ; i++)
                {
                    BigDecimal      dr = refAutomata.getRules().getRuleWeights()[i];
                    BigDecimal      dc = curAutomata.getRules().getRuleWeights()[i];
                    Set<Integer>    sr = null;
                    Set<Integer>    sc = null;
                    
                    if (refMultimap.containsKey(dr))
                    {
                        refMultimap.get(dr).add(i);
                    }
                    else
                    {
                        refMultimap.put(dr, sr = new TreeSet<Integer>());
                        sr.add(i);
                    }
                    
                    if (curMultimap.containsKey(dc))
                    {
                        curMultimap.get(dc).add(i);
                    }
                    else
                    {
                        curMultimap.put(dc, sc = new TreeSet<Integer>());
                        sc.add(i);
                    }
                }
                
                
                Iterator<Set<Integer>>    itr = refMultimap.values().iterator();
                Iterator<Set<Integer>>    itc = curMultimap.values().iterator();
                Set<Integer>    sr = null;
                Set<Integer>    sc = null;
                
                while (true)
                {

                    if (!itr.hasNext())
                    {
                        if (!itc.hasNext())
                        {
                            break;
                        }
                        else
                        {
                            while (itc.hasNext())
                                cdiff += itc.next().size();
                            break;
                        }
                    }
                    else
                    {
                        if (!itc.hasNext())
                        {
                            while (itr.hasNext())
                                cdiff += itr.next().size();
                            break;
                        }
                        else
                        {
                            sr = itr.next();
                            sc = itc.next();
                            
                            for (Integer ir : sr)
                            {
                                if (!sc.contains(ir))
                                    cdiff++;
                            }
                        }
                    }
                }
                //System.out.println(cdiff);
                fitness = diff * (513 * 513);
                //fitness = 0;
                fitness += cdiff;
                */
                
                double  u_diff = 0;
                
                for (int i = 0 ; i < 512 ; i++)
                {
                    double  c = curAutomata.getRules().getApproxRuleWeight(i);
                    double  r = refAutomata.getRules().getApproxRuleWeight(i);
                    double  r_diff = Math.abs(c - r);
                    
                    //System.out.println("c=" + c + " ; r=" + r + " => " + r_diff);
                    
                    u_diff += r_diff;
                    
                    //System.out.println(u_diff);
                }
                
                // max u_diff -> 512
                
                //fitness = diff * (512 * 512);
                //fitness = 0;
                fitness = diff * 513 + u_diff;
                //System.out.println(fitness);
                /*
                
                for (int i = 0 ; i < 512 ; i++)
                {
                    if (refAutomata.getRules().getRuleWeights()[i] != curAutomata.getRules().getRuleWeights()[i])
                        bfitness = bfitness.add(refAutomata.getRules().getRuleWeights()[i].subtract(curAutomata.getRules().getRuleWeights()[i]).abs());
                }
                
                bfitness = bfitness.divide(new BigDecimal(512));
                //fitness *= diff;
                //System.out.println(bfitness.toString());
                //bfitness = bfitness.multiply(bdiff);
                //System.out.println(bfitness.toString());
                fitness = bfitness.doubleValue() + diff;
                */
                //System.out.println(fitness);
                //fitness *= (max_diff + 1);
                //fitness -= tmp_fitness;
            }
            else
                fitness = diff;
        }
        catch(Exception e)
        {}
        
        //fitness = diff + ((inPosition.getAutomata().getStep() / PSOConstants.AUTO_MAX_STEPS) * 0.1);
        inPosition.setDiff(diff);
        inPosition.setFitness(fitness);
    }

    @Override
    public Criterion buildCopy() throws Exception {
        return new CDLE2Criterion(refMatrix);
    }

    @Override
    public void copyContentFrom(Criterion inCriterion) throws Exception {
        refMatrix.copyContentFrom(((CDLE2Criterion)inCriterion).refMatrix);
        curMatrix.copyContentFrom(((CDLE2Criterion)inCriterion).curMatrix);
        refAutomata.copyContentFrom(((CDLE2Criterion)inCriterion).refAutomata);
        curAutomata.copyContentFrom(((CDLE2Criterion)inCriterion).curAutomata);
    }

    @Override
    public Matrix getRefMatrix() {
        return refMatrix;
    }
}
