package kagy.com.calldaemon;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The main activity of the application.
 */
public class MainActivity extends AppCompatActivity {

    //the minimum delay expressed in seconds
    private static final int MIN_DELAY = 2;
    //the code of the call intent
    private static final int CALL_INTENT_CODE = 1663;
    //the code of the pick content intent
    private static final int PICK_CONTACT_CODE = 8086;


    //this is the call handler
    private Handler callHandler = new Handler();
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
        started = false;
        delay = MIN_DELAY;
    }

    /**
     * The on clock method of the pick button.
     *
     * @param v is the pickButton if the program works correcty.
     */
    public void onPickClick(View v) {
        if (!started) {
            //creating the intent used for calling
            Intent pickIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            pickIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            //starts the pick activity
            startActivityForResult(pickIntent, PICK_CONTACT_CODE);
        }
    }

    /**
     * The on click method of the start button.
     *
     * @param v this should always be the startButton.
     */
    public void onStartClick(View v) {
        if (!started) {
            started = true;
            //starting the Call immediatly
            startCall();
        }
    }

    /**
     * The on clock method of the stop button.
     *
     * @param v this should always be the stopButton.
     */
    public void onStopClick(View v) {
        if (started)
            started = false;
    }

    /**
     * Starts a call in an asynchronous manner.
     */
    private void startCall() {

        EditText numberET = (EditText) findViewById(R.id.numberET);
        //the indentifier of the called phone
        String uri = "tel:" + numberET.getText();
        //creating and configuring the intent
        Intent intent = new Intent(Intent.ACTION_CALL);

        intent.setData(Uri.parse(uri));
        //u[dating the delay
        delay = extractDelay();
        //launching the intent
        try {
            startActivityForResult(intent, CALL_INTENT_CODE);
        } catch (SecurityException e) {
            Toast.makeText(this, "Not allowed!", Toast.LENGTH_SHORT).show();
        }
    }

    //The intent activity results ahndler methods

    /**
     * Handles the result of the pick activity.
     *
     * @param data the intent that started the pick activity is needed.
     *             This reference should never be null.
     */
    private void onPickActivityResult(Intent data) {
        //verifying the intent exists
        if (data == null)
            throw new IllegalArgumentException("The calling intent is null!\n");
        //the ui of the contact
        Uri contactUri = data.getData();

        if (contactUri != null) {
            //the cursor used to extract data
            Cursor cursor = null;
            String filter[] = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
            String number;

            try {
                ContentResolver cr=getContentResolver();
                //getting the number from the data base
                cursor=cr.query(contactUri, filter, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    //moves the cursor to the first position in the data
                    //geting the first number with the given configuration
                    number = cursor.getString(0);
                    setNewNumber(number);
                }
            } finally {
                //closing the cursor if possible
                if (cursor != null)
                    cursor.close();
            }
        }
    }

    /**
     * The method handling the end of the call activity.
     */
    private void onCallActivityResult() {
        if (started) {
            //calling the handler for the new call
            callHandler.removeCallbacks(callNextTask);
            callHandler.postDelayed(callNextTask, delay * 1000);
        }
    }

    /**
     * Method for intent result handling.
     *
     * @param requestCode is the constant code of the Intent.
     * @param resultCode  is the result code of the Intent.
     * @param data        is the refrence of the Intent.
     */
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {

        //only the call reqest code is needed to be verified
        switch (requestCode) {
            case PICK_CONTACT_CODE:
                onPickActivityResult(data);
                break;
            case CALL_INTENT_CODE:
                onCallActivityResult();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Sets a new number to the telephone number field.
     *
     * @param number is the new telephone number set on the GUI.
     */
    private void setNewNumber(String number) {
        EditText numberEt = (EditText) findViewById(R.id.numberET);
        //sets the new number for the edit text
        numberEt.setText(number);
    }

    /**
     * Extracts the delay from the interface.
     *
     * @return is the delay expressed in seconds, extracted from the interface.
     */
    private int extractDelay() {

        EditText delayET = (EditText) findViewById(R.id.delayED);
        int delay = Integer.valueOf(delayET.getText().toString());

        if (delay < MIN_DELAY)
            delay = MIN_DELAY;
        return delay;
    }
}
