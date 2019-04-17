/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myUtils;
//import edu.princeton.cs.algs4.IndexMinPQ;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import fs.MyClassifier;
//import measures.Distance;
//import measures.Measures;
//import measures.intersection_family.CzekanowskiS;

import myUtils.analysis.MyStatistics;

//import javax.xml.crypto.dsig.spec.XPathType.Filter;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

/**
 * This class handle dataset read from Weka format, using Instances class of Weke
 * @author tranbinh
 */
public class WekaDatasetHandle {

	/**
	 * This method construct the dataset (stored in dts) from the file given in the path
	 * @param path
	 * @throws IOException
	 */
	public static Instances ReadDataset(String path, int class_idx) throws IOException{
		Instances data = null;
		try {
			ArffLoader loader = new ArffLoader();
			loader.setSource(new File(path));
			data = loader.getDataSet();
			if (class_idx ==0)
				data.setClassIndex(class_idx);
			else
				data.setClassIndex(data.numAttributes()-1);
		}  catch (Exception e) {//Catch exception if any
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * Remove features from input_data with feature array is a bit string showing 0 is feature to be removed.
	 * @param input_data
	 * @param features
	 * @param nbrFea
	 * @return the new dataset
	 */
	public static Instances removeFeatures(Instances input_data, double[] features, int nbrFea) {

		Instances new_data = new Instances(input_data);

		int num_del_fea = 0;
		//Remove features from new_data
		for(int i=0; i< features.length; i++){
			if(features[i] == 0) {
				new_data.deleteAttributeAt(i - num_del_fea);
				num_del_fea++;
			}
		}
		return new_data;
	}


	/**
	 * Print out some characteristics of the dataset: #features, #instances, #instances for each class
	 */
	public static void PrintDatasetCharacter(Instances dts){

		Map<Object, Integer> classes = new TreeMap<Object, Integer>();

		System.out.println("Nbr of features: " + (dts.numAttributes()-1));
		System.out.println("Nbr of instances: " + dts.numInstances());

		//        Set classes = new HashSet();

		for(int i = 0; i< dts.numInstances(); i++){
			//            System.out.println(dts.instance(i).toString());
			if (classes.containsKey(dts.instance(i).classValue()))
				classes.put(dts.instance(i).classValue(), classes.get(dts.instance(i).classValue())+1);
			else
				classes.put(dts.instance(i).classValue(), 1);
		}

		Iterator<Object> keySetIterator = classes.keySet().iterator();

		while(keySetIterator.hasNext()){
			Object key = keySetIterator.next();
			System.out.printf("Class: %s  nbr. of instances: %d (%.2f%%)\n", key , classes.get(key) , (double)classes.get(key) * 100 /dts.numInstances() );
		}

		//        System.out.println("Class set: " + classes.toString());
	}
	/**
	 * Method transforms the input dataset by creating features in order of the list
	 * @param dataset
	 * @param featureList
	 * @param datasetName
	 * @return the new dataset with the input string name
	 */
	public static Instances transformDataset(Instances dataset, ArrayList<Integer> featureList, String datasetName) {

		//Step 1. Create attribute set for new transformed dataset from featureList array
		FastVector attributes = new FastVector();
		for(int i =0; i< featureList.size(); i++) {
			//            Attribute att = new Attribute("F"+terminalNodes.get(i));
			Attribute att = (Attribute) dataset.attribute(featureList.get(i)).copy();
			attributes.addElement(att);
		}

		Attribute attCls = dataset.classAttribute().copy("nclass");
		attributes.addElement(attCls);

		//Step 2. Create new transformed dataset
		Instances transfDataset = new Instances(datasetName, attributes, 0 );
		transfDataset.setClassIndex(featureList.size());

		//Step 3. Copy values from instances into new transform dataset
		for(int i =0; i< dataset.numInstances(); i++) {
			//Step 3.1. Copy selected features' values into an array
			double[] new_inst_value = new double[featureList.size()+1];
			for(int j = 0; j< featureList.size(); j++)
				new_inst_value[j] = dataset.instance(i).value(featureList.get(j));
			//Step 3.2 Copy class label
			new_inst_value[featureList.size()] = dataset.instance(i).classValue();

			//Step 3.3. Form the new instance and add it to transform dataset
			Instance new_inst = new Instance(1, new_inst_value);
			transfDataset.add(new_inst);
			new_inst.setDataset(transfDataset);
		}

		return transfDataset;
	}

	/**
	 *
	 * @param data
	 * @return
	 */
	public static double[] CalculateDistanceAndLOOCVBalAcc(Instances data) {
		double[][] confusion_matrix = new double[data.numClasses()][data.numClasses()];

		//1. calculate distance between pair of instances
		int nbr_inst = data.numInstances();
		double[][] pair_dis = new double[nbr_inst][nbr_inst];

		//Using Manhattan distance
		for (int i = 0; i < nbr_inst - 1; i++) {
			for (int j = i+1; j < nbr_inst; j++) {
				//calcuate distance between instance i and j
				double distance = 0;
				for(int k=0; k< data.numAttributes() - 1; k++){
					double diff = data.instance(i).value(k) - data.instance(j).value(k);
					distance += Math.abs(diff);
				} //end all attribute
				pair_dis[i][j] = pair_dis[j][i] = distance/(data.numAttributes() - 1);
			}//end for instance j
		}//end for instance i

		//2. calculate the d_b and d_w
		double d_b = 0, d_w = 0;

		for(int i = 0; i < nbr_inst; i++) { //for each instance calculate the min_d_b and max_d_w and add to d_b and d_w
			double min_d_b = 1, max_d_w = 0;
			int nearest_ins = 0;
			double min_distance = Double.MAX_VALUE;
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
					if (pair_dis[i][j] < min_distance ){
						min_distance = pair_dis[i][j];
						nearest_ins = j;
					}
				}
			}
			d_b += min_d_b;
			d_w += max_d_w;
			confusion_matrix[(int)data.instance(i).classValue()][(int)data.instance(nearest_ins).classValue()] += 1;
		} // end all instances
		d_b /= nbr_inst;
		d_w /= nbr_inst;

		double distance = 1.0 / (1.0 + Math.exp(-5.0 * (d_b - d_w)));
		return new double[]{MyClassifier.unbalanceAcc(confusion_matrix), distance};

	}

	public static double CalculateLOOCVBalAcc(Instances data) {
		double[][] confusion_matrix = new double[data.numClasses()][data.numClasses()];

		//1. calculate distance between pair of instances
		int nbr_inst = data.numInstances();
		double[][] pair_dis = new double[nbr_inst][nbr_inst];

		for (int i = 0; i < nbr_inst - 1; i++) {
			for (int j = i+1; j < nbr_inst; j++) {
				//calcuate distance between instance i and j
				double distance = 0;
				for(int k=0; k< data.numAttributes() - 1; k++){
					double diff = data.instance(i).value(k) - data.instance(j).value(k);
					distance += diff * diff ;
				} //end all attribute
				pair_dis[i][j] = pair_dis[j][i] = distance ; ///(data.numAttributes() - 1);
			}//end for instance j
		}//end for instance i

		//2. calculate the nearest neighbour

		for(int i = 0; i < nbr_inst; i++) { //for each instance calculate the min_d_b and max_d_w and add to d_b and d_w
			int nearest_ins = 0;
			double min_distance = Double.MAX_VALUE;
			for(int j = 0; j < nbr_inst; j++)
				if ( (i!=j) && (pair_dis[i][j] < min_distance )){
					min_distance = pair_dis[i][j];
					nearest_ins = j;
				}
			confusion_matrix[(int)data.instance(i).classValue()][(int)data.instance(nearest_ins).classValue()] += 1;
		} // end all instances

		return MyClassifier.unbalanceAcc(confusion_matrix);

	}

}
