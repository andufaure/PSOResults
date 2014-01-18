/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.cac;

import isima.random.MTRandom;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * 2D Moore 2-states evolution rules (index : NW-N-NE-W-C-E-SW-S-SE)
 * @author onio
 */
public class M2Rules {
        
    // rule data
    private             byte[]          rules = new byte[512];
    private             BigDecimal[]    ruleWeights = new BigDecimal[512];
    private             BigDecimal      maxWeight;
    
    // internal for O(1) check
    private transient   boolean[]       mutats = new boolean[512];
    
    // internal for memory opt
    private transient   List<Integer>   chosenMutats = new ArrayList<Integer>();
    
    public M2Rules() {
        Arrays.fill(rules, (byte)0);
        Arrays.fill(mutats, false);
    }
    
    public M2Rules(M2Rules inRules) throws Exception
    {
        if (inRules == null)
            throw new Exception("rules given is null");
        
        System.arraycopy(inRules.rules, 0, this.rules, 0, 512);
    }
    
    public void rol(int rol_val)
    {
        byte    tmp;
        
        if (rol_val == 512)
            return;
        
        for (int i = 0 ; i < rol_val ; i++)
        {
            tmp = rules[0];
            for (int j = 0 ; j < 511 ; j++)
                rules[j] = rules[j + 1];
            rules[511] = tmp;
        }
    }
    
    
    public byte get(int i) throws Exception
    {
        if (i < 0 || i >= 512)
            throw new Exception("i given is wrong");
        
        return rules[i];
    }
    
    public void set(int i, byte value) throws Exception
    {
        if (i < 0 || i >= 512)
            throw new Exception("i given is wrong");
        
        rules[i] = value;
    }
    
    public void uniformContent() {
        
        int     rnd;
        Random  r = new MTRandom();
        
        for (int i = 0 ; i < 512 ; i++) {
            rnd = r.nextInt() & 1;
            rules[i] = (byte)rnd;
        }
    }
    
    public M2Rules  buildCopy() {
        
        M2Rules obj = null;
        
        // impossible exception
        try
        {   obj = new M2Rules(this);    }
        catch(Exception e)  {}
        
        return (obj);
    }
    
    public void copyContentFrom(M2Rules inRules) {
        System.arraycopy(inRules.rules, 0, this.rules, 0, 512);
    }
    
    /***
     * 
     * @param inPrct Real [0 ; 1.[ => mutat according to prcnt
     *                    [1. ; +inf[ => total mutat
     */
    public void uniformMutat(double inPrct)      {
        
        
        int n_mutats = (int)Math.floor(512. * inPrct);
        
        if (n_mutats <= 0)
            ;
        else if (n_mutats == 512)
        {
            // mutat all
            this.completeMutat();
        }
        else if (n_mutats > (512 >> 1))
        {
            // mutat all
            this.completeMutat();
            // apply uniform mutat to 512 - n_mutats (since nUniformMutat
            // require CPU ressources)
            this.nUniformMutat(512 - n_mutats);
        }
        else
            this.nUniformMutat(n_mutats);
    }
    
    public void completeMutat()
    {
        for (int i = 0 ; i < 512 ; i++)
            rules[i] = (byte)((rules[i] ^ 1) & 1);
    }
    
    protected void nUniformMutat(int n_mutats)
    {
        Random r = new MTRandom();

        chosenMutats.clear();
        Arrays.fill(this.mutats, false);

        for (int i = 0 ; i < n_mutats ; i++)
        {
            Integer n = 0;

            do
                n = r.nextInt() % 512;
            while (this.mutats[n]);

            this.mutats[n] = true;
            chosenMutats.add(n);
        }

        for (Integer n : chosenMutats)
            rules[n] ^= 1;
    }
    
    public void     clearWeights()
    {
        maxWeight = new BigDecimal(0);
        
        for (int i = 0 ; i < 512 ; i++)
            this.ruleWeights[i] = new BigDecimal(0);
    }
    
    public void     incrWeight(int inIndex, double inValue)
    {
        this.ruleWeights[inIndex] = this.ruleWeights[inIndex].add(new BigDecimal(inValue));
        maxWeight = maxWeight.max(this.ruleWeights[inIndex]);    
    }
    
    public void     mulWeights(double inFactor)
    {
        for (int i = 0 ; i < 512 ; i++)
        {
            this.ruleWeights[i] = this.ruleWeights[i].multiply(new BigDecimal(inFactor));
            maxWeight = maxWeight.max(this.ruleWeights[i]);
        }
    }

    public BigDecimal[] getRuleWeights() {
        return ruleWeights;
    }
    
    public double       getApproxRuleWeight(int inIndex)
    {
       // System.out.println(ruleWeights[inIndex].toString());
       // System.out.println(maxWeight.toString());
       // System.out.println(ruleWeights[inIndex].divide(maxWeight, 25, 1).toString());
        return (ruleWeights[inIndex].divide(maxWeight, 45, 1)).doubleValue();
    }
    
    @Override
    public String toString() 
    {
        String str = new String("");
        
        for (int i = 0 ; i < 512 ; i++)
            str += this.rules[i];
        
        return str;
    }
}
