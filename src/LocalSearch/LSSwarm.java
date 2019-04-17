package LocalSearch;

import myUtils.SU;
import psofs.Particle;
import psofs.Swarm;

public class LSSwarm extends Swarm{
	//For local search:
	protected double _percentFlip;
	public SU _su;
	public int COUNT_LS_FOUND_PBEST = 0;
	public int TOTAL_LS_CALL = 0;

	public LSSwarm(double percent_flip){
		_percentFlip = percent_flip;
		//			System.out.printf("Local Search Pbest Swarm with %.2f%% flip \n",percent_flip);
	}

	public void prepareLS(){
		_su = new SU(getProblem().getTraining());
	}

	public boolean updateFitnessAndLSPbest(boolean local_search, int LS_max_times){

		boolean have_new_gbest = false;
		for (int i = 0; i < numberOfParticles(); ++i) {
			Particle p_i = getParticle(i);

			if (getProblem().subsetSize(p_i.getPosition()) == 0 )
				p_i.setFitness(getProblem().getWorstFitness());
			else
			{
				double new_fitness = getProblem().fitness(p_i.getPosition());
				p_i.setFitness(new_fitness);

				//Check if new position is better than personal position...
				double is_better = getProblem().compare(p_i.getFitness(), p_i.getPersonalFitness());
				if (( is_better > 0) ||
						( (is_better == 0 ) && //equal fitness
								(getProblem().subsetSize(p_i.getPosition())
										< getProblem().subsetSize(p_i.getPersonalPosition()) )) )  //smaller size
				{ //update pbest
					p_i.setPersonalFitness(new_fitness);
					for (int j = 0; j < p_i.getSize(); ++j) {
						p_i.setPersonalPosition(j, p_i.getPosition(j));
					}

					if (local_search) {
						TOTAL_LS_CALL ++;
						KNNopt myknn = new KNNopt(1, getProblem().getTraining(), 123);

						boolean[] selected_pbest = getProblem().positionToBinarySubset(p_i.getPersonalPosition());
						//	                     int flip_size = (int) Math.round(getProblem().subsetSize(p_i.getPosition()) * percentFlip);

						SolutionPack new_pbest = myknn.localsearchSU(_su, selected_pbest,
								p_i.getPersonalFitness(), _percentFlip, LS_max_times, true, getProblem());

						//check to copy new solution to pbest
						if (new_pbest != null) {
							COUNT_LS_FOUND_PBEST ++;
							p_i.setPersonalFitness(new_pbest.acc);

							for (int j = 0; j < p_i.getSize(); ++j) {
								if (new_pbest.sol[j]) {
									p_i.setPersonalPosition(j, 1);
								} else {
									p_i.setPersonalPosition(j, 0);
								}
							}

						}
					}//end if local search
					//check to update gbest
					Particle gbest = this.getGbest();
					is_better = getProblem().compare(p_i.getPersonalFitness(), gbest.getPersonalFitness());
					if (( is_better > 0) ||
							( (is_better == 0 ) && //equal fitness
									(getProblem().subsetSize(p_i.getPosition())
											< getProblem().subsetSize(gbest.getPersonalPosition()) )) ) {
						gbest.copyParticle(p_i);
						have_new_gbest = true;
					}
				}
			}
		}// end all particles
		return have_new_gbest;
	}

}
