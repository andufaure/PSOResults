/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bpso;

/**
 *
 * @author onio
 */
public class BPSOConstants {
    
    public static final int AUTO_MIN_STEPS = 1;             // minimum automata steps
    public static final int AUTO_MAX_STEPS = 10;            // maximum automata steps
    
    
    public static final int INDEX_RULE_DATA_BEGIN = 0;                          // rule data begin
    public static final int INDEX_RULE_DATA_END = INDEX_RULE_DATA_BEGIN + 512;  // rule data end (out)
    
    public static final int INDEX_STEPS_DATA_BEGIN = INDEX_RULE_DATA_END;               // steps data begin
    public static final int INDEX_STEPS_DATA_END = INDEX_STEPS_DATA_BEGIN + (AUTO_MAX_STEPS - AUTO_MIN_STEPS);     // steps data end (out)

    public static final int N_DIMS = INDEX_STEPS_DATA_END;
    
    public static final double  ACC_P = 2.;                 // personnal best acc
    public static final double  ACC_G = 2.;                 // global best acc
    public static final double  ACC_L = 2.2;                 // local best acc
    
    public static final int     NEIGHBORHOOD_SIZE = 7;      
    
    public static final int     NTHREAD_DECODE = 2;
    public static final int     N_PARTICLES = 100;
    
    public static final double  INERTIA_MAX = 0.9;
    public static final double  INERTIA_MIN = 0.1;
    
    public static final int     MAX_ITERATION = 25000;
    public static final double  INERTIA_DECR = (INERTIA_MAX - INERTIA_MIN) / MAX_ITERATION;
}
