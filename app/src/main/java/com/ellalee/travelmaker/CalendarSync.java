package com.ellalee.travelmaker;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.calendar.model.Event;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class CalendarSync extends Thread implements Runnable {
    static GoogleAccountCredential mCredential;
    private static HttpTransport transport;
    private static JsonFactory jsonFactory;
    ProgressDialog mProgress;
    boolean createOneSchedule = true;
    GmailSync gmailThread;
    private Context mContext;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static  com.google.api.services.calendar.Calendar mService = null;
    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String[] SCOPES = { "https://www.googleapis.com/auth/calendar",
            "https://www.googleapis.com/auth/calendar.readonly" };
    //public static String calendarName = "";

    public CalendarSync(GoogleAccountCredential mCredential, Context mContext) {
        this.mContext = mContext;
        this.mCredential = mCredential;
    }

    public void run() {
        getResultsFromApi(mCredential);
        gmailThread = new GmailSync(transport, jsonFactory, mCredential, mContext);
        Thread gmail = new Thread(gmailThread);
        gmail.start();
    }
    public static void createEvent(com.google.api.services.calendar.Calendar mService,
                                   String startD, String endD, String nation, String fromNation) throws IOException {
        Event event = new Event().setSummary("Travel to " + nation)
                .setLocation(nation)
                .setDescription("Flight from " + fromNation + " to " + nation);
        DateTime startDateTime = new DateTime(startD + "T09:00:00");

        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Seoul");
        event.setStart(start);

        DateTime endDateTime = new DateTime(endD + "T11:00:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Seoul");
        event.setEnd(end);

        EventReminder[] reminderOverrides = new EventReminder[]{
                new EventReminder().setMethod("email").setMinutes(24 * 60),
                new EventReminder().setMethod("popup").setMinutes(10),
        };

        String calendarId = "primary";
        event = mService.events().insert(calendarId, event).execute();
        Log.d("create", "Event created : " + event.getHtmlLink());
    }
    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     * @param mCredential
     */
    public void getResultsFromApi(GoogleAccountCredential mCredential) {
        new MakeRequestTask(mCredential).execute();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {

        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            transport = AndroidHttp.newCompatibleTransport();
            jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Travel Maker")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = event.getStart().getDate();
                }
                eventStrings.add(
                        String.format("%s (%s)", event.getSummary(), start));
            }
            if(createOneSchedule) {
                //createEvent(mService);`
                createOneSchedule = false;
            }
            return eventStrings;
        }

        @Override
        protected void onPreExecute() {
            //mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();
            if (output == null || output.size() == 0) {
                Toast.makeText(mContext, "No results returned", Toast.LENGTH_LONG).show();
            } else {
                output.add(0, "구글 캘린더 연동 완료");
                Toast.makeText(mContext, TextUtils.join("\n", output), Toast.LENGTH_LONG).show();
            }
        }
/*
        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CalendarSync.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(mContext, "The following error occurred:\n" + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(mContext, "Request Canceled", Toast.LENGTH_LONG).show();
            }
        }*/
    }
}