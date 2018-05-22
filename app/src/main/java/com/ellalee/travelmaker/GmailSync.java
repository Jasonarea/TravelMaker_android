package com.ellalee.travelmaker;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
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
import java.util.List;
import java.util.Locale;


public class GmailSync implements Runnable {

    private static final String TAG = "PlayHelloActivity";

    protected final static String GMAIL_SCOPE
            = "https://www.googleapis.com/auth/gmail.readonly";
    protected final static String SCOPE
            = "oauth2:" + GMAIL_SCOPE;
    public Context mContext;
    public static int count = 0;
    HttpTransport mHttpTransport;
    JsonFactory mJsonFactory;
    Handler handler = new Handler();
    GoogleAccountCredential mCredential;
    TextView ticket;
    List<Email> allMail;
    MySQLiteHelper db;
    ArrayList<String> sub, bod;

    public GmailSync(Context mContext, HttpTransport mHttpTrans, JsonFactory mJasonfact, GoogleAccountCredential mCredential, TextView ticket) {
        this.mContext = mContext;
        this.mHttpTransport = mHttpTrans;
        this.mJsonFactory = mJasonfact;
        this.mCredential = mCredential;
        this.ticket = ticket;
        Log.d("Gmail Sync access", "Gmail Sync Access Complete");
    }


    public void run() {
        try {
            fetchNameFromProfileServer();


            /*allMail = db.getAllBooks();
            sub = new ArrayList<String>();
            bod = new ArrayList<String>();
            for(Email e : allMail) {
                Log.d("sub", e.getSubject());
                sub.add(e.getSubject());
                bod.add(e.getBody());
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fetchNameFromProfileServer() throws IOException, JSONException {

        db = new MySQLiteHelper(mContext);
        db.deleteEverything();

        Gmail service = new Gmail.Builder(mHttpTransport, mJsonFactory, mCredential).setApplicationName("GmailApiTP").build();
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
        int emailDate[] = {0, 0, 0};
        try {
            threadsResponse = service.users().threads().list("me").execute();
            t = threadsResponse.getThreads();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Thread thread : t) {
            String id = thread.getId();
            response = service.users().threads().get("me", id).execute();
            List<MessagePartHeader> messageHeader = response.getMessages().get(0).getPayload().getHeaders();

            List<Message> testing = response.getMessages();
            for (Message test : testing) {
                if (test.getPayload().getMimeType().contains("multipart")) {
                    builder = new StringBuilder();
                    for (MessagePart part : test.getPayload().getParts()) {
                        if (part.getMimeType().contains("multipart")) {
                            for (MessagePart part2 : part.getParts()) {
                                if (part2.getMimeType().equals("text/plain")) {
                                    builder.append(new String(
                                            Base64.decodeBase64(part2.getBody().getData())));
                                }
                            }
                        } else if (part.getMimeType().equals("text/plain")) {
                            builder.append(new String(Base64.decodeBase64(part.getBody().getData())));
                        }
                    }

                } else {
                    body2 = new String(Base64.decodeBase64(test.getPayload().getBody().getData()));
                }
            }
            if (!body.toString().isEmpty()) {
                body.add(builder.toString());
                bod = builder.toString();
            } else {
                body.add(body2);
                bod = body2;
            }

            for (MessagePartHeader h : messageHeader) {
                if (h.getName().equals("Subject")) {
                    sub = h.getValue();
                    l.add(h.getValue());
                    subs.add(h.getValue());
                    break;
                } else if (h.getName().equals("Date")) {
                    emailDate = getDate(h.getValue());
                } else if (h.getName().equals("From")) {
                    author = h.getValue();
                }
            }
            ++count;
            Log.d("count", String.valueOf(count));

            final String finalBod = bod;
            final String finalSub = sub;
            if(finalBod.contains("E-ticket") || finalBod.contains("항공권") || finalBod.contains("VOUCHER") ||
                    finalBod.contains("에어비앤비")) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ticket.append(finalSub + '\n');
                    }
                });
                db.addBook(new Email(sub, bod, author, emailDate[0], emailDate[1], emailDate[2], 1));
            }
        }
    }



    public int[] getDate(String time) {
        int day[] = {0, 0, 0};
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "EEE, d MMM yyyy HH:mm:ss Z", Locale.KOREA);
        Date date = null;
        try {
            date = simpleDateFormat.parse(time);

            Calendar fDate = Calendar.getInstance();
            fDate.setTime(date);

            simpleDateFormat = new SimpleDateFormat("dd");
            String d = simpleDateFormat.format(fDate.getTime());
            day[0] = Integer.parseInt(d);

            simpleDateFormat = new SimpleDateFormat("MM");
            d = simpleDateFormat.format(fDate.getTime());
            day[1] = Integer.parseInt(d);

            simpleDateFormat = new SimpleDateFormat("yyyy");
            d = simpleDateFormat.format(fDate.getTime());
            day[2] = Integer.parseInt(d);

            return day;
        } catch (ParseException e) {
            e.printStackTrace();
            return day;
        }
    }

    public int[] getCurrentDate() {
        int day[] = {0, 0, 0};
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

