/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myUtils.maths;

//import java.util.List;
//import java.util.ArrayList;
import myUtils.RandomBing;
//import myUtils.algo.pickFeatures;

import java.lang.Math.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//import pso.psofs.Swarm;

/**
 *
 * @author xuebing
 */
public class NewMath {

    public static double Scale(double src_min, double src_max, double value, double target_min, double target_max) {

        if (src_max == src_min) {
            System.out.println("src_max == src_min ");
            return (1 / 2) * (target_max - target_min) + target_min;
        } else {
            return  (target_max - target_min)*(value -src_min) / (src_max - src_min) + target_min;
        }
    }


    public static double[] AverageRunIterations(double ARI[][]) {
        double results[] = new double[ARI[0].length];
        for (int i = 0; i < ARI[0].length; ++i) {
            for (int r = 0; r < ARI.length; ++r) {
                results[i] += ARI[r][i];
            }
            results[i] = results[i] / ARI.length;
        }
        return results;
    }

    public static int ModEuclidean(int D, int d) {
        int r = D % d;
        if (r < 0) {
            if (d > 0) {
                r = r + d;
            } else {

                r = r - d;
            }
        }
        return r;
    }


    // prodce t different int numbers within 0-n
    public static int[] fn(int t, int n) {
        Set<Integer> set = new HashSet<Integer>();

        while (set.size() < t) {
            int raNum = (int) (RandomBing.Create().nextDouble() * n);
            set.add(raNum);
        }
        int[] array = new int[t];

        int i = 0;
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
        	array[i++] = (int)iter.next();
        }

        return array;
    }

    /*Divid the datasets, select test from two sides,
    select the first fold, last fold, the third fold, and the third last fold as test set; 0 9 2,   0 9 2 7 4,  0 9 2 7 4 5;
     */
    public static int[] divData(int numTest, int numTrain) {
        int[] select = new int[numTest];
        int tem1 = 0;
        for (int i = 0; i < numTest; i = i + 2) {
            select[tem1] = i;
            if (tem1 < numTest - 1) {
                select[tem1 + 1] = numTrain + numTest - 1 - i;
                tem1 = tem1 + 2;
            } else {
                break;
            }
        }
        return select;
    }

    public static void main(String[] args) {
    	int[] tmp ;

    	tmp = fn(10, 150);
    	for(int i=0; i< tmp.length; i++)
    		System.out.print(tmp[i] + ", ");
    }
}
