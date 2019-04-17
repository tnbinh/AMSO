/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package psofs;

import myUtils.maths.NewMath;
import myUtils.RandomBing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import fs.Problem;

//import myUtils.pickFeatures;



/**
 *
 * @author xuebing
 */
public class Swarm {

	public static int count=0;

    protected Problem _problem;
    private VelocityClamp _velocityClamp;
    private Topology _topology;
    protected List<Particle> _swarm = new ArrayList<Particle>();
    protected Random _random = RandomBing.Create();
    private Initialisation _initialisation;
    int gbest_idx = 0;
    double _c1, _c2;
	private double _inertia;
	protected Particle _gbest;

	public Particle getGbest() {
		return _gbest;
	}
    /**
    *
    * @return
    */
   public double getC1() {
       return _c1;
   }

   /**
    *
    * @param c1
    */
   public void setC1(double c1) {
       this._c1 = c1;
   }

   /**
    *
    * @return
    */
   public double getC2() {
       return _c2;
   }

   /**
    *
    * @param c2
    */
   public void setC2(double c2) {
       this._c2 = c2;
   }

    public int getGbest_idx() {
		return gbest_idx;
	}

	public void setGbest_idx(int gbest_idx) {
		this.gbest_idx = gbest_idx;
	}

	public Swarm() {
    }

    public Problem getProblem() {
        return _problem;
    }

    public void setProblem(Problem problem) {
        this._problem = problem;
    }

    public Particle getParticle(int index) {
        return _swarm.get(index);
    }

    public void addParticle(Particle p) {
        _swarm.add(p);
    }

    public int numberOfParticles() {
        return _swarm.size();
    }

    public Random getRandom() {
        return _random;
    }

    public void initialize(int number_of_particle) {

        for (int i = 0; i < number_of_particle; ++i) {
        	Particle p = new Particle();
			p.setSize(getProblem().getDimension());
			addParticle(p);
            double[] position = Initialisation.NormalInitialisation(p.getSize(), getProblem());
//            System.out.print("\n" + i + ": ");
            for (int d = 0; d < p.getSize(); ++d) {
                p.setPosition(d, position[d]);
                p.setPersonalPosition(d, position[d]);
                p.setNeighborhoodPosition(d, position[d]);

                double velocity = NewMath.Scale(0, 1, _random.nextDouble(), getProblem().getMinVelocity(), getProblem().getMaxVelocity());
                p.setVelocity(d, velocity);
            }
            p.setPersonalFitness(getProblem().getWorstFitness());
            p.setNeighborhoodFitness(getProblem().getWorstFitness());
        }
		_gbest = new Particle();
		_gbest.setSize(getProblem().getDimension());
		_gbest.copyParticle(getParticle(0));
    }
/**
 * Iterate function is only used in Bing's psofs package
 * @param w
 */
    public void iterate(double w) {

//       for selection
        for (int i = 0; i < numberOfParticles(); ++i) {
            Particle p_i = getParticle(i);
            double new_fitness = getProblem().fitness(p_i.getPosition());
            p_i.setFitness(new_fitness);

            //Check if new position is better than personal position...
            if (getProblem().isBetter(new_fitness, p_i.getPersonalFitness())) {
                //update pbest
                p_i.setPersonalFitness(new_fitness);
                for (int j = 0; j < p_i.getSize(); ++j) {
                    p_i.setPersonalPosition(j, p_i.getPosition(j));
                }
            }

        } // end all particles
        int gbest_idx = getTopology().share(this);  // update gbest

        //Update velocity and position
        for (int i = 0; i < numberOfParticles(); ++i) {
            updateVelocity(i, w);
            getVelocityClamp().clamp(getParticle(i), getProblem().getMaxVelocity(), getProblem().getMinVelocity());
            updatePosition(i);
        }

    }

    public void updateFitnessAndPbest() {

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
    			if (( is_better > 0) )

    			{ //update pbest
    				p_i.setPersonalFitness(p_i.getFitness());
    				for (int j = 0; j < p_i.getSize(); ++j) {
    					p_i.setPersonalPosition(j, p_i.getPosition(j));
    				}
    			}
    		}
    	}// end all particles
    }

    public void updateVelocityPosition(double w) {
		for (int i = 0; i < numberOfParticles(); ++i) {
			updateVelocity(i, w);
			getVelocityClamp().clamp(getParticle(i), getProblem().getMaxVelocity(), getProblem().getMinVelocity());
			updatePosition(i);
		}
	}

    public Topology getTopology() {
        return _topology;
    }

    public void setTopology(Topology topology) {
        this._topology = topology;
    }

    /**
     * @return the _VelocityClamp
     */
    public VelocityClamp getVelocityClamp() {
        return _velocityClamp;
    }

    /**
     * @param VelocityClamp the _VelocityClamp to set
     */
    public void setVelocityClamp(VelocityClamp velocityClamp) {
        this._velocityClamp = velocityClamp;
    }

    public Initialisation getInitialisation() {
        return _initialisation;
    }

    public void setInitialisation(Initialisation initialisation) {
        this._initialisation = initialisation;
    }

    public double averageFitness() {
        double sumFit = 0;
        for(int i = 0; i< _swarm.size(); i++)
            sumFit += getParticle(i).getFitness();

        return sumFit/_swarm.size();
    }

        public double averageSize() {
        double sumSize = 0;
        for(int i = 0; i< _swarm.size(); i++)
            sumSize += getProblem().subsetSize(getParticle(i).getPosition());

        return sumSize/_swarm.size();
    }

		public void smallInitialize(int size) {
			for (int i = 0; i < numberOfParticles(); ++i) {
	            Particle p = getParticle(i);
	            double[] position = Initialisation.NInitialisation(p.getSize(), size , false);

	            for (int j = 0; j < p.getSize(); ++j) {
	                p.setPosition(j, position[j]);
	                p.setPersonalPosition(j, position[j]);
	                p.setNeighborhoodPosition(j, position[j]);
	                double velocity = NewMath.Scale(0, 1, RandomBing.Create().nextDouble(), getProblem().getMinVelocity(), getProblem().getMaxVelocity());
	                p.setVelocity(j, velocity);

	                p.setPersonalFitness(getProblem().getWorstFitness());
	                p.setNeighborhoodFitness(getProblem().getWorstFitness());

	            }
	        }

		}

		public void mixInitialize() {
			int half = numberOfParticles() / 2;

			for (int i = 0; i < half; ++i) {
	            Particle p = getParticle(i);
	            double[] position = Initialisation.NInitialisation(p.getSize(), p.getSize()/20 , false);

	            for (int j = 0; j < p.getSize(); ++j) {
	                p.setPosition(j, position[j]);
	                p.setPersonalPosition(j, position[j]);
	                p.setNeighborhoodPosition(j, position[j]);

	                double velocity = NewMath.Scale(0, 1, RandomBing.Create().nextDouble(), getProblem().getMinVelocity(), getProblem().getMaxVelocity());
	                p.setVelocity(j, velocity);

	                p.setPersonalFitness(getProblem().getWorstFitness());
	                p.setNeighborhoodFitness(getProblem().getWorstFitness());

	            }
	        }
			half = numberOfParticles() - half;

			for (int i = 0; i < half; ++i) {
	            Particle p = getParticle(i);
	            double[] position = Initialisation.NormalInitialisation(p.getSize(), getProblem());

	            for (int j = 0; j < p.getSize(); ++j) {
	                p.setPosition(j, position[j]);
	                p.setPersonalPosition(j, position[j]);
	                p.setNeighborhoodPosition(j, position[j]);

	                double velocity = NewMath.Scale(0, 1, RandomBing.Create().nextDouble(), getProblem().getMinVelocity(), getProblem().getMaxVelocity());
	                p.setVelocity(j, velocity);

	                p.setPersonalFitness(getProblem().getWorstFitness());
	                p.setNeighborhoodFitness(getProblem().getWorstFitness());

	            }
	        }
		}

		/**
		 * Binh: This function is used to get all features selected by the top nbr_par in the swarm
		 * @param nbr_par
		 * @return
		 */
		public double[] getFeaturesFromBestParticle(double nbr_par) {
			double[] fea_set = new double[getProblem().getDimension()];

			//sort particles based on fitness
			Collections.sort(_swarm,new Comparator<Particle>(){
                public int compare(Particle p1, Particle p2){
					return (int)((p2.getPersonalFitness() - p1.getPersonalFitness()) * 1000);
                }});

			for(int i = 0; i< fea_set.length; i++){
				//collect features from gbest
				if (_swarm.get(0).getNeighborhoodPosition(i) >= getProblem().getThreshold()) {
					 fea_set[i] = 1.0; //this feature is selected
				 }
				else {
					//collect features from other best top particles
					int par_idx = 0;
					while ( par_idx < nbr_par ) {
						if (_swarm.get(par_idx).getPersonalPosition(i) >= getProblem().getThreshold()) {
							fea_set[i] = 1.0; //this feature is selected
							break;
						}
						else
							par_idx ++;
					}
				}
			}
			return fea_set;
		}

		/**
	     *
	     * @param w
	     */
	    public void updateVelocity(int p, double w) {
	    	Particle par = getParticle(p);

	    	for (int d = 0; d < par.getSize(); ++d) {

				double v_i = w * par.getVelocity(d);
	            v_i += getC1() * getRandom().nextDouble() * (par.getPersonalPosition(d) - par.getPosition(d));
	            v_i += getC2() * getRandom().nextDouble() * (par.getNeighborhoodPosition(d) - par.getPosition(d));
	            par.setVelocity(d, v_i);
			}

	    }

	    /**
	     * */
	    public void updatePosition(int p) {
	    	Particle par = getParticle(p);

	    	for (int d = 0; d < par.getSize(); ++d) {
	            double p_i = par.getPosition(d) + par.getVelocity(d);
	            if (p_i > getProblem().getMaxDomain()) {
	                p_i = getProblem().getMaxDomain();
	            }
	            if (p_i < getProblem().getMinDomain()) {
	                p_i = getProblem().getMinDomain();
	            }

	            par.setPosition(d, p_i);
	        }
	    }

		public double getInertia() {
			return _inertia;
		}

		public void setInertia(double inertia) {
			this._inertia = inertia;
		}


}
