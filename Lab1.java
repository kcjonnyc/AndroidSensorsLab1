package lab1.uwaterloo.ca.lab1;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import ca.uwaterloo.sensortoy.LineGraphView;

public class Lab1 extends AppCompatActivity {
    // class variables
    LinearLayout Lay1; // layout
    TextView view1; // textviews
    TextView view2;
    TextView view3;
    TextView view4;
    TextView view5;
    TextView view6;
    TextView view7;
    TextView view8;
    TextView lightSensorReading; // reading textviews
    TextView highLightSensorReading;
    TextView accelerometerReading;
    TextView highAccelerometerReading;
    TextView magneticSensorReading;
    TextView highMagneticSensorReading;
    TextView rotationalSensorReading;
    TextView highRotationalSensorReading;
    LineGraphView graph; // graph
    float[][] accelReadings = new float[100][3];

    // method to create label and add to layout
    public TextView createLabel(String text) {
        // create a textview that has not been defined in the xml
        // getApplContext generates a new id for textview reference
        TextView tv = new TextView(getApplicationContext());
        tv.setText(text); // set text passed to method
        tv.setTextColor(Color.BLACK);
        Lay1.addView(tv); // add to layout
        return tv;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab1);

        // Layout initialization
        Lay1 = (LinearLayout) findViewById(R.id.activity_lab1); // get reference to layout
        Lay1.setBackgroundColor(Color.WHITE);
        Lay1.setOrientation(LinearLayout.VERTICAL);

        // Graph initialization
        graph = new LineGraphView(getApplicationContext(), 100, Arrays.asList("x", "y", "z"));
        Lay1.addView(graph);
        graph.setVisibility(View.VISIBLE);

        // Reset button initialization
        Button resetButton = new Button(getApplicationContext());
        resetButton.setText("Clear Record-High Data");
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LightSensorEventListener.first = true;
                accelerometerEventListener.first = true;
                magneticSensorEventListener.first = true;
                rotationalSensorEventListener.first = true;
            }
        });
        Lay1.addView(resetButton);

        // Generate CSV button initialization
        Button generateButton = new Button(getApplicationContext());
        generateButton.setText("Generate CSV Record for Accel. Readings");
        generateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                File file = new File(getExternalFilesDir("Readings"), "accelReadings.csv");
                Log.w("File Path", file.getAbsolutePath());
                PrintWriter printWriter; // buffer
                try {
                    printWriter = new PrintWriter(file);
                    for (int i = 0; i < 100; i++) {
                        printWriter.println(String.format("%f, %f, %f", accelReadings[i][0], accelReadings[i][1], accelReadings[i][2]));
                    }
                    printWriter.close();
                    Log.i("Completion", "Successfully wrote file");
                } catch (FileNotFoundException e) {
                    Log.i("Completion", "Failed to write file");
                }
            }
        });
        Lay1.addView(generateButton);

        // Textview creation for light sensors
        view1 = createLabel("\nThe Light Sensor Reading is: ");
        lightSensorReading = createLabel("");
        view2 = createLabel("\nThe Record-High Light Sensor Reading is: ");
        highLightSensorReading = createLabel("");

        // Sensor manager initialization
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Light sensor initialization
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); // request sensor
        // Create instance of sensorEventListener
        // Two arguments are passed to the LightSensorEventListener constructor, the TextViews are passed by reference
        // and therefore, the LightSensorEventListener can obtain the value and change the text displayed
        SensorEventListener e1 = new LightSensorEventListener(lightSensorReading, highLightSensorReading);
        sensorManager.registerListener(e1, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Textview creation for accelerometer
        view3 = createLabel("\nThe Accelerometer Reading is: ");
        accelerometerReading = createLabel("");
        view4 = createLabel("\nThe Record-High Accelerometer Reading is: ");
        highAccelerometerReading = createLabel("");

        // Accelerometer initialization
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // The graph is passed to the accelerometerEventListener so points can be added
        SensorEventListener e2 = new accelerometerEventListener(accelerometerReading, highAccelerometerReading, graph, accelReadings);
        sensorManager.registerListener(e2, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);

        // Textview creation for magnetic sensor
        view5 = createLabel("\nThe Magnetic Sensor Reading is: ");
        magneticSensorReading = createLabel("");
        view6 = createLabel("\nThe Record-High Magnetic Sensor Reading is: ");
        highMagneticSensorReading = createLabel("");

        // Magnetic sensor initialization
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        SensorEventListener e3 = new magneticSensorEventListener(magneticSensorReading, highMagneticSensorReading);
        sensorManager.registerListener(e3, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Textview creation for rotational sensor
        view7 = createLabel("\nThe Rotational Sensor Reading is: ");
        rotationalSensorReading = createLabel("");
        view8 = createLabel("\nThe Record-High Rotational Sensor Reading is: ");
        highRotationalSensorReading = createLabel("");

        // Rotational sensor initialization
        Sensor rotationalSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        SensorEventListener e4 = new rotationalSensorEventListener(rotationalSensorReading, highRotationalSensorReading);
        sensorManager.registerListener(e4, rotationalSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }
}

class LightSensorEventListener implements SensorEventListener {
    TextView output;
    TextView outputHigh;
    float high;
    static boolean first;
    NumberFormat formatter = new DecimalFormat("#0.00"); // formats a float into a string
    public LightSensorEventListener(TextView outputView, TextView outputHighView) {
        output = outputView; // assigns references
        outputHigh = outputHighView;
        high = 0;
        first = true;
    }
    public void onAccuracyChanged(Sensor s, int i) {
    }
    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_LIGHT) {
            if (first) {
                high = se.values[0];
                first = false;
            }
            if (se.values[0] > high) {
                high = se.values[0];
            }
            output.setText(formatter.format(se.values[0]));
            outputHigh.setText(formatter.format(high));
        }
    }
}

class accelerometerEventListener implements SensorEventListener {
    TextView output;
    TextView outputHigh;
    LineGraphView graph;
    float[][] aReadings;
    float high1, high2, high3;
    static boolean first;
    public accelerometerEventListener(TextView outputView, TextView outputHighView, LineGraphView graphRef, float[][] readings) {
        output = outputView;
        outputHigh = outputHighView;
        graph = graphRef;
        aReadings = readings;
        high1 = 0;
        high2 = 0;
        high3 = 0;
        first = true;
    }
    public void onAccuracyChanged(Sensor s, int i) {
    }
    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            graph.addPoint (se.values);
            String s = String.format("(%.2f, %.2f, %.2f)", se.values[0], se.values[1], se.values[2]);
            if (first) {
                high1 = se.values[0];
                high2 = se.values[1];
                high3 = se.values[2];
                first = false;
            }
            if (se.values[0] > high1) {
                high1 = se.values[0];
            }
            if (se.values[1] > high2) {
                high2 = se.values[1];
            }
            if (se.values[2] > high3) {
                high3 = se.values[2];
            }
            String sHigh = String.format("(%.2f, %.2f, %.2f)", high1, high2, high3);
            // create a new array and copy over old values shifted by one
            for (int i = aReadings.length - 2; i >= 0; i--) {
                for (int j = 0; j < 3; j++) {
                    aReadings[i+1][j] = aReadings[i][j];
                }
            }
            for (int j = 0; j < 3; j++) {
                aReadings[0][j] = se.values[j];
            }
            output.setText(s);
            outputHigh.setText(sHigh);
        }
    }
}

class magneticSensorEventListener implements SensorEventListener {
    TextView output;
    TextView outputHigh;
    float high1, high2, high3;
    static boolean first;
    public magneticSensorEventListener(TextView outputView, TextView outputHighView) {
        output = outputView;
        outputHigh = outputHighView;
        high1 = 0;
        high2 = 0;
        high3 = 0;
        first = true;
    }
    public void onAccuracyChanged(Sensor s, int i) {
    }
    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            String s = String.format("(%.2f, %.2f, %.2f)", se.values[0], se.values[1], se.values[2]);
            if (first) {
                high1 = se.values[0];
                high2 = se.values[1];
                high3 = se.values[2];
                first = false;
            }
            if (se.values[0] > high1) {
                high1 = se.values[0];
            }
            if (se.values[1] > high2) {
                high2 = se.values[1];
            }
            if (se.values[2] > high3) {
                high3 = se.values[2];
            }
            String sHigh = String.format("(%.2f, %.2f, %.2f)", high1, high2, high3);
            output.setText(s);
            outputHigh.setText(sHigh);
        }
    }
}

class rotationalSensorEventListener implements SensorEventListener {
    TextView output;
    TextView outputHigh;
    float high1, high2, high3;
    static boolean first;
    public rotationalSensorEventListener(TextView outputView, TextView outputHighView) {
        output = outputView;
        outputHigh = outputHighView;
        high1 = 0;
        high2 = 0;
        high3 = 0;
        first = true;
    }
    public void onAccuracyChanged(Sensor s, int i) {
    }
    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            String s = String.format("(%.2f, %.2f, %.2f)", se.values[0], se.values[1], se.values[2]);
            if (first) {
                high1 = se.values[0];
                high2 = se.values[1];
                high3 = se.values[2];
                first = false;
            }
            if (se.values[0] > high1) {
                high1 = se.values[0];
            }
            if (se.values[1] > high2) {
                high2 = se.values[1];
            }
            if (se.values[2] > high3) {
                high3 = se.values[2];
            }
            String sHigh = String.format("(%.2f, %.2f, %.2f)", high1, high2, high3);
            output.setText(s);
            outputHigh.setText(sHigh);
        }
    }
}
