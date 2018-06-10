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

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
    Handler handler = new Handler();
    ProgressBar pb;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static  com.google.api.services.calendar.Calendar mService = null;
    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "AccountName";
    private static boolean isTravelMaker = false;
    public static final String[] SCOPES = { "https://www.googleapis.com/auth/calendar",
            "https://www.googleapis.com/auth/calendar.readonly" };
    //public static String calendarName = "";
    static com.google.api.services.calendar.Calendar service;
    static String newCalId;
    List<String> eventStrings;

    public CalendarSync(GoogleAccountCredential mCredential, Context mContext, ProgressBar pb, Handler handler) {
        this.mContext = mContext;
        this.mCredential = mCredential;
        this.pb = pb;
        this.handler = handler;
    }

    public void run() {
        getResultsFromApi(mCredential);
        try {
            calendarList();

        } catch (IOException e) {
            e.printStackTrace();
        }
        gmailThread = new GmailSync(transport, jsonFactory, mCredential, mContext, pb, handler);
        Thread gmail = new Thread(gmailThread);
        gmail.start();
    }
    public static String insertCal() throws IOException {
        // Initialize Calendar service with valid OAuth credentials
        service = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Travel Maker")
                .build();


        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary("Travel Maker");
        calendar.setTimeZone("Asia/Seoul");

        com.google.api.services.calendar.model.Calendar createdCalendar = service.calendars().insert(calendar).execute();
        newCalId = createdCalendar.getId();
        Log.d("Cal_ID", "New Calendar is created");

        return createdCalendar.getId();
    }

    public static String getCal() throws IOException{
        service = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Travel Maker")
                .build();

// Iterate through entries in calendar list
        String pageToken = null;
        String id = null;
        do {
            CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                if(calendarListEntry.getSummary().equals("Travel Maker")) {
                    id = calendarListEntry.getId();
                    break;
                }
                else continue;
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
        Log.d("Get ID", "Travel Maker Calendar 생성 성공");
        return id;
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

        String calendarId = newCalId;
        event = mService.events().insert(calendarId, event).execute();
        Log.d("create", "Event created : " + event.getHtmlLink());
    }

    public static void createEvent2(com.google.api.services.calendar.Calendar mService,
                                   String startD, String endD, String hotelName) throws IOException {
        Event event = new Event().setSummary("Stay in " + hotelName)
                .setLocation(hotelName)
                .setDescription("Stay in " + hotelName);
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

        String calendarId = newCalId;
        event = mService.events().insert(calendarId, event).execute();
        Log.d("create", "Event created : " + event.getHtmlLink());
    }



    public static void calendarList() throws IOException {
        service = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Travel Maker")
                .build();

// Iterate through entries in calendar list
        String pageToken = null;
        do {
            CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                if(calendarListEntry.getSummary().equals("Travel Maker")) {
                    Log.d("List view", calendarListEntry.getSummary());
                    isTravelMaker = true;
                }
                else continue;
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
        if(!isTravelMaker) newCalId = insertCal();
        else newCalId = getCal();



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
            eventStrings = new ArrayList<String>();
            Log.d("id", newCalId);
            Events events = mService.events().list(newCalId)
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
                Log.d("eventString", event.getSummary() + " " + start);
            }
            if(createOneSchedule) {
                //createOneSchedule = false;
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
                output.add(0, "Data retrieved using the Google Calendar API:");
                Log.d("Calendar Test", TextUtils.join("\n", output));
                Toast.makeText(mContext, "캘린더 수집 완료", Toast.LENGTH_LONG).show();
                CalendarMain.setDoList(eventStrings);
            }
        }
    }
    class UIUpdate implements Runnable {
        @Override
        public void run() {
            MainActivity.pb.setVisibility(View.INVISIBLE);
            MainActivity.ptt.setVisibility(View.GONE);
        }
    }
}