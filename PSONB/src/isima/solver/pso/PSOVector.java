/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso;

import isima.random.RandomManager;

/**
 *
 * @author onio
 */
public class PSOVector {
    
    protected double[]      data = new double[PSOConstants.N_DIMS];
    
    public static double getEuclideanDistance(PSOVector v1, PSOVector v2)
    {
        double  dist = 0;
        
        for (int i = 0 ; i < PSOConstants.N_DIMS ; i++)
            dist += Math.pow(v2.data[i] - v1.data[i], 2.);
        
        return (Math.sqrt(dist));
    }
    
    public PSOVector()
    {}

    public double[] getData() {
        return data;
    }
    
    public void copyContentFrom(PSOVector inVector) {
        System.arraycopy(inVector.data, 0, this.data, 0, PSOConstants.N_DIMS);
    }
    
    public void nullify() {
        for (int i = 0 ; i < PSOConstants.N_DIMS ; i++)
            data[i] = 0.;
    }
    
    public void randomize(double inMin, double inMax) {
        for (int i = 0 ; i < PSOConstants.N_DIMS ; i++)
            data[i] = RandomManager.getInstance().getDoubleIn(inMin, inMax);
    }
    
    @Override
    public String toString() {
        String str = new String("{ ");
        
        for (int i = 0 ; i < PSOConstants.N_DIMS ; i++)
            str += data[i] + ", ";
        
        str += "}";
        return str;
    }
}
