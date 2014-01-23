/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.main;

import isima.cac.M2Rules;
import isima.cac.Matrix;
import isima.solver.bga.BGA;
import isima.solver.bpso.BPSOConstants;
import isima.solver.bpso.BPSOCore;
import isima.solver.incr.vnd.VND;
import isima.solver.incr.vnd.criterion.VNDCDCriterion;
import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOCore;
import isima.solver.pso.criterion.CellDiffCriterion;

/**
 *
 * @author onio
 */
/*
 * 
 */
public class Main {
    
    public static void main(String[] args)
    {
        try
        {
            
            if (args.length < 2)
            {
                System.err.println("ARGS ERROR : init.csv ref.csv");
                return;
            }
            
            Matrix init = Matrix.buildFromCSV(args[0]);            
            Matrix ref = Matrix.buildFromCSV(args[1]);
            
            System.out.println("PSO TRACE");
            System.out.println("INIT:" + args[0]);
            System.out.println("REF:" + args[1]);
            System.out.println("DecodingPolicy:CPP2:CDLE2");
            System.out.println("MutationPolicy:Std");
            System.out.println("ParticleNeighborhood:Fixed");
            
            System.out.println("ParticleDims:" + PSOConstants.N_DIMS);
            System.out.println("ParticleCount:" + PSOConstants.N_PARTICLES);
            System.out.println("AccP:" + PSOConstants.ACC_P);
            System.out.println("AccG:" + PSOConstants.ACC_G);
            System.out.println("AccL:" + PSOConstants.ACC_L);
            System.out.println("AccI:" + PSOConstants.ACC_I);
            System.out.println("Pos:" + PSOConstants.POS_MIN + "-" + PSOConstants.POS_MAX);
            System.out.println("Inertia:" + PSOConstants.INERTIA_MAX + "-" + PSOConstants.INERTIA_MIN);
            System.out.println("VeloMax:" + PSOConstants.VELO_MAX);
            System.out.println("MaxPSOIteration:" + PSOConstants.MAX_ITERATION);
            System.out.println("InertiaDecrFactor:" + PSOConstants.INERTIA_DECR);
            
            System.out.println("INIT");
            init.print();
            //System.out.println(args[1] + " :");
            System.out.println("REF");
            ref.print();
            /*
            M2Rules rules = new M2Rules();
            
            rules.uniformContent();
            
            VND vnd = new VND(init, ref, rules, 4, new VNDCDCriterion(init, ref));
            
            rules.completeMutat();
            
            vnd.execute(init, ref, rules, 5);
            */
            
            
            PSOCore core = new PSOCore(init, ref);
            
            for (int i = 0 ; i < PSOConstants.MAX_ITERATION ; i++)
                core.step();

            core.finish();
            System.out.println("end");
            
            
            
            
            /*
            BPSOCore core = new BPSOCore(init, ref);
            
            for (int i = 0 ; i < BPSOConstants.MAX_ITERATION ; i++)
            {
                core.step();
                //System.out.println(i);
            }*/
            /*
            BGA bga = new BGA(init, ref);
            
            for (int i = 0 ; i < 1000000000 ; i++)
                bga.step();
             * 
             */
            
        }
        catch(Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
}
