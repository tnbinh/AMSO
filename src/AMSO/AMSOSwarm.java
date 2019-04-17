package AMSO;

import LocalSearch.KNNopt;
import LocalSearch.LSSwarm;
import LocalSearch.SolutionPack;
import edu.princeton.cs.algs4.IndexMaxPQ;
import edu.princeton.cs.algs4.IndexMinPQ;
import myUtils.maths.NewMath;
import psofs.Initialisation;
import psofs.Particle;
import weka.core.Utils;

public class AMSOSwarm extends LSSwarm {

	//to divide particles into different sizes
	protected int _nbr_division = 0;
	protected int _max_size = 0;

	public AMSOSwarm(double percent_flip){
		super(percent_flip);
	}

	public void initParticle(AMSOParticle par, int type) {

		double[] position = new double[par.getSize()];
		if (type == 0) //normal
			position = Initialisation.NormalInitialisation(par.getSize(), getProblem());
		else //type ==1 //all position is 1
			for (int d = 0; d < par.getSize(); ++d) {
				position[d] = 1.0;
			}

		for (int d = 0; d < par.getSize(); ++d) {
			par.setPosition(d, position[d]);
			par.setPersonalPosition(d, position[d]);

			double velocity = NewMath.Scale(0, 1, _random.nextDouble(), getProblem().getMinVelocity(), getProblem().getMaxVelocity());
			par.setVelocity(d, velocity);
			par.setPersonalFitness(getProblem().getWorstFitness());
		}
	}

	public void initialize(int size_division, int number_of_particles) {
		_nbr_division = size_division;
		_max_size = getProblem().getDimension();

		for (int i = 0; i < _nbr_division; ++i) {
			AMSOParticle p = new AMSOParticle();
			p.setSize((i+1) * getProblem().getDimension()/_nbr_division);
			addParticle(p);
			initParticle(p, 1); //add one particle with all 1s in position (i.e. select all features)
//			_lbest[i] = new AMSOParticle();
//			_lbest[i].copyParticle(p);
			for(int j = 1; j < number_of_particles/ _nbr_division; ++j) {
				p = new AMSOParticle();
				p.setSize((i+1) * getProblem().getDimension()/_nbr_division);
				addParticle(p);
				initParticle(p, 0);
			}
		}
		_gbest = new AMSOParticle();
		_gbest.copyParticle(getParticle(0));
	}

	public boolean updateFitnessAndLSPbest(boolean local_search, int LS_max_times) {

		boolean have_new_gbest = false;

		for (int i = 0; i < _nbr_division; ++i) {
			int start_particle_idx = i * _swarm.size()/_nbr_division;
			for(int j = 0; j < _swarm.size()/_nbr_division; ++j) {
				AMSOParticle par_i = (AMSOParticle) getParticle(start_particle_idx + j);
				if (getProblem().subsetSize(par_i.getPosition()) == 0 )
					par_i.setFitness(getProblem().getWorstFitness());
				else
				{
					double new_fitness = getProblem().fitness(par_i.getPosition());
					par_i.setFitness(new_fitness);

					//Check if new position is better than personal position...
					double is_better = getProblem().compare(par_i.getFitness(), par_i.getPersonalFitness());
					if (( is_better > 0) ||
							( (is_better == 0 ) && //equal fitness
									(getProblem().subsetSize(par_i.getPosition())
											< getProblem().subsetSize(par_i.getPersonalPosition()) )) )  //smaller size
					{ //update pbest
						par_i.setPersonalFitness(par_i.getFitness());
						for (int d = 0; d < par_i.getSize(); ++d) {
							par_i.setPersonalPosition(d, par_i.getPosition(d));
						}

						if (local_search) {
							TOTAL_LS_CALL ++;
							KNNopt myknn = new KNNopt(1, getProblem().getTraining(), 123);

							boolean[] selected_pbest = getProblem().positionToBinarySubset(par_i.getPersonalPosition());
							//                     int flip_size = (int) Math.round(getProblem().subsetSize(p_i.getPosition()) * percentFlip);

							SolutionPack new_pbest = myknn.localsearchSU(_su, selected_pbest,
									par_i.getPersonalFitness(), _percentFlip, LS_max_times, true, getProblem());

							//check to copy new solution to pbest
							if (new_pbest != null) {
								COUNT_LS_FOUND_PBEST ++;
								par_i.setPersonalFitness(new_pbest.acc);

								for (int d = 0; d < par_i.getSize(); d++) {
									if (new_pbest.sol[d]) {
										par_i.setPersonalPosition(d, 1);
									} else {
										par_i.setPersonalPosition(d, 0);
									}
								}
								//The following code used to check the accuracy of new pbest
								//=> RECOMMEND TO RUN WHEN DEBUG, NOT WHEN DEPLOY since it is costly
								double fitn = getProblem().fitness(par_i.getPersonalPosition());
		                         if( (int)(fitn * 10000) != (int)(new_pbest.acc*10000)){
		             				System.out.printf("\nLS fitn = %.4f <> fitn (%.4f)\n", new_pbest.acc, fitn);
		             				getProblem().fitness(par_i.getPersonalPosition());

		                         }

							}
						}//end local search

						//check to update gbest
						is_better = getProblem().compare(par_i.getPersonalFitness(), _gbest.getPersonalFitness());
						if (( is_better > 0) ||
								( (is_better == 0 ) && //equal fitness
										(getProblem().subsetSize(par_i.getPosition())
												< getProblem().subsetSize(_gbest.getPersonalPosition()) )) ) {
							_gbest.copyParticle(par_i);
							have_new_gbest = true;
						}


					}

				}


			}//end all partilces in one division
		}//end all division

		return have_new_gbest;
	}

	public void updateSwarm() {

		System.out.println("Current Max_size is " + _max_size + ", Gbest length is " + _gbest.getSize());

		if (_max_size != _gbest.getSize()) {
			int [] size = new int [_nbr_division];
			IndexMinPQ sort = new IndexMinPQ(_nbr_division);
			for (int i = 0; i < _nbr_division; ++i) {
				int start_particle_idx = i * _swarm.size()/_nbr_division;
				sort.insert(i,getParticle(start_particle_idx).getSize());
			}
			ResizeParticles(sort);
		}
		else {
			System.out.println("GBest size unchanged");
		}
	}

	private void ResizeParticles(IndexMinPQ sort) {

			_max_size = _gbest.getSize();
			System.out.println("Resize all particles to the best size " + _max_size);
			int k = 1;
			while (!sort.isEmpty()){
				int div_idx = sort.minIndex();
				int div_len = (int) sort.minKey();
				sort.delMin();
				if (div_len != _max_size) {
					int new_size = k * _max_size / _nbr_division;
					for(int j = 0; j < _swarm.size()/_nbr_division; ++j) {
						AMSOParticle par_i = (AMSOParticle) getParticle(div_idx * _swarm.size()/_nbr_division + j);
						int cur_size = par_i.getSize();
						if ( cur_size > new_size) //remove the last positions
							for (int d = cur_size-1; d>= new_size; d--) {
								par_i.remove_pos(d);
							}
						else if (cur_size < new_size)  //append more positions
							for (int d = cur_size; d < new_size; d++) {//remove the last positions
								par_i.add_pos();
								par_i.setPosition(d, _random.nextDouble());
								par_i.setVelocity(d, NewMath.Scale(0, 1, _random.nextDouble(), getProblem().getMinVelocity(), getProblem().getMaxVelocity()));
							}
					}

					k++;
				}

			}



	}


	/**
	 * * @return current max length of particles in the whole swarm
	 */
	public int get_max_size() {
		return _max_size;
	}

	@Override
	public void updateVelocityPosition(double w) {

		//For each division
		for (int div = 0; div < _nbr_division; ++div) {
			int div_size = _swarm.size()/_nbr_division;
			int start_particle_idx = div * div_size;

			//Calculate mean position of all particles in this division
			int len = getParticle(start_particle_idx).getSize();

			IndexMinPQ<Double> rnd_val = new IndexMinPQ<Double>(_swarm.size());
			for (int i = 0; i < div_size; i++) {
				rnd_val.insert(start_particle_idx + i, _random.nextDouble());
			}
			//Pairwise comparison
			//The loser will learn from the winner, the winner does not change
			for (int i = 0 ; i < div_size / 2; ++i) {
				int idxLoser = rnd_val.delMin();
                int idxWinner = rnd_val.delMin();
                double fitn_Loser = getParticle(idxLoser).getFitness();
                double fitn_Winner= getParticle(idxWinner).getFitness();
                int len_Loser = getParticle(idxLoser).getSize();
                int len_Winner= getParticle(idxWinner).getSize();
                if ((fitn_Loser > fitn_Winner) || (Utils.eq(fitn_Loser, fitn_Winner) && (len_Loser < len_Winner))) {
                	int tmp = idxLoser ;
                    idxLoser = idxWinner;
                    idxWinner = tmp;
                }
				updateVelocity(idxLoser, idxWinner, w);
				getVelocityClamp().clamp(getParticle(idxLoser), getProblem().getMaxVelocity(), getProblem().getMinVelocity());
				updatePosition(idxLoser);
			}
		}


	}

	/**
    * update loser based on winner and lbest.
    * @param w
    */
   public void updateVelocity(int loser, int winner, double w) {
   	Particle lose = getParticle(loser);
   	Particle win = getParticle(winner);


   	for (int d = 0; d < lose.getSize(); ++d) {
   		   double v_i = w * lose.getVelocity(d);
           v_i += getC1() * getRandom().nextDouble() * (win.getPersonalPosition(d) - lose.getPosition(d));
//           v_i += getC2() * getRandom().nextDouble() * (lbest.getPersonalPosition(d) - lose.getPosition(d));
           lose.setVelocity(d, v_i);
		}
   }


}
