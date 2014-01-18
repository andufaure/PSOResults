/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bga;

import isima.cac.Matrix;
import isima.random.RandomManager;
import isima.solver.bpso.BPSOConstants;
import isima.solver.bpso.BPSOPosition;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author onio
 */
public class BGA {
    
    private BGAIndividualEvaluator  evaluator = null;
    private BGAIndividual[]         individuals = new BGAIndividual[BGAConstants.N_INDIVIDUALS];
    private BigInteger              seed = null;
    private BigInteger              zero = null;
    private BigInteger              one = null;
    private BigInteger              tmp = null;
    private BPSOPosition            record = null;
    private BGACrossOver            crossOver = null;
    private BGAIndividual[]         tmpIndividuals = new BGAIndividual[BGAConstants.N_INDIVIDUALS];
    
    private TreeMap<Double, List<BGAIndividual>>  individualsByFitness = new TreeMap<Double, List<BGAIndividual>>();
    
    private int                     lifeTime = 0;
    
    public BGA(Matrix inInitMatrix, Matrix inRefMatrix) throws Exception
    {
        String  str = "0";
        str = "";
        String  str2 = "1";
        crossOver = new BGACrossOver();
        evaluator = new BGAStdEvaluator();
        
        System.out.print("SEED GENERATION, please wait...");
        
        for (int j = 0 ; j < BGAConstants.N_GENES ; j++)
        {
            str += RandomManager.getInstance().getIntIn(0, 2);
            str2 += "1";
        }
        System.out.println(" Done!");
        
        seed = new BigInteger(str, 2);
        one = new BigInteger(str2, 2);
        zero = one.xor(one);
        tmp = one.xor(one);
        
        System.out.println(seed.bitLength());
        System.out.println(seed.toString(2));
        System.out.print("POPULATION GENERATION, please wait...");
        
        for (int i = 0 ; i < BGAConstants.N_INDIVIDUALS ; i++)
        {
            tmp = tmp.xor(tmp);
            
            if (seed.testBit(0))
            {
                tmp = tmp.setBit(0);
                tmp = tmp.shiftLeft(BGAConstants.N_GENES - 1);
                seed = seed.shiftRight(1);
                seed = seed.or(tmp);
            }
            else
                seed = seed.shiftRight(1);
            
            individuals[i] = new BGAIndividual(inInitMatrix, inRefMatrix, seed, evaluator);
        }
        
        record = new BPSOPosition(individuals[0].getBpso().getCore().getParticles()[0].getX());
        
        System.out.println(" Done!");
        
        lifeTime = 2;
        //lifeTime = BGAConstants.LIFE_TIME_MAX / 1000;
    }
    
    public void step() throws Exception
    {
        boolean nrecord = false;
        
        individualsByFitness.clear();
        
        // MAKE INDIVIDUALS LIVE
        for (int i = 0 ; i < BGAConstants.N_INDIVIDUALS ; i++)
        {
            BPSOPosition gbest = null;
            double       ifit = 0;
            double       icorefit = 0;
            
            //individuals[i].reset();
            System.out.print("Live " + i + "...");
            individuals[i].live(lifeTime);
            
            individuals[i].evaluate();
            ifit = individuals[i].getFitness();
            icorefit = individuals[i].getBpso().getCore().getGbest().getFitness();
            System.out.println(" => " + ifit + ", " + icorefit + " @" + individuals[i].getBpso().getCore().getIteration() );
            
            // extract best values
            if (record.getFitness() > (gbest = individuals[i].getBpso().getCore().getGbest()).getFitness())
            {
                record.copyContentFrom(gbest);
                
                for (int j = 0 ; j < 100 ; j++)
                {
                    individuals[i].getBpso().getCore().step();
                }
                record.copyContentFrom(gbest);
                System.out.println("NEW GBEST : " + gbest.getDiff());
                nrecord = true;
            }
            
            
            
            if (individualsByFitness.containsKey(ifit))
            {
                individualsByFitness.get(ifit).add(individuals[i]);
            }
            else
            {
                List<BGAIndividual> l = null;
                
                individualsByFitness.put(ifit, l = new LinkedList<BGAIndividual>());
                l.add(individuals[i]);
            }
        }
        
        if (!nrecord)
            lifeTime *= 1.5;
        
        int k = 0;
        
        for (List<BGAIndividual> l : individualsByFitness.values())
        {
            for (BGAIndividual i : l)
            {
                //System.out.print(i.getFitness() + " : ");
                
                if (k < (BGAConstants.N_INDIVIDUALS >> 2))
                {
                    //System.out.println(k * 2);
                    tmpIndividuals[k * 2] = i;
                    k++;
                }
                else if (k < (BGAConstants.N_INDIVIDUALS >> 1))
                {
                    tmpIndividuals[(k - (BGAConstants.N_INDIVIDUALS >> 2)) * 2 + 1] = i;
                    //System.out.println((k - (BGAConstants.N_INDIVIDUALS >> 2)) * 2 + 1);
                    k++;
                }
                else
                {
                    //System.out.println(k);
                    tmpIndividuals[k] = i;
                    k++;
                }
            }
        }
        
        // enfants
        for (int i = 0 ; i < (BGAConstants.N_INDIVIDUALS >> 2) ; i++)
        {
            //System.out.println("father : " + (i << 1));
            //System.out.println("mother : " + ((i << 1) + 1));
            crossOver.execute(tmpIndividuals[(i << 1) + 1].getCurSequence(), tmpIndividuals[(i << 1)].getCurSequence());
            
            //System.out.println("son0 : " + ((BGAConstants.N_INDIVIDUALS >> 1) + (i << 1) ));
            //System.out.println("son1 : " + ((BGAConstants.N_INDIVIDUALS >> 1) + (i << 1)  + 1));
            tmpIndividuals[(i << 1)].reset(crossOver.getMutator().xor(tmpIndividuals[(i << 1)].getCurSequence()));
            tmpIndividuals[(i << 1) + 1].reset(crossOver.getMutator().xor(tmpIndividuals[(i << 1) + 1].getCurSequence()));
            tmpIndividuals[((BGAConstants.N_INDIVIDUALS >> 1) + (i << 1) )].reset(crossOver.getSon1());
            tmpIndividuals[(BGAConstants.N_INDIVIDUALS >> 1) + (i << 1)  + 1].reset(crossOver.getSon0());
            
        }
        
        
    }
}
