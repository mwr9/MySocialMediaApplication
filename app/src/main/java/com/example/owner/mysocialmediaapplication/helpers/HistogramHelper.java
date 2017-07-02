package com.example.owner.mysocialmediaapplication.helpers;

import android.util.SparseArray;

import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 Helper classes for the Histogram
 */
public class HistogramHelper {

    private static final String TAG = HistogramHelper.class.getSimpleName();

    private HistogramHelper() {
    }

    public static SparseArray<ArrayList<Float>> createCompartments(Mat histogram) {
        int binsCount = 256;
        float[] histData = new float[binsCount];
        histogram.get(0, 0, histData);
        int compartmentsCount = 5;
        int interval = binsCount / compartmentsCount;
        SparseArray<ArrayList<Float>> compartments = new SparseArray<>();
        for (int i = 0; i < compartmentsCount; i++) {
            int start = interval * i;
            int end = start + interval;
            ArrayList<Float> tmp = new ArrayList<>();
            for (int j = start; j < end; j++) {
                tmp.add(histData[j]);
            }
            compartments.put(i, tmp);
        }
        return compartments;
    }

    public static float sumCompartmentValues(int index, SparseArray<ArrayList<Float>> compartments) {
        float sum = 0L;
        if (index < 0 || index > compartments.size()) {
            throw new ArrayIndexOutOfBoundsException("index ∈ <0;" + (compartments.size() - 1) + ">");
        } else {
            for (int i = 0; i < compartments.get(index).size(); i++) {
                sum += compartments.get(index).get(i);
            }
        }
        return sum;
    }

    public static float sumCompartmentsValues(SparseArray<ArrayList<Float>> compartments) {
        float sum = 0L;
        for (int i = 0; i < compartments.size(); i++) {
            for (int j = 0; j < compartments.get(i).size(); j++) {
                sum += compartments.get(i).get(j);
            }
        }
        return sum;
    }

    public static float averageValueOfCompartment(int index, SparseArray<ArrayList<Float>> compartments) {
        if (index < 0 || index > compartments.size()) {
            throw new ArrayIndexOutOfBoundsException("index ∈ <0;" + (compartments.size() - 1) + ">");
        } else {
            return sumCompartmentValues(index, compartments) / compartments.get(index).size();
        }
    }

    public static float averageValueOfCompartments(SparseArray<ArrayList<Float>> compartments) {
        return sumCompartmentsValues(compartments) / compartments.size();
    }

    public static float percentageOfCompartment(int index, SparseArray<ArrayList<Float>> compartments) {
        if (index < 0 || index > compartments.size()) {
            throw new ArrayIndexOutOfBoundsException("index ∈ <0;" + (compartments.size() - 1) + ">");
        } else {
            return (sumCompartmentValues(index, compartments) * 100) / sumCompartmentsValues(compartments);
        }
    }

    public static float averagePercentageOfCompartment(int index, SparseArray<ArrayList<Float>> compartments) {
        if (index < 0 || index > compartments.size()) {
            throw new ArrayIndexOutOfBoundsException("index ∈ <0;" + (compartments.size() - 1) + ">");
        } else {
            return (averageValueOfCompartment(index, compartments) * 100) / averageValueOfCompartments(compartments);
        }
    }
}
