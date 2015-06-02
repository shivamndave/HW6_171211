package sd.cmps121.com.hw6_171211;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import de.greenrobot.event.EventBus;

public class MyServiceTask implements Runnable {

    public static final String LOG_TAG = "MyService";
    private boolean running;
    private Context context;

    private int CAP_TIME = 10000;

    private Long T1 = 0L;
    private Long T0 = 0L;
    private float _accelx;
    private float _accely;
    private AtomicLong first_accel_time;

    public MyServiceTask(Context _context) {
        context = _context;
        T0 = new Date().getTime();
    }

    /*
     * Consistently runs the sensor, where we check if the phone has moved at all.
     * If it has, we set the first_accel_time and then pass that into the ServiceResult
     * which is accested in MainActivity by onEventMainThread. In addition to passing
     * that movement value, it will also pass when we began detecting (used to display time)
     */
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
                                if (T1 - T0 > CAP_TIME) {
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

    /*
     * Clears the ServiceTask by setting the T1 and T0 values anew and first_accel_time to null.
     * This allows us to reset the clock and start detecting over again, as though the app just
     * restarted.
     */
    public void clearMyServiceTask(){
        T1 = 0L;
        T0 = new Date().getTime();
        first_accel_time = null;
    }


}
