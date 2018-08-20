package main;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.support.v4.content.ContextCompat;

import java.util.Date;

import de.jonathanstroebele.callcalendar.R;

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end)
    {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // check if app is enabled in't settings
        if (sharedPref.getInt(ctx.getString(R.string.preference_on), 0) != 1) {
            return;
        }

        // check for write calendar permission: save event in calendar
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CallLogEntry cle = new CallLogEntry(ctx);
        cle.setNumber(number);
        cle.setCallType(CallLog.Calls.INCOMING_TYPE);
        cle.setCallStart(start);
        cle.setCallDuration( (end.getTime() - start.getTime()) / 1000 );

        try {
            long calendar_id = sharedPref.getLong(ctx.getString(R.string.preference_calendar_id), 0);
            ContentValues values = cle.getCallEvent(calendar_id);

            Uri uri = ctx.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //insertLatCallToCalendar(ctx);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end)
    {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // check if app is enabled in't settings
        if (sharedPref.getInt(ctx.getString(R.string.preference_on), 0) != 1) {
            return;
        }

        // check for write calendar permission: save event in calendar
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CallLogEntry cle = new CallLogEntry(ctx);
        cle.setNumber(number);
        cle.setCallType(CallLog.Calls.OUTGOING_TYPE);
        cle.setCallStart(start);
        cle.setCallDuration( (end.getTime() - start.getTime()) / 1000 );

        try {
            long calendar_id = sharedPref.getLong(ctx.getString(R.string.preference_calendar_id), 0);
            ContentValues values = cle.getCallEvent(calendar_id);

            Uri uri = ctx.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //insertLatCallToCalendar(ctx);
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start)
    {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // check if app is enabled in't settings
        if (sharedPref.getInt(ctx.getString(R.string.preference_on), 0) != 1) {
            return;
        }

        // check for write calendar permission: save event in calendar
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CallLogEntry cle = new CallLogEntry(ctx);
        cle.setNumber(number);
        cle.setCallType(CallLog.Calls.MISSED_TYPE);
        cle.setCallStart(start);
        cle.setCallDuration( 0 ); // missed call duration is 0?

        try {
            long calendar_id = sharedPref.getLong(ctx.getString(R.string.preference_calendar_id), 0);
            ContentValues values = cle.getCallEvent(calendar_id);

            Uri uri = ctx.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //insertLatCallToCalendar(ctx);
    }


    private void insertLatCallToCalendar(Context ctx) {

        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // check if app is enabled in't settings
        if (sharedPref.getInt(ctx.getString(R.string.preference_on), 0) != 1) {
            return;
        }

        // check for call log permission: read latest call log entry
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // check for write calendar permission: save event in calendar
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // last call entry
        // todo: this fetched the last  - 1 entry of the calllog, the actual call this event handles doesn't semm to be present yet in the log
        // so wie we put the "manual" functions obove.
        Cursor cursor = ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC LIMIT 1");

        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);

        if(cursor.moveToFirst()) {

            CallLogEntry cle = new CallLogEntry(ctx);
            cle.setNumber(cursor.getString(number));
            cle.setCallType(cursor.getInt(type));
            cle.setCallStart(cursor.getLong(date));
            cle.setCallDuration(cursor.getLong(duration));

            try {

                long calendar_id = sharedPref.getLong(ctx.getString(R.string.preference_calendar_id), 0);
                ContentValues values = cle.getCallEvent(calendar_id);

                Uri uri = ctx.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        cursor.close();

    }
}