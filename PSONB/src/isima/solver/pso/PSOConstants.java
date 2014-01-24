/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso;

import isima.solver.pso.decoders.PSOBlockDecoder;
import isima.solver.pso.decoders.PSOBlockOpDecoder;
import isima.solver.pso.decoders.PSOBlockOpSearchDecoder;
import isima.solver.pso.decoders.PSOCPP2Decoder;
import isima.solver.pso.decoders.PSOCPPDecoder;
import isima.solver.pso.decoders.PSOCPPNotVNDDecoder;
import isima.solver.pso.decoders.PSONibbleDecoder;
import isima.solver.pso.decoders.PSONibbleRolDecoder;

/**
 *
 * @author onio
 */
public class PSOConstants {
    
    public static final int N_DIMS = PSONibbleDecoder.N_DIMS;
    
    /* non utilisé */
    public static final int INDEX_RULE_REF = 0;             // rule reference
    public static final int INDEX_STEPS_REF = 1;            // step reference
    public static final int INDEX_RULE_DATA_BEGIN = 2;      // rule data begin
    public static final int INDEX_RULE_DATA_END = 514;      // rule data end (out)

    public static final int INDEX_STEPS_DATA_BEGIN = INDEX_RULE_DATA_END;   // steps data begin
    public static final int INDEX_STEPS_DATA_END = N_DIMS;                  // steps data end (out)
    /* fin non utilisé */
    
    public static final int AUTO_MIN_STEPS = 1;             // minimum automata steps
    public static final int AUTO_MAX_STEPS = 10;            // maximum automata steps
    
    public static final double  POS_MIN = 0.;              // minimum position
    public static final double  POS_MAX = 3.;              // maximum position
    public static final double  VELO_MAX = 2.;              // maximum velocity
    
    public static final double  ACC_P = 2.;                 // personnal best acc
    public static final double  ACC_G = 2.;                 // global best acc
    public static final double  ACC_L = 1.5;                // local best acc
    public static final double  ACC_I = 2.3;
    
    public static final int     N_PARTICLES = 100;
    public static final int     NEIGHBORHOOD_SIZE = (int)((2 - 1.61803398875) * N_PARTICLES);      
    
    public static final int     NTHREAD_DECODE = 4;
    
    
    public static final double  INERTIA_MAX = 0.9;
    public static final double  INERTIA_MIN = 0.6;
    
    public static final int     MAX_ITERATION = 50000 / N_PARTICLES;
    public static final double  INERTIA_DECR = (INERTIA_MAX - INERTIA_MIN) / MAX_ITERATION;
}
