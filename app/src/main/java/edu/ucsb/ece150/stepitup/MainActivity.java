package edu.ucsb.ece150.stepitup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener  {
    private StepDetector mStepDetector = new StepDetector();
    private int currentStepGoal = 0;

    private int builtInStepCounter;
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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Code to be executed in the UI thread
                            // For example, updating UI components
                            stepsPerHourValueView = findViewById(R.id.stepsPerHourValue);
                            stepsPerHourValueView.setText("" + stepsPerHour);
                        }
                    });

                    try {
                        Thread.sleep(3000); // Sleep for 3 seconds (3000 milliseconds)
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
            mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
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



            builtInStepCounterView = findViewById(R.id.builtInstepCounterValue);
            Log.d("MainActivity", "restoreData: builtInStepCounter " + builtInStepCounter);
            builtInStepCounterView.setText("" + builtInStepCounter);

            stepCounterValueView = findViewById(R.id.totalStepsValue);
            Log.d("MainActivity", "restoreData: totalSteps " + totalSteps);
            stepCounterValueView.setText(""+ totalSteps);

            stepsPerHourValueView = findViewById(R.id.stepsPerHourValue);
            Log.d("MainActivity", "restoreData: stepsPerHour " + stepsPerHour);
            stepsPerHourValueView.setText("" + stepsPerHour);

            if(goalSet == true) {
                goalsCompletedValueView = findViewById(R.id.goalsCompletedValue);
                Log.d("MainActivity", "restoreData: goalsCompleted " + goalsCompleted);
                goalsCompletedValueView.setText("" + goalsCompleted);

                editTextNumber = findViewById(R.id.editTextNumber);
                Log.d("MainActivity", "restoreData: currentStepGoal " + currentStepGoal);
                editTextNumber.setText("" + (currentStepGoal));
            }else{
                goalsCompletedValueView = findViewById(R.id.goalsCompletedValue);
                goalsCompleted+=1;
                Log.d("MainActivity", "restoreData: goalsCompleted " + goalsCompleted);
                goalsCompletedValueView.setText("" + goalsCompleted);

                editTextNumber = findViewById(R.id.editTextNumber);
                Log.d("MainActivity", "restoreData: currentStepGoal " + currentStepGoal);
                editTextNumber.setText("" + 0);
            }


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
             //Log.d("MainActivity", "onSensorChanged stepCounterSensor: " + builtInStepCounter);
             builtInStepCounterView = findViewById(R.id.builtInstepCounterValue);
             builtInStepCounterView.setText(""+ builtInStepCounter);
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
    protected void onResume() {
        super.onResume();


        Log.d("MainActivity", "onResume: entered");

    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Unused
    }

    private void sendNotification(String text) {
        // Create and display a notification
        Log.d("MainActivity", "sendNotification: entered");
        Context context = getApplicationContext(); // Get the application context

        // Create an Intent to launch when the notification is clicked
        Intent intent = new Intent(context, MainActivity.class); // Replace MainActivity with your desired activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        Log.d("MainActivity", "sendNotification: intent created");

        // Create a NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Step It Up Notification")
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Log.d("MainActivity", "sendNotification:Notification builder created");

        // Get the NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null)
        {
            Log.d("MainActivity", "sendNotification: notificationManager");
        }
        // Check if the notification channel exists (for Android 8.0 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            CharSequence channelName = "Channel Name";
            String channelDescription = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
            Log.d("MainActivity", "sendNotification: build version code passed");

        }

        // Show the notification
        notificationManager.notify(0, builder.build());
        Log.d("MainActivity", "sendNotification: notification manager notified");


        Toast.makeText(context, "Step It Up goal complete", Toast.LENGTH_SHORT).show();
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
    }
}
