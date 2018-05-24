package com.ellalee.travelmaker;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by jiwon on 2018-05-18.
 */

public class NewFont extends AppCompatTextView {
    public NewFont(Context context) {
        super(context);
        setType(context);
    }

    public NewFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        setType(context);
    }

    public NewFont(Context context, AttributeSet attrs, int defStyleAttrs) {
        super(context, attrs, defStyleAttrs);
        setType(context);
    }

   /*public NewFont(Context context, AttributeSet attrs, int defStyleAttrs, int defStyleRes) {
        super(context, attrs, defStyleAttrs, defStyleRes);
        setType(context);
    }*/

    private void setType(Context context) {
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "NanumSquareRoundB.ttf"));
    }
}
