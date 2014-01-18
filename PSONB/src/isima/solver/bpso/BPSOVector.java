/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bpso;

import isima.solver.pso.*;
import isima.random.RandomManager;

/**
 *
 * @author onio
 */
public class BPSOVector {
    
    protected byte[]      data = new byte[BPSOConstants.N_DIMS];
    
    public static double getEuclideanDistance(BPSOVector v1, BPSOVector v2)
    {
        double  dist = 0;
        
        for (int i = 0 ; i < BPSOConstants.N_DIMS ; i++)
            dist += Math.pow(v2.data[i] - v1.data[i], 2.);
        
        return (Math.sqrt(dist));
    }
    
    public BPSOVector()
    {}

    public byte[] getData() {
        return data;
    }
    
    public void copyContentFrom(BPSOVector inVector) {
        System.arraycopy(inVector.data, 0, this.data, 0, BPSOConstants.N_DIMS);
    }
    
    public void nullify() {
        for (int i = 0 ; i < BPSOConstants.N_DIMS ; i++)
            data[i] = 0;
    }
    
    public void randomize() {
        for (int i = 0 ; i < BPSOConstants.N_DIMS ; i++)
            data[i] = (byte)RandomManager.getInstance().getIntIn(0, 2);
    }
    
    @Override
    public String toString() {
        String str = new String("{ ");
        
        for (int i = 0 ; i < BPSOConstants.N_DIMS ; i++)
            str += data[i] + ", ";
        
        str += "}";
        return str;
    }
}
