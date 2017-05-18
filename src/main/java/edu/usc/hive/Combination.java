package edu.usc.hive;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by charith on 5/17/17.
 */
public class Combination {

    static void combinationUtil(List<Integer> arr, int data[], int start,
                                int end, int index, int r, List<HashSet<Integer>> result)
    {
        // Current combination is ready to be printed, print it
        if (index == r)
        {
            HashSet<Integer> vals = new HashSet<Integer>();
            for (int j=0; j<r; j++) {
                vals.add(data[j]);
            }
            if(vals.size() == r)
                result.add(vals);
            return;
        }

        // replace index with all possible elements. The condition
        // "end-i+1 >= r-index" makes sure that including one element
        // at index will make a combination with remaining elements
        // at remaining positions
        for (int i=start; i<=end && end-i+1 >= r-index; i++)
        {
            data[index] = arr.get(i);
            combinationUtil(arr, data, i+1, end, index+1, r, result);
        }
    }



    public static List<HashSet<Integer>> getCombination(List<Integer> arr, int r) {

        int data[]= new int[r];
        List<HashSet<Integer>> result = new ArrayList<HashSet<Integer>>();
        combinationUtil(arr, data, 0, arr.size() -1, 0, r, result);
        return result;
    }


    /**
     * Just for testing
     * @param args
     */
    public static void main(String[] args) {

        ArrayList<Integer> vals = new ArrayList<Integer>();
        for(int i=0; i < 5; i++) {
            vals.add(i);
        }

        List<HashSet<Integer>> result = getCombination(vals, 3);

        for(int i=0; i < result.size(); i++) {

            HashSet<Integer> l = result.get(i);

            for(Integer e: l) {
                System.out.println(e);
            }

            System.out.println("*********");

        }

        System.out.println(result.size());

    }

}
