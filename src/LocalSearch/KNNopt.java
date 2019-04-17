/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package LocalSearch;

import edu.princeton.cs.algs4.IndexMinPQ;
import fs.MyClassifier;
import fs.Problem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import AMSO.AMSOMain;
import myUtils.SU;
import myUtils.algo.QSort;
import weka.core.Instances;


/**
 *
 * @author
 */
public class KNNopt {
	int K = 3;
	protected Instances dat;
	protected Random rnd;
	protected double[][] insDistance;

	public static void main(String[] args) throws IOException {
		/*        MyDiscDataset dat = new MyDiscDataset("dataset/11_Tumors_GEMS.txt", " ",0);
//        dat.normalise();
         KNNopt myknn = new KNNopt(1, dat, 123);
        for (int iter = 1; iter < 10; iter++) {
            boolean[] selected = myknn.randomSelectedFeature();
            //double acc = myknn.localsearch(selected, myknn.getAccurarySelectedFeatures(selected), 100, 100, false).acc; //myknn.KFoldcrossvalidation(dat.nInstance, selected);
            //System.out.println("++++++++++++++++++++++++ \naccuracy ("  + myknn.K + ") = " + acc);
        }*/
	}

	/**
	 * Constructor of KNNopt
	 * @param k K-neighbours
	 * @param d training dataset
	 */
	public KNNopt(int k, Instances d, int seed) {
		rnd = new Random(seed);
		K = k;
		dat = d;
	}

	/**
	 * Prepare cross distances for selected feature subsets:
	 * Calculate sum distances of all cross instances in the dataset	 *
	 * @param selected
	 * @return the distance matrix
	 */
	protected double[][] preprocessing(Instances data, boolean[] selected) {

		double[][] distances;
		distances = new double[data.numInstances()][data.numInstances()];
		for (int i = 0; i < data.numInstances()-1; i++) {
			for (int j = i+1; j < data.numInstances(); j++) {
				double distance = 0;
				for (int k = 0; k < selected.length; k++) {
					if(selected[k]) { // && (data.instance(j).value(k) != data.instance(i).value(k))){
						double diff = data.instance(j).value(k) - data.instance(i).value(k);
						//Using Euclidean distance
//						distance += diff * diff;
						//Using Manhattan distance
						distance += Math.abs(diff);
						//Overlapping in discrete data
						//						distance += ( data.instance(i).value(k) == data.instance(j).value(k) ? 0 : 1);

					}
				}
				distances[j][i] = distances[i][j] = distance;
			}
		}
		return distances;
	}

	/**
	 * Prepare cross distances for selected feature subsets:
	 * Calculate sum distances of all cross instances in the dataset
	 * Calculate sum of max difference of all selected features
	 * @param selected
	 * @return the distance matrix and the max difference
	 */
	private int preprocessing(Instances data, boolean[] selected, double[][] distances) {

		//		distances = new double[data.numInstances()][data.numInstances()];
		for (int i = 0; i < data.numInstances()-1; i++) {
			for (int j = i+1; j < data.numInstances(); j++) {
				double distance = 0;
				for (int k = 0; k < selected.length; k++) {
					if(selected[k]) { // && (data.instance(j).value(k) != data.instance(i).value(k))){
						double diff = data.instance(j).value(k) - data.instance(i).value(k);
						//Using Euclidean distance
//						distance += diff * diff;
						//Using Manhattan distance
						distance += Math.abs(diff);
						//Overlapping in discrete data
						//						distance += ( data.instance(i).value(k) == data.instance(j).value(k) ? 0 : 1);
					}
				}
				distances[j][i] = distances[i][j] = distance;
			}
		}
		int max_diff = 0;
		for (int k = 0; k < selected.length; k++)
			if(selected[k]) {
				max_diff += data.attribute(k).numValues();
			}
		return max_diff;
	}

	/**
	 * Using different distance: Euclidean, Manhattan, or Overlapping
	 * @param selectedFeatures
	 * @param flipfeature
	 * @param distances
	 * @param curSize
	 * @return
	 */
	public double DistanceAndLOOCVAcc(boolean[] selectedFeatures, int[] flipfeature, double[][] distances , int curSize, int CurMaxDiff) {

		//		data.randomize(new Random(1));
		//		data.stratify(data.numInstances()); //to make it same as the performance eval at the end.
		double[][] confusion_matrix = new double[dat.numClasses()][dat.numClasses()];

		//1. update distance between pair of instances based on flipped indexes
		int nbr_inst = dat.numInstances();
		double d_b = 0, d_w = 0;
		for (int i = 0; i < nbr_inst; i++) {
			double min_d_b = 1, max_d_w = 0;
			int nearest_ins = 0;
			double min_distance = Double.MAX_VALUE;
			//Euclidean distance
			/*
			for (int j = 0; j < nbr_inst; j++) {
				if(i!=j) {
				//get the old distance between instance i and j
				double dis = distances[i][j];
				int tmp_size = curSize;
				if (flipfeature!=null) {
					for (int k = 0; k < flipfeature.length; k++) {
//						if (dat.instance(i).value(flipfeature[k]) != dat.instance(j).value(flipfeature[k]))
						double diff = dat.instance(i).value(flipfeature[k]) - dat.instance(j).value(flipfeature[k]);
							if (selectedFeatures[flipfeature[k]] ) {
								dis -= (diff * diff);
								tmp_size --;
							}
							else {
								dis += (diff * diff);
								tmp_size ++;
							}
					}
				} //end update dis
				double nor_dis = Math.sqrt(dis) / Math.sqrt(tmp_size);
				//******** End Euclidean distance
			 */
			//Using Manhattan distance
			for (int j = 0; j < nbr_inst; j++) {
				if(i!=j) {
					//get the old distance between instance i and j
					double dis = distances[i][j];
					int tmp_size = curSize;
					if (flipfeature!=null) {
						for (int k = 0; k < flipfeature.length; k++) {
							double diff = dat.instance(i).value(flipfeature[k]) - dat.instance(j).value(flipfeature[k]);
							if (selectedFeatures[flipfeature[k]] ) {
								dis -= Math.abs(diff);
								tmp_size --;
							}
							else {
								dis += Math.abs(diff);
								tmp_size ++;
							}
						}
					} //end update dis
					double nor_dis = dis / tmp_size;
					//******** End Manhattan distance


					/*//Using Hamming distance with my simple discretisation to -1,0,1 => max difference btw values is 2
			for (int j = 0; j < nbr_inst; j++) {
				if(i!=j) {
				//get the old distance between instance i and j
				double dis = distances[i][j];
				int tmp_size = curSize;
				if (flipfeature!=null) {
					for (int k = 0; k < flipfeature.length; k++) {
						double diff = dat.instance(i).value(flipfeature[k]) - dat.instance(j).value(flipfeature[k]);
							if (selectedFeatures[flipfeature[k]] ) {
								dis -= Math.abs(diff);
								tmp_size --;
							}
							else {
								dis += Math.abs(diff);
								tmp_size ++;
							}
					}
				} //end update dis
				double nor_dis = dis / (2.0 * tmp_size);
				//******** End Hamming distance with simple disc
					 */
					//Using Hamming distance with my MDL discretisation values => max difference btw values need to be sum to normalise distance

					/*for (int j = 0; j < nbr_inst; j++) {
				if(i!=j) {
				//get the old distance between instance i and j
				double dis = distances[i][j];
				int tmp_max_diff = CurMaxDiff;
				if (flipfeature!=null) {
					for (int k = 0; k < flipfeature.length; k++) {
						double diff = dat.instance(i).value(flipfeature[k]) - dat.instance(j).value(flipfeature[k]);
							if (selectedFeatures[flipfeature[k]] ) {
								dis -= Math.abs(diff);
								tmp_max_diff -= dat.attribute(flipfeature[k]).numValues();
							}
							else {
								dis += Math.abs(diff);
								tmp_max_diff += dat.attribute(flipfeature[k]).numValues();
							}
					}
				} //end update dis
				double nor_dis = dis / tmp_max_diff;
				//******** End Hamming distance with MDL
					 */
					//Overlapping distance
					/*for (int j = 0; j < nbr_inst; j++) {
					if(i!=j) {
					//get the old distance between instance i and j
					double dis = distances[i][j];
					int tmp_size = curSize;
					if (flipfeature!=null) {
						for (int k = 0; k < flipfeature.length; k++) {
								if (selectedFeatures[flipfeature[k]] ) {
									dis -= (dat.instance(i).value(flipfeature[k]) == dat.instance(j).value(flipfeature[k])? 0:1);
									tmp_size --;
								}
								else {
									dis += (dat.instance(i).value(flipfeature[k]) == dat.instance(j).value(flipfeature[k])? 0:1);
									tmp_size ++;
								}
						}
					} //end update dis
					double nor_dis =dis / tmp_size;
					//******** End Overlapping distance
					 */
					if(dat.instance(i).classValue() == dat.instance(j).classValue()) {//same class => update max_d_w
						if ( nor_dis > max_d_w)
							max_d_w = nor_dis;
					}
					else { //different class => update min_d_b
						if (nor_dis < min_d_b)
							min_d_b = nor_dis;
					}
					if (dis < min_distance ){
						min_distance = dis;
						nearest_ins = j;
					}
				} //end if i!= j
			}//end for instance j
			d_b += min_d_b;
			d_w += max_d_w;
			confusion_matrix[(int)dat.instance(i).classValue()][(int)dat.instance(nearest_ins).classValue()] += 1;
		}//end for instance i
		d_b /= nbr_inst;
		d_w /= nbr_inst;

		double dist = 1.0 / (1.0 + Math.exp(-5.0 * (d_b - d_w)));
		return 0.8 * MyClassifier.unbalanceAcc(confusion_matrix) + 0.2 * dist;

	}

	/**
	 * generate 2 random feature subset which has different % of 0s and 1s.
	 * @return feature set
	 */
	protected void halfOnesZeros(boolean[] sol, int [] ones, int[] zeros) {

		int nbr_0 = zeros.length;
		int nbr_1 = ones.length;
		int zero_idx = 0, one_idx = 0;
		boolean do_it_again;
		do {
			do_it_again = false;
			IndexMinPQ sort = new IndexMinPQ(sol.length);
			for (int i = 0; i < sol.length; i++) {
				sort.insert(i, rnd.nextDouble());
			}

			try {
				while ((zero_idx < nbr_0) && (one_idx < nbr_1)) {
					int tmp = sort.delMin();
					if (sol[tmp]) {
						ones[one_idx] = tmp;
						one_idx++;
					} else {
						zeros[zero_idx] = tmp;
						zero_idx++;
					}
				}
				while (one_idx < nbr_1) {
					int tmp = sort.delMin();
					if (sol[tmp]) {
						ones[one_idx] = tmp;
						one_idx++;
					}
				}
				while ((zero_idx < nbr_0)) {
					int tmp = sort.delMin();
					if (!(sol[tmp])) {
						zeros[zero_idx] = tmp;
						zero_idx++;
					}
				}
			} catch (NoSuchElementException e) {
				do_it_again = true;
			}

		} while (do_it_again);

	}

	protected void fullOnes(boolean[] sol, int [] ones) {

		int nbr_1 = ones.length;
		int zero_idx = 0, one_idx = 0;
		boolean do_it_again;
		do {
			do_it_again = false;
			IndexMinPQ sort = new IndexMinPQ(sol.length);
			for (int i = 0; i < sol.length; i++) {
				sort.insert(i, rnd.nextDouble());
			}

			try {
				while (one_idx < nbr_1) {
					int tmp = sort.delMin();
					if (sol[tmp]) {
						ones[one_idx] = tmp;
						one_idx++;
					}

				}

			} catch (NoSuchElementException e) {
				do_it_again = true;
			}

		} while (do_it_again);

	}

	/**
	 * Take random selected features
	 * @param sol
	 * @param return_pos
	 */
	void RandomPickPosition(boolean[] sol, int [] return_pos, boolean takeOnes) {

		int nbr_0 = return_pos.length;
		int j = 0, one_idx = 0;
		boolean do_it_again;
		do {
			do_it_again = false;
			IndexMinPQ sort = new IndexMinPQ(sol.length);
			for (int i = 0; i < sol.length; i++) {
				sort.insert(i, rnd.nextDouble());
			}

			try {
				if (takeOnes)
					while (j < nbr_0) {
						int tmp = sort.delMin();
						if (sol[tmp]) {
							return_pos[one_idx] = tmp;
							j++; one_idx++;
						}
					}
				else
					while (j < nbr_0) {
						int tmp = sort.delMin();
						if (!sol[tmp]) {
							return_pos[one_idx] = tmp;
							j++; one_idx++;
						}
					}
			} catch (NoSuchElementException e) {
				do_it_again = true;
			}

		} while (do_it_again);

	}

	/**
	 * In this LS, only add or remove is applied at once LS process.
	 * @param su
	 * @param sol
	 * @param initialAcc
	 * @param flipPercent
	 * @param maxEval
	 * @param needPreprocessing
	 * @param prb
	 * @return
	 */
	/*public SolutionPack localsearchSUOneOperator(SU su, boolean[] sol, double initialAcc, double flipPercent, int maxEval, boolean needPreprocessing, Problem prb)  {
		// iterate through all neigbours
		double best_acc = initialAcc;
		int best_size = prb.booleanSubsetSize(sol);
		boolean found = false;

//		System.out.printf("\n****************Local search START ***************:\n");// init acc = %.4f, size: %d ",initialAcc, prb.booleanSubsetSize(sol));
		//Calculate all cross distances for each data instance
		if (needPreprocessing) {
			insDistance = preprocessing(dat, sol);
			needPreprocessing = false;
		}

		int[] best_flip_pos = null;
		int nbr_fails = 0;
		for( int count = 0; count < maxEval; count++) {
			int flipSize = (int) (flipPercent * prb.booleanSubsetSize(sol));
//			int flipSize = (int) Math.round ((flipPercent - (flipPercent - 0.1) * (count/maxEval) ) * prb.booleanSubsetSize(sol));
			int[] to_flip = new int[flipSize];
//			int[] zeros = new int[flipSize/2];
			int[] feature_to_flip = null;
			int tries = 0;
			while (feature_to_flip == null && tries <10) {
//				halfOnesZeros(sol, ones, zeros);
//				feature_to_flip = flip(su, ones, zeros);
				if ( (rnd.nextDouble() < 0.3) && ((sol.length - best_size) > to_flip.length) ){
					RandomPickPosition(sol, to_flip, false);
					feature_to_flip = flipTurnOnRelevant(su, to_flip, sol);
				}else {
					RandomPickPosition(sol, to_flip, true);
					feature_to_flip = flipTurnOffRedundant(su, to_flip);
				}

				tries++;
			}

			if(feature_to_flip != null){
				double acc =
						DistanceAndLOOCVAcc(sol, feature_to_flip, insDistance, best_size);
//						LOOCV(sol, feature_to_flip, insDistance);

				//Calculate the new solution
				boolean[] sol_tmp = new boolean[sol.length];
				sol_tmp = sol.clone();
				for (int i = 0; i < feature_to_flip.length; i++) {
					sol_tmp[feature_to_flip[i]] = !sol_tmp[feature_to_flip[i]];
				}
				int tmp_size = prb.booleanSubsetSize(sol_tmp);

				if ( prb.isBetter(acc, best_acc) ||
						((acc == best_acc) &&  tmp_size < best_size) )
				{
					best_acc = acc;
					best_flip_pos = feature_to_flip.clone();
					best_size = tmp_size;
					//Update distance matrix according to new solution
					updatingdistance(insDistance, sol, best_flip_pos);
					//Update the solution
					for (int i = 0; i < best_flip_pos.length; i++) {
						sol[best_flip_pos[i]] = !sol[best_flip_pos[i]];
					}
					found = true;
					nbr_fails = 0;
				} else
					nbr_fails++;
			}//end if
		}//end for count

				//Print selected features:

				for (int j = 0; j < sol.length; ++j)
					if (sol[j])
						System.out.printf("%d, ", j);

				// print the pair distance after flipping
				System.out.printf("\nPair Distance in DiscKNNOpt.localSearchSU:\n");
				for (int i = 0; i < dat.numInstances()-1; i++){
					for (int j = i+1; j < dat.numInstances(); j++)
						System.out.printf("%d, ", (int) insDistance[i][j]);
					System.out.printf("\n");
				}

				double[][] temp = preprocessing(dat, sol);
				System.out.printf("\nPair Distance PREPROCESS again:\n");
				for (int i = 0; i < dat.numInstances()-1; i++){
					for (int j = i+1; j < dat.numInstances(); j++)
						System.out.printf("%d, ", (int) temp[i][j]);
					System.out.printf("\n");
				}
				System.out.printf("************************");

		//End maxEval

		if (found) {
//			System.out.printf("-> LS found acc = %.4f, size: %d ",best_acc,best_size);
			return new SolutionPack(sol, best_acc);
		}
		else {
//			System.out.printf("\n-> LS NOT found \n");
			return null;
		}

	}
	 */
	/**Immitate the way FCBF does to remove all redundant features
	 *
	 * @param su
	 * @param sol
	 * @param initialAcc
	 * @param flipPercent
	 * @param maxEval
	 * @param needPreprocessing
	 * @param prb
	 * @return
	 */
	/*public SolutionPack localsearchFCBF(SU su, boolean[] sol, double initialAcc, Problem prb)  {
		// iterate through all neigbours
		double best_acc = initialAcc;
		int best_size = prb.booleanSubsetSize(sol);
		boolean found = false;

//		System.out.printf("\n****************Local search START ***************:\n");// init acc = %.4f, size: %d ",initialAcc, prb.booleanSubsetSize(sol));
		//Calculate all cross distances for each data instance
		insDistance = preprocessing(dat, sol);

		int[] best_flip_pos = null;


		int[] ones = new int[best_size];

		int[] feature_to_flip = null;
		feature_to_flip = flipOffRedundant(su, sol, ones);

		if(feature_to_flip != null){
				double acc =
						DistanceAndLOOCVAcc(sol, feature_to_flip, insDistance, best_size);
//						LOOCV(sol, feature_to_flip, insDistance);

				//Calculate the new solution
				boolean[] sol_tmp = new boolean[sol.length];
				sol_tmp = sol.clone();
				for (int i = 0; i < feature_to_flip.length; i++) {
					sol_tmp[feature_to_flip[i]] = !sol_tmp[feature_to_flip[i]];
				}
				int tmp_size = prb.booleanSubsetSize(sol_tmp);
				if ( prb.isBetter(acc, best_acc) ||
						((acc == best_acc) &&  tmp_size < best_size) )
				{
					best_acc = acc;
					best_flip_pos = feature_to_flip.clone();
					best_size = tmp_size;
					//Update distance matrix according to new solution
					updatingdistance(insDistance, sol, best_flip_pos);
					//Update the solution
					for (int i = 0; i < best_flip_pos.length; i++) {
						sol[best_flip_pos[i]] = !sol[best_flip_pos[i]];
					}
					found = true;
				}
			}//end if


				//Print selected features:

				for (int j = 0; j < sol.length; ++j)
					if (sol[j])
						System.out.printf("%d, ", j);

				// print the pair distance after flipping
				System.out.printf("\nPair Distance in DiscKNNOpt.localSearchSU:\n");
				for (int i = 0; i < dat.numInstances()-1; i++){
					for (int j = i+1; j < dat.numInstances(); j++)
						System.out.printf("%d, ", (int) insDistance[i][j]);
					System.out.printf("\n");
				}

				double[][] temp = preprocessing(dat, sol);
				System.out.printf("\nPair Distance PREPROCESS again:\n");
				for (int i = 0; i < dat.numInstances()-1; i++){
					for (int j = i+1; j < dat.numInstances(); j++)
						System.out.printf("%d, ", (int) temp[i][j]);
					System.out.printf("\n");
				}
				System.out.printf("************************");

		//End maxEval

		if (found) {
//			System.out.printf("-> LS found acc = %.4f, size: %d ",best_acc,best_size);
			return new SolutionPack(sol, best_acc);
		}
		else {
//			System.out.printf("\n-> LS NOT found \n");
			return null;
		}

	}*/


	private int[] flipOffRedundant(SU su, boolean[] sol, int[] ones) {
		int[] feature_to_flip = new int[ones.length];

		for (int x = 0, k= 0; x< sol.length; x++)
			if(sol[x])
				ones[k++] = x;

		//consider the ones
		int k = 0;
		for(int i= 0; i < ones.length-1; i++)
			if (ones[i] != Integer.MAX_VALUE) {
				for (int j = i+1; j< ones.length; j++)
					if (ones[j] != Integer.MAX_VALUE){
						double su_btw_fea = su.get_su(ones[i],ones[j]);
						if ( su_btw_fea > su.get_suic(ones[j]) ) { // flip j off
							feature_to_flip[k++] = ones[j];
							ones[j] = Integer.MAX_VALUE;
						}
					}

			}

		//consider the zeros
		//		for (int x = 0; x < zeros.length; x++)
		//			if ( su.get_suic(zeros[x]) > avg_su)
		//				feature_to_flip[k++] = zeros[x];

		//resize the feature_to_flip to k features
		if (k > 0) {
			int[] f_to_flip = new int [k];
			System.arraycopy(feature_to_flip, 0, f_to_flip, 0, k);
			return f_to_flip;}
		else return null;
	}

	/**
	 * Update old cross distances for new feature subsets
	 * new feature subsets = old solution + apply flip
	 * @param oldDistance
	 * @param oldsolution
	 * @param flip
	 */
	/*private void updatingdistance(double[][] oldDistance,
			boolean[] oldsolution, int[] flip) {

		for (int i = 0; i < dat.numInstances()-1; i++) {
			for (int j = i+1; j < dat.numInstances(); j++) {
				for (int k = 0; k < flip.length; k++) {

					double diff = dat.instance(i).value(flip[k]) - dat.instance(j).value(flip[k]);
					if (oldsolution[flip[k]]) { //if feature at flip[k] was selected (true) in old solution => subtract its dis. from sum dis.
						//Using Euclidean distance
//						oldDistance[i][j] -= diff * diff;
						//Using Manhattan distance
						oldDistance[i][j] -= Math.abs(diff);
						//Using Overlapping distance
//						oldDistance[i][j] -= (dat.instance(i).value(flip[k]) == dat.instance(j).value(flip[k]) ? 0 : 1);
					}//else = feature at flip[k] was not selected (false) in old solution => add its dis. to sum dis.
					else {
						//Using Euclidean distance
//						oldDistance[i][j] += diff * diff;
						//Using Manhattan distance
						oldDistance[i][j] += Math.abs(diff);
						//Using Overlapping distance
//						oldDistance[i][j] += (dat.instance(i).value(flip[k]) == dat.instance(j).value(flip[k]) ? 0 : 1);
					}
				}
				oldDistance[j][i] = oldDistance[i][j];
			}
		}


	}*/

	/**
	 *
	 * @param oldDistance
	 * @param oldsolution
	 * @param flip
	 * @return
	 */
	protected int updatingdistance(double[][] oldDistance,
			boolean[] oldsolution, int[] flip, int cur_max_diff) {

		int ret_max_diff = cur_max_diff;
		for (int i = 0; i < dat.numInstances()-1; i++) {
			for (int j = i+1; j < dat.numInstances(); j++) {
				for (int k = 0; k < flip.length; k++) {

					double diff = dat.instance(i).value(flip[k]) - dat.instance(j).value(flip[k]);
					if (oldsolution[flip[k]]) { //if feature at flip[k] was selected (true) in old solution => subtract its dis. from sum dis.
						//Using Euclidean distance
//												oldDistance[i][j] -= diff * diff;
						//Using Manhattan distance
						oldDistance[i][j] -= Math.abs(diff);
						//Using Overlapping distance
						//						oldDistance[i][j] -= (dat.instance(i).value(flip[k]) == dat.instance(j).value(flip[k]) ? 0 : 1);
					}//else = feature at flip[k] was not selected (false) in old solution => add its dis. to sum dis.
					else {
						//Using Euclidean distance
//												oldDistance[i][j] += diff * diff;
						//Using Manhattan distance
						oldDistance[i][j] += Math.abs(diff);
						//Using Overlapping distance
						//						oldDistance[i][j] += (dat.instance(i).value(flip[k]) == dat.instance(j).value(flip[k]) ? 0 : 1);
					}
				}
				oldDistance[j][i] = oldDistance[i][j];
			}
		}
		//update max_diff
		for (int k = 0; k < flip.length; k++) {
			if (oldsolution[flip[k]])
				ret_max_diff -= dat.attribute(flip[k]).numValues();
			else
				ret_max_diff += dat.attribute(flip[k]).numValues();

		}
		return ret_max_diff;

	}

	/**
	 * given two arrays of random flips (ones and zeros) and return the index that is decided to flip.
	 * @param su
	 * @param ones and zeros
	 * @return
	 */
	private int[] flipTurnOffRedundant(SU su, int[] ones) {
		int[] feature_to_flip = new int[ones.length];

		//get su of ones
		//		double [] su_ones = new double[ones.length];
		//		double avg_su = 0;
		//		for (int x = 0; x< ones.length; x++){
		//			su_ones[x] = su.get_suic(ones[x]);
		//			avg_su += su_ones[x];
		//		}
		//		avg_su /= ones.length;
		//		QSort.sort(su_ones, ones, 0, ones.length-1);

		double[] sorted_ones = new double[ones.length];
		for (int x = 0; x< ones.length; x++)
			sorted_ones[x] = ones[x];
		QSort.sortAsc(sorted_ones, ones);

		//consider the ones
		int k = 0;
		for(int i= 0; i < ones.length-1; i++)
			if (ones[i] != Integer.MAX_VALUE) {
				for (int j = i+1; j< ones.length; j++)
					if (ones[j] != Integer.MAX_VALUE){
						double su_btw_fea = su.get_su(ones[i],ones[j]);
						if ( su_btw_fea > su.get_suic(ones[j]) ) { // flip j off
							feature_to_flip[k++] = ones[j];
							ones[j] = Integer.MAX_VALUE;
						}
					}

			}

		//consider the zeros
		//		for (int x = 0; x < zeros.length; x++)
		//			if ( su.get_suic(zeros[x]) > avg_su)
		//				feature_to_flip[k++] = zeros[x];

		//resize the feature_to_flip to k features
		if (k > 0) {
			int[] f_to_flip = new int [k];
			System.arraycopy(feature_to_flip, 0, f_to_flip, 0, k);
			return f_to_flip;}
		else return null;
	}

	/**
	 * given two arrays of random flips (ones and zeros) and return the index that is decided to flip.
	 * @param su
	 * @param zeros and zeros
	 * @return
	 */
	private int[] flipTurnOnRelevant(SU su, int[] zeros, boolean[] sol) {
		int[] feature_to_flip = new int[zeros.length];

		//get su of sol

		double avg_su = 0; int count =0;
		for (int x = 0; x< sol.length; x++){
			if (sol[x]) {
				avg_su += su.get_suic(x);
				count++;
			}
		}
		avg_su /= (count * 2);

		//consider the zeros
		int k = 0;
		for (int x = 0; x < zeros.length; x++)
			if ( su.get_suic(zeros[x]) > avg_su)
				feature_to_flip[k++] = zeros[x];

		//resize the feature_to_flip to k features
		if (k > 0) {
			int[] f_to_flip = new int [k];
			System.arraycopy(feature_to_flip, 0, f_to_flip, 0, k);
			return f_to_flip;
		}
		else return null;
	}

	protected int[] flip(SU su, int[] ones, int[] zeros) {
		int[] feature_to_flip = new int[ones.length + zeros.length];

		//get su of ones
		double [] su_ones = new double[ones.length];
		double avg_su = 0;
		for (int x = 0; x< ones.length; x++){
			su_ones[x] = su.get_suic(ones[x]);
			avg_su += su_ones[x];
		}
		avg_su /= ones.length;
		QSort.sort(su_ones, ones, 0, ones.length-1);

		//consider the ones
		int k = 0;
		for(int i= 0; i < ones.length-1; i++)
			if (ones[i] != Integer.MAX_VALUE) {
				for (int j = i+1; j< ones.length; j++)
					if (ones[j] != Integer.MAX_VALUE){
						double su_btw_fea = su.get_su(ones[i],ones[j]);
						if ( su_btw_fea > su.get_suic(ones[j]) ) { // flip j off
							feature_to_flip[k++] = ones[j];
							ones[j] = Integer.MAX_VALUE;
						}
					}

			}

		//consider the zeros
		for (int x = 0; x < zeros.length; x++)
			if ( su.get_suic(zeros[x]) > avg_su)
				feature_to_flip[k++] = zeros[x];

		//resize the feature_to_flip to k features
		if (k > 0) {
			int[] f_to_flip = new int [k];
			System.arraycopy(feature_to_flip, 0, f_to_flip, 0, k);
			return f_to_flip;}
		else return null;
	}

	protected int[] flipOnes(SU su, int[] ones) {
		int[] feature_to_flip = new int[ones.length];

		//consider the ones
		int k = 0;
		for(int i= 0; i < ones.length-1; i++)
			if (ones[i] != Integer.MAX_VALUE) {
				for (int j = i+1; j< ones.length; j++)
					if (ones[j] != Integer.MAX_VALUE){
						double su_btw_fea = su.get_su(ones[i],ones[j]);
						if ( su_btw_fea > su.get_suic(ones[j]) ) { // flip j off
							feature_to_flip[k++] = ones[j];
							ones[j] = Integer.MAX_VALUE;
						}
					}

			}

		//resize the feature_to_flip to k features
		if (k > 0) {
			int[] f_to_flip = new int [k];
			System.arraycopy(feature_to_flip, 0, f_to_flip, 0, k);
			return f_to_flip;
		}else
			return null;
	}

	public SolutionPack localsearchSU(SU su, boolean[] sol, double initialAcc, double flipPercent, int maxEval, boolean needPreprocessing, Problem prb)  {
		// iterate through all neigbours
		double best_fitn = initialAcc;
		int best_size = prb.booleanSubsetSize(sol);
		int cur_max_diff = 0;
		boolean found = false;

		//		System.out.printf("\n****************Local search START ***************:\n");// init acc = %.4f, size: %d ",initialAcc, prb.booleanSubsetSize(sol));
		//Calculate all cross distances for each data instance
		if (needPreprocessing) {

			insDistance = preprocessing(dat, sol);

			//In case of Hamming distance + MDL disc data
			/*insDistance = new double[dat.numInstances()][dat.numInstances()];
			cur_max_diff = preprocessing(dat, sol, insDistance);
			needPreprocessing = false;*/
		}

		int[] best_flip_pos = null;
		for( int count = 0; count < maxEval; count++) {
			int flipSize = (int) (flipPercent * prb.booleanSubsetSize(sol));
			int zero_size = (int) Math.round( rnd.nextDouble() * (sol.length - best_size > flipSize ? flipSize : (sol.length - best_size)));

			int[] zeros = new int[zero_size];
			int[] ones = new int[flipSize - zero_size];
			int[] feature_to_flip = null;
			int tries = 0;
			while (feature_to_flip == null && tries <10) {
				//Option 1: LS that remove and add at the same time
				halfOnesZeros(sol, ones, zeros);
				feature_to_flip = flip(su, ones, zeros);
				//Option 2: LS that remove only: I tried on Brain1, but the result is not good
//				fullOnes(sol, ones);
//				feature_to_flip = flipOnes(su, ones);
				tries++;
			}

			if(feature_to_flip != null){
				double fitn = DistanceAndmFCVAcc(sol, feature_to_flip, insDistance, best_size, cur_max_diff);
//				mFCVAcc(sol, feature_to_flip, insDistance, best_size, cur_max_diff);
				//						DistanceAndLOOCVAcc(sol, feature_to_flip, insDistance, best_size, cur_max_diff);
				//						LOOCV(sol, feature_to_flip, insDistance);

				//Calculate the new solution
				boolean[] sol_tmp = new boolean[sol.length];
				sol_tmp = sol.clone();
				for (int i = 0; i < feature_to_flip.length; i++) {
					sol_tmp[feature_to_flip[i]] = !sol_tmp[feature_to_flip[i]];
				}
				int tmp_size = prb.booleanSubsetSize(sol_tmp);
				if ( prb.isBetter(fitn, best_fitn) ||
						((fitn == best_fitn) &&  tmp_size < best_size) )
				{
					best_fitn = fitn;
					best_flip_pos = feature_to_flip.clone();
					best_size = tmp_size;

					//Update distance matrix according to new solution and cur_max_diff
					cur_max_diff = updatingdistance(insDistance, sol, best_flip_pos, cur_max_diff);
					//Update the solution
					for (int i = 0; i < best_flip_pos.length; i++) {
						sol[best_flip_pos[i]] = !sol[best_flip_pos[i]];
					}
//					System.out.println("Take this");
					found = true;
				}
			}//end if
		}//end for count

		//Print selected features:

		/*for (int j = 0; j < sol.length; ++j)
					if (sol[j])
						System.out.printf("%d, ", j);

				// print the pair distance after flipping
				System.out.printf("\nPair Distance in DiscKNNOpt.localSearchSU:\n");
				for (int i = 0; i < dat.numInstances()-1; i++){
					for (int j = i+1; j < dat.numInstances(); j++)
						System.out.printf("%d, ", (int) insDistance[i][j]);
					System.out.printf("\n");
				}

				double[][] temp = preprocessing(dat, sol);
				System.out.printf("\nPair Distance PREPROCESS again:\n");
				for (int i = 0; i < dat.numInstances()-1; i++){
					for (int j = i+1; j < dat.numInstances(); j++)
						System.out.printf("%d, ", (int) temp[i][j]);
					System.out.printf("\n");
				}
				System.out.printf("************************");*/

		//End maxEval

		if (found) {
//			System.out.printf("-> LS found acc = %.4f, size: %d ",best_fitn,best_size);
			return new SolutionPack(sol, best_fitn);
		}
		else {
			//			System.out.printf("\n-> LS NOT found \n");
			return null;
		}

	}

	/**
	 * To calculate the sum max difference of all selected features
	 * @param data
	 * @param selected
	 * @return
	 */
	/*private int calcMaxDiff(Instances data, boolean[] selected) {

		int sum_max_diff = 0;

		for (int k = 0; k < selected.length; k++)
			if(selected[k]) { // && (data.instance(j).value(k) != data.instance(i).value(k))){
				sum_max_diff += data.attribute(k).numValues();
			}

		return sum_max_diff;
	}*/

	/**
	 * Using different distance: Manhattan
	 * @param selectedFeatures
	 * @param flipfeature
	 * @param distances
	 * @param curSize
	 * @return
	 */
	public double DistanceAndmFCVAcc(boolean[] selectedFeatures, int[] flipfeature, double[][] distances , int curSize, int CurMaxDiff) {

		int max_fold = AMSOMain.FITN_M_FOLD;
		int k_nn = AMSOMain.K_NN;
		//		data.randomize(new Random(1));
		//		data.stratify(data.numInstances()); //to make it same as the performance eval at the end.
		double[][] confusion_matrix = new double[dat.numClasses()][dat.numClasses()];

		//1. update distance between pair of instances based on flipped indexes
		int nbr_inst = dat.numInstances();
		int fold_ins_start_ind[] = new int[max_fold + 1]; //+1 because I want to use it as a marker to stop.
		for(int i = 0; i < fold_ins_start_ind.length-1; i++){
			fold_ins_start_ind[i] = i * nbr_inst/max_fold;
		}
		fold_ins_start_ind[fold_ins_start_ind.length-1] = nbr_inst;

		double d_b = 0, d_w = 0;
		int tmp_size = curSize;

		for(int test_fold = 0; test_fold < max_fold; test_fold++){
			for(int i = fold_ins_start_ind[test_fold]; i < fold_ins_start_ind[test_fold+1]; i++) { //for each test instance
				List<Double> nearest_distance = new ArrayList<Double>();
				List<Integer> nearest_instance = new ArrayList<Integer>();
				double k_max_distance = Double.MAX_VALUE;
				double min_d_b = 1, max_d_w = 0;

				for(int train_fold = 0; train_fold < max_fold; train_fold++){
					for(int j = fold_ins_start_ind[train_fold]; j < fold_ins_start_ind[train_fold+1]; j++) {
						//Using Manhattan distance
						//get the old distance between instance i and j
						double dis = distances[i][j];
						double nor_dis =0;
						if(i!=j) {
							tmp_size = curSize;
							if (flipfeature!=null) {
								for (int k = 0; k < flipfeature.length; k++) {
									double diff = dat.instance(i).value(flipfeature[k]) - dat.instance(j).value(flipfeature[k]);
									if (selectedFeatures[flipfeature[k]] ) {
										dis -= Math.abs(diff);
										tmp_size --;
									}
									else {
										dis += Math.abs(diff);
										tmp_size ++;
									}
								}
							} //end update dis
							nor_dis = dis / tmp_size;
							//******** End Manhattan distance

							if(dat.instance(i).classValue() == dat.instance(j).classValue()) {//same class => update max_d_w
								if ( nor_dis > max_d_w)
									max_d_w = nor_dis;
							}
							else { //different class => update min_d_b
								if (nor_dis < min_d_b)
									min_d_b = nor_dis;
							}
						} //end if i!= j
						if (train_fold != test_fold){
							if (nor_dis < k_max_distance) {
								nearest_distance.add(nor_dis);
								nearest_instance.add(j);

								if(nearest_distance.size()> k_nn) { //remove the furthest one
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
					}//end for instance j
				}//end of train_fold
				d_b += min_d_b;
				d_w += max_d_w;
				//Voting
				int[] class_vote = new int[dat.numClasses()];
				for (int l = 0; l< nearest_distance.size(); l++) {
					class_vote[(int)dat.instance(nearest_instance.get(l)).classValue()]++;
				}
				//Find the highest vote
				int highest_vote_class = 0;
				for(int l= 1; l<class_vote.length; l++)
					if (class_vote[l] > class_vote[highest_vote_class])
						highest_vote_class = l;
				confusion_matrix[(int)dat.instance(i).classValue()][highest_vote_class] += 1;
			}//end of instance i
		}//end of test_fold
		d_b /= nbr_inst;
		d_w /= nbr_inst;

		double dist = 1.0 / (1.0 + Math.exp(-5.0 * (d_b - d_w)));

		return 0.8 * MyClassifier.unbalanceAcc(confusion_matrix) + 0.2 * dist;
//		return 0.7 * MyClassifier.unbalanceAcc(confusion_matrix) + 0.2 * dist + 0.1 * (1 - tmp_size/dat.numAttributes());
//		return 0.8 * MyClassifier.unbalanceAcc(confusion_matrix) + //0.2 * dist +
//				0.2 * (1 - 1.0 * tmp_size/dat.numAttributes());

	}

	/**
	 * Using different distance: Manhattan
	 * @param selectedFeatures
	 * @param flipfeature
	 * @param distances
	 * @param curSize
	 * @return
	 */
	public double mFCVAcc(boolean[] selectedFeatures, int[] flipfeature, double[][] distances , int curSize, int CurMaxDiff) {

		int max_fold = AMSOMain.FITN_M_FOLD;
		int k_nn = AMSOMain.K_NN;
		//		data.randomize(new Random(1));
		//		data.stratify(data.numInstances()); //to make it same as the performance eval at the end.
		double[][] confusion_matrix = new double[dat.numClasses()][dat.numClasses()];

		//1. update distance between pair of instances based on flipped indexes
		int nbr_inst = dat.numInstances();
		int fold_ins_start_ind[] = new int[max_fold + 1]; //+1 because I want to use it as a marker to stop.
		for(int i = 0; i < fold_ins_start_ind.length-1; i++){
			fold_ins_start_ind[i] = i * nbr_inst/max_fold;
		}
		fold_ins_start_ind[fold_ins_start_ind.length-1] = nbr_inst;

		int tmp_size = curSize;
		for(int test_fold = 0; test_fold < max_fold; test_fold++){
			for(int i = fold_ins_start_ind[test_fold]; i < fold_ins_start_ind[test_fold+1]; i++) { //for each test instance
				List<Double> nearest_distance = new ArrayList<Double>();
				List<Integer> nearest_instance = new ArrayList<Integer>();
				double k_max_distance = Double.MAX_VALUE;

				for(int train_fold = 0; train_fold < max_fold; train_fold++){
					if (train_fold != test_fold) {
					for(int j = fold_ins_start_ind[train_fold]; j < fold_ins_start_ind[train_fold+1]; j++) {//for each train instance
						//get the old distance between instance i and j
						double dis = distances[i][j];
							tmp_size = curSize;
							if (flipfeature!=null) {
								for (int k = 0; k < flipfeature.length; k++) {
									double diff = dat.instance(i).value(flipfeature[k]) - dat.instance(j).value(flipfeature[k]);
									if (selectedFeatures[flipfeature[k]] ) {
										// Using Manhattan distance
										 dis -= Math.abs(diff);
										// Using Euclidean distance
//										dis -= diff * diff;
										tmp_size --;
									}
									else {
										// Using Manhattan distance
										 dis += Math.abs(diff);
										// Using Euclidean distance
//										dis += diff * diff;
										tmp_size ++;
									}
								} //end of k attribute changed
							} //end update dis

							if (dis < k_max_distance) {
								nearest_distance.add(dis);
								nearest_instance.add(j);

								if(nearest_distance.size()> k_nn) { //remove the furthest one
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
							}// end if
						} //end for each train instance
					}//end if trainfold <> testfold
				}//end of train_fold

				//Voting
				int[] class_vote = new int[dat.numClasses()];
				for (int l = 0; l< nearest_distance.size(); l++) {
					class_vote[(int)dat.instance(nearest_instance.get(l)).classValue()]++;
				}
				//Find the highest vote
				int highest_vote_class = 0;
				for(int l= 1; l<class_vote.length; l++)
					if (class_vote[l] > class_vote[highest_vote_class])
						highest_vote_class = l;
				confusion_matrix[(int)dat.instance(i).classValue()][highest_vote_class] += 1;
			}//end of each test instance
		}//end of test_fold

		double acc = MyClassifier.unbalanceAcc(confusion_matrix);
//		System.out.printf("Acc: %.4f, Size: %d, 2nd comp: %2f\n", acc, tmp_size,
//				(1 - 1.0 * tmp_size/dat.numAttributes()));
		return 0.8 *  acc + //0.2 * dist +
				0.2 * (1 - 1.0 * tmp_size/dat.numAttributes());

	}

}

