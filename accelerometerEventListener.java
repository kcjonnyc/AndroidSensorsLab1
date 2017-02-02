package lab2.uwaterloo.ca.lab2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.TextView;

import ca.uwaterloo.sensortoy.LineGraphView;

/**
 * Created by KCJon on 2017-01-19.
 */

public class accelerometerEventListener implements SensorEventListener {

    public final float C = 4f;

    enum myState{WAIT, RISE_A, RISE_B, FALL_A, FALL_B, DETERMINED};
    private myState state = myState.WAIT;

    enum mySig{SIG_A, SIG_B, SIG_X};
    private mySig signature = mySig.SIG_X;

    // threshold constants
    private final float[] THRES_A = {0.5f, 0.4f, -0.4f};    // for left *need work finding these constants
    private final float[] THRES_B = {-0.5f, -0.4f, 0.4f};   // for right

    int sampleCounter = 30;
    final int SAMPLEDEFAULT = 30;

    // variables
    TextView output;
    TextView outputHigh;
    LineGraphView graph;
    float[][] aReadings;
    float high1, high2, high3;
    float[] filteredReadings = {0, 0, 0};
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

    public void callFSM(){

        float deltaA = aReadings[99][0] - aReadings[98][0];

        switch (state) {
            case WAIT:

                sampleCounter = SAMPLEDEFAULT; // we want to keep at 30 at wait
                signature = mySig.SIG_X;

                Log.d("FSM: ", "State WAIT");
                Log.d("Readings: ", Float.toString(aReadings[0][0]));
                Log.d("Readings: ", Float.toString(aReadings[1][0]));
                Log.d("deltaA: ", Float.toString(deltaA));

                if (deltaA > THRES_A[0]) {
                    state = myState.RISE_A;
                }
                else if (deltaA < THRES_B[0]) {
                    state = myState.FALL_B;
                }
                break;

            case RISE_A:

                Log.d("FSM: ", "State RISE_A");

                if (deltaA <= 0) { // finished rising and starting to fall
                    if (aReadings[99][0] >= THRES_A[1]) {
                        state = myState.FALL_A;
                    }
                    else {
                        state = myState.DETERMINED;
                    }
                }
                break;

            /*case RISE_B:

                Log.d("FSM: ", "State RISE_B");

                if (deltaA <= 0) { // crossed over from rising to falling (rising to falling to rebound)
                    if (aReadings[99][0] >= THRES_B[2]) {
                        signature = mySig.SIG_B;
                    }
                    state = myState.DETERMINED;
                }
                break;*/

            case FALL_A:

                Log.d("FSM: ", "State FALL_A");

                if (deltaA >= 0) { // crossed over from falling to rising (rising to falling to rebound)
                    if (aReadings[99][0] <= THRES_A[2]) {
                        signature = mySig.SIG_A;
                    }
                    state = myState.DETERMINED;
                }
                break;

            /*case FALL_B:

                Log.d("FSM: ", "State FALL_B");

                if (deltaA >= 0) { // finished falling and starting to rise
                    if (aReadings[99][0] <= THRES_B[1]) {
                        state = myState.RISE_B;
                    }
                    else {
                        state = myState.DETERMINED;
                    }
                }
                break;*/

            case DETERMINED:
                Log.d("FSM: ", "State DETERMINED " + signature.toString());
                break;

            default:
                state = myState.WAIT;
                break;

        }

        sampleCounter--;

    }

    public void onAccuracyChanged(Sensor s, int i) {

    }

    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            // apply LPF implementation
            filteredReadings[0] += (se.values[0] - filteredReadings[0]) / C;
            filteredReadings[1] += (se.values[1] - filteredReadings[1]) / C;
            filteredReadings[2] += (se.values[2] - filteredReadings[2]) / C;
            graph.addPoint (filteredReadings);

            String s = String.format("(%.2f, %.2f, %.2f)", filteredReadings[0], filteredReadings[1], filteredReadings[2]);

            // create a new array and copy over old values shifted by one
            for(int i = 1; i < 100; i++){
                aReadings[i - 1][0] = aReadings[i][0];
                aReadings[i - 1][1] = aReadings[i][1];
                aReadings[i - 1][2] = aReadings[i][2];
            }
            for (int j = 0; j < 3; j++) {
                aReadings[99][j] = filteredReadings[j];
            }
            // we will always have the most recent 100 values stored

            // start FSM analysis
            callFSM();
            if(sampleCounter <= 0){

                if(state == myState.DETERMINED){
                    if(signature == mySig.SIG_B)
                        outputHigh.setText("LEFT");
                    else if(signature == mySig.SIG_A)
                        outputHigh.setText("RIGHT");
                    else
                        outputHigh.setText("Undetermined");
                }
                else{
                    state = myState.WAIT;
                    outputHigh.setText("Undetermined");
                }

                sampleCounter = SAMPLEDEFAULT;
                state = myState.WAIT;

            }

            output.setText(s);
        }
    }
}
