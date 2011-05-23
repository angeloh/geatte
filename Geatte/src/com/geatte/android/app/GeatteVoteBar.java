package com.geatte.android.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;

public class GeatteVoteBar extends LinearLayout {
    public GeatteVoteBar(Context context) {
	super(context);
    }
    public GeatteVoteBar(Context context, AttributeSet attrs) {
	super(context, attrs);
	setOrientation(HORIZONTAL);
	setGravity(Gravity.CENTER);
	setWeightSum(1.0f);

	LayoutInflater.from(context).inflate(R.layout.geatte_vote_bar, this, true);

	TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GeatteVoteBar, 0, 0);

	String text = array.getString(R.styleable.GeatteVoteBar_yes_label);
	if (text == null) text = "Yes";
	((Button) findViewById(R.id.geatte_vote_btn_yes)).setText(text);

	text = array.getString(R.styleable.GeatteVoteBar_no_label);
	if (text == null) text = "No";
	((Button) findViewById(R.id.geatte_vote_btn_no)).setText(text);

	array.recycle();
    }
}
