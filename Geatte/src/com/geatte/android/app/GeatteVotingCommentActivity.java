package com.geatte.android.app;

import com.geatte.android.app.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class GeatteVotingCommentActivity extends Activity {

    private EditText mCommentEditText;
    private Button mCommentCancelButton;
    private Button mCommentOkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	Log.d(Config.LOGTAG, "GeatteVotingCommentActivity:onCreate(): START");
	super.onCreate(savedInstanceState);

	setContentView(R.layout.geatte_vote_comment_edit_view);

	mCommentEditText = (EditText) findViewById(R.id.voting_comment);

	mCommentEditText.setOnClickListener(new OnClickListener() {
	    String name = mCommentEditText.getText().toString();
	    String origVal = getResources().getText(R.string.voting_comment_text_default).toString();

	    @Override
	    public void onClick(View v) {
		if(name.equals(origVal));
		{
		    mCommentEditText.setText("");
		}

	    }
	});

	mCommentCancelButton = (Button) findViewById(R.id.voting_cancel_button);
	mCommentCancelButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View view) {
		Intent resultIntent = new Intent();
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	    }

	});

	mCommentOkButton = (Button) findViewById(R.id.voting_ok_button);
	mCommentOkButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View view) {
		String text = mCommentEditText.getText().toString();
		Intent resultIntent = new Intent();
		resultIntent.putExtra(Config.EXTRA_KEY_VOTING_COMMENT, text);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	    }

	});

	Log.d(Config.LOGTAG, "GeatteVotingCommentActivity:onCreate(): END");
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
    }

}
