/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.solver.bga;

/**
 *
 * @author onio
 */
// ******************************************************************************
// NOTE : LA FITNESS DEVRAIT ETRE LA FITNESS DE G COMBINEE AU NOMBRE DITERATIONS
// QUI ONT CONDUIT A UNE AMELIORATION (afin de degager les individus qui
// evoluent comme des merdes (foutre un compteur interne))
// NOTE : LA PSO BINAIRE NEST PEUT ETRE PAS ADAPTEE
// ******************************************************************************
public class BGAStdEvaluator implements BGAIndividualEvaluator {

    @Override
    public void evaluate(BGAIndividual inIndividual) {
        inIndividual.setFitness(inIndividual.getBpso().getCore().getGbest().getFitness());
    }
    
}
