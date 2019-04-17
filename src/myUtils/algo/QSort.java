/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myUtils.algo;

/**
 *
 * @author tranbinh
 */
public class QSort {

    public static void main(String[] args) {
        double[] key = {2.3, 4.6, 6.1, 3.6, 2.3, 5.7, 9.1, 1.8};
        int[] idx = {0, 1, 2, 3, 4, 5, 6, 7};

//        sort(key, idx, 0, key.length - 1);
        sortAsc(key, idx);

        for (int i = 0; i < key.length; i++) {
            System.out.print(key[i] + ", ");
        }
        System.out.println();
        for (int i = 0; i < key.length; i++) {
            System.out.print(idx[i] + ", ");
        }
    }

    /**
     * Sort (descending order) the sort_idx array based on the based_key array value.
     *
     */
    public static void sort(double[] based_key, int[] sort_idx, int lo, int hi) { // See page 289 for public sort() that calls this method.

        if (hi <= lo) {
            return;
        }
        int lt = lo, i = lo + 1, gt = hi;
        double v = based_key[lo];
        while (i <= gt) {
//            int cmp = a[i].compareTo(v);
            if (based_key[i] > v) {
                exch(based_key, sort_idx, lt++, i++);
            } else if (based_key[i] < v) {
                exch(based_key, sort_idx, i, gt--);
            } else {
                i++;
            }
        } // Now a[lo..lt-1] < v = a[lt..gt] < a[gt+1..hi].
        sort(based_key, sort_idx, lo, lt - 1);
        sort(based_key, sort_idx, gt + 1, hi);

    }


    private static void exch(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static void exch(double[] a, int[] sort, int i, int j) {
        double tmp1 = a[i];
        a[i] = a[j];
        a[j] = tmp1;

        int tmp2 = sort[i];
        sort[i] = sort[j];
        sort[j] = tmp2;
    }


    public static void sortAsc(double[] key, int[] idx){

    	sort(key, idx, 0, key.length - 1);
    	int start = 0, end = key.length - 1;
        while (start < key.length/2) {
        	exch(key, idx, start, end);
        	start++;
        	end--;
        }

    }

	public static void sort(float[] based_key, int[] sort_idx, int lo, int hi) {
		if (hi <= lo) {
            return;
        }
        int lt = lo, i = lo + 1, gt = hi;
        float v = based_key[lo];
        while (i <= gt) {
//            int cmp = a[i].compareTo(v);
            if (based_key[i] > v) {
                exch(based_key, sort_idx, lt++, i++);
            } else if (based_key[i] < v) {
                exch(based_key, sort_idx, i, gt--);
            } else {
                i++;
            }
        } // Now a[lo..lt-1] < v = a[lt..gt] < a[gt+1..hi].
        sort(based_key, sort_idx, lo, lt - 1);
        sort(based_key, sort_idx, gt + 1, hi);

	}

	private static void exch(float[] a, int[] sort, int i, int j) {
		float tmp1 = a[i];
        a[i] = a[j];
        a[j] = tmp1;

        int tmp2 = sort[i];
        sort[i] = sort[j];
        sort[j] = tmp2;

	}

	public static void sort(int[] based_key, int[] sort_idx, int lo, int hi) { // See page 289 for public sort() that calls this method.

        if (hi <= lo) {
            return;
        }
        int lt = lo, i = lo + 1, gt = hi;
        double v = based_key[lo];
        while (i <= gt) {
//            int cmp = a[i].compareTo(v);
            if (based_key[i] > v) {
                exch(based_key, sort_idx, lt++, i++);
            } else if (based_key[i] < v) {
                exch(based_key, sort_idx, i, gt--);
            } else {
                i++;
            }
        } // Now a[lo..lt-1] < v = a[lt..gt] < a[gt+1..hi].
        sort(based_key, sort_idx, lo, lt - 1);
        sort(based_key, sort_idx, gt + 1, hi);

    }

	private static void exch(int[] a, int[] sort, int i, int j) {
		int tmp1 = a[i];
        a[i] = a[j];
        a[j] = tmp1;

        int tmp2 = sort[i];
        sort[i] = sort[j];
        sort[j] = tmp2;
    }

}
