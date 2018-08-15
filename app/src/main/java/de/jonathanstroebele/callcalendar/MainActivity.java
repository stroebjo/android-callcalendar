package de.jonathanstroebele.callcalendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.provider.CallLog;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import main.CalendarSpinner;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {


    private static String getCallDetails(Context context) {

        StringBuffer stringBuffer = new StringBuffer();
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");

        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);

        while (cursor.moveToNext()) {
            String phNumber = cursor.getString(number);
            String callType = cursor.getString(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = cursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);

            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }

            stringBuffer.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                    + dir + " \nCall Date:--- " + callDayTime
                    + " \nCall duration in sec :--- " + callDuration);
            stringBuffer.append("\n----------------------------------");
        }

        cursor.close();
        return stringBuffer.toString();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String[] projection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE};


        Spinner calendarSpinner = (Spinner) findViewById(R.id.spinner);



        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED) {


            Cursor calCursor = getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,
                    projection,
                    CalendarContract.Calendars.VISIBLE + " = 1" + " and " +  CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " >= " + CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR,
                    null,
                    CalendarContract.Calendars._ID + " ASC");
            if (calCursor.moveToFirst()) {

                List<CalendarSpinner> spinnerArray =  new ArrayList<CalendarSpinner>();

                do {
                    long id = calCursor.getLong(0);
                    String displayName = calCursor.getString(1);
                    spinnerArray.add(new CalendarSpinner(id, displayName));
                } while (calCursor.moveToNext());


                ArrayAdapter<CalendarSpinner> adapter = new ArrayAdapter<CalendarSpinner>(
                        this, android.R.layout.simple_spinner_item, spinnerArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner sItems = (Spinner) findViewById(R.id.spinner);
                sItems.setAdapter(adapter);

            }




        }
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        // Do something in response to button

        EditText editText = (EditText) findViewById(R.id.editText5);


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CALL_LOG)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALL_LOG},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted

            //String foo = this.getCallDetails(this);

            //editText.append(foo);
        }


        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        CalendarSpinner st = (CalendarSpinner) spinner.getSelectedItem();


        editText.append( Long.toString( st.id) );
    }

    public void importCalls(View view) {

        StringBuffer stringBuffer = new StringBuffer();
        Cursor cursor = this.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");

        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);

        ContentResolver cr = getContentResolver();
        TimeZone timeZone = TimeZone.getDefault();

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        CalendarSpinner st = (CalendarSpinner) spinner.getSelectedItem();

        boolean first = true;

        while (cursor.moveToNext() && first) {

            String title = "";

            String phNumber = cursor.getString(number);
            String callType = cursor.getString(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = cursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);


            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    title = "Call to " + phNumber;
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    title = "Call from " + phNumber;
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    title = "Missed call from " + phNumber;
                    dir = "MISSED";
                    break;
            }

            long stTime = callDayTime.getTime();
            long enTime = stTime + (Long.valueOf(callDuration) * 1000);


            stringBuffer.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                    + dir + " \nCall Date:--- " + callDayTime
                    + " \nCall duration in sec :--- " + callDuration);
            stringBuffer.append("\n----------------------------------");

            try {
                ContentValues values = new ContentValues();

                values.put(CalendarContract.Events.DTSTART, stTime);
                values.put(CalendarContract.Events.DTEND, enTime);
                values.put(CalendarContract.Events.TITLE, title);
                values.put(CalendarContract.Events.DESCRIPTION, "descr");
                values.put(CalendarContract.Events.CALENDAR_ID, st.id);
                //values.put(CalendarContract.Events.EVENT_LOCATION, place);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());


                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            } catch (Exception e) {
                e.printStackTrace();
            }





            first = false;

        }

        cursor.close();



    }

}