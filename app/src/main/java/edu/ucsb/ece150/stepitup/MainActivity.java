package edu.ucsb.ece150.stepitup;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
public class MainActivity extends AppCompatActivity implements SensorEventListener  {
    private StepDetector mStepDetector = new StepDetector();
    private int currentStepGoal = 0;
    private int totalSteps;
    private float stepsPerHour;
    private int goalsCompleted;

    private EditText editTextNumber;
    private Button saveButton;
    private Button resetButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        // [TODO] Initialize UI elements
        // [TODO] Request ACTIVITY_RECOGNITION permission
        // [TODO] Initialize accelerometer and step counter sensors
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // [TODO] Handle the raw data. Hint: Provide data to the step detector, call `handleStep` if step detected
        // [TODO] Update UI elements
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Unused
    }

    private void sendNotification(String text) {
        // [TODO] Implement notification
    }

    private void handleStep() {
        // [TODO] Update state / UI on step detected
    }
}
