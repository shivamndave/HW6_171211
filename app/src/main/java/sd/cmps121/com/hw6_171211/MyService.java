package sd.cmps121.com.hw6_171211;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class MyService extends Service {

    private static final String LOG_TAG = "MyService";

    // Handle to notification manager.
    private NotificationManager notificationManager;
    private int ONGOING_NOTIFICATION_ID = 1; // This cannot be 0. So 1 is a good candidate.

    // Motion detector thread and runnable.
    private Thread myThread;
    private MyServiceTask myTask;
    private PowerManager.WakeLock wakeLock;

    // Binder given to clients
    private final IBinder myBinder = new MyBinder();

    // Binder class.
    public class MyBinder extends Binder {
        MyService getService() {
            // Returns the underlying service.
            return MyService.this;
        }
    }

    public MyService() {
    }

    @Override
    public void onCreate() {

        Log.i(LOG_TAG, "Service is being created");

        // Display a notification about us starting.  We put an icon in the status bar.
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showMyNotification();

        // Creates the thread running the camera service.
        myTask = new MyServiceTask(getApplicationContext());
        myThread = new Thread(myTask);
        myThread.start();
    }

    /*
     * Added to call to clear the service task from MainActivity
     */
    public void clearTask(){
        myTask.clearMyServiceTask();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "Service is being bound");
        // Returns the binder to this service.
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);
        // We start the task thread.
        if (!myThread.isAlive()) {
            myThread.start();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        wakeLock.release();
    }

    // Interface which sets recording on/off.
    public void setTaskState(boolean b) {
        myTask.setTaskState(b);
    }


    /**
     * Show a notification while this service is running.
     */
    @SuppressWarnings("deprecation")
    private void showMyNotification() {

        // Creates a notification.
        Notification notification = new Notification(
                R.mipmap.ic_launcher,
                getString(R.string.my_service_started),
                System.currentTimeMillis());

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getText(R.string.notification_title),
                getText(R.string.my_service_running), pendingIntent);
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

}
