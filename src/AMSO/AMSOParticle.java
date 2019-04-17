package AMSO;

import psofs.Particle;

public class AMSOParticle extends Particle {

	public AMSOParticle() {
		super();
	}

	/**
	 * Construct this particle based on the given particle but not copying the given useless features
	 * @param particle
	 * @param uselessFeatures
	 */
	public AMSOParticle(AMSOParticle particle, int [] uselessFeatures) {

		//setSize(particle._position.size() - uselessFeatures.length);

		int k = 0;
        for(int i = 0; i < particle._position.size(); i++){
        	if (k < uselessFeatures.length && i == uselessFeatures[k]) {  //skip copy the useless feature
        		k++;
        	} else {
        		_position.add(particle._position.get(i));
        		_velocity.add(particle._velocity.get(i));
        		_personal_position.add(particle._personal_position.get(i));
        		_neighborhood_position.add(particle._neighborhood_position.get(i));
        	}
        }
        _fitness = particle._fitness;
        _personal_fitness = particle._personal_fitness;
        _neighborhood_fitness = particle._neighborhood_fitness;
	}

	public void remove_pos(int i) {
		_position.remove(i);
		_velocity.remove(i);
		_personal_position.remove(i);
		_neighborhood_position.remove(i);
	}

	public void add_pos() {
		_position.add(0.0);
		_velocity.add(0.0);
		_personal_position.add(0.0);
		_neighborhood_position.add(0.0);
	}
}
