/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package psofs;

import myUtils.maths.NewMath;
import fs.Problem;
import myUtils.RandomBing;

/**
 *
 * @author xuebing
 */
public class Initialisation {

    public static double[] NormalInitialisation(int positionsize, Problem problem) {
        double[] position = new double[positionsize];

        if (problem.getBinaryContin()) {
            for (int i = 0; i < positionsize; ++i) {
                position[i] = RandomBing.Create().nextDouble();
                if (position[i] < problem.getThreshold()) {
                    position[i] = 1.0;
                } else {
                    position[i] = 0.0;
                }
            }
        } else {
            for (int i = 0; i < positionsize; ++i) {
                position[i] = RandomBing.Create().nextDouble();
            }
        }
        return position;
    }

/**
 * Initialize a random feature subsets that has N features selected
 * @param positionsize
 * @param N
 * @param binaryContinue: true is binary
 * @return
 */
    public static double[] NInitialisation(int positionsize, int N, Boolean binaryContinue) {
        double[] position = new double[positionsize];
        if (binaryContinue) { //binary
            for (int i = 0; i < positionsize; i++) {
                position[i] = 0.0;
            }
            int[] Ini = NewMath.fn(N, positionsize - 1);
            for (int i = 0; i < N; ++i) {
                position[Ini[i]] = 1.0;
            }

        } else { //continuous
            for (int i = 0; i < positionsize; i++) {
                position[i] = RandomBing.Create().nextDouble() / 2.0;
            }
            int[] Ini = NewMath.fn(N, positionsize - 1);
            for (int i = 0; i < N; ++i) {
                position[Ini[i]] = RandomBing.Create().nextDouble() * 0.4 + 0.6;  // 0.6 is the threshold
            }
        }
//        for (int i = 0; i < position.length; i++) {
//            System.out.print(position[i] + " ");
//        }
//        System.out.println("---");


        return position;

    }

    /**
     * Initialize a random feature subsets that has more than 50% nbr features selected
     * @param positionsize
     * @param binaryContinue
     * @return
     */
    public static double[] Halfintialisation1(int positionsize, Boolean binaryContinue) {
        double[] position = new double[positionsize];
        int largeNO = (int) ((RandomBing.Create().nextDouble() / 2.0 + 0.5) * positionsize); // to generate a number between positionsize/2 and positionsize;
//        System.out.println("largeNO  " + largeNO);
        int[] Ini = NewMath.fn(largeNO, positionsize - 1);
        if (binaryContinue) {
            for (int i = 0; i < positionsize; i++) {
                position[i] = 0.0;
            }
            for (int i = 0; i < Ini.length; ++i) {
                position[Ini[i]] = 1.0;
            }
        } else {
            for (int i = 0; i < positionsize; i++) {
                position[i] = RandomBing.Create().nextDouble() / 2.0;
            }
            for (int i = 0; i < Ini.length; ++i) {

                position[Ini[i]] = RandomBing.Create().nextDouble() * 0.4 + 0.6;  // 0.6 is the threshold
            }
        }
//        for (int i = 0; i < position.length; i++) {
//            System.out.print(position[i] + " ");
//        }
//        System.out.println("---");


        return position;

    }
}
