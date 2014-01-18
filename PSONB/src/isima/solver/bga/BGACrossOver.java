/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bga;

import isima.random.RandomManager;
import java.math.BigInteger;

/**
 *
 * @author onio
 */
public class BGACrossOver {
    
    private BigInteger  crossMaster = null;
    private BigInteger  mutator = null;
    private BigInteger  one = null;
    private BigInteger  zero = null;
    
    private BigInteger  son0 = null;
    private BigInteger  son1 = null;
    
    public BGACrossOver()
    {
        String str = new String("1");
        String str2 = new String("0");
        String str1 = new String("1");
        
        System.out.print("Building cross master & mutator, please wait... ");
        
        for (int i = 0 ; i < BGAConstants.N_GENES ; i++)
        {
            if (RandomManager.getInstance().getDoubleIn(0, 1) < 0.5)
            {
                str2 += "1";
            }
            else
                str2 += "0";
            
            str += RandomManager.getInstance().getIntIn(0, 2);
            str1 += "1";
        }
        
        crossMaster = new BigInteger(str, 2);
        mutator = new BigInteger(str2, 2);
        one = new BigInteger(str1, 2);
        zero = one.xor(one);
        System.out.println("Done!");
    }
    
    public void   execute(BigInteger father, BigInteger mother)
    {
        BigInteger tmp = null;
        
        //son0 = zero.xor(zero);
 //       son0 = (father.xor(mother)).and(one.clearBit(BGAConstants.N_GENES));
 //       son1 = (son0.xor(one)).and(one.clearBit(BGAConstants.N_GENES));
        
        zero = zero.xor(zero);
        son0 = zero.xor(zero);
        //son0 = (father.and(crossMaster)).or(mother.and(crossMaster.xor(one)));
        son0 = father.or(mother).and(crossMaster);
        son0 = son0.xor(mutator);
        son0 = son0.and(one.clearBit(BGAConstants.N_GENES));
        //son1 = (father.and(crossMaster.xor(one))).or(mother.and(crossMaster));
        son1 = mother.or(father).and(crossMaster.and(one));
        son1 = son1.xor(mutator);
        son1 = son1.and(one.clearBit(BGAConstants.N_GENES));
        
        
        
        
        /*
        System.out.println("FATHER " + father.bitLength() + " " + father.toString(2));
        System.out.println("MOTHER " + mother.bitLength() + " " + mother.toString(2));
        System.out.println("CROSSMASTER " + crossMaster.bitLength() + " " + crossMaster.toString(2));
        System.out.println("SON0 " + son0.bitLength() + " " + son0.toString(2));
        System.out.println("SON1 " + son1.bitLength() + " " + son1.toString(2));
        */
        tmp = zero.xor(zero);
        
        int shift = 23;// RandomManager.getInstance().getIntIn(1, 200);
        
        for (int i = 0 ; i < shift ; i++)
            tmp.setBit(i);
        
        // ROTAT MUTATOR !
        
        tmp = crossMaster.and(tmp);
        crossMaster = crossMaster.shiftRight(shift);
        tmp = tmp.shiftLeft(BGAConstants.N_GENES - shift);
        crossMaster = crossMaster.or(tmp);
        //crossMaster = crossMaster.xor(one);
        crossMaster = crossMaster.and(one.clearBit(BGAConstants.N_GENES));
        //mutator = mutator.xor(one);
        mutator = mutator.or(mutator.shiftLeft(1));
        mutator = mutator.and(one.clearBit(BGAConstants.N_GENES));
    }

    public BigInteger getSon0() {
        return son0;
    }

    public BigInteger getSon1() {
        return son1;
    }

    public BigInteger getMutator() {
        return mutator;
    }
    
}
