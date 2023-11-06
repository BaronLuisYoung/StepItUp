package edu.ucsb.ece150.stepitup;

import android.util.Log;

public class StepDetector {

    private static final int SAMPLE_HISTORY_SIZE = 1000;
    private double[] sampleHistory = new double[SAMPLE_HISTORY_SIZE];
    private int sampleIndex = 0;
    private double runningAverage = 0.0;
    private static final double THRESHOLD = 13; // Adjust this threshold as needed
    public StepDetector() {
        for (int i = 0; i < SAMPLE_HISTORY_SIZE; i++) {
            sampleHistory[i] = 9.80; // Initialize each value to 9.80
        }
    }//CTOR for class

    public boolean detectStep(float x, float y, float z) {
        double sample = Math.sqrt(x * x + y * y + z * z);
        sampleHistory[sampleIndex] = sample;

        // Calculate the running average
        runningAverage = calculateRunningAverage();

        // Check if a step occurred based on the running average and threshold
        boolean stepDetected = sample > runningAverage + THRESHOLD;



        // Increment the sample index and wrap around if needed
        sampleIndex = (sampleIndex + 1) % SAMPLE_HISTORY_SIZE;

        return stepDetected;
    }

    private double calculateRunningAverage() {
        double sum = 0.0;
        for (int i = 0; i < SAMPLE_HISTORY_SIZE; i++) {
            sum += sampleHistory[i];
        }
        return sum / SAMPLE_HISTORY_SIZE;
    }
}