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

    private Long T1 = 0L;
    private Long T0 = 0L;
    private float _accelx;
    private float _accely;
    private AtomicLong first_accel_time;

    public MyServiceTask(Context _context) {
        context = _context;
        T0 = new Date().getTime();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE)).registerListener(
                    new SensorEventListener() {
                        @Override
                        public void onSensorChanged(SensorEvent event) {
                            _accelx = -event.values[0];
                            _accely = event.values[1];
                            if (Math.abs(_accely) > .5 || Math.abs(_accelx) > .5) {
                                T1 = new Date().getTime();
                                if (T1 - T0 > 10000) {
                                    first_accel_time = new AtomicLong(T1);
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
            ServiceResult result = new ServiceResult();
            result.lngValue = first_accel_time;
            result.startValue = T0;
            EventBus.getDefault().post(result);
        }
    }

    public void stopProcessing() {
        running = false;
    }

    public void setTaskState(boolean b) {
        // Do something with b.
    }

    public void clearMyServiceTask(){
        T1 = 0L;
        T0 = new Date().getTime();
        first_accel_time = null;
    }


}
