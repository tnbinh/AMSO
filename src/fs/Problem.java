/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fs;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instances;


/**
 *
 * @author xuebing
 */
public abstract class Problem {

    private boolean _minimization = true;
    private double _threshold;
    private double _max_domain, _min_domain;
    private double _max_velocity, _min_velocity;
    private int _dimension;
//    private int _N;
    private int _numFolds;

    private Classifier _myclassifier;
    private Instances _training_set;
    private Instances _test_set;
    String evalType = "wrapper";

    public String getEvalType() {
		return evalType;
	}

	public void setEvalType(String evalType) {
		this.evalType = evalType;
	}
    private Boolean binaryContin;

    public Problem() {
    }

    public boolean isMinimization() {
        return _minimization;
    }

    public void setMinimization(boolean minimization) {
        this._minimization = minimization;
        System.out.printf("Set minimization is %b\n", minimization);
    }

    public double getMaxDomain() {
        return _max_domain;
    }

    public void setMaxDomain(double max_domain) {
        this._max_domain = max_domain;
        System.out.printf("Set max domain = %.2f\n", max_domain);
    }

    public double getMinDomain() {
        return _min_domain;
    }

    public void setMinDomain(double min_domain) {
        this._min_domain = min_domain;
        System.out.printf("Set min domain = %.2f\n", min_domain);
    }

    /**
     * @return the _max_velocity
     */
    public double getMaxVelocity() {
        return _max_velocity;
    }

    /**
     * @param max_velocity the _max_velocity to set
     */
    public void setMaxVelocity(double max_velocity) {
        this._max_velocity = max_velocity;
        System.out.printf("Set max velocity = %.2f\n", max_velocity);
    }

    /**
     * @return the _min_velocity
     */
    public double getMinVelocity() {
        return _min_velocity;
    }

    /**
     * @param min_velocity the _min_velocity to set
     */
    public void setMinVelocity(double min_velocity) {
        this._min_velocity = min_velocity;
        System.out.printf("Set min velocity = %.2f\n", min_velocity);
    }

    public boolean isBetter(double fitness_a, double fitness_b) {
        return isMinimization() ? fitness_a < fitness_b : fitness_a > fitness_b;
    }

    /**
     * return value >0 if a is better than b, 0 if they are equal.
     * @param fitness_a
     * @param fitness_b
     * @return
     */
    public double compare(double fitness_a, double fitness_b) {
//        return isMinimization() ? fitness_a < fitness_b : fitness_a > fitness_b;
        if (isMinimization()) {
            return fitness_b - fitness_a;
        } else //if is Maximization
        {
            return fitness_a - fitness_b;
        }
    }

    /**
     * @return the _threshold
     */
    public double getThreshold() {
        return _threshold;
    }

    /**
     * @param threshold the _threshold to set
     */
    public void setThreshold(double threshold) {
        this._threshold = threshold;
        System.out.printf("Set threshold = %.4f\n", threshold);
    }

    public int getDimension() {
        return _dimension;
    }

    public void setDimension(int dimension) {
        this._dimension = dimension;
    }

    /**
     * @return the _training
     */
    public Instances getTraining() {
        return _training_set;
    }

    /**
     * @param train the _training to set
     */
    public void setTraining(Instances train) {
        this._training_set = train;
    }

    public double getWorstFitness() {
     //******change this in case of fitness value range change********THE FOLLOWING use for [0..1] case//
        return isMinimization() ? 1 : 0;
    }

    public double getBestFitness() {
    //******change this in case of fitness value range change********THE FOLLOWING use for [0..1] case//
        return isMinimization() ? 0 : 1;
    }

    public abstract double fitness(List<Double> position);
    /**
     * Return the number of selected feature.
     * @param position of the particle
     * @return the number of selected feature.
     */
    public abstract int subsetSize(List<Double> position);

    /**
     * Return an integer array storing the selected feature indexes only
     * @param position
     * @return
     */
    public abstract int[] selFeaIdx(List<Double> position);

    /**
     * Return the number of selected feature.
     * @param position of the particle
     * @return the number of selected feature.
     */
    public abstract boolean selectedFeature(double positionVal);

    /**
     * @return the _numFolds
     */
    public int getNumFolds() {
        return _numFolds;
    }

    /**
     * @param numFoldsCV the _numFolds to set
     */
    public void setNumFolds(int numFoldsCV) {
        this._numFolds = numFoldsCV;
        System.out.printf("Set numFoldsCV = %d\n", numFoldsCV);
    }

    /**
     * @return the binaryContin
     */
    public Boolean getBinaryContin() {
        return binaryContin;
    }

    /**
     * @param binaryContin the binaryContin to set
     */
    public void setBinaryContin(Boolean binaryContin) {
        this.binaryContin = binaryContin;
    }

    /**
     * @return the _myclassifier
     */
    public Classifier getClassifier() {
        return _myclassifier;
    }

    /**
     * @param myclassifier the _myclassifier to set
     */
    public void setClassifier(Classifier myclassifier) {
        this._myclassifier = myclassifier;

    }

    public Instances getTestSet() {
        return _test_set;
    }

    public void setTestSet(Instances test) {
        this._test_set = test;
    }

    public int booleanSubsetSize(boolean[] features) {
        int size = 0;

        for (int j = 0; j < features.length; j++) {
            if (features[j] == true) {
                size++;
            }
        }
        return size;
    }

	public boolean[] positionToBinarySubset(List<Double> position){
        boolean[] subset = new boolean[position.size()];

        for (int i=0; i < position.size(); i++)
            if (position.get(i) < _threshold)
                subset[i] = false;
            else
                subset[i] = true;
        return subset;
    }

}
