package sd.cmps121.com.hw6_171211;

import android.content.Context;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.greenrobot.event.EventBus;

public class MyServiceTask implements Runnable {

    public static final String LOG_TAG = "MyService";
    private boolean running;
    private Context context;


    public MyServiceTask(Context _context) {
        context = _context;
        // Put here what to do at creation.
    }

    @Override
    public void run() {
        running = true;
        Random rand = new Random();
        while (running) {
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
            Log.i(LOG_TAG, "Sending random number: " + r);
            ServiceResult result = new ServiceResult();
            result.intValue = r;
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
