/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myUtils.maths;

import java.util.Arrays;



/**
 *
 * @author tranbinh
 */


public class MyStatistics
{

    public static double meanMeadian(double[] fea_value) {

        double mean_val = mean(fea_value), median_val = median(fea_value);
        System.out.println("mean = " + mean_val + ", median = " + median_val+ "\t-> "+ (mean_val - median_val));
        return Math.abs(mean_val - median_val);


    }

    public static void main(String[] args)  {

        double[] arr = {3,6,8,3,14,7,73,12,19,5};
        System.out.print(mean(arr) + "," + median(arr));

    }

    double[] data;
    double size;

    public MyStatistics(double[] data)
    {
        this.data = data;
        size = data.length;
    }

    public double getMean()
    {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum/size;
    }

    public static double mean(double[] arr)
    {
        double sum = 0.0;
        for(double a : arr)
            sum += a;
        return sum/arr.length;
    }

    public double getMax()
    {
        double max = data[0];
        for(double a : data)
            if (max < a) max = a;
            return max;
    }

    public double getMaxIdx()
    {
        double max = data[0];
        int max_idx = 0;
        for(int i = 0; i< data.length; i++)
            if (max < data[i])  max_idx = i;
        return max_idx;
    }

    public double getMin()
    {
        double min = data[0];
        for(double a : data)
            if (min> a) min = a;
            return min;
    }

    public double getVariance()
    {
        double mean = getMean();
        double temp = 0;
        for(double a :data)
            temp += (a-mean)*(a-mean);
        return temp/(size-1);
    }

    public double getStdDev()
    {
        return Math.sqrt(getVariance());
    }

    public static double median(double[] arr)
    {
       double[] b = new double[arr.length];
       System.arraycopy(arr, 0, b, 0, b.length);
       Arrays.sort(b);

       for(int i=0; i< b.length; i++)
           System.out.print(b[i] + ", ");

       System.out.println();

       if (arr.length % 2 == 0)
       {
          return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
       }
       else
       {
          return b[b.length / 2];
       }
    }
}


