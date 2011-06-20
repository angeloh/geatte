package com.geatte.android.app;

import greendroid.app.GDActivity;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geatte.android.c2dm.C2DMessaging;

/**
 * Setup activity - takes user through the setup.
 */
public class GeatteSetupActivity extends GDActivity {

    private boolean mPendingAuth = false;
    private int mScreenId = -1;
    private int mAccountSelectedPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	Context context = getApplicationContext();
	final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	int savedScreenId = prefs.getInt(Config.SAVED_SCREEN_ID, -1);
	if (savedScreenId == -1) {
	    setScreenContent(R.layout.geatte_intro);
	} else {
	    setScreenContent(savedScreenId);
	}

	registerReceiver(mUpdateUIReceiver, new IntentFilter(Config.INTENT_ACTION_UPDATE_UI));
	registerReceiver(mAuthPermissionReceiver, new IntentFilter(Config.INTENT_ACTION_AUTH_PERMISSION));
    }

    @Override
    protected void onResume() {
	Log.d(Config.LOGTAG_C2DM, "SetupActivity:onResume() START");
	super.onResume();
	if (mPendingAuth) {
	    mPendingAuth = false;
	    String regId = C2DMessaging.getRegistrationId(this);
	    if (regId != null && !"".equals(regId)) {
		DeviceRegistrar.registerWithServer(this, regId);
	    } else {
		C2DMessaging.register(this, Config.C2DM_SENDER);
	    }
	}
	Log.d(Config.LOGTAG_C2DM, "SetupActivity:onResume() END");
    }

    @Override
    public void onDestroy() {
	unregisterReceiver(mUpdateUIReceiver);
	unregisterReceiver(mAuthPermissionReceiver);
	super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	//	case R.id.help: {
	//	    startActivity(new Intent(this, HelpActivity.class));
	//	    return true;
	//	}
	default: {
	    return super.onOptionsItemSelected(item);
	}
	}
    }

    @Override
    public void setActionBarContentView(int resID) {
	setContentView(createLayout());
	LayoutInflater.from(this).inflate(resID, getContentView());
    }

    private void setScreenContent(int screenId) {
	mScreenId = screenId;
	setActionBarContentView(screenId);
	switch (screenId) {
	// Screen shown if phone is registered/connected
	//	case R.layout.connected: {
	//	    setConnectedScreenContent();
	//	    break;
	//	}
	// Ordered sequence of screens for setup
	case R.layout.geatte_intro: {
	    setIntroScreenContent();
	    break;
	}
	case R.layout.geatte_select_account: {
	    setSelectAccountScreenContent();
	    break;
	}
	case R.layout.geatte_setup_complete: {
	    setSetupCompleteScreenContent();
	    break;
	}
	}
	Context context = getApplicationContext();
	final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	SharedPreferences.Editor editor = prefs.edit();
	editor.putInt(Config.SAVED_SCREEN_ID, screenId);
	editor.commit();
    }

    private void setIntroScreenContent() {
	String introText = getString(R.string.intro_text);
	TextView textView = (TextView) findViewById(R.id.intro_text);
	textView.setText(Html.fromHtml(introText));
	textView.setMovementMethod(LinkMovementMethod.getInstance());

	TextView phoneMissingTextView = (TextView) findViewById(R.id.setup_intro_phone_missing_text);
	final EditText phoneEditText = (EditText) findViewById(R.id.setup_intro_edit_phone);

	String myNumber = DeviceRegistrar.getPhoneNumberFromTeleService(getApplicationContext());
	if (myNumber != null) {
	    Log.d(Config.LOGTAG, "SetupActivity:setSetupCompleteScreenContent() has phone " +
		    "number : " + myNumber + ", so disable edit text");
	    phoneMissingTextView.setVisibility(View.GONE);
	    phoneEditText.setVisibility(View.GONE);
	    phoneEditText.setText(myNumber);
	}

	Button exitButton = (Button) findViewById(R.id.intro_exit);
	exitButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		finish();
	    }
	});

	Button nextButton = (Button) findViewById(R.id.intro_next);
	nextButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		String num = phoneEditText.getText().toString().trim();
		if (num != null && num.length() > 0) {
		    Context context = getApplicationContext();
		    final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
		    SharedPreferences.Editor editor = prefs.edit();
		    editor.putString(Config.PREF_PHONE_NUMBER, num);
		    editor.commit();
		    setScreenContent(R.layout.geatte_select_account);
		} else {
		    Toast.makeText(GeatteSetupActivity.this, "Please input your phone number", Toast.LENGTH_SHORT).show();
		}
	    }
	});
    }

    private void setSelectAccountScreenContent() {
	final Button backButton = (Button) findViewById(R.id.select_account_back);
	backButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		setScreenContent(R.layout.geatte_intro);
	    }
	});

	final Button nextButton = (Button) findViewById(R.id.select_account_next);
	nextButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		ListView listView = (ListView) findViewById(R.id.select_account);
		mAccountSelectedPosition = listView.getCheckedItemPosition();
		TextView account = (TextView) listView.getChildAt(mAccountSelectedPosition);
		backButton.setEnabled(false);
		nextButton.setEnabled(false);
		register((String) account.getText());
	    }
	});

	// Display accounts
	String accounts[] = getGoogleAccounts();
	if (accounts.length == 0) {
	    TextView promptText = (TextView) findViewById(R.id.select_account_text);
	    promptText.setText(R.string.no_accounts);
	    TextView nextText = (TextView) findViewById(R.id.select_account_click_next_text);
	    nextText.setVisibility(TextView.INVISIBLE);
	    nextButton.setEnabled(false);
	} else {
	    ListView listView = (ListView) findViewById(R.id.select_account);
	    listView.setAdapter(new ArrayAdapter<String>(this, R.layout.email_account, accounts));
	    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    listView.setItemChecked(mAccountSelectedPosition, true);
	}
    }

    private void setSetupCompleteScreenContent() {
	TextView textView = (TextView) findViewById(R.id.setup_complete_text);
	textView.setText(Html.fromHtml(getString((R.string.setup_complete_text))));

	Button backButton = (Button) findViewById(R.id.setup_complete_back);
	backButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		setScreenContent(R.layout.geatte_select_account);
	    }
	});

	Button finishButton = (Button) findViewById(R.id.setup_complete_finish);
	finishButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		Context context = getApplicationContext();
		final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(Config.SAVED_SCREEN_ID, R.layout.geatte_intro);
		editor.commit();
		finish();
	    }
	});
    }

    private void register(String account) {
	ProgressBar progressBar = (ProgressBar) findViewById(R.id.select_account_progress_bar);
	progressBar.setVisibility(ProgressBar.VISIBLE);
	TextView textView = (TextView) findViewById(R.id.select_account_connecting_text);
	textView.setVisibility(ProgressBar.VISIBLE);

	Context context = getApplicationContext();
	final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	SharedPreferences.Editor editor = prefs.edit();
	editor.putString(Config.PREF_USER_EMAIL, account);
	editor.commit();

	C2DMessaging.register(this, Config.C2DM_SENDER);
    }

    //    private void unregister() {
    //	ProgressBar progressBar = (ProgressBar) findViewById(R.id.app_register_prog_bar);
    //	progressBar.setVisibility(ProgressBar.VISIBLE);
    //	TextView textView = (TextView) findViewById(R.id.app_register_prog_text);
    //	textView.setVisibility(ProgressBar.VISIBLE);
    //
    //	Button disconnectButton = (Button) findViewById(R.id.app_disconnect);
    //	disconnectButton.setEnabled(false);
    //
    //	C2DMessaging.unregister(this);
    //    }

    private String[] getGoogleAccounts() {
	ArrayList<String> accountNames = new ArrayList<String>();
	Account[] accounts = AccountManager.get(this).getAccounts();
	for (Account account : accounts) {
	    if (account.type.equals("com.google")) {
		accountNames.add(account.name);
	    }
	}

	String[] result = new String[accountNames.size()];
	accountNames.toArray(result);
	return result;
    }

    private void handleConnectingUpdate(int status) {
	if (status == DeviceRegistrar.REGISTERED_STATUS) {
	    Log.d(Config.LOGTAG, "SetupActivity:handleConnectingUpdate() status : registered");
	    setScreenContent(R.layout.geatte_setup_complete);
	} else {
	    ProgressBar progressBar = (ProgressBar) findViewById(R.id.select_account_progress_bar);
	    progressBar.setVisibility(ProgressBar.INVISIBLE);
	    TextView textView = (TextView) findViewById(R.id.select_account_connecting_text);
	    textView.setText(status == DeviceRegistrar.AUTH_ERROR_STATUS ? R.string.auth_error_text
		    : R.string.connect_error_text);

	    Button backButton = (Button) findViewById(R.id.select_account_back);
	    backButton.setEnabled(true);

	    Button nextButton = (Button) findViewById(R.id.select_account_next);
	    nextButton.setEnabled(true);
	}
    }

    private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    if (mScreenId == R.layout.geatte_select_account) {
		handleConnectingUpdate(intent.getIntExtra(DeviceRegistrar.STATUS_EXTRA, DeviceRegistrar.ERROR_STATUS));
	    }
	}
    };

    private final BroadcastReceiver mAuthPermissionReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    Bundle extras = intent.getBundleExtra(Config.EXTRA_KEY_ACCOUNT_BUNDLE);
	    if (extras != null) {
		Intent authIntent = (Intent) extras.get(AccountManager.KEY_INTENT);
		if (authIntent != null) {
		    mPendingAuth = true;
		    startActivity(authIntent);
		}
	    }
	}
    };
}
