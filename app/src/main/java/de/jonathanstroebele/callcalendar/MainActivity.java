package de.jonathanstroebele.callcalendar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.provider.CallLog;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import main.CalendarSpinner;
import main.CallLogEntry;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_READ_CALL_LOG  = 1;
    private final int MY_PERMISSIONS_REQUEST_READ_CALENDAR  = 10;
    private final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 20;
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS  = 30;

    private final int MY_PERMISSIONS_REQUEST_ALL = 100;


    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);




        Switch onOffSwitch = (Switch) findViewById(R.id.switch1);

        if (sharedPref.getInt(getString(R.string.preference_on), 0) == 1) {
            onOffSwitch.setOnCheckedChangeListener (null); // make sure nur event listener is present
            onOffSwitch.setChecked(true);
        }

        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                SharedPreferences.Editor editor = sharedPref.edit();

                if (isChecked) {
                    editor.putInt(getString(R.string.preference_on), 1);

                    requestPermissions();
                } else {
                    editor.putInt(getString(R.string.preference_on), 0);
                }

                editor.apply();
            }
        });


        Spinner calendarSpinner = (Spinner) findViewById(R.id.spinner);
        _populateCalendarSpinner();

        // Save selected calendar. this will also run on init
        calendarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                CalendarSpinner active = new CalendarSpinner(
                        sharedPref.getLong(getString(R.string.preference_calendar_id), 0),
                        sharedPref.getString( getString(R.string.preference_calendar_name), "")
                );

                CalendarSpinner st = (CalendarSpinner) parentView.getSelectedItem();

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong(getString(R.string.preference_calendar_id), st.id);
                editor.putString(getString(R.string.preference_calendar_name), st.name);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALL_LOG:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // granted
                } else {
                    // denied
                }
                break;
            case MY_PERMISSIONS_REQUEST_ALL:
                for(int i = 0; i < grantResults.length; i++) {
                    if (permissions[i] == Manifest.permission.READ_CALENDAR) {
                        _populateCalendarSpinner();
                    }
                }
        }
    }

    /**
     * Fill the Calendar Spinner with values + set the selected one from preferences if any.
     *
     */
    private void _populateCalendarSpinner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {

            String[] projection = new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.NAME};

            Cursor calCursor = getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, projection,
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

                ArrayAdapter<CalendarSpinner> adapter = new ArrayAdapter<CalendarSpinner>(this, android.R.layout.simple_spinner_item, spinnerArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner calendarSpinner = (Spinner) findViewById(R.id.spinner);
                calendarSpinner.setAdapter(adapter);

                if (sharedPref.getLong(getString(R.string.preference_calendar_id), 0) > 0) {
                    CalendarSpinner active = new CalendarSpinner(
                            sharedPref.getLong(getString(R.string.preference_calendar_id), 0),
                            sharedPref.getString( getString(R.string.preference_calendar_name), "")
                    );

                    calendarSpinner.setSelection(adapter.getPosition(active));
                }
            }
        }
    }


    private void _requestAllPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CONTACTS
        }, MY_PERMISSIONS_REQUEST_ALL);
    }

    private void requestPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)  ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                // permission were denied once, show explanation

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Permissions")
                        .setMessage("Reason for all permissions.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                _requestAllPermissions();
                            }
                        });

                builder.create().show();

            } else {
                // first request for permissions
                _requestAllPermissions();
            }

        } else {
            // all permissions granted...
        }
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        // Do something in response to button

        //EditText editText = (EditText) findViewById(R.id.editText5);


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


        //editText.append( Long.toString( st.id) );
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

            CallLogEntry cle = new CallLogEntry(this);
            cle.setNumber(cursor.getString(number));
            cle.setCallType(cursor.getInt(type));
            cle.setCallStart(cursor.getLong(date));
            cle.setCallDuration(cursor.getLong(duration));

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
                    title = "Call to " + getContactName(phNumber, this);
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    title = "Call from " + getContactName(phNumber, this);
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    title = "Missed call from " + getContactName(phNumber, this);
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

    public String getContactName(final String phoneNumber, Context context)
    {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = phoneNumber;
        Cursor cursor = context.getContentResolver().query(uri, projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }
}