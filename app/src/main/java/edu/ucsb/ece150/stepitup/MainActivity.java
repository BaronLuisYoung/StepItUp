package edu.ucsb.ece150.stepitup;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;


import java.time.temporal.ValueRange;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener  {
    private StepDetector mStepDetector = new StepDetector();
    private int currentStepGoal = 0;
    private int totalSteps;
    private float stepsPerHour;
    private int goalsCompleted;

    private EditText editTextNumber;
    private Button saveButton;
    private Button resetButton;

    private TextView stepCounterValueView;
    private TextView totalStepsValueView;
    private TextView stepsPerHourValueView;
    private TextView goalsCompletedValueView;


    private long startTimeMillis = System.currentTimeMillis();
    private final Handler handler = new Handler();
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mStepCounterSensor;
    private float[][] accelerometerBuffer;
    private Boolean stepFlag = false;
    private Boolean goalSet = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        restoreData();
        //Creates toolbar and title
        Toolbar toolbar = findViewById(R.id.StepItUpTitle);
        toolbar.setTitle("Step It Up");
        setSupportActionBar(toolbar);

        // [TODO] Setup button behavior
        editTextNumber = findViewById(R.id.editTextNumber);
        saveButton = findViewById(R.id.saveButton);
        resetButton = findViewById(R.id.restartButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "onCreate/saveButton/onClick: entered");
                String text = editTextNumber.getText().toString();
                goalSet = true;
                if (!text.isEmpty()) {
                    try {
                        //save current input
                        int value = Integer.parseInt(text);
                        currentStepGoal = value;
                        Log.d("MainActivity", "onCreate/saveButton/onClick: currentStepGoal value " + Integer.toString(currentStepGoal));

                    } catch (NumberFormatException e) {
                        Log.d("MainActivity", "onCreate/onClick: input value has thrown exception");
                    }
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Reset the count to null (zero in this case)
                Log.d("MainActivity", "onCreate/resetButton/onClick: entered");
                currentStepGoal = 0;
                editTextNumber.setText(""); // Clear the EditText
            }
        });

        // [TODO] Create a thread to calculate steps/hr
        Thread stepsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
                    // Calculate steps/hr
                    stepsPerHour = (totalSteps * 3600000 / elapsedTimeMillis);
                }
            }
        });
        Log.d("MainActivity", "Starting stepsPerHour Thread");
        stepsThread.start();


        // [TODO] Initialize UI elements

        // [TODO] Request ACTIVITY_RECOGNITION permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions( this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
            }
        }
        // [TODO] Initialize accelerometer and step counter sensors

        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor (Sensor.TYPE_ACCELEROMETER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
        if (mAccelerometer != null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (mStepCounterSensor != null) {
            mSensorManager.registerListener(this, mStepCounterSensor, SensorManager. SENSOR_DELAY_FASTEST);
        }
    }

    private void restoreData() {
        Log.d("MainActivity", "restoreData:");
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        if(!sharedPref.contains("MyPrefs")){
            return;
        }

        Log.d("MainActivity", "restoreData: Buffer Restored");

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // [TODO] Handle the raw data. Hint: Provide data to the step detector, call `handleStep` if step detected
        float[] accelerometerVectorSample = event.values;
        if (accelerometerVectorSample.length >= 3) {
            float x = accelerometerVectorSample[0];
            float y = accelerometerVectorSample[1];
            float z = accelerometerVectorSample[2];

            //Log.d("MainActivity", "onSensorChanged - X: " + x + " Y: " + y + " Z: " + z);

            if(mStepDetector.detectStep(x, y, z) && stepFlag == false){
                stepFlag = true;
                handleStep();
            } else if (!mStepDetector.detectStep(x, y, z) && stepFlag == true) {
                stepFlag = false;
            }

        }
        // [TODO] Update UI elements
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause:");
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.apply();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Unused
    }

    private void sendNotification(String text) {
        // [TODO] Implement notification
        // Create and display a Toast notification
        Context context = getApplicationContext(); // Get the application context

        // Duration can be Toast.LENGTH_SHORT or Toast.LENGTH_LONG
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void handleStep() {
        // [TODO] Update state / UI on step detected



        if (currentStepGoal > 0) {
            currentStepGoal -= 1;
            EditText editText = findViewById(R.id.editTextNumber);
            editText.setText("" + (currentStepGoal));
            if(currentStepGoal == 0 && goalSet == true) {
                goalSet = false;
                sendNotification("Goal Completed");
                goalsCompleted += 1;
                goalsCompletedValueView = findViewById(R.id.goalsCompletedValue);
                goalsCompletedValueView.setText("" + goalsCompleted);
            }
        }

        totalSteps++;
        stepCounterValueView = findViewById(R.id.totalStepsValue);
        stepCounterValueView.setText(""+ totalSteps);

        stepsPerHourValueView = findViewById(R.id.stepsPerHourValue);
        stepsPerHourValueView.setText("" + stepsPerHour);

    }
}
