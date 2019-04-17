/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myUtils.analysis;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

import org.omg.stub.java.rmi._Remote_Stub;



/**
 * This version is revised from ResultV3. It will record the result of many classifiers
 * for PSO selected features. used in MDL_PSO program
 *
 * @author tranbinh
 */
public class ResultsV6 {

	String _res_file_name = "DFSRun.txt";
	int _max_folds;

	//record for all folds in 1 run
	Map<String, PerformanceResult> _per_result;
	private String[] _clsfr_name;
	String[] _solution; //one solution is a feature subset
	double[] _subset_size;
	double[] _fitness;
	int[] _nbr_actual_iter;
	boolean _debug_mode;

	long _seeder;
	double _cpu_time ; //the CPUTime

	private String _data_name;

	public ResultsV6(String res_file_name, int max_folds, String[] clsfr_name, boolean debug_mode){
		_max_folds = max_folds;
		_subset_size = new double[_max_folds];
		_fitness = new double[_max_folds];
		_nbr_actual_iter = new int[max_folds];
		_solution = new String[_max_folds];
		_clsfr_name = clsfr_name;
		_res_file_name = res_file_name;
		_debug_mode = debug_mode;
	}

	public void recordRun (long seeder) {
		_seeder = seeder;
		writeResult1Run();
	}

	public void setDataName(String dataName)
	{
		_data_name = dataName;
	}

	private void writeResult1Run() {
		String s;

		double  avg_train_acc = 0, avg_test_acc = 0, avg_subset_size = 0, avg_fitness = 0;

		//Calculate average and put into _per_result as the _max_foldsth fold
		for(int k = 0; k< _clsfr_name.length; k++) {
			_per_result.put(_clsfr_name[k] + " " + _max_folds, new PerformanceResult(0, 0));
		}
		for(int i=0; i< _max_folds; i++){
			avg_subset_size += _subset_size[i];
			avg_fitness += _fitness[i];
			for(int k = 0; k< _clsfr_name.length; k++) {
				double tmpTr = _per_result.get(_clsfr_name[k] + " " + i).getTrain() + _per_result.get(_clsfr_name[k] + " " + _max_folds).getTrain();
				double tmpTe = _per_result.get(_clsfr_name[k] + " " + i).getTest() + _per_result.get(_clsfr_name[k] + " " + _max_folds).getTest();
				_per_result.put(_clsfr_name[k] + " " + _max_folds, new PerformanceResult(tmpTr, tmpTe));
			}
		}

		avg_subset_size /= _max_folds;
		avg_fitness /= _max_folds;

		for(int k = 0; k< _clsfr_name.length; k++) {
			avg_train_acc = _per_result.get(_clsfr_name[k] + " " + _max_folds).getTrain() / _max_folds;
			avg_test_acc = _per_result.get(_clsfr_name[k] + " " + _max_folds).getTest() / _max_folds;
			_per_result.put(_clsfr_name[k] + " " + _max_folds, new PerformanceResult(avg_train_acc, avg_test_acc));
		}

		try {
			FileWriter fileWriter = new FileWriter(_seeder + _res_file_name, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			//Write average results of all folds

			bufferedWriter.write("NbrFold\t AvgSize\t AvgFit\t CPUTime\t");
			for(int k = 0; k< _clsfr_name.length; k++) {
				bufferedWriter.write(_clsfr_name[k]+"Tr\t" + _clsfr_name[k]+"Te\t");
			}

			s = String.format("\n%d\t%.2f\t%.2f\t%.2f\t", _max_folds, avg_subset_size, avg_fitness,_cpu_time);
			bufferedWriter.write(s);
			for(int k = 0; k< _clsfr_name.length; k++) {
				s = String.format("%.2f\t%.2f\t",
						_per_result.get(_clsfr_name[k]+ " " + _max_folds).getTrain() ,
						_per_result.get(_clsfr_name[k]+ " " + _max_folds).getTest() );
				bufferedWriter.write(s);
			}

			//Write results of each fold:
			bufferedWriter.write("\nFold\tRunIter\tSize\tFitn\t");
			for(int k = 0; k< _clsfr_name.length; k++) {
				bufferedWriter.write(_clsfr_name[k]+"Tr\t" + _clsfr_name[k]+"Te\t");
			}
			bufferedWriter.write("\n");

			for (int i = 0; i < _max_folds; i++) {
				s = String.format("%d\t%d\t%.2f\t%.2f\t", i + 1, _nbr_actual_iter[i], _subset_size[i], _fitness[i]);
				bufferedWriter.write(s);
				for(int k = 0; k< _clsfr_name.length; k++) {
					s = String.format("%.2f\t%.2f\t",
							_per_result.get(_clsfr_name[k]+ " " + i).getTrain() ,
							_per_result.get(_clsfr_name[k]+ " " + i).getTest() );
					bufferedWriter.write(s);
				}
				bufferedWriter.write("\n");
			}
			//Write feature subset for each fold
			if(!_debug_mode) {
				s = "";
				for (int fold = 0; fold < _max_folds; fold++) {
					s += String.format("\n %s Solution of fold %d :\n ", _data_name, fold+1);
					if (_solution[fold] != null) {
						s += _solution[fold] ;
					}
				}
				bufferedWriter.write("\n");
				bufferedWriter.write(s);
			}

			bufferedWriter.write("\n");
			bufferedWriter.close();

		} catch (IOException ex) {
			System.out.println("Error writing to file '" + _res_file_name + "'");
		}

	}//end function

	public void recordFold(int fold, int nbr_actual_iter, String solution,
			double fitness, Map<String, PerformanceResult> per_result, int size, double time) {

		_nbr_actual_iter[fold]  = nbr_actual_iter;
		_subset_size[fold]      = size;
		if (_per_result == null)
			_per_result = per_result;
		else
			_per_result.putAll(per_result);
		_solution[fold]     = solution;
		_fitness[fold] = fitness;
		_cpu_time += time;  //time here is in milisecond


	}

}//end class
