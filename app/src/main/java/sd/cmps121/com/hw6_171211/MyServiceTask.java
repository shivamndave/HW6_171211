package sd.cmps121.com.hw6_171211;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import de.greenrobot.event.EventBus;

public class MyServiceTask implements Runnable {

    public static final String LOG_TAG = "MyService";
    private boolean running;
    private Context context;

    private Long T1;
    private float _accelx;
    private float _accely;
    private Long T0 = 0L;
    private Long first_accel_time = 0L;
    private Boolean moved;

    public MyServiceTask(Context _context) {
        context = _context;
        T0 = new Date().getTime();
    }

    @Override
    public void run() {
        moved = false;
        running = true;
        Random rand = new Random();
        while (running) {
            ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE)).registerListener(
                    new SensorEventListener() {
                        @Override
                        public void onSensorChanged(SensorEvent event) {
                            _accelx = -event.values[0];
                            _accely = event.values[1];
//                            Log.e(Log_Tag, Float.toString(_accelx));
//                            Log.e(Log_Tag, Float.toString(_accely));
//                            Log.e(Log_Tag, T0.toString());
                            if (Math.abs(_accely) > .5 || Math.abs(_accelx) > .5) {
//                                Log.e("Log_Tag", Float.toString(_accely));
                                T1 = new Date().getTime();
                                if (T1 - T0 > 10000) {
                                    first_accel_time = new AtomicLong(T1).longValue();
                                    moved = true;
//                                    Log.e(Log_Tag, Long.toString(first_accel_time));
                                }
                            }


                        }

                        @Override
                        public void onAccuracyChanged(Sensor sensor, int accuracy) {
                        }
                    },
                    ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE))
                            .getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);

            // Sleep a tiny bit.
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
            // Generate a random number.
            int r = rand.nextInt(100);
            // Sends it to the UI thread in MainActivity (if MainActivity
            // is running).
            ServiceResult result = new ServiceResult();
            result.lngValue = first_accel_time;
            result.boolValue = moved;
            EventBus.getDefault().post(result);
        }
    }

    public void stopProcessing() {
        running = false;
    }

    public void setTaskState(boolean b) {
        // Do something with b.
    }

}
