/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package psofs;

import myUtils.RandomBing;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author xuebing
 */
public class Particle {

    protected List<Double> _position;
    protected List<Double> _velocity;
    protected List<Double> _personal_position;
    protected List<Double> _neighborhood_position;
    protected double _fitness;
    protected double _personal_fitness;
    protected double _neighborhood_fitness;

    private double _accuracy; //in case that fitness is not accuracy
    private double _pbest_accuracy; //in case that fitness is not accuracy

    /**
     *
     */
    public Particle() {
    	_position = new ArrayList<Double>();
        _velocity = new ArrayList<Double>();
        _personal_position = new ArrayList<Double>();
        _neighborhood_position = new ArrayList<Double>();
    }

/**
 * Copy particle
 */
    public void copyParticle(Particle p){
        setSize(p._position.size());

        for(int i = 0; i < p._position.size(); i++){
            _position.set(i, p._position.get(i));
            _velocity.set(i, p._velocity.get(i));
            _personal_position.set(i, p._personal_position.get(i));
            _neighborhood_position.set(i, p._neighborhood_position.get(i));
        }
        _fitness = p._fitness;
        _personal_fitness = p._personal_fitness;
        _neighborhood_fitness = p._neighborhood_fitness;
    }
    /**
     *
     * @param size
     */
    public void setSize(int size) {
        _position.clear();
        _velocity.clear();
        _personal_position.clear();
        _neighborhood_position.clear();
        for (int i = 0; i < size; ++i) {
            _position.add(0.0);
            _velocity.add(0.0);
            _personal_position.add(0.0);
            _neighborhood_position.add(0.0);
        }
    }

    /**
     *
     * @return
     */
    public int getSize() {
        return _position.size();
    }

    public double getAccuracy() {
        return _accuracy;
    }

    public double getPbestAccuracy() {
        return _pbest_accuracy;
    }

    public void setPbestAccuracy(double _pbest_accuracy) {
        this._pbest_accuracy = _pbest_accuracy;
    }


    public void setAccuracy(double _accuracy) {
        this._accuracy = _accuracy;
    }

    /**
     *
     * @param index
     * @param value
     */
    public void setPosition(int index, double value) {
        this._position.set(index, value);
    }

    /**
     *
     * @param index
     * @return
     */
    public double getPosition(int index) {
        return _position.get(index);
    }

    /**
     *
     * @return
     */
    public List<Double> getPosition() {
        return _position;
    }

    /**
     *
     * @return
     */
    public List<Double> getNeighborhoodPosition() {
        return _neighborhood_position;
    }

    /**
     *
     * @return
     */
    public List<Double> getPersonalPosition() {
        return _personal_position;
    }

    /**
     *
     * @param index
     * @param value
     */
    public void setVelocity(int index, double value) {
        _velocity.set(index, value);
    }

    /**
     *
     * @param index
     * @return
     */
    public double getVelocity(int index) {
        return _velocity.get(index);
    }

    /**
     *
     * @return
     */
    public double getFitness() {
        return _fitness;
    }

    /**
     *
     * @param fitness
     */
    public void setFitness(double fitness) {
        this._fitness = fitness;
    }

    /**
     *
     * @param index
     * @param value
     */
    public void setPersonalPosition(int index, double value) {
        _personal_position.set(index, value);
    }

    /**
     *
     * @param index
     * @return
     */
    public double getPersonalPosition(int index) {
        return _personal_position.get(index);
    }

    /**
     *
     * @return
     */
    public double getPersonalFitness() {
        return _personal_fitness;
    }

    /**
     *
     * @param fitness_best_personal
     */
    public void setPersonalFitness(double fitness_best_personal) {
        _personal_fitness = fitness_best_personal;
    }

    /**
     *
     * @param index
     * @param value
     */
    public void setNeighborhoodPosition(int index, double value) {
        this._neighborhood_position.set(index, value);
    }

    /**
     *
     * @param index
     * @return
     */
    public double getNeighborhoodPosition(int index) {
        return _neighborhood_position.get(index);
    }

    /**
     *
     * @return
     */
    public double getNeighborhoodFitness() {
        return _neighborhood_fitness;
    }

    /**
     *
     * @param fitness_best_neighbor
     */
    public void setNeighborhoodFitness(double fitness_best_neighbor) {
        this._neighborhood_fitness = fitness_best_neighbor;
    }

}
