package sd.cmps121.com.hw6_171211;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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
        return tempRes.boolValue;
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
        super.onPause();
    }

    public void onEventMainThread(ServiceResult result) {
        Date d = new Date();
        Log.i("RALFP", "Displaying: " + result.boolValue);
        TextView tv = (TextView) findViewById(R.id.number_view);
        if (result.boolValue == true && result.lngValue != 0L && d.getTime() - result.lngValue > 10000) {
            tv.setText("YAH BRUH BRUH");
        }else{
            tv.setText("NAH" + result.boolValue.toString());
        }
    }
}



