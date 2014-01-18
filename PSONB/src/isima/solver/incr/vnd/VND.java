/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.incr.vnd;

import isima.cac.M2Auto;
import isima.cac.M2Rules;
import isima.cac.Matrix;
import isima.solver.incr.vnd.criterion.VNDCriterion;
import isima.solver.pso.criterion.Criterion;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author onio
 */
public class VND {
    
    public static final int TJ_NONE = 0;
    public static final int TJ_PAST = 1;
    public static final int TJ_FUTURE = 2;
    
    // POWER OF VND IS RELATED TO THIS CONSTANT
    // 4-5 IS A GOOD VALUE, 
    //  >5 --> BETTER SEARCH BUT TIME++
    //  <4 --> TIME-- BUT SEARCH NOT GOOD
    public static final int NB_PER_CLASS = 5;
    
    private int                                 steps;
    private M2Auto                              automata;
    private M2Rules                             rules;
    private M2Rules                             localSearchResult;
    private double                              localSearchFitness;
    private M2Rules                             globalResult;
    private double                              globalFitness;
    private Matrix                              refMatrix;
    private VNDCriterion                        criterion;
    private Map<BigDecimal, List<Integer>>      multimap;
    private List<List<Integer>>                 classes;
    
    private int                                 currentNeighborhood;
    private int                                 timeJump;
    private int                                 timeJumpType;
    
    public VND(         Matrix          inInitMatrix,
                        Matrix          inRefMatrix,
                        M2Rules         inRules, 
                        int             inSteps,
                        VNDCriterion    inCriterion) throws Exception
    {
        steps = inSteps;
        rules = inRules.buildCopy();
        localSearchResult = inRules.buildCopy();
        automata = new M2Auto(inInitMatrix);
        refMatrix = inRefMatrix.buildCopy();
        multimap = new TreeMap<BigDecimal, List<Integer>>();
        classes = new ArrayList<List<Integer>>();
        criterion = inCriterion.buildCopy();
    }
    
    public void reset(  Matrix  inInitMatrix,
                        Matrix  inRefMatrix,
                        M2Rules inRules, 
                        int     inSteps) throws Exception
    {
        steps = inSteps;
        rules.copyContentFrom(inRules);
        localSearchResult.copyContentFrom(inRules);
        automata.reset(rules);
        automata.reset(inInitMatrix);
        refMatrix.copyContentFrom(inRefMatrix);
        multimap.clear();
        classes.clear();
    }
    
    public void execute(Matrix  inInitMatrix,
                        Matrix  inRefMatrix,
                        M2Rules inRules,
                        int     inSteps) throws Exception
    {
        reset(inInitMatrix, inRefMatrix, inRules, inSteps);
        this.init();
        currentNeighborhood = 0;
        
        
        while (true)
        {
            //System.out.println("it");
            //buildNeighborhoods();
            
            //timeJump = 0;
            if (currentNeighborhood >= classes.size())
                break;
            
            step();
        }
        //System.out.println("--------------------------------------");
    }
    
    
    public void step() throws Exception
    {
        int     initial_neighborhood;
        
        
        init();
        initial_neighborhood = currentNeighborhood;
        //initial_neighborhood = 0;
        //System.out.println("INITIAL NEIGHBORHOOD : " + initial_neighborhood);
        //System.out.println("CLASSES SIZE : " + classes.size());
        while (!localSearchInNeighborhood(currentNeighborhood))
        {
            timeJump++;
            //System.out.println("TIME JUMP : " + timeJump);
            currentNeighborhood = initial_neighborhood + timeJump;
            if (currentNeighborhood >= classes.size())
                break;
        }

        timeJump = 0;
        if (currentNeighborhood >= classes.size())
            return;
        
        
        rules.copyContentFrom(localSearchResult);
        //System.out.println(localSearchFitness);
    }
    
    // calcs initial selector
    private void    init() throws Exception
    {
        buildNeighborhoods();
        currentNeighborhood = selectNeighborhoodFromFitness(criterion.calc(rules, steps), criterion.getMaxFitness());
        timeJumpType = TJ_NONE;
        timeJump = 0;
    }
    
    
    private void    buildNeighborhoods() throws Exception
    {
        M2Rules     arules = null;
        int         ccount = 0;
        int         class_id = 0;
        int         nb_positive = 0;
        
        automata.activateDynRuleWeightsCalc();
        automata.reset(rules);
        automata.run(steps);
        multimap.clear();
        
        arules = automata.getRules();
        
        for (int i = 0 ; i < 512 ; i++)
        {
            BigDecimal          weight = arules.getRuleWeights()[i];
            List<Integer>       ll = null;
            
            if (weight == BigDecimal.ZERO)
                continue;
            
            nb_positive++;
            if (multimap.containsKey(weight))
            {
                multimap.get(weight).add(i);
            }
            else
            {
                multimap.put(weight, ll = new LinkedList<Integer>());
                ll.add(i);
            }
        }
        
        class_id = -1;
        classes.clear();
        for (List<Integer> ll : multimap.values())
        {
            for (Integer il : ll)
            {   
                if ((ccount % NB_PER_CLASS) == 0)
                {
                    class_id++;
                    classes.add(new LinkedList<Integer>());
                }
                
                //System.out.println(il + " => " + class_id);
                classes.get(class_id).add(il);
                ccount++;
            }
        }
        
        
        //System.out.println("USED : " + nb_positive);
        //System.out.println("AT_END : " + ccount);
    }
    
    private int    selectNeighborhoodFromFitness(double inFitness, double inMaxFitness)
    {
        return (selectNeighborhood(calcSelector(inFitness, inMaxFitness)));
    }
    
    // inSelector : 0 --> 1
    private int    selectNeighborhood(double inSelector)
    {
        int val = (int)Math.round(inSelector * classes.size());
        
        if (val >= classes.size())
            val = classes.size() - 1;
        
        return (val);
    }
    
    
    private double calcSelector(double inFitness, double inMaxFitness)
    {
        return (inFitness / inMaxFitness);
    }
    
    private boolean localSearchInNeighborhood(int inNeighborhood) throws Exception
    {
        List<Integer>   neighborhood = classes.get(inNeighborhood);
        List<Integer>   values = new ArrayList<Integer>(neighborhood);
        int             solution_set_size = (int)Math.pow(2, neighborhood.size());
        double          initial_fitness = 0;
        double          best_fitness = Double.MAX_VALUE;
        double          fitness = 0;
        M2Rules         bestRules = this.rules.buildCopy();
        M2Rules         tmpRules = this.rules.buildCopy();
        
        localSearchFitness = initial_fitness = criterion.calc(rules, steps);
        localSearchResult.copyContentFrom(rules);
        
        for (int i = 0 ; i < solution_set_size ; i++)
        {
            fillListFromBinaryInteger(i, values);
            for (int j = 0 ; j < neighborhood.size() ; j++)
            {
                tmpRules.set(neighborhood.get(j), values.get(j).byteValue());
                fitness = criterion.calc(tmpRules, steps);
                
                if (localSearchFitness > fitness)
                {
                    localSearchResult.copyContentFrom(tmpRules);
                    localSearchFitness = fitness;
                    //return (true);  //
                }
            }
        }
        //return (false); //
        return (localSearchFitness < initial_fitness);
    }
    
    private void    fillListFromBinaryInteger(int inValue, List<Integer> inValues)
    {
        for (int i = 0 ; i < inValues.size() ; i++)
        {
            inValues.set(i, inValue & 1);
            inValue >>= 1;
            inValue &= 0xffffffff;
        }
    }
    
    public Matrix   getInitMatrix()
    {
        return automata.getInitMatrix();
    }
    
    public M2Rules  getRules()
    {
        return rules;
    }
    
}
