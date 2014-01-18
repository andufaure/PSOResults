/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.pso.decoders;

import isima.cac.M2Auto;
import isima.cac.M2Rules;
import isima.solver.pso.criterion.Criterion;
import isima.solver.pso.PSOConstants;
import isima.solver.pso.PSOPosition;
import isima.solver.pso.decoders.blockops.BlockOpFactory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author onio
 */
public class PSOBlockOpSearchDecoder implements PSOPositionDecoder {
    
    public static final int BLOCK_SIZE = 4;
    public static final int BLOCKOP_LENGTH = 2;
    
    public static final int NB_BLOCKOPS = (512 / BLOCK_SIZE);
    
    public static final int STEP_DATA_LENGTH = 100;
    
    public static final int BLOCK_SEED_0 = 0;
    public static final int BLOCK_SEED_1 = 1;
    public static final int BLOCKOP_START = 2;
    public static final int BLOCKOP_END = BLOCKOP_START + NB_BLOCKOPS * BLOCKOP_LENGTH;
    
    
    public static final int STEP_REF1 = BLOCKOP_END;
    public static final int STEP_REF2 = STEP_REF1 + 1;
        
    public static final int N_DIMS = STEP_REF2 + 1;
    
    public static final BlockOpFactory  opFactory = BlockOpFactory.getInstance();
    
    private transient byte[]                          ruleBlocks = new byte[512];
    private transient TreeMap<Double, List<Integer>>  orders = new TreeMap<Double, List<Integer>>();
    private int                             decode_cnt = 0;
    private Criterion                       criterion = null;
    
    private M2Rules                         bestRules = new M2Rules();
    private double                          bestFitness = Double.MAX_VALUE;
    
    
    public PSOBlockOpSearchDecoder(Criterion inCriterion) throws Exception {
        Arrays.fill(ruleBlocks, (byte)0);
        criterion = inCriterion.buildCopy();
    }
    
    public PSOBlockOpSearchDecoder(PSOBlockOpSearchDecoder inStrat) throws Exception {
        criterion = inStrat.criterion.buildCopy();
        decode_cnt = inStrat.decode_cnt;
        System.arraycopy(inStrat.ruleBlocks, 0, this.ruleBlocks, 0, 512);
    }
    
    @Override
    public PSOPositionDecoder buildCopy() throws Exception {
        return (new PSOBlockOpSearchDecoder(this));
    }
    
    @Override
    public void execute(PSOPosition inPosition) throws Exception {
        
        double[]    pos = inPosition.getData();
        M2Auto      automata = inPosition.getAutomata();
        M2Rules     rules = automata.getRules();
        int         step = 0;
        byte        seed = 0;
        byte        block = 0;
        double      op;
        double      arg;
        double      initial_fitness = 0;
        double      final_fitness = 0;
        
        // decode seed
        seed = (byte)(decodeSeedBlock(pos[BLOCK_SEED_0]) | decodeSeedBlock(pos[BLOCK_SEED_1]) & 15);
        
        seed = decodeSeedBlock(pos[BLOCK_SEED_0]);
        seed = 15;
        block = seed;
        
        // decode step
        step = this.decodeStep(pos);
        
        for (int i = 0 ; i < NB_BLOCKOPS ; i++)
        {
            op = pos[BLOCKOP_START + i * BLOCKOP_LENGTH];
            arg = pos[BLOCKOP_START + (i * BLOCKOP_LENGTH) + 1];
            
            block = PSOBlockOpSearchDecoder.opFactory.getOp(op).execute(block, arg, BLOCK_SIZE);
            this.injectBlock(rules, block, i * BLOCK_SIZE);
        }
        
        
        //this.fillWithBlock(rules, seed);
        automata.reset();
        automata.run(step);
        criterion.calc(inPosition);
        
        
        bestRules.copyContentFrom(rules);
        bestFitness = inPosition.getFitness();
        /*
        initial_fitness = bestFitness;
        
        block = seed;
        
        // methode meilleure
        // générer les blocs selon la methode de NibbleDecoder
        // générer toutes les operations de mutations (16 operations) (maximum 2048 iterations)
        // de la plus faible a la plus forte
        // appliquer plus faible sur block 0, et tant que ameliore ou reste equivalente
        //  alors appliquer la superieure
        // si pas amelioration fin boucle => passer a plus faible sur block 1
        
        // ou alors faire les 16 operations, et prendre, parmis tous les nibble
        // celui qui donne le meilleur resultat (methode de ctor)
        
        
        // autre methode
        // générer la regle
        // pour block 0 à block n
        // -> not tous les bits
        // si amelioration, alors garder, sinon restaurer
        // pour block 1 à block n
        // -> not tous les bits
        // si amelioration, alors garder, sinon restaurer
        // pour block 2 à block n
        // -> not tous les bits
        
        // grace a cette methode, vecteur pso contient un vecteur de 128 double
        // representant l'ordre des blocs a associer a cette descente
        
        // nb_dims : 128 * 2 + 16 = 272 + data_steps
        
        // prevoir aussi une dim qui code un offset de 0, 1, 2 ou 3 pour
        // faire un rotate de la sequence de bits (si methode de recherche locale par not)
        
        // exemple :
        // 0000 1111 2222 3333
        // avec un ROL de 2 donne :
        // 0011 1122 2233 3300
        // le ROL permet de peter le sequencage fermé sous nibble utilisé
        // pour iterer sur les données (voir comment faire par rapport à
        // la table de priorité)
        
        
        for (int i = 0 ; i < NB_BLOCKOPS ; i++)
        {
            op = pos[BLOCKOP_START + i * BLOCKOP_LENGTH];
            arg = pos[BLOCKOP_START + (i * BLOCKOP_LENGTH) + 1];
            
            
            block = this.opFactory.getOp(op).execute(block, arg, BLOCK_SIZE);
            
            this.fillWithBlock(rules, block, i * BLOCK_SIZE);
            automata.reset();
            automata.run(step);
            
            criterion.calc(inPosition);
            
            if (inPosition.getFitness() < bestFitness)
            {
                bestRules.copyContentFrom(rules);
                bestFitness = inPosition.getFitness();
            }
            else
                rules.copyContentFrom(bestRules);
            
        }
        
        final_fitness = bestFitness;
        
        //System.out.println(rules.toString());
        //System.out.println("FITNESS AM : " + (initial_fitness - final_fitness));
        
        rules.copyContentFrom(bestRules);
        automata.reset();
        automata.run(step);
        criterion.calc(inPosition);
        
        //System.out.println(rules.toString());
         * 
         */
    }

    protected byte decodeSeedBlock(double inValue) throws Exception
    {   
        return ((byte)Math.floor(inValue 
                            / ((PSOConstants.POS_MAX - PSOConstants.POS_MIN) 
                                / Math.pow(2, (double)BLOCK_SIZE))));
    }
    
    protected void injectBlock(M2Rules inRules, byte inBlock, int inIndex) throws Exception
    {
        for (int i = BLOCK_SIZE - 1 ; i > 0 ; i--)
            inRules.set(inIndex + ((BLOCK_SIZE - 1) - i), (byte)((inBlock >> i) & 1));
    }
    
    protected void fillWithBlock(M2Rules inRules, byte inBlock, int inStart) throws Exception
    {
        for (int i = inStart ; i < NB_BLOCKOPS ; i++)
            injectBlock(inRules, inBlock, i * BLOCK_SIZE);
    }
    
    protected void fillWithBlock(M2Rules inRules, byte inBlock) throws Exception
    {
        for (int i = 0 ; i < NB_BLOCKOPS ; i++)
            injectBlock(inRules, inBlock, i * BLOCK_SIZE);
    }
    
    protected int decodeStep(double[] pos)
    {
        int step = 0;
        
        if (pos[STEP_REF1] < pos[STEP_REF2])
        {
            step = (int)(PSOConstants.AUTO_MIN_STEPS
                    + ((pos[STEP_REF2] - pos[STEP_REF1]) / pos[STEP_REF2])
                        * (PSOConstants.AUTO_MAX_STEPS - PSOConstants.AUTO_MIN_STEPS));
        }
        else if (pos[STEP_REF1] > pos[STEP_REF2])
        {
            step = (int)(PSOConstants.AUTO_MIN_STEPS
                    + ((pos[STEP_REF1] - pos[STEP_REF2]) / pos[STEP_REF1])
                        * (PSOConstants.AUTO_MAX_STEPS - PSOConstants.AUTO_MIN_STEPS));
        }
        else
            step = PSOConstants.AUTO_MIN_STEPS + ((PSOConstants.AUTO_MAX_STEPS - PSOConstants.AUTO_MIN_STEPS) / 2);
        
        if (step == 0)
            step++;
        
        return (step);
    }

    @Override
    public void reset() {
        decode_cnt = 0;
    }

    @Override
    public void copyContentFrom(PSOPositionDecoder inDecoder) throws Exception {
        
        if (!(inDecoder instanceof PSOBlockOpSearchDecoder))
            throw new Exception("not a PSOBlockDecoder given");
        
        PSOBlockOpSearchDecoder bdec = (PSOBlockOpSearchDecoder)inDecoder;
        criterion.copyContentFrom(bdec.criterion);
        decode_cnt = bdec.decode_cnt;
    }

    @Override
    public String toDescString() {
        return ("Block Op Search Decoder");
    }
    
}
