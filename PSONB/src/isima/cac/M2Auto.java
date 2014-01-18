/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.cac;

import java.math.BigDecimal;

/**
 * 2D Moore 2-states cellular automata
 * @author onio
 */
public class M2Auto {
    
    private boolean     ACTIVATE_DYN_RULE_WEIGHT_CALC = false;
    
    private             Matrix  initMatrix;
    private             Matrix  currentMatrix;
    private transient   Matrix  tmpMatrix;
    private             M2Rules rules;
    private             int     step;
    
    public M2Auto(int w, int h) throws Exception
    {
        initMatrix = new Matrix(w, h);
        currentMatrix = initMatrix.buildCopy();
        tmpMatrix = initMatrix.buildCopy();
        step = 0;
        rules = new M2Rules();
    }
    
    public M2Auto(Matrix inMatrix) throws Exception
    {
        initMatrix = new Matrix(inMatrix);
        currentMatrix = initMatrix.buildCopy();
        tmpMatrix = initMatrix.buildCopy();
        step = 0;
        rules = new M2Rules();
    }
    
    public void activateDynRuleWeightsCalc()
    {
        ACTIVATE_DYN_RULE_WEIGHT_CALC = true;
    }
    
    public void reset() throws Exception
    {
        currentMatrix.copyContentFrom(initMatrix);
        step = 0;
    }
    
    public void reset(M2Rules inRules) throws Exception
    {
        reset();
        rules.copyContentFrom(inRules);
    }
    
    public void reset(Matrix inMatrix) throws Exception
    {
        initMatrix = new Matrix(inMatrix);
        currentMatrix = initMatrix.buildCopy();
        tmpMatrix = initMatrix.buildCopy();
        step = 0;
    }
    
    public void step() throws Exception
    {
        int         index = -1;
        
        int         n = -1;
        int         s = -1;
        int         w = -1;
        int         e = -1;
        
        int         gn = 0;
        int         gs = 0;
        int         gw = 0;
        int         ge = 0;
        
        tmpMatrix.copyContentFrom(currentMatrix);
        
        for (int y = 0 ; y < tmpMatrix.getHeight() ; y++)
        {
            gn = ((n = (y - 1)) == -1) ? 0 : 1;
            gs = ((s = (y + 1)) == tmpMatrix.getHeight()) ? 0 : 1;
            
            for (int x = 0 ; x < tmpMatrix.getWidth() ; x++)
            {
                gw = ((w = (x - 1)) == -1) ? 0 : 1;
                ge = ((e = (x + 1)) == tmpMatrix.getWidth()) ? 0 : 1;
                
                
                index = 0;
                
                {
                    // NW
                    index |= ((gw & gn) == 1) ? tmpMatrix.get(w, n) : 0;
                    index <<= 1;

                    // N
                    index |= (gn == 1) ? tmpMatrix.get(x, n) : 0;
                    index <<= 1;

                    // NE
                    index |= ((ge & gn) == 1) ? tmpMatrix.get(e, n) : 0;
                    index <<= 1;

                    // W
                    index |= (gw == 1) ? tmpMatrix.get(w, y) : 0;
                    index <<= 1;

                    // C
                    index |= tmpMatrix.get(x, y);
                    index <<= 1;

                    // E
                    index |= (ge == 1) ? tmpMatrix.get(e, y) : 0;
                    index <<= 1;

                    // SW
                    index |= ((gw & gs) == 1) ? tmpMatrix.get(w, s) : 0;
                    index <<= 1;

                    // S
                    index |= (gs == 1) ? tmpMatrix.get(x, s) : 0;
                    index <<= 1;

                    // SE
                    index |= ((gs & ge) == 1) ? tmpMatrix.get(e, s) : 0;
                }
                
                if (ACTIVATE_DYN_RULE_WEIGHT_CALC)
                    rules.incrWeight(index, 1.);
                
                currentMatrix.set(x, y, rules.get(index));
            }
        }
        
        if (ACTIVATE_DYN_RULE_WEIGHT_CALC)
            rules.mulWeights(currentMatrix.getHeight() * currentMatrix.getWidth());
        
        step++;
    }
    
    public void run(int steps) throws Exception
    {   
        if (ACTIVATE_DYN_RULE_WEIGHT_CALC)
            rules.clearWeights();
        
        for (int i = 0 ; i < steps ; i++)
            this.step();
    }

    public Matrix getCurrentMatrix() {
        return currentMatrix;
    }

    public Matrix getInitMatrix() {
        return initMatrix;
    }

    public M2Rules getRules() {
        return rules;
    }

    public int getStep() {
        return step;
    }
    
    public void copyContentFrom(M2Auto inAuto) throws Exception {
        initMatrix.copyContentFrom(inAuto.initMatrix);
        currentMatrix.copyContentFrom(inAuto.currentMatrix);
        rules.copyContentFrom(inAuto.rules);
        step = inAuto.step;
    }
    
    
}
