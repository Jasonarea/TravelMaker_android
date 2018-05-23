package com.ellalee.travelmaker;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ellalee.travelmaker.R;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

public class Sharing extends AppCompatActivity {

    private KakaoLink kakaoLink;
    private KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder;
    private EditText mEditText;
    private Button mSendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            kakaoLink = KakaoLink.getKakaoLink(getApplicationContext());
            kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
        } catch (KakaoParameterException e) {
            e.getMessage();
        }

        mEditText = (EditText) findViewById(R.id.editText);
        mSendBtn = (Button) findViewById(R.id.btnSend);
        mSendBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLink();
            }
        });
    }

    private void sendLink(){
        try {
            kakaoTalkLinkMessageBuilder.addText(mEditText.getText().toString());
            final String linkContents = kakaoTalkLinkMessageBuilder.build();
            //kakaoLink.sendMessage(linkContents, this);
        } catch (KakaoParameterException e) {
            e.getMessage();
        }
    }
}