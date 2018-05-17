package com.ellalee.travelmaker;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.ellalee.travelmaker.R;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.Profile;
import com.google.api.services.gmail.model.Thread;

import org.json.JSONException;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class GmailSync extends Activity {

    private static final String TAG = "PlayHelloActivity";

    protected final static String GMAIL_SCOPE
            = "https://www.googleapis.com/auth/gmail.readonly";
    protected final static String SCOPE
            = "oauth2:" +  GMAIL_SCOPE;

    private TextView mOut;
    private ListView lView;
    private ProgressBar spinner;
    private ArrayList<String> l;
    private ArrayList<String> b;
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    public static final String PREFS_NAME = "PrimeFile";
    protected String mEmail;
    public static int count = 0;
    MySQLiteHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail_sync);

        String email = loadSavedPreferences();
        if(!email.equals("EmailStuff")){
            mEmail = loadSavedPreferences();
            Log.d("Email2", mEmail);
        }

        Log.d("createGmail", "Create GmailSync");
        db = new MySQLiteHelper(this);
        l = new ArrayList<String>();
        b = new ArrayList<String>();
        List<Email> list = db.getAllBooks();
        for(Email e : list){
            l.add(e.getSubject());
            b.add(e.getBody());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, l );

        mOut = (TextView) findViewById(R.id.message);
        lView = (ListView) findViewById(R.id.listView);

        spinner = (ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        lView.setAdapter(arrayAdapter);
        lView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(getApplicationContext(), InfoActivity.class);
                i.putExtra("body",b.get(position));
                i.putExtra("subject", l.get(position));
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                savePreferences("email", mEmail);
                Log.d("Email1", "Putting " + mEmail + " into prefs");
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You must pick an account", Toast.LENGTH_SHORT).show();
            }
        } else if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK) {
            handleAuthorizeResult(resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleAuthorizeResult(int resultCode, Intent data) {
        if (data == null) {
            show("Unknown error, click the button again");
            return;
        }
        if (resultCode == RESULT_OK) {
            Log.i(TAG, "Retrying");
            getTask(this, mEmail, SCOPE).execute();
            return;
        }
        if (resultCode == RESULT_CANCELED) {
            show("User rejected authorization.");
            return;
        }
        show("Unknown error, click the button again");
    }

    /** Called by button in the layout */
    public void greetTheUser(View view) {
        getUsername();
    }

    private void savePreferences(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private String loadSavedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sharedPreferences.getString("email", "EmailStuff");
        return name;
    }



    /** Attempt to get the user name. If the email address isn't known yet,
     * then call pickUserAccount() method so the user can pick an account.
     */
    protected void getUsername() {
        if (mEmail == null) {
            pickUserAccount();
        } else {
            if (isDeviceOnline()) {
                getTask(GmailSync.this, mEmail, SCOPE).execute();
            } else {
                Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Starts an activity in Google Play Services so the user can pick an account */
    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    /** Checks whether the device currently has a network connection */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }


    /**
     * This method is a hook for background threads and async tasks that need to update the UI.
     * It does this by launching a runnable under the UI thread.
     */
    public void show(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOut.setText(message);
            }
        });
    }


    public void list(final ArrayList<String> l) {
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, l );
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lView.setAdapter(arrayAdapter);
            }
        });
    }

    public void showSpinner(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideSpinner(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.GONE);
            }
        });
    }

    public void setItemListener(final ArrayList<String> b, final ArrayList<String> s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Intent i = new Intent(getApplicationContext(), InfoActivity.class);
                        i.putExtra("body",b.get(position));
                        i.putExtra("subject", s.get(position));
                        startActivity(i);
                    }
                });
            }
        });
    }

    /**
     * This method is a hook for background threads and async tasks that need to provide the
     * user a response UI when an exception occurs.
     */
    public void handleException(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException)e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            GmailSync.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    /**
     * Note: This approach is for demo purposes only. Clients would normally not get tokens in the
     * background from a Foreground activity.
     */
    protected GetNameTask getTask(
            GmailSync activity, String email, String scope) {

        return new GetNameTask(activity, email, scope);

    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }


    public static class GetNameTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "TokenInfoTask";
        private static final String NAME_KEY = "given_name";
        protected GmailSync mActivity;

        protected String mScope;
        protected String mEmail;

        GetNameTask(GmailSync activity, String email, String scope) {
            this.mActivity = activity;
            this.mScope = scope;
            this.mEmail = email;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                fetchNameFromProfileServer();
            } catch (IOException ex) {
                onError("Following Error occured, please try again. " + ex.getMessage(), ex);
            } catch (JSONException e) {
                onError("Bad response: " + e.getMessage(), e);
            }
            return null;
        }

        protected void onError(String msg, Exception e) {
            if (e != null) {
                Log.e(TAG, "Exception: ", e);
            }
            mActivity.show(msg);  // will be run in UI thread
        }

        /**
         * Get a authentication token if one is not available. If the error is not recoverable then
         * it displays the error message on parent activity.
         */
        protected String fetchToken() throws IOException {
            try {
                return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
            } catch (UserRecoverableAuthException userRecoverableException) {
                // GooglePlayServices.apk is either old, disabled, or not present, which is
                // recoverable, so we need to show the user some UI through the activity.
                mActivity.handleException(userRecoverableException);
            } catch (GoogleAuthException fatalException) {
                onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
            }
            return null;
        }

        /**
         * Contacts the user info server to get the profile of the user and extracts the first name
         * of the user from the profile. In order to authenticate with the user info server the method
         * first fetches an access token from Google Play services.
         * @throws IOException if communication with user info server failed.
         * @throws JSONException if the response from the server could not be parsed.
         */
        protected void fetchNameFromProfileServer() throws IOException, JSONException {
            MySQLiteHelper db = new MySQLiteHelper(mActivity);
            db.deleteEverything();
            //mActivity.showSpinner();
            //mActivity.show("Getting emails...");
            String token = fetchToken();
            if (token == null) {
                return;
            }

            GoogleCredential credential = new GoogleCredential().setAccessToken(token);
                      JsonFactory jsonFactory = new JacksonFactory();
            HttpTransport httpTransport = new NetHttpTransport();

            Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential).setApplicationName("GmailApiTP").build();
            String author = "";
            ListThreadsResponse threadsResponse;
            Profile p;
            Thread response;
            List<Message> m = null;
            List<Thread> t = null;
            BigInteger i;
            ArrayList<String> subs = new ArrayList<String>();
            ArrayList<String> body = new ArrayList<String>();

            ArrayList<String> l = new ArrayList<String>();
            StringBuilder builder = new StringBuilder();
            String body2 = "";
            String sub = "";
            String bod = "";
            int emailDate[] = {0,0,0};
            //Note for later.
            //p = service.users().getProfile("me").execute();
            //i = p.getHistoryId();
            //Log.d("Task", "Test history id: " + i);

            try {
                threadsResponse = service.users().threads().list("me").execute();
                t = threadsResponse.getThreads();
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(Thread thread : t) {
                String id = thread.getId();
                response = service.users().threads().get("me",id).execute();
                List<MessagePartHeader> messageHeader = response.getMessages().get(0).getPayload().getHeaders();

                List<Message> testing = response.getMessages();
                for(Message test : testing){
                    if(test.getPayload().getMimeType().contains("multipart")){
                        builder = new StringBuilder();
                        for(MessagePart part : test.getPayload().getParts()){
                            if (part.getMimeType().contains("multipart")) {
                                for (MessagePart part2 : part.getParts()) {
                                    if (part2.getMimeType().equals("text/plain")) {
                                        builder.append(new String(
                                                Base64.decodeBase64(part2.getBody().getData())));
                                    }
                                }
                            }else if (part.getMimeType().equals("text/plain")) {
                                builder.append(new String(Base64.decodeBase64(part.getBody().getData())));
                            }
                        }

                    }else{
                        body2 = new String(Base64.decodeBase64(test.getPayload().getBody().getData()));
                    }
                }
                if(!body.toString().isEmpty()){
                    body.add(builder.toString());
                    bod = builder.toString();
                }else{
                    body.add(body2);
                    bod = body2;
                }

                for( MessagePartHeader h : messageHeader) {
                    if(h.getName().equals("Subject")){
                        sub = h.getValue();
                        l.add(h.getValue());
                        subs.add(h.getValue());
                        //mActivity.list(l);
                        break;

                    }else if(h.getName().equals("Date")){
                        emailDate = getDate(h.getValue());
                    }else if(h.getName().equals("From")){
                        author = h.getValue();
                    }
                }
                ++count;
                Log.d("count", String.valueOf(count));
                db.addBook(new Email(sub,bod,author,emailDate[0],emailDate[1],emailDate[2],1));
            }


            //mActivity.list(l);
            //mActivity.setItemListener(body, subs);
            //mActivity.hideSpinner();
        }

        public int[] getDate(String time){
            int day[] = {0,0,0};
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    "EEE, d MMM yyyy HH:mm:ss Z", Locale.KOREA);
            Date date = null;
            try {
                date = simpleDateFormat.parse(time);

                Calendar fDate = Calendar.getInstance();
                fDate.setTime(date);

                simpleDateFormat = new SimpleDateFormat("dd");
                String d =  simpleDateFormat.format(fDate.getTime());
                day[0] = Integer.parseInt(d);

                simpleDateFormat = new SimpleDateFormat("MM");
                d =  simpleDateFormat.format(fDate.getTime());
                day[1] = Integer.parseInt(d);

                simpleDateFormat = new SimpleDateFormat("yyyy");
                d =  simpleDateFormat.format(fDate.getTime());
                day[2] = Integer.parseInt(d);

                return day;
            } catch (ParseException e) {
                e.printStackTrace();
                return day;
            }
        }

        public int[] getCurrentDate(){
            int day[] = {0,0,0};
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd");
            String dd = sdf.format(c.getTime());
            day[0] = Integer.parseInt(dd);

            sdf = new SimpleDateFormat("MM");
            String mm = sdf.format(c.getTime());
            day[1] = Integer.parseInt(mm);

            sdf = new SimpleDateFormat("yyyy");
            String yy = sdf.format(c.getTime());
            day[2] = Integer.parseInt(yy);

            return day;
        }


    }
}

