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

public class MainActivity extends AppCompatActivity implements SensorEventListener  {
    private StepDetector mStepDetector = new StepDetector();
    private int currentStepGoal = 0;

    private int builtInStepCounter = 0;
    private int totalSteps  = 0;
    private float stepsPerHour = 0;
    private int goalsCompleted = 0;


    private EditText editTextNumber;
    private Button saveButton;
    private Button resetButton;

    private TextView stepCounterValueView;
    private TextView builtInStepCounterView;
    private TextView stepsPerHourValueView;
    private TextView goalsCompletedValueView;


    private long startTimeMillis = System.currentTimeMillis();
    private final Handler handler = new Handler();
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mStepCounterSensor;
    private Boolean stepFlag = false;
    private Boolean goalSet = false;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_land);
        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
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
                        goalSet = true;
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
                goalSet = false;
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
            Log.d("MainActivity", "onCreate: enabled mStepCounterSensor ");
            mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
        if (mAccelerometer != null) {
            Log.d("MainActivity", "Accelerometer sensor available.");
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        } else {
        Log.d("MainActivity", "Accelerometer sensor not available.");
        }

        if (mStepCounterSensor != null) {
            Log.d("MainActivity", "Step counter sensor available.");
            mSensorManager.registerListener(this, mStepCounterSensor, SensorManager. SENSOR_DELAY_FASTEST);
        } else {
        Log.d("MainActivity", "Step counter sensor not available.");
        }
    }

    private void restoreData() {
        Log.d("MainActivity", "restoreData:entered");
        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        if(sharedPref.contains("builtInStepCounter")){
            currentStepGoal = sharedPref.getInt("currentStepGoal", 0);
            builtInStepCounter =  sharedPref.getInt("builtInStepCounter", 0);
            totalSteps = sharedPref.getInt("totalSteps",0);
            stepsPerHour = sharedPref.getFloat("stepsPerHour", 0);
            goalsCompleted = sharedPref.getInt("goalsCompleted", 0);

            Log.d("MainActivity", "restoreData: Data values Restored");

            editTextNumber = findViewById(R.id.editTextNumber);
            Log.d("MainActivity", "restoreData: currentStepGoal " + currentStepGoal);
            editTextNumber.setText("" + (currentStepGoal));

            builtInStepCounterView = findViewById(R.id.builtInstepCounterValue);
            Log.d("MainActivity", "restoreData: builtInStepCounter " + builtInStepCounter);
            builtInStepCounterView.setText("" + builtInStepCounter);

            stepCounterValueView = findViewById(R.id.totalStepsValue);
            Log.d("MainActivity", "restoreData: totalSteps " + totalSteps);
            stepCounterValueView.setText(""+ totalSteps);

            stepsPerHourValueView = findViewById(R.id.stepsPerHourValue);
            Log.d("MainActivity", "restoreData: stepsPerHour " + stepsPerHour);
            stepsPerHourValueView.setText("" + stepsPerHour);


            goalsCompletedValueView = findViewById(R.id.goalsCompletedValue);
            Log.d("MainActivity", "restoreData: goalsCompleted " + goalsCompleted);
            goalsCompletedValueView.setText("" + goalsCompleted);

            Log.d("MainActivity", "restoreData: Data Restored to screen");

        }



    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // [TODO] Handle the raw data. Hint: Provide data to the step detector, call `handleStep` if step detected
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float[] accelerometerVectorSample = event.values;
            if (accelerometerVectorSample.length >= 3) {
                float x = accelerometerVectorSample[0];
                float y = accelerometerVectorSample[1];
                float z = accelerometerVectorSample[2];

                if (mStepDetector.detectStep(x, y, z) && stepFlag == false) {
                    stepFlag = true;
                    handleStep();
                } else if (!mStepDetector.detectStep(x, y, z) && stepFlag == true) {
                    stepFlag = false;
                }
        }
            //Log.d("MainActivity", "onSensorChanged: TEST ");
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
             builtInStepCounter = (int) event.values[0];
            Log.d("MainActivity", "onSensorChanged stepCounterSensor: " + builtInStepCounter);
             builtInStepCounterView = findViewById(R.id.builtInstepCounterValue);
             builtInStepCounterView.setText(""+ builtInStepCounterView);
        }

        }
        // [TODO] Update UI elements
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause: entered");
        editor = sharedPref.edit();

        editor.putInt("currentStepGoal", currentStepGoal);
        editor.putInt("builtInStepCounter", builtInStepCounter);
        editor.putInt("totalSteps",totalSteps);
        editor.putFloat("stepsPerHour", stepsPerHour);
        editor.putInt("goalsCompleted", goalsCompleted);

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
            editTextNumber = findViewById(R.id.editTextNumber);
            editTextNumber.setText("" + (currentStepGoal));
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
