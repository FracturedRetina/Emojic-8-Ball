package com.eshimoniak.emojic8ball;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private static ImageButton button;
    private static ImageView triangle;
    private static TextView answer;
    private static EditText question;
    private float x, y, z, last_x, last_y, last_z;
    private long lastUpdate;
    private boolean paused = false;
    private static final int SHAKE_THRESHOLD = 400;
    private static final int SHAKE_REFRESH = 225;

    SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (ImageButton) findViewById(R.id.ask_button);
        triangle = (ImageView) findViewById(R.id.triangle);
        answer = (TextView) findViewById(R.id.answer);
        question = (EditText) findViewById(R.id.question_field);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ask();
            }
        });
        question.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                ask();
                return true;
            }
        });
        question.setImeActionLabel("Ask", KeyEvent.KEYCODE_ENTER);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void ask() {
        triangle.setVisibility(View.VISIBLE);
        answer.setText(EightBall.getAnswer());

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(question.getWindowToken(), 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }
    @Override
    public void onResume() {
        super.onResume();
        paused = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !paused) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > SHAKE_REFRESH) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                float speed = Math.abs(
                        (x - last_x) +
                        (y - last_y) +
                        (z - last_z)
                ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    ask();
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(100);
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
