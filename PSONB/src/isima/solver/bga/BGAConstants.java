/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bga;

import isima.solver.bpso.BPSOConstants;

/**
 *
 * @author onio
 */
public class BGAConstants {
    
    public static final int     N_INDIVIDUALS = 8;  // PUISSANCE DE 2 PLZ
    public static final int     N_GENES = (BPSOConstants.N_DIMS * BPSOConstants.N_PARTICLES);
    public static final int     LIFE_TIME_MAX = BPSOConstants.MAX_ITERATION;
}
