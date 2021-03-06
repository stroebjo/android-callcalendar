package main;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;

import java.util.Date;
import java.util.TimeZone;

import de.jonathanstroebele.callcalendar.R;

public class CallLogEntry {

    private String number;

    private int callType;

    private Date callStart;

    private long callDuration;

    private Context context;


    public CallLogEntry(Context context) {
        this.context = context;
    }

    public ContentValues getCallEvent(long calendar_id) {
        ContentValues values = new ContentValues();

        long stTime = getCallStart().getTime();
        long enTime = stTime + (getCallDuration() * 1000); // convert seconds to milli seconds
        TimeZone timeZone = TimeZone.getDefault();

        values.put(CalendarContract.Events.DTSTART, stTime);
        values.put(CalendarContract.Events.DTEND, enTime);
        values.put(CalendarContract.Events.TITLE, getTitle());
        values.put(CalendarContract.Events.DESCRIPTION, getDescription());
        values.put(CalendarContract.Events.CALENDAR_ID, calendar_id);
        //values.put(CalendarContract.Events.EVENT_LOCATION, place);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());

        return values;
    }

    public String getTitle() {
        String title = "";

        String contactName = getContactName();

        if (contactName == null) {
            contactName = getNumberName();
        }

        switch (getCallType()) {
            case CallLog.Calls.OUTGOING_TYPE:
                title = context.getResources().getString(R.string.event_title_outgoing, contactName);
                break;
            case CallLog.Calls.INCOMING_TYPE:
                title = context.getResources().getString(R.string.event_title_incoming, contactName);
                break;

            case CallLog.Calls.MISSED_TYPE:
                title = context.getResources().getString(R.string.event_title_missed, contactName);
                break;
        }

        // add duration if available
        if (getCallDuration() > 0) {
           title += String.format(" (%s)", DateUtils.formatElapsedTime(getCallDuration()));
        }

        return title;
    }

    public String getDescription() {
        String description = "";

        description += context.getResources().getString(R.string.event_description_number, getNumberName() ) + "\n";

        if (getContactName() != null) {
            description += context.getResources().getString(R.string.event_description_name, getContactName() ) + "\n";
        }

        if (getCallDuration() > 0) {
            description += context.getResources().getString(R.string.event_description_duration, DateUtils.formatElapsedTime(getCallDuration()) ) + "\n";
        }

        return description;
    }

    private String getNumberName() {
        String nr = getNumber();
        return (nr == null) ? context.getString(R.string.number_unknown) : nr;
    }

    /**
     * Get the contacts name from the address book, if permission was granted.
     * Otherwise return NULL.
     *
     * todo: caching of the query result?
     *
     * @return String|null
     */
    private String getContactName() {
        String number = getNumber();

        if (number != null && ContextCompat.checkSelfPermission(this.context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( number ));
            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

            Cursor cursor = context.getContentResolver().query(uri, projection,null,null,null);

            if (cursor != null) {
                if(cursor.moveToFirst()) {
                    String name = cursor.getString(0);
                    cursor.close();
                    return name;
                }
            }
        }

        return null;
    }

    public String toString() {
        return getTitle();
    }

    //

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public Date getCallStart() {
        return callStart;
    }

    public void setCallStart(Date callStart) {
        this.callStart = callStart;
    }

    public void setCallStart(long callStart) {
        this.callStart = new Date(callStart);
    }

    public long getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(long callDuration) {
        this.callDuration = callDuration;
    }

    //
}
