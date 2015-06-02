package sd.cmps121.com.hw6_171211;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import de.greenrobot.event.EventBus;
import sd.cmps121.com.hw6_171211.MyService.MyBinder;

public class MainActivity extends Activity {

    private static final String LOG_TAG = "MainActivity";

    private int CAP_TIME = 10000;

    // Service connection variables.
    private boolean serviceBound;
    private MyService myService;
    private boolean moved;
    private Long dateMoved = 0L;
    private AtomicLong firstAccTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceBound = false;
        moved = false;
        firstAccTime = null;
        // Prevents the screen from dimming and going to sleep.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

    }

    /*
     * Checks the movement and if it was done atleast the CAP_TIME (30 seconds) ago
     * This also sets the dateMoved so that we can easily do the time count for when
     * it was moved according to the current time.
     */
    public boolean didItMove(ServiceResult tempRes) {
        Date d = new Date();
        firstAccTime = tempRes.lngValue;
        if (tempRes.lngValue != null && moved == false) {
            if (d.getTime() - firstAccTime.longValue() > CAP_TIME) {
                dateMoved = firstAccTime.longValue();
                moved = true;
            }
        }
        return moved;
    }

    /*
     * Initializes the new intent, starts/binds the service,
     * and then registers the EventBus
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Starts the service, so that the service will only stop when explicitly stopped.
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        bindMyService();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void bindMyService() {
        // We are ready to show images, and we should start getting the bitmaps
        // from the motion detection service.
        // Binds to the service.
        Log.i(LOG_TAG, "Starting the service");
        Intent intent = new Intent(this, MyService.class);
        Log.i("LOG_TAG", "Trying to bind");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    // Service connection code.
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
            // We have bound to the camera service.
            MyBinder binder = (MyBinder) serviceBinder;
            myService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };

    /*
     * During an onPause were only unbind the service, allowing it
     * to run in the background
     */

    @Override
    protected void onPause() {
        if (serviceBound) {
            Log.i("MyService", "Unbinding");
            unbindService(serviceConnection);
            serviceBound = false;
        }
        super.onPause();
    }

    /*
     * Clears the current movement detection. This is a reset without actually resetting the
     * app, it simply allows you to start redetecting easily
     */
    public void onClearClick(View v) {
        myService.clearTask();
        moved = false;
        firstAccTime = null;
    }

    /*
     * The exit button function that is used to unbind the service, stop the service
     * and then exit the app completely.
     */
    public void onExitClick(View v) {
        if (serviceBound) {
            Log.i("MyService", "Unbinding");
            unbindService(serviceConnection);
            serviceBound = false;
            // If we like, stops the service.
            if (true) {
                Log.i(LOG_TAG, "Stopping.");
                Intent intent = new Intent(this, MyService.class);
                stopService(intent);
                Log.i(LOG_TAG, "Stopped.");
            }
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        System.exit(0);
    }

    /*
     * Where the ServiceResult is grabbed and handled. Also calls didItMove to
     * check if the device has moved. Based on what that returns it will let you know
     * if the device will start detecting, is detecting, or has detected movement
     * and the current time of that.
     */
    public void onEventMainThread(ServiceResult result) {
        TextView tv = (TextView) findViewById(R.id.status_view);

        Boolean movedQ = didItMove(result); // Checks the movement
        Long countTime = new Date().getTime() - result.startValue; // Gets the countdown until detection

        // If movement was detected
        if (movedQ) {
            Long seconds = ((new Date().getTime()) - dateMoved) / 1000;
            Long minutes = seconds / 60;
            Long minSeconds = seconds - minutes * 60;

            if (seconds < 60) {
                tv.setText("Your device was moved\n" + Long.toString(seconds) + " seconds ago!");
            } else {
                tv.setText("Your device was moved\n" + Long.toString(minutes) + " minute(s) and " + Long.toString(minSeconds) + " second(s) ago!");
            }

            // If the device is not detecting yet, this is the countdown to that
        } else if (countTime < CAP_TIME) {
            Long seconds = (CAP_TIME - countTime) / 1000;
            tv.setText("App will start detecting in\n" + Long.toString(seconds) + " second(s)...");

            // Any other case where the device has not moved
        } else {
            Long seconds = (countTime - CAP_TIME) / 1000;
            Long minutes = seconds / 60;
            Long minSeconds = seconds - minutes * 60;

            if (seconds < 60) {
                tv.setText("Your device has been quiet for\n" + Long.toString(seconds) + " second(s)...");
            } else {
                tv.setText("Your device has been quiet for\n" + Long.toString(minutes) + " minute(s) and " + Long.toString(minSeconds) + " second(s).");
            }
        }
    }
}



