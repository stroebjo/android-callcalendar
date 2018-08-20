package de.jonathanstroebele.callcalendar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.provider.CallLog;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import main.CalendarSpinner;
import main.CallLogEntry;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_ALL = 100;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);


        Switch onOffSwitch = (Switch) findViewById(R.id.switch1);
        int isOn = sharedPref.getInt(getString(R.string.preference_on), 0);

        if (isOn == 1) {
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
            case MY_PERMISSIONS_REQUEST_ALL:

                // at index 3 is READ_CALENDAR
                // todo: is this actually how the callback works?! I should check if the permission is at the index
                if (grantResults.length > 0 && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    _populateCalendarSpinner();
                }
                break;
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

    /**
     * Make the actual permission requests to the system.
     *
     */
    private void _requestAllPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CONTACTS,
        }, MY_PERMISSIONS_REQUEST_ALL);
    }

    /**
     * Check if permissions are already granted, or were denied once and we should show the explanation.
     *
     */
    private void requestPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)  ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PROCESS_OUTGOING_CALLS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {

                // permission were denied once, show explanation
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.permissions_explanation_title))
                        .setMessage(getString(R.string.permissions_explanation_description))
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

        // check if app is enabled in't settings
        if (sharedPref.getInt(this.getString(R.string.preference_on), 0) != 1) {
            return;
        }

        // check for call log permission: read latest call log entry
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // check for write calendar permission: save event in calendar
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // last call entry
        Cursor cursor = this.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC LIMIT 5");

        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);

        while (cursor.moveToNext()) {

            CallLogEntry cle = new CallLogEntry(this);
            cle.setNumber(cursor.getString(number));
            cle.setCallType(cursor.getInt(type));
            cle.setCallStart(cursor.getLong(date));
            cle.setCallDuration(cursor.getLong(duration));

            try {
                long calendar_id = sharedPref.getLong(this.getString(R.string.preference_calendar_id), 0);
                ContentValues values = cle.getCallEvent(calendar_id);

                Uri uri = this.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);

                Context context = getApplicationContext();
                CharSequence text = "Added to calendar: " + cle;
                int toastduration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, toastduration);
                toast.show();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        cursor.close();
    }
}