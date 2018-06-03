package com.ellalee.travelmaker;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
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
    TextView test;
    GoogleAccountCredential mCredential;
    List<Email> allMail;
    MySQLiteHelper db;
    ArrayList<String> sub, bod;

    public GmailSync(Context mContext, HttpTransport mHttpTrans, JsonFactory mJasonfact, GoogleAccountCredential mCredential, TextView test) {
        this.mContext = mContext;
        this.mHttpTransport = mHttpTrans;
        this.mJsonFactory = mJasonfact;
        this.mCredential = mCredential;
        this.test = test;
        Log.d("Gmail Sync access", "Gmail Sync Access Complete");

    }


    public void run() {
        try {
            fetchNameFromProfileServer();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void fetchNameFromProfileServer() throws IOException, JSONException {
        int cou = 0;
        db = new MySQLiteHelper(mContext);
        db.deleteEverything();
        final String[] total = new String[10];
        for (int j = 0; j < 10; j++) total[j] = "";
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
        String text = "";
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
            int nationCo = 0;
            if (finalSub.contains(""))
                if (finalSub.contains("항공 결제요청") || finalSub.contains("항공권 결제요청")) {
                    cou++;
                    String[] bods = finalBod.split("\n");
                    for (int k = 0; k < bods.length; k++) {
                        if (bods[k].contains("출국일자")) {
                            Log.d("cou", Integer.toString(cou));
                            total[cou] += bods[k] + '\n';
                        } else if (bods[k].contains("일반석1석OK")) {
                            int isNumIndex = 0;
                            Log.d("HELLLOWORLD", bods[k - 2]);
                            String[] nation = new String[4];
                            for (int j = bods[k - 2].length() - 1; j >= 4; j--) {
                                if (Character.isDigit(bods[k - 2].charAt(j))) {
                                    isNumIndex = j + 1;
                                    break;
                                }
                            }

                            nation[nationCo++] = bods[k - 2].substring(isNumIndex);
                            total[cou] += nation[nationCo - 1] + '\n';
                            Log.d("Nation", nation[nationCo - 1]);
                        }
                    }
                    for (int k = 0; k < total.length; k++)
                        text += total[k] + '\n';
                }
        }
        final String finalText = text;
        final String[] textArray = total;

        Log.d("TextArray", textArray[1] + "This is one\n" + textArray[2] + "This is two\n");

        for(int j = 1;!textArray[j].equals("");j++) {
            String toNation, fromNation;
            String[] parseData = textArray[j].split("\n");
            fromNation = parseData[1];
            if (parseData.length == 3) toNation = parseData[2];
            else toNation = parseData[3];

            String startD = "", endD = "";
            for (int k = 4; k < parseData[0].length() / 2; k++) {
                if (Character.isDigit(parseData[0].charAt(k)))
                    startD += parseData[0].charAt(k);
            }

            for (int k = parseData[0].length() / 2; k < parseData[0].length(); k++)
                if (Character.isDigit(parseData[0].charAt(k)))
                    endD += parseData[0].charAt(k);
            startD = startD.substring(0, 4) + "-" + startD.substring(4, 6) + "-" + startD.substring(6);
            endD = endD.substring(0, 4) + "-" + endD.substring(4, 6) + "-" + endD.substring(6);
            Log.d("start end date", startD + ' ' + endD);
            CalendarSync.createEvent(CalendarSync.mService, startD, startD, toNation, fromNation);
            CalendarSync.createEvent(CalendarSync.mService, endD, endD, fromNation, toNation);
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