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
            int splitCount = 0;
            String check = "";
            int siteIndex = 0;
            String[] nationCheck = new String[2];
            int nationIndex = 0;
            //호텔 바우처 등록
            /*if (finalSub.contains("HOTEL VOUCHER") || finalSub.contains("호스트") || finalBod.contains("투숙객") ||finalBod.contains("체크인")) {
                //booking.com
                cou++;
                String[] bods = finalBod.split("\n");
                String checkInOut[] = new String[2];
                for(int k = 0;k<bods.length;k++){
                    if(bods[k].contains("*체크인*") || bods[k].contains("*체크아웃*")){
                        for(int lo = 0;lo<8;lo++){
                            if(Character.isDigit(bods[k].charAt(lo)))
                                if(bods[k].contains("체크인")) {checkInOut[0] += bods[k].charAt(lo);Log.d("checkinout", checkInOut[0]);}
                               
                        }
                        if(bods[k].contains("체크인")) {
                            total[cou] += "체크인" + checkInOut[0].substring(0,4) + "/" +"0" +
                                    checkInOut[0].substring(4,5) + "/" + checkInOut[0].substring(5, 7);
                        }
                        else
                            total[cou] += "체크아웃"+ checkInOut[1].substring(0,4) + "/" +"0" +
                                    checkInOut[1].substring(4,5) + "/" + checkInOut[1].substring(5, 7) + "\n";

                    }
                }
            }*/
            // 항공사에서 티켓을 예매했을경우
            if (finalSub.contains("제주항공")) {
                if(db.select(sub)) continue;
                else db.addBook(new Email(sub,bod,author,emailDate[0],emailDate[1],emailDate[2],1));
                //제주항공
                cou++;
                String[] bods = finalBod.split("\n");
                for (int k = 0; k < bods.length; k++) {
                    String[] split = new String[2];

                    if (bods[k].contains("Departure")) {
                        split[splitCount] += bods[k] + '\n';
                        String[] date = new String[2];
                        for (int la = 0; la < split[splitCount].length(); la++) {
                            if (Character.isDigit(split[splitCount].charAt(la))) {
                                check += split[splitCount].charAt(la);
                            }
                            if (la == split[splitCount].length() - 1) check += ' ';
                        }
                        Log.d("CheckJeju", check);
                        nationCheck[nationIndex++] = bods[k].split(" ")[2];
                        Log.d("NationChecking", nationCheck[nationIndex - 1]);
                    }
                }
                String[] StartEndJeju = check.split(" ");
                total[cou] += "출국일자" + StartEndJeju[0].substring(0, 4) + "/0" +
                        StartEndJeju[0].substring(4, 5) + "/" + StartEndJeju[0].substring(5, 7) + "(화)";
                total[cou] += "귀국일자" + StartEndJeju[1].substring(0, 4) + "/0" +
                        StartEndJeju[1].substring(4, 5) + "/" + StartEndJeju[1].substring(5, 7) + "(화)" + "\n";
                Log.d("JejuSE", total[cou]);
                total[cou] += nationCheck[0] + "\n";
                total[cou] += nationCheck[1] + "\n";
            } else if (finalSub.contains("티웨이항공")) {
                if(db.select(sub)) continue;
                else db.addBook(new Email(sub,bod,author,emailDate[0],emailDate[1],emailDate[2],1));
                cou++;
                String[] split = new String[5];
                String[] bods = finalBod.split("\n");
                String[] site = new String[3];
                for (int k = 0; k < bods.length; k++) {

                    if (bods[k].contains("2018/") && (bods[k].contains("인천") || bods[k].contains("김포"))) {
                        split = bods[k].split(" ");
                        if (splitCount == 0) total[cou] += "출국일자" + split[0].substring(0, 13);
                        else total[cou] += "귀국일자" + split[0] + "\n";
                        site[siteIndex++] = split[1];
                        splitCount++;
                    }
                }
                for (int lol = 0; lol < siteIndex; lol++) {
                    total[cou] += site[lol] + "\n";
                    Log.d("T-way", total[cou]);
                }
            } else if (finalSub.contains("에미레이트 항공")) {
                //에미레이트항공 e티켓
            } else if (finalBod.contains("LJ") && finalBod.contains("ICN")) {
                //진에어 e티켓
            }
            //발권대행사를 이용했을 경우
            if (finalSub.contains("항공 결제요청") || finalSub.contains("항공권 결제요청")) {
                if(db.select(sub)) continue;
                else db.addBook(new Email(sub,bod,author,emailDate[0],emailDate[1],emailDate[2],1));
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
                        db.addBook(new Email(sub,bod,author,emailDate[0],emailDate[1],emailDate[2],1));
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