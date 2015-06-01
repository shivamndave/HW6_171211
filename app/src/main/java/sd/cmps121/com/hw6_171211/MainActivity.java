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

import de.greenrobot.event.EventBus;
import sd.cmps121.com.hw6_171211.MyService.MyBinder;

public class MainActivity extends Activity {

    private static final String LOG_TAG = "MainActivity";

    // Service connection variables.
    private boolean serviceBound;
    private MyService myService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceBound = false;
        // Prevents the screen from dimming and going to sleep.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

    }

    public boolean didItMove(ServiceResult tempRes) {
        Date d = new Date();
        boolean moved = false;
        long firstAccTime = tempRes.lngValue;

        if (tempRes.boolValue == true && tempRes.lngValue != 0L && d.getTime() - firstAccTime > 10000) {
            moved = true;
        }

        return moved;
    }

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

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onClearClick(View v) {
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

        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        bindMyService();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

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

    public void onEventMainThread(ServiceResult result) {
        TextView tv = (TextView) findViewById(R.id.number_view);

        Boolean movedQ = didItMove(result);

        Long countTime = new Date().getTime() - result.startValue;

        if (movedQ) {
            tv.setText("Your phone was moved " + Long.toString(((new Date().getTime()) - result.lngValue) / 1000) + " seconds ago.");
        } else if (countTime < 10000) {
            tv.setText("Engaging in: " + Long.toString((10000 - countTime) / 1000) + " second(s)...");
        } else {
            tv.setText("App Engaged for: " + Long.toString((countTime - 10000) / 1000) + " seconds...");
        }
    }
}



