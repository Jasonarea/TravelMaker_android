package com.ellalee.travelmaker;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;

import com.kakao.KakaoLink;
import com.kakao.KakaoParameterException;
import com.kakao.Session;
/**
 * Copyright 2012 Kakao Crop. All rights reserved.
 *
 * @author kakaolink@kakao.com
 */
public class Sharing extends Activity {

    // Recommened Charset UTF-8
    private String encoding = "UTF-8";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakao_link);
    }

    /**
     * Send URL
     * @throws NameNotFoundException
     */
    public void sendUrlLink(View v) throws NameNotFoundException {
        // Recommended: Use application context for parameter.
        KakaoLink kakaoLink = null;
        try {
            kakaoLink = KakaoLink.getKakaoLink((getApplicationContext()));
        } catch (KakaoParameterException e) {
            e.printStackTrace();
        }

        /**
         * @param activity
         * @param url
         * @param message
         * @param appId
         * @param appVer
         * @param appName
         * @param encoding
         */
        kakaoLink.createKakaoTalkLinkMessageBuilder();
    }

    /**
     * Send App data
     */
    public void sendAppData(View v) throws NameNotFoundException {
        ArrayList<Map<String, String>> metaInfoArray = new ArrayList<Map<String, String>>();

        // If application is support Android platform.
        Map<String, String> metaInfoAndroid = new Hashtable<String, String>(1);
        metaInfoAndroid.put("os", "android");
        metaInfoAndroid.put("devicetype", "phone");
        metaInfoAndroid.put("installurl", "market://details?id=com.kakao.talk");
        metaInfoAndroid.put("executeurl", "kakaoLinkTest://starActivity");

        // If application is support ios platform.
        Map<String, String> metaInfoIOS = new Hashtable<String, String>(1);
        metaInfoIOS.put("os", "ios");
        metaInfoIOS.put("devicetype", "phone");
        metaInfoIOS.put("installurl", "your iOS app install url");
        metaInfoIOS.put("executeurl", "kakaoLinkTest://starActivity");

        // add to array
        metaInfoArray.add(metaInfoAndroid);
        metaInfoArray.add(metaInfoIOS);

        // Recommended: Use application context for parameter.
        KakaoLink kakaoLink = null;
        try {
            kakaoLink = KakaoLink.getKakaoLink(getApplicationContext());
        } catch (KakaoParameterException e) {
            e.printStackTrace();
        }


        /**
         * @param activity
         * @param url
         * @param message
         * @param appId
         * @param appVer
         * @param appName
         * @param encoding
         * @param metaInfoArray
         */
        kakaoLink.createKakaoTalkLinkMessageBuilder();
    }

}