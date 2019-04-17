/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fs;

import java.util.List;
import myUtils.WekaDatasetHandle;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author xuebing
 */
public class Featureselection extends Problem {

	//	double suic[];
	public Featureselection() {
		System.out.println("Featureselection ");
		setMinimization(false);
		setMaxDomain(1.0);
		setMinDomain(0.0);
		setMaxVelocity(0.6);
		setMinVelocity(-0.6);
		setThreshold(0.6);
		System.out.println("setThreshold(0.6);");
		setBinaryContin(Boolean.FALSE);
	}

	/**
	 * Return the fitness value by apply classification algo. on the 9 training folds keeping in problem object
	 * @throws Exception
	 */
	public double fitness(List<Double> position) {

		double fit = 0.0;
		try {
			int[] selfeatIdx = selFeaIdx(position);
			Remove delTransform = new Remove();
			delTransform.setInvertSelection(true);
			delTransform.setAttributeIndicesArray(selfeatIdx);
			delTransform.setInputFormat(this.getTraining());
			Instances new_train = Filter.useFilter(this.getTraining(), delTransform);

			fit = WekaDatasetHandle.CalculateLOOCVBalAcc(new_train);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return fit;
	}

	/**
	 * Return the number of selected feature by counting the position that has value > threshold.
	 * @param position of the particle
	 * @return the number of selected feature.
	 */
	@Override
	public int subsetSize(List<Double> position) {
		int size = 0;

		for (int j = 0; j < position.size(); j++) {
			if (position.get(j) >= this.getThreshold()) {
				size++;
			}
		}
		return size;
	}

	/**
	 * Return the indexes of selected features
	 */
	@Override
	public int[] selFeaIdx(List<Double> position) {
		int nbr_fea = 0;
		for (int i=0; i < position.size(); i++)
			if (position.get(i) >= this.getThreshold())
				nbr_fea++;

		int[] selfeatIdx = new int[nbr_fea + 1]; //plus one for the class attribute
		int i, j;

		for (i = 0, j = 0; i < position.size(); i++) {
			if (position.get(i) >= this.getThreshold()) {
				selfeatIdx[j++] = i;
			}
		}
		selfeatIdx[j] = getDimension();  //the class index is the dimension of data

		return selfeatIdx;
	}

	public boolean selectedFeature(double value){

		if (value >= this.getThreshold())
			return true;
		return false;
	}

}
