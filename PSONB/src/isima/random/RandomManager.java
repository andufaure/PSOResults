/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.random;

import java.util.Random;

/**
 *
 * @author onio
 */
public class RandomManager {
    
    private static  RandomManager   instance = null;
    private Random  r;
    
    public static RandomManager getInstance()
    {
        if (instance == null)
            instance = new RandomManager();
        
        return (instance);
    }
    
    private RandomManager()
    {
        r = new MTRandom();
    }
    
    public double getDoubleIn(double inMin, double inMax)
    {
        double  d = 0;
        
        
        d = (inMin + r.nextDouble() * (inMax - inMin));
        //System.out.println("inMin : " + inMin + " inMax : " + inMax + "- Ret : " + d);
        return (d);
    }
    
    public int    getIntIn(int inMin, int inMax)
    {
        int n = inMin + (int)Math.floor(r.nextDouble() * (inMax - inMin));
        return n;
    }
}
