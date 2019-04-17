package AMSO;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import fs.MyClassifier;
import fs.Problem;
import myUtils.RandomBing;
import myUtils.SU;
import myUtils.WekaDatasetHandle;
import myUtils.algo.QSort;
import myUtils.analysis.CPUTime;
import myUtils.analysis.PerformanceResult;
import myUtils.analysis.ResultsV6;
import psofs.VelocityClampBasic;
import weka.classifiers.lazy.IB1;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.Remove;

public class AMSOMain {

	private static String[] clsfr_name= {"3NN", "J48", "NB"}; //"3NN","5NN","SVM",
	static int REPLICATE = 10;
	static int [] rankF;
	private static int MAX_ITER = 100;

	private static boolean DYNAMIC_SWARM = true;
	private static int MAX_ITER_STAG = 7;
	private static int SIZE_DIVISION = 7;

	private static boolean LOCAL_SEARCH = true;
	private static double FLIP_PERCENTAGE = 0.25;
	private static int LS_MAX_ITER = 50;
	private static int LS_FREQ = 2;
	public static int FITN_M_FOLD = 10;
	public static int K_NN = 5;
	private static String RANKING_METHOD = "SU";

	/**
	 * Have 5 command line arg: /local/scratch/Data/HighDimen/SRBCT_GEMS 0 " " 1 0
	 * 0: dataset path: eg. /local/scratch/Data/HighDimen/SRBCT_GEMS
	 * 1: class idx: eg. 0
	 * 2: data seperator, eg. " "
	 * 3: run times: eg. 1
	 * 4: seed: eg. 0
	 * @param args the command line arguments
	 * @throws Exception
	 */

	public static void main(String[] args) throws Exception {

		//1. Read dataset
		Instances data = WekaDatasetHandle.ReadDataset(args[0] + "/data.arff", 1);
		WekaDatasetHandle.PrintDatasetCharacter(data);

		//*********************** IMPORTANT PARAMETER SETTING *********************

		REPLICATE = 10;
		MAX_ITER = 100;

		DYNAMIC_SWARM = true;
		LOCAL_SEARCH = true;

		FLIP_PERCENTAGE = 0.25;
		LS_MAX_ITER = 100;
		FITN_M_FOLD = 10;
		K_NN = 1;

		//read parameters:
		int seed = Integer.parseInt(args[1]);
		SIZE_DIVISION = Integer.parseInt(args[2]);
		System.out.printf("Variable length PSO with size division %d \n", SIZE_DIVISION );
		MAX_ITER_STAG = Integer.parseInt(args[3]);
		System.out.printf("Reinit population when PSO stagnate for %d iterations \n", MAX_ITER_STAG );
		//*********************** END IMPORTANT PARAMETER SETTING *************
		data.randomize(new Random(1));
		data.stratify(REPLICATE);

		//Prepare result recording objects
		ResultsV6 result = new ResultsV6("VLPso.txt", REPLICATE, clsfr_name, false);
		result.setDataName(args[0]);

		FileWriter fileWriter = new FileWriter("Run_"+ seed + "_iter_res.txt");
		BufferedWriter iterWriter = new BufferedWriter(fileWriter);
		iterWriter.write("Iter\tGbestFitn\tGbestsize\tAvgFitn\tAvgSize");

		//3.4 For each fold
		//		int fold = 0; //only run one fold 70 and 30 split
		for (int fold = 0; fold < REPLICATE; fold++)
		{
			Instances train = data.trainCV(REPLICATE, fold);
			Instances test = data.testCV(REPLICATE, fold);

			//Normalise data
			Normalize filter = new Normalize();
			filter.setInputFormat(train);
			train = Filter.useFilter(train, filter);  // configures the Filter based on train instances and returns filtered instances
			test = Filter.useFilter(test, filter);

			//Rank features
			rankF = RankFeature(RANKING_METHOD, train);
			//Reorder the features in training and test set using the rankF order.
			ArrayList<Integer> fea_idx = new ArrayList<Integer>();
			for(int i = 0; i< rankF.length; i++)
				fea_idx.add(rankF[i]);

			train = WekaDatasetHandle.transformDataset(train, fea_idx, "train");
			test = WekaDatasetHandle.transformDataset(test, fea_idx, "test");

			System.out.println("***********FOLD " + fold);

			DynPSO(fold, train, test, seed, result, iterWriter);
		}
		String param = "\nParam: Data, Path, Seed, SIZE_DIVISION, MAX_ITER_STAG, MAX_ITER, LS_FREQ, FITN_M_FOLD, "
				+ "K_NN, Data_Fold:\n";
		for(int i = 0; i< args.length; i++) {
			param += (args[i] + ", ");
		}
		iterWriter.write(param);
		iterWriter.close();
		//record the result to file
		result.recordRun(seed);

	}

	private static void DynPSO(int fold, Instances train, Instances test, long seed, ResultsV6 result, BufferedWriter iterWriter) throws Exception {

		//**********************IMPORTANT SETTINGS
		int number_of_particles =   (train.numAttributes()-1)/20;
		if (number_of_particles > 300)
			number_of_particles = 300;
		if (number_of_particles <= 100)
			number_of_particles = 30;

		int max_number_of_iterations = MAX_ITER;

		System.out.printf("Variable length PSO with size division %d \n", SIZE_DIVISION );
		System.out.println("Pop size: " + number_of_particles + "\nMax iter: "+ max_number_of_iterations);
		//Velocity
		double w;
		double c = 1.49445;
		//Seed
		RandomBing.Seeder.setSeed(seed);
		//Problem
		AMSOFeatureselection problem = new AMSOFeatureselection();
		problem.setMaxVelocity(0.6);
		problem.setMinVelocity(-0.6);
		problem.setClassifier(new IB1());
		problem.setNumFolds(10);
		problem.setDimension(train.numAttributes()-1);
		//		problem.setSU(new SU(train));

		//Set train fold and test fold to problem object
		problem.setTraining(train);
		problem.setTestSet(test);
		//Swarm
		AMSOSwarm s = new AMSOSwarm(FLIP_PERCENTAGE);
		s.setProblem(problem);
		s.setVelocityClamp(new VelocityClampBasic());
		s.setC1(c);
		s.setC2(c);
		if (LOCAL_SEARCH)
			s.prepareLS();

		s.COUNT_LS_FOUND_PBEST = 0;
		//**********************END IMPORTANT SETTINGS

		System.out.println("**********************************************************************************");
		CPUTime a = new CPUTime();
		long startCPUTimeNano = a.getCpuTime();

		s.initialize(SIZE_DIVISION, number_of_particles);

		//5. START PSO
		int iter = 0;
		int nbr_iter_not_improve = 0;
		//		int gbest_idx = 0;//, old_gbest_idx = 0, gbest_age = 0;

		//5.2 Update particle fitness and pbest
		boolean local_search = LOCAL_SEARCH; //((iter < 20)  && (iter % 2 == 0)); true
		boolean found_new_gbest = s.updateFitnessAndLSPbest( local_search, LS_MAX_ITER);
		if (found_new_gbest)
			nbr_iter_not_improve = 0;
		else
			nbr_iter_not_improve++;

		while ((iter < max_number_of_iterations) ) { //&& (nbr_iter_not_improve < MAX_ITER_STAG )) {

			int gbest_size = s.getProblem().subsetSize(s.getGbest().getPersonalPosition());
			double avg_fitn = s.averageFitness();
			double avg_size = s.averageSize();
			double[] res_acc = CalculateTestKNNacc(problem, problem.selFeaIdx(s.getGbest().getPersonalPosition()));
			System.out.printf("iter %d, gbest fitnest: %.4f (%d)| Train Acc: %.2f, Test Acc: %.2f\n", iter, s.getGbest().getPersonalFitness(),
					gbest_size, res_acc[0], res_acc[1]);

			iterWriter.write("\n" +iter + "\t" + s.getGbest().getPersonalFitness() +
					"\t" + gbest_size +
					"\t" + avg_fitn + "\t" + avg_size +
					"\t" + res_acc[0] +"\t"+ res_acc[1] +
					"\t" + (found_new_gbest?"1":"0") );

			s.getProblem().fitness(s.getGbest().getPersonalPosition());

			if (DYNAMIC_SWARM && (nbr_iter_not_improve >= MAX_ITER_STAG)) {
					s.updateSwarm();

				s.updateFitnessAndLSPbest( false, LS_MAX_ITER);
				nbr_iter_not_improve = 0;
			}

			iterWriter.write("\t" + s.get_max_size());

			//5.1 Update the inertia w
			w = (0.9 - ((iter / max_number_of_iterations) * 0.5));

			//5.4 Update velocity and position
			s.updateVelocityPosition(w);

			iter++;
			//5.2 Update particle fitness and pbest
			local_search = LOCAL_SEARCH && (iter < (LS_FREQ * 10) && (iter % LS_FREQ == 0));

			found_new_gbest = s.updateFitnessAndLSPbest( local_search, LS_MAX_ITER);
			if (found_new_gbest)
			{nbr_iter_not_improve = 0; System.out.printf("Gbest changed!\n");}
			else
				nbr_iter_not_improve++;

		}  //end all iterations
		System.out.printf("LS: FOUND/TOTAL: %d/%d = %.2f\n", s.COUNT_LS_FOUND_PBEST,s.TOTAL_LS_CALL, (double)s.COUNT_LS_FOUND_PBEST/s.TOTAL_LS_CALL);

		// ******************** PERFORMANCE EVAL for the fold-th Fold  ***********************
		//3.4. end timer
		long taskCPUTimeNano = a.getCpuTime() - startCPUTimeNano;

		//6. Get the best subset and Transform training and test set by removing unselected features
		int[] selfeatIdx = problem.selFeaIdx(s.getGbest().getPersonalPosition());
		Remove delTransform = new Remove();
		delTransform.setInvertSelection(true);
		delTransform.setAttributeIndicesArray(selfeatIdx);
		delTransform.setInputFormat(problem.getTraining());

		System.out.printf("gbest: subset size / particle size = %d/ %d\n", selfeatIdx.length-1, s.getGbest().getPersonalPosition().size());

		Instances new_train = Filter.useFilter(problem.getTraining(), delTransform);
		Instances new_test = Filter.useFilter(problem.getTestSet(), delTransform);

		Map<String, PerformanceResult> per_result = new HashMap<String, PerformanceResult>();

		for(int i = 0; i< clsfr_name.length; i++) {
			//8. Calculate accuracy
			PerformanceResult res = MyClassifier.CalculateUnbalanceAccuracy(clsfr_name[i],new_train,new_test);
			per_result.put(clsfr_name[i] + " " + fold, res);
			System.out.print(clsfr_name[i] + ": ");
			System.out.println("===> PSO train acc: " + res.getTrain() + ", PSO test acc: " + res.getTest());
		}

		String solution="";
		for(int i = 0; i< selfeatIdx.length-1; i++)	//-1 because the last feature is the class attribute
			solution += rankF[selfeatIdx[i]] +" , ";

		//9. record the train and test result to file
		result.recordFold(fold, iter, solution, s.getGbest().getPersonalFitness(), per_result, new_train.numAttributes()-1, taskCPUTimeNano / 1E6);

	}


	private static double[] CalculateTestKNNacc(Problem problem, int[] selfeatIdx) throws Exception {

		double [] ret = new double[2];
		Remove delTransform = new Remove();
		delTransform.setInvertSelection(true);
		delTransform.setAttributeIndicesArray(selfeatIdx);
		delTransform.setInputFormat(problem.getTraining());

		Instances new_train = Filter.useFilter(problem.getTraining(), delTransform);
		Instances new_test = Filter.useFilter(problem.getTestSet(), delTransform);

		PerformanceResult res = MyClassifier.CalculateUnbalanceAccuracy(clsfr_name[0],new_train,new_test);
		ret[0] = res.getTrain();
		ret[1] = res.getTest();

		return ret;
	}

	static int[] RankFeature(String rankmethod, Instances trainingset)  {
		int[] ranking = new int[trainingset.numAttributes()-1];

		switch(rankmethod) {
		case "SU":
			for(int i = 0; i< ranking.length; i++)
				ranking[i] = i;
			SU su = new SU(trainingset);
			//			for(int i = 0; i< su.suic.length; i++) {
			//				System.out.print(ranking[i] + ":" + su.suic[i] + "\n");
			//			}
			QSort.sort(su.suic, ranking, 0, ranking.length-1);
//						for(int i = 0; i< su.suic.length; i++) {
//							System.out.print(ranking[i] + ":" + su.suic[i] + "\n");
//						}
			break;
		}
		return ranking;
	}

}
