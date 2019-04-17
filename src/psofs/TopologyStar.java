/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package psofs;


/**
 *
 * @author xuebing
 */
public class TopologyStar extends Topology {

    public TopologyStar(){
        System.out.println("Star Topology");
    }

    public int share(Swarm s) {
    	int gbest_idx=0, best_size = s.getProblem().subsetSize(s.getParticle(0).getPosition());

//      Find the particle with the best pbest
//        Particle best_particle = s.getParticle(0);
        double best_fitness = s.getProblem().getWorstFitness();
        for (int i = 0; i < s.numberOfParticles(); ++i) {
            if  (s.getProblem().isBetter(s.getParticle(i).getPersonalFitness(), best_fitness)
                || ( (best_fitness == s.getParticle(i).getPersonalFitness()) &&
                       ( s.getProblem().subsetSize(s.getParticle(i).getPersonalPosition()) < best_size) )  )
                {
//                best_particle = s.getParticle(i);
                best_fitness = s.getParticle(i).getPersonalFitness();
                best_size = s.getProblem().subsetSize(s.getParticle(i).getPosition());
                gbest_idx = i;
            }
        }
        //Copy this pbest as gbest of all particles.
        for (int i = 0; i < s.numberOfParticles(); ++i) {
            Particle p_i = s.getParticle(i);
            for (int j = 0; j < p_i.getSize(); ++j) {
                p_i.setNeighborhoodPosition(j, s.getParticle(gbest_idx).getPersonalPosition(j));
                p_i.setNeighborhoodFitness(best_fitness);
            }

        }

        return gbest_idx;
    }



}
