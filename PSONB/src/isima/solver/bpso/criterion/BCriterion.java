/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bpso.criterion;

import isima.solver.bpso.BPSOPosition;

/**
 *
 * @author onio
 */
public interface BCriterion {
    
    public void calc(BPSOPosition inPosition);
}
