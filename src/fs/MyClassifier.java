/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fs;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import myUtils.analysis.PerformanceResult;
import myUtils.WekaDatasetHandle;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
//import libsvm.LibSVM;
//import net.sf.javaml.classification.BingKNearestNeighbors;
//import net.sf.javaml.classification.Classifier;
//import net.sf.javaml.classification.KNearestNeighbors;
//import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
//import net.sf.javaml.classification.evaluation.PerformanceMeasure;
//import net.sf.javaml.classification.tree.RandomTree;
//import net.sf.javaml.core.Dataset;
//import net.sf.javaml.core.Instance;
//import net.sf.javaml.tools.weka.WekaClassifier;
import weka.classifiers.lazy.IB1;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomTree;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.Utils;

/**
 *
 * @author xuebing
 */
public class MyClassifier {

	//private NaiveBayesClassifier classifier;
	private Classifier classifier;
	protected Random random;
	/*
	 */

	public MyClassifier() {
	}
	public MyClassifier(Random random) {
		this.random = random;
	}

	public void ClassifierKNN(int k) {
		System.out.println("Myclassifier:  new -- Bing-- KNearestNeighbors("+k+")");
		classifier = new IBk();
	}

	public void ClassifierNB() {
		System.out.println("Myclassifier:  new NaiveBayesClassifier()");
		classifier = new NaiveBayes ();
	}

	public void ClassifierLibSVM() {
		System.out.println("Myclassifier:  new LibSVM()");
		classifier = new LibSVM();
	}

	public void ClassifierIB1() {
		System.out.println("Myclassifier: new  Weka IB1()");
		classifier = new IB1();
	}

	public void ClassifierJ48() {
		System.out.println("Myclassifier: new  DT - J48()");
		classifier = new J48();
	}

	public void ClassifierRT(int noFeatures) {
		System.out.println("Myclassifier:  new RandomTree()");
		classifier = new RandomTree();
	}

	/**
	 * @return the classifier
	 */
	public Classifier getClassifier() {
		return classifier;
	}

	/**
	 * @param classifier the classifier to set
	 */
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	public static double[][] crossValidEval(Classifier cls, Instances data, int nbrFold, Random rand) {
		double[][] conf_matrix = new double[data.numClasses()][data.numClasses()];
		data.randomize(rand);
    	data.stratify(nbrFold);

		for(int f = 0; f < nbrFold; f++){
			Instances train = new Instances (data.trainCV(nbrFold, f));
			Instances test = new Instances (data.testCV(nbrFold, f));
			try {
				cls.buildClassifier(train);
				for (int i = 0; i < test.numInstances(); i++){
					int pred = (int) cls.classifyInstance(test.instance(i));
					conf_matrix[(int)test.instance(i).classValue()][pred] +=1;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return conf_matrix;
	}

	public static double[][] eval(Classifier cls, Instances train, Instances test) {
		double[][] conf_matrix = new double[train.numClasses()][train.numClasses()];

		try {
			cls.buildClassifier(train);
			for (int i = 0; i < test.numInstances(); i++){
				int pred = (int) cls.classifyInstance(test.instance(i));
				conf_matrix[(int)test.instance(i).classValue()][pred] +=1;
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return conf_matrix;
	}

	/**
	 * return the unbalanced accuracy of the given classifier name on test set
	 * @param classifier_name
	 * @param train
	 * @param test
	 * @return
	 */
	public static PerformanceResult CalculateUnbalanceAccuracy(String classifier_name, Instances train, Instances test) {

		PerformanceResult ret = new PerformanceResult();
		Classifier clsfr = null;
		try {
			switch (classifier_name) {
			case "IB1":  	{ clsfr = new IB1(); break; }
			case "3NN": 	{ clsfr = new IBk(3); break;}
			case "5NN": 	{ clsfr = new IBk(5); break;}
			case "NB": 	{ clsfr = new NaiveBayes(); break;}
			case "SVM": 	{ clsfr = new LibSVM();
			clsfr.setOptions(Utils.splitOptions("-S 0 -K 0 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -seed 1"));
			break;		}
			case "J48": 	{ clsfr = new J48(); break;}
			}

			Evaluation eval;
			double acc;
			if (classifier_name == "IB1"){
//				acc = unbalanceAcc(crossValidEval(new IB1(), train, 10, new Random(1)));
				acc = WekaDatasetHandle.CalculateLOOCVBalAcc(train);
				ret.setTrain(acc*100);
				acc = unbalanceAcc(eval(new IB1(), train, test));
				ret.setTest(acc*100);
			}
			else {
				clsfr.buildClassifier(train);
				eval = new Evaluation(train);
				eval.evaluateModel(clsfr, train);
				acc = unbalanceAcc(eval.confusionMatrix());
				ret.setTrain(acc*100);

				eval = new Evaluation(train);
				eval.evaluateModel(clsfr, test);
				acc = unbalanceAcc(eval.confusionMatrix());
				ret.setTest(acc*100);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}//End function testFS


	/**
	 * return the unbalanced accuracy of the given classifier name on test set
	 * @param classifier_name
	 * @param train
	 * @param test
	 * @return
	 */
	public static PerformanceResult CalculateUnbalanceAccuracyUsingEvalPackage(String classifier_name, Instances train, Instances test) {

		PerformanceResult ret = new PerformanceResult();
		Classifier clsfr = null;
		try {
			switch (classifier_name) {
			case "IB1":  	{ clsfr = new IB1(); break; }
			case "3NN": 	{ clsfr = new IBk(3); break;}
			case "5NN": 	{ clsfr = new IBk(5); break;}
			case "NB": 	{ clsfr = new NaiveBayes(); break;}
			case "SVM": 	{ clsfr = new LibSVM();
			clsfr.setOptions(Utils.splitOptions("-S 0 -K 0 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -seed 1"));
			break;		}
			case "J48": 	{ clsfr = new J48(); break;}
			}

			Evaluation eval;
			double acc;
			if (classifier_name == "IB1"){
				eval = new Evaluation(train);
				eval.crossValidateModel(new IB1(), train, 10, new Random(1));
				acc = unbalanceAcc(eval.confusionMatrix());
				ret.setTrain(acc*100);
			}
			else {
				clsfr.buildClassifier(train);
				eval = new Evaluation(train);
				eval.evaluateModel(clsfr, train);
				acc = unbalanceAcc(eval.confusionMatrix());
				ret.setTrain(acc*100);
			}

			clsfr.buildClassifier(train);
			eval = new Evaluation(train);
			eval.evaluateModel(clsfr, test);
			acc = unbalanceAcc(eval.confusionMatrix());
			ret.setTest(acc*100);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}//End function testFS

	public static double unbalanceAcc(double[][] confusion_matrix) {
		double nbr_class_has_instances = 0;
		double acc=0;
		for(int i = 0; i< confusion_matrix.length; i++) {
			double sum_row = 0;
			for (int j = 0; j < confusion_matrix[i].length; j++)
				sum_row += confusion_matrix[i][j];
			if (sum_row !=0){
				nbr_class_has_instances ++;
				acc += confusion_matrix[i][i] / sum_row;
			}
		}
		return acc/nbr_class_has_instances;
	}

	/**
	 * return the unbalanced accuracy of the given classifier name on test set
	 * @param classifier_name
	 * @param train
	 * @param test
	 * @return
	 */
	public static PerformanceResult CalculateUnbalance10FCVAccuracy(String classifier_name, Instances data) {

		PerformanceResult ret = new PerformanceResult();
		Classifier clsfr = null;
		try {
			switch (classifier_name) {
			case "IB1":  	{ clsfr = new IB1(); break; }
			case "3NN": 	{ clsfr = new IBk(3); break;}
			case "5NN": 	{ clsfr = new IBk(5); break;}
			case "NB": 	{ clsfr = new NaiveBayes(); break;}
			case "SVM": 	{ clsfr = new LibSVM();
			clsfr.setOptions(Utils.splitOptions("-S 0 -K 0 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -seed 1"));
			break;		}
			case "J48": 	{ clsfr = new J48(); break;}
			}

			Evaluation eval;
			double acc;

			/*if (classifier_name == "IB1"){
				acc = unbalanceAcc(crossValidEval(new IB1(), data, 10, new Random(1)));
				ret.setTrain(acc);
				ret.setTest(acc);
			}
			else {*/
				eval = new Evaluation(data);
				eval.crossValidateModel(clsfr, data, 10, new Random(1));
				acc = unbalanceAcc(eval.confusionMatrix());
				ret.setTrain(acc);
				ret.setTest(acc);
//			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}//End function

	/**
	 * return the standard accuracy of the given classifier name on test set
	 * @param classifier_name
	 * @param train
	 * @param test
	 * @return
	 */
	public static PerformanceResult CalculateStandardAccuracy(String classifier_name, Instances train, Instances test) {

		PerformanceResult ret = new PerformanceResult();
		Classifier clsfr = null;
		try {
			switch (classifier_name) {
			case "IB1":  	{ clsfr = new IB1(); break; }
			case "3NN": 	{ clsfr = new IBk(3); break;}
			case "5NN": 	{ clsfr = new IBk(5); break;}
			case "NB": 	{ clsfr = new NaiveBayes(); break;}
			case "SVM": 	{ clsfr = new LibSVM();
			clsfr.setOptions(Utils.splitOptions("-S 0 -K 0 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -seed 1"));
			break;		}
			case "J48": 	{ clsfr = new J48(); break;}
			}
			Evaluation eval;
			/*clsfr.buildClassifier(fullTrain);
			eval = new Evaluation(fullTrain);
			eval.evaluateModel(clsfr, fullTrain);
			ret.setFtrain(eval.pctCorrect());

			eval = new Evaluation(fullTrain);
			eval.evaluateModel(clsfr, fullTest);
			ret.setFtest(eval.pctCorrect());*/

			clsfr.buildClassifier(train);
			eval = new Evaluation(train);
			eval.evaluateModel(clsfr, train);
			ret.setTrain(eval.pctCorrect());

			eval = new Evaluation(train);
			eval.evaluateModel(clsfr, test);
			ret.setTest(eval.pctCorrect());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}//End function testFS

	//    //do full classification and return accuracy value
	//    public double fullclassify(Dataset training, Dataset testing) {
	//        /*double[] features = new double[training.noAttributes()];
	//        for (int i = 0; i < training.noAttributes(); i++) {
	//            features[i] = 1.0;
	//        }*/
	//        double acc = 0.0;
	//        acc = classify(training, testing);
	//        return acc;
	//    }

	/*    public double classify(Instances training, Instances testing) {
//        Classifier knn = new KNearestNeighbors(5);//        KNearestNeighbors knn = new KNearestNeighbors(5);
        getClassifier().buildClassifier(training);
        Dataset dataForClassification = testing;

        Map<Object, PerformanceMeasure> out = new HashMap<Object, PerformanceMeasure>();
        for (Object o : training.classes()) {
            out.put(o, new PerformanceMeasure());
        }

        for (Instance instance : dataForClassification) {
            Object prediction = getClassifier().classify(instance);
            if (instance.classValue().equals(prediction)) {// prediction                // ==class
                for (Object o : out.keySet()) {
                    if (o.equals(instance.classValue())) {
                        out.get(o).tp++;
                    } else {
                        out.get(o).tn++;
                    }
                }
            } else {// prediction != class
                for (Object o : out.keySet()) {
                     prediction is positive class
                    if (prediction.equals(o )) {
                        out.get(o).fp++;
                    }  instance is positive class  else if (o.equals(instance.classValue())) {
                        out.get(o).fn++;
                    }  none is positive class  else {
                        out.get(o).tn++;
                    }

                }
            }
        }
//                System.out.println("out====: "+out);
        double tp = 0.0, tn = 0.0;
        double Accuracy = 0.0;
        for (Object o : out.keySet()) {
//           System.out.println(o+" TP: " + p.get(o).tp);
            tp += out.get(o).tp;
            tn += out.get(o).tn;
//            System.out.println((p.get(o).tp+p.get(o).tn)/ data.size());
        }


        Accuracy = (tn + tp) / (double) (out.size() * dataForClassification.size());

//        System.out.println("dataForClassification.size()  " + dataForClassification.size());
//        System.out.println("tn " + tn + "    tp  " + tp + "   Accuracy  " + Accuracy);

        //System.out.println("tn " + tn + "    tp  " + tp + "   Accuracy  " + Accuracy);
        return Accuracy;
    }

    public double classify_stand_acc(Dataset training, Dataset testing) {
//        Classifier knn = new KNearestNeighbors(5);//        KNearestNeighbors knn = new KNearestNeighbors(5);
        getClassifier().buildClassifier(training);

        double acc = 0.0;
        for (Instance instance : testing) {
            Object prediction = getClassifier().classify(instance);
            if (instance.classValue().equals(prediction)) {// prediction                // ==class
                {
                    acc +=1;
                }
            }
        }
        return acc/testing.size();
    }

	 *//**
	 * Use unbalanced accuracy which treats all class equally in the formula
	 * NOTE: this function does not work if the test set have only 1 instance like iin case of using LOOCV
	 * @param training
	 * @param testing
	 * @return
	 *//*
    public double classify_unbalanced(Dataset training, Dataset testing) {
//      Classifier knn = new KNearestNeighbors(5);//        KNearestNeighbors knn = new KNearestNeighbors(5);
      getClassifier().buildClassifier(training);
      Dataset dataForClassification = testing;

      Map<Object, PerformanceMeasure> out = new HashMap<Object, PerformanceMeasure>();
      for (Object o : training.classes()) {
          out.put(o, new PerformanceMeasure());
      }

      for (Instance instance : dataForClassification) {
          Object prediction = getClassifier().classify(instance);
          if (instance.classValue().equals(prediction)) {// prediction                // ==class
              for (Object o : out.keySet()) {
                  if (o.equals(instance.classValue())) {
                      out.get(o).tp++;
                  } else {
                      out.get(o).tn++;
                  }
              }
          } else {// prediction != class
              for (Object o : out.keySet()) {
                   prediction is positive class
                  if (prediction.equals(o)) {
                      out.get(o).fp++;
                  }  instance is positive class  else if (o.equals(instance.classValue())) {
                      out.get(o).fn++;
                  }  none is positive class  else {
                      out.get(o).tn++;
                  }

              }
          }
      }

      double Accuracy = 0.0;
      for (Object o : out.keySet()) { //for each class: calculate the weighted accuracy

          Accuracy += 0.5 * (out.get(o).tp / (out.get(o).tp + out.get(o).fn) + out.get(o).tn / (out.get(o).tn + out.get(o).fp));

      }

      Accuracy /= (double) out.size();

      return Accuracy;
  }*/

	/**
	 * 21-July-2017: create for Contrib 1 with FS Bias case:
	 * return the unbalanced LOOCV accuracy for IB1 and
	 * 10F-CV for other clsfrs
	 * @param classifier_name
	 * @param train
	 * @param test
	 * @return
	 */
	public static PerformanceResult CalculateUnbalanceAccuracyFSBias(String classifier_name, Instances data) {

		PerformanceResult ret = new PerformanceResult();
		Classifier clsfr = null;
		try {
			switch (classifier_name) {
			case "IB1":  	{ clsfr = new IB1(); break; }
			case "3NN": 	{ clsfr = new IBk(3); break;}
			case "5NN": 	{ clsfr = new IBk(5); break;}
			case "NB": 	{ clsfr = new NaiveBayes(); break;}
			case "SVM": 	{ clsfr = new LibSVM();
			clsfr.setOptions(Utils.splitOptions("-S 0 -K 0 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -seed 1"));
			break;		}
			case "J48": 	{ clsfr = new J48(); break;}
			}

			Evaluation eval;
			double acc;
			if (classifier_name == "IB1"){
				acc = WekaDatasetHandle.CalculateLOOCVBalAcc(data);
				ret.setTrain(100);
				ret.setTest(acc*100);
			}
			else {
				eval = new Evaluation(data);
				eval.crossValidateModel(clsfr, data, 10, new Random(1));
				acc = unbalanceAcc(eval.confusionMatrix()) * 100;
				ret.setTrain(acc);
				ret.setTest(acc);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}//End function testFS
}
