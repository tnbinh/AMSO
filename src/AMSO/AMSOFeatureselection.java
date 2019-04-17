package AMSO;

import java.util.ArrayList;
import java.util.List;

import fs.Featureselection;
import fs.MyClassifier;
import myUtils.SU;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class AMSOFeatureselection extends Featureselection {

	SU _su = null;
	public int method = 1; //1: acc+distance, 2: CFS
	/**
	 *
	 * @throws Exception
	 */
	@Override
	public double fitness(List<Double> position) {

		double fit = 0.0;
		try {
			int[] selfeatIdx = selFeaIdx(position);
			Remove delTransform = new Remove();
			delTransform.setInvertSelection(true);
			delTransform.setAttributeIndicesArray(selfeatIdx);
			delTransform.setInputFormat(this.getTraining());
			Instances new_train = Filter.useFilter(this.getTraining(), delTransform);

			int max_fold = AMSOMain.FITN_M_FOLD;
			int k_nn = AMSOMain.K_NN;
			double[] measure = CalculateDistanceAndmFCVBalAcckNN(new_train, k_nn, max_fold);
			fit = 0.8 * measure[0] + 0.2 * measure[1];

		} catch (Exception e) {
			e.printStackTrace();
		}
		return fit;
	}

	public void setSU(SU su) {
		_su = su;
	}

	/**
	 *
	 * @param data
	 * @param k: The k of KNN
	 * @param max_fold: The m of m-fold Cross validation
	 * @return
	 */
	public static double[] CalculateDistanceAndmFCVBalAcckNN(Instances data, int k, int max_fold) {
		double[][] confusion_matrix = new double[data.numClasses()][data.numClasses()];

		//1. calculate distance between pair of instances
		int nbr_inst = data.numInstances();
		double[][] pair_dis = new double[nbr_inst][nbr_inst];

		//Using Manhattan distance
		for (int i = 0; i < nbr_inst - 1; i++) {
			for (int j = i+1; j < nbr_inst; j++) {
				//calcuate distance between instance i and j
				double distance = 0;
				for(int m=0; m< data.numAttributes() - 1; m++){
					double diff = data.instance(i).value(m) - data.instance(j).value(m);
					distance += Math.abs(diff);
				} //end all attribute
				pair_dis[i][j] = pair_dis[j][i] = distance/(data.numAttributes() - 1);
			}//end for instance j
		}//end for instance i

		//2. calculate the d_b and d_w
		double d_b = 0, d_w = 0;

		for(int i = 0; i < nbr_inst; i++) { //for each instance calculate the min_d_b and max_d_w and add to d_b and d_w
			double min_d_b = 1, max_d_w = 0;

			for(int j = 0; j < nbr_inst; j++){
				if(i!=j) {
					if(data.instance(i).classValue() == data.instance(j).classValue()) {//same class => update max_d_w
						if (pair_dis[i][j] > max_d_w)
							max_d_w = pair_dis[i][j];
					}
					else { //different class => update min_d_b
						if (pair_dis[i][j] < min_d_b)
							min_d_b = pair_dis[i][j];
					}
				}
			}
			d_b += min_d_b;
			d_w += max_d_w;
		} // end all instances
		d_b /= nbr_inst;
		d_w /= nbr_inst;

		double distance = 1.0 / (1.0 + Math.exp(-5.0 * (d_b - d_w)));

		//Calculate mFCV with kNN, given max_fold and k
		int fold_ins_start_ind[] = new int[max_fold + 1]; //+1 because I want to use it as a marker to stop.
		for(int i = 0; i < fold_ins_start_ind.length-1; i++){
			fold_ins_start_ind[i] = i * nbr_inst/max_fold;
		}
		fold_ins_start_ind[fold_ins_start_ind.length-1] = nbr_inst;

		for(int test_fold = 0; test_fold < max_fold; test_fold++){

			for(int i = fold_ins_start_ind[test_fold]; i < fold_ins_start_ind[test_fold+1]; i++) { //for each test instance
				List<Double> nearest_distance = new ArrayList<Double>();
				List<Integer> nearest_instance = new ArrayList<Integer>();
				double k_max_distance = Double.MAX_VALUE;

				for(int train_fold = 0; train_fold < max_fold; train_fold++){
					if (train_fold != test_fold){
						for(int j = fold_ins_start_ind[train_fold]; j < fold_ins_start_ind[train_fold+1]; j++) { //for each training instance
							//update k nearest neighbour
							if (pair_dis[i][j] < k_max_distance) {
								nearest_distance.add(pair_dis[i][j]);
								nearest_instance.add(j);

								if(nearest_distance.size()> k) { //remove the furthest one
									int the_furthest_instance_pos = 0;
									for (int l = 1; l<nearest_distance.size(); l++) {
										if (nearest_distance.get(l) > nearest_distance.get(the_furthest_instance_pos)) {
											the_furthest_instance_pos = l;
										}
									}
									k_max_distance = nearest_distance.get(the_furthest_instance_pos);
									nearest_distance.remove(the_furthest_instance_pos);
									nearest_instance.remove(the_furthest_instance_pos);
								}
							}
						}
					}
				}//end of train fold

				//Voting
				int[] class_vote = new int[data.numClasses()];
				for (int l = 0; l<nearest_distance.size(); l++) {
					class_vote[(int)data.instance(nearest_instance.get(l)).classValue()]++;
				}
				//Find the highest vote
				int highest_vote_class = 0;
				for(int l= 1; l<class_vote.length; l++)
					if (class_vote[l] > class_vote[highest_vote_class])
						highest_vote_class = l;

				confusion_matrix[(int)data.instance(i).classValue()][highest_vote_class] += 1;
			} // end all test instances
		}//end all folds
		return new double[]{MyClassifier.unbalanceAcc(confusion_matrix), distance};

	}

	public int[] selFeaIdx(int[] useless_features) {
		int nbr_fea = getDimension() - useless_features.length;

		int[] selfeatIdx = new int[nbr_fea + 1]; //plus one for the class attribute

		int useless_features_ind = 0, useful_features_ind = 0;
		for (int i = 0; i < getDimension(); i++) {
			if (useless_features_ind < useless_features.length && i == useless_features[useless_features_ind]) {//skip this feature
				useless_features_ind ++;
			}else {
				selfeatIdx[useful_features_ind++] = i;
			}
		}
		selfeatIdx[useful_features_ind] = getDimension();  //the class index is the dimension of data

		return selfeatIdx;
	}
}
