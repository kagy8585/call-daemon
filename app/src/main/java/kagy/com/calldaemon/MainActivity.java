package kagy.com.calldaemon;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //the minimum delay expressed in seconds
    private static final int MIN_DELAY=2;
    //the code of the call intent
    private static final int CALL_INTENT_CODE=1663;


    //this is the call handler
    private Handler callHandler=new Handler();
    //this runnable is responsible for the next call
    private Runnable callNextTask = new Runnable() {
        @Override
        public void run() {
            if (started)
                startCall();
        }
    };

    //the delay used in seconds
    private int delay;

    //the main started flag
    private volatile boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        started=false;
        delay=MIN_DELAY;
    }

    public void onStartClick(View v) {
        if (!started) {
            started = true;
            //starting the Call immediatly
            startCall();
        }
    }

    public void onStopClick(View v){
        if (started)
            started=false;
    }

    private void startCall() {

        EditText numberET=(EditText)findViewById(R.id.numberET);
        //the indentifier of the called phone
        String uri = "tel:"+numberET.getText();
        //creating and configuring the intent
        Intent intent=new Intent(Intent.ACTION_CALL);

        intent.setData(Uri.parse(uri));
        //u[dating the delay
        delay=extractDelay();
        //launching the intent
        try {
            startActivityForResult(intent, CALL_INTENT_CODE);
        }
        catch (SecurityException e) {
            Toast.makeText(this, "Not allowed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult (int requestCode,
                           int resultCode,
                           Intent data)
    {   //only the call reqest code is needed to be verified
        if (requestCode==CALL_INTENT_CODE) {
            if (started) {
                //calling the handler for the new call
                callHandler.removeCallbacks(callNextTask);
                callHandler.postDelayed(callNextTask, delay*1000);
            }
        }
    }

    /**
     * Extracts the delay from the interface.
     * @return is the delay expressed in seconds, extracted from the interface.
     */
    private int extractDelay() {
        EditText delayET=(EditText)findViewById(R.id.delayED);
        int delay=new Integer(delayET.getText().toString());

        if (delay<MIN_DELAY)
            delay=MIN_DELAY;
        return delay;
    }
}
