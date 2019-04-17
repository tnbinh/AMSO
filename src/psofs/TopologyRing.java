/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package psofs;

import myUtils.maths.NewMath;

/**
 *
 * @author xuebing
 */
public final class TopologyRing extends Topology {

    private int _neighbors = 2;

    /**
     *
     */
    public TopologyRing() {
    }

    /**
     *
     * @param n
     */
    public TopologyRing(int n) {
        setNeighbors(n);
        System.out.println("Topology topology = new TopologyRing(n);  n=" + n);
    }

    /**
     * Share the neighborhood best for particle
     * and return the index of the best particle (gbest index)
     * @param s
     */
    public int share(Swarm s) {

        for (int i = 0; i < s.numberOfParticles(); ++i) {
            Particle p_i = s.getParticle(i);

            int best_neighbor_idx = i;
            double nbh_best_fitness = s.getProblem().getWorstFitness(); //p_i.getPersonalFitness();

//            double best_fitness = p_i.getNeighborhoodFitness();  // This does not work !!! be careful
            //find best neighbor of p_i
            for (int j = -getNeighbors() / 2; j <= getNeighbors() / 2; ++j) {

                int neighbor = NewMath.ModEuclidean(i + j, s.numberOfParticles());
                if ( (s.getProblem().isBetter(s.getParticle(neighbor).getPersonalFitness(), nbh_best_fitness))
                		/*|| ( (s.getParticle(neighbor).getPersonalFitness() == nbh_best_fitness ) &&
                                ( s.getProblem().subsetSize(s.getParticle(neighbor).getPersonalPosition()) <
                                s.getProblem().subsetSize(s.getParticle(best_neighbor_idx).getPersonalPosition()) ) )*/ )
                		{
                	best_neighbor_idx = neighbor;
                    nbh_best_fitness = s.getParticle(best_neighbor_idx).getPersonalFitness();
                }
            }

            //update my best neighbor
            p_i.setNeighborhoodFitness(nbh_best_fitness);
            Particle best_neighbor = s.getParticle(best_neighbor_idx);
            for (int n = 0; n < p_i.getSize(); ++n) {
                p_i.setNeighborhoodPosition(n, best_neighbor.getPersonalPosition(n));
            }

          //update global best index (gbest)
            if (s.getProblem().isBetter(best_neighbor.getPersonalFitness(),
            		s.getParticle(s.getGbest_idx()).getPersonalFitness())) {
            	s.setGbest_idx(best_neighbor_idx);
            }

        }
        return s.getGbest_idx();
    }

    /**
     * Get number of neighbors
     * @return
     */
    public int getNeighbors() {
        return _neighbors;
    }

    /**
     * Set number of neighbors
     * @param neighbors
     */
    public void setNeighbors(int neighbors) {
        this._neighbors = neighbors;
    }

}

