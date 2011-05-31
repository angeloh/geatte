package com.geatte.android.app;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.geatte.android.c2dm.C2DMessaging;

/**
 * Setup activity - takes user through the setup.
 */
public class SetupActivity extends Activity {

    private boolean mPendingAuth = false;
    private int mScreenId = -1;
    private int mAccountSelectedPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	Context context = getApplicationContext();
	final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	int savedScreenId = prefs.getInt("savedScreenId", -1);
	if (savedScreenId == -1) {
	    setScreenContent(R.layout.intro);
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
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.setup, menu);
	return true;
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

    private void setScreenContent(int screenId) {
	mScreenId = screenId;
	setContentView(screenId);
	switch (screenId) {
	// Screen shown if phone is registered/connected
	case R.layout.connected: {
	    setConnectedScreenContent();
	    break;
	}
	// Ordered sequence of screens for setup
	case R.layout.intro: {
	    setIntroScreenContent();
	    break;
	}
	case R.layout.select_account: {
	    setSelectAccountScreenContent();
	    break;
	}
	case R.layout.select_launch_mode: {
	    setSelectLaunchModeScreenContent();
	    break;
	}
	case R.layout.setup_complete: {
	    setSetupCompleteScreenContent();
	    break;
	}
	}
	SharedPreferences prefs = Prefs.get(this);
	SharedPreferences.Editor editor = prefs.edit();
	editor.putInt("savedScreenId", screenId);
	editor.commit();
    }

    private void setIntroScreenContent() {
	String introText = getString(R.string.intro_text).replace("{tos_link}", HelpActivity.getTOSLink()).replace(
		"{pp_link}", HelpActivity.getPPLink());
	TextView textView = (TextView) findViewById(R.id.intro_text);
	textView.setText(Html.fromHtml(introText));
	textView.setMovementMethod(LinkMovementMethod.getInstance());

	Button exitButton = (Button) findViewById(R.id.exit);
	exitButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		finish();
	    }
	});

	Button nextButton = (Button) findViewById(R.id.next);
	nextButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		setScreenContent(R.layout.select_account);
	    }
	});
    }

    private void setSelectAccountScreenContent() {
	final Button backButton = (Button) findViewById(R.id.back);
	backButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		setScreenContent(R.layout.intro);
	    }
	});

	final Button nextButton = (Button) findViewById(R.id.next);
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
	    TextView promptText = (TextView) findViewById(R.id.select_text);
	    promptText.setText(R.string.no_accounts);
	    TextView nextText = (TextView) findViewById(R.id.click_next_text);
	    nextText.setVisibility(TextView.INVISIBLE);
	    nextButton.setEnabled(false);
	} else {
	    ListView listView = (ListView) findViewById(R.id.select_account);
	    listView.setAdapter(new ArrayAdapter<String>(this, R.layout.account, accounts));
	    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    listView.setItemChecked(mAccountSelectedPosition, true);
	}
    }

    private void setSelectLaunchModeScreenContent() {
	Button backButton = (Button) findViewById(R.id.back);
	backButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		setScreenContent(R.layout.select_account);
	    }
	});

	Button nextButton = (Button) findViewById(R.id.next);
	nextButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		storeLaunchModePreference();
		setScreenContent(R.layout.setup_complete);
	    }
	});

	setLaunchModePreferenceUI();
    }

    private void setSetupCompleteScreenContent() {
	TextView textView = (TextView) findViewById(R.id.setup_complete_text);
	textView.setText(Html.fromHtml(getString((R.string.setup_complete_text))));

	Button backButton = (Button) findViewById(R.id.back);
	backButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		setScreenContent(R.layout.select_launch_mode);
	    }
	});

	final Context context = this;
	Button finishButton = (Button) findViewById(R.id.finish);
	finishButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		SharedPreferences prefs = Prefs.get(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("savedScreenId", R.layout.connected);
		editor.commit();
		finish();
	    }
	});
    }

    private void setConnectedScreenContent() {
	SharedPreferences prefs = Prefs.get(this);
	TextView statusText = (TextView) findViewById(R.id.connected_with_account_text);
	statusText.setText(getString(R.string.connected_with_account_text) + " "
		+ prefs.getString("accountName", "error"));

	setLaunchModePreferenceUI();

	RadioGroup launchMode = (RadioGroup) findViewById(R.id.launch_mode_radio);
	launchMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	    public void onCheckedChanged(RadioGroup group, int checkedId) {
		storeLaunchModePreference();
	    }
	});

	Button disconnectButton = (Button) findViewById(R.id.disconnect);
	disconnectButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		unregister();
	    }
	});
    }

    private void storeLaunchModePreference() {
	SharedPreferences prefs = Prefs.get(this);
	SharedPreferences.Editor editor = prefs.edit();
	RadioGroup launchMode = (RadioGroup) findViewById(R.id.launch_mode_radio);
	editor.putBoolean("launchBrowserOrMaps", launchMode.getCheckedRadioButtonId() == R.id.auto_launch);
	editor.commit();
    }

    private void setLaunchModePreferenceUI() {
	SharedPreferences prefs = Prefs.get(this);
	if (prefs.getBoolean("launchBrowserOrMaps", true)) {
	    RadioButton automaticButton = (RadioButton) findViewById(R.id.auto_launch);
	    automaticButton.setChecked(true);
	} else {
	    RadioButton manualButton = (RadioButton) findViewById(R.id.manual_launch);
	    manualButton.setChecked(true);
	}
    }

    private void register(String account) {
	ProgressBar progressBar = (ProgressBar) findViewById(R.id.app_register_prog_bar);
	progressBar.setVisibility(ProgressBar.VISIBLE);
	TextView textView = (TextView) findViewById(R.id.app_register_prog_text);
	textView.setVisibility(ProgressBar.VISIBLE);

	Context context = getApplicationContext();
	final SharedPreferences prefs = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
	SharedPreferences.Editor editor = prefs.edit();
	editor.putString(Config.PREF_USER_EMAIL, account);
	editor.commit();

	C2DMessaging.register(this, Config.C2DM_SENDER);
    }

    private void unregister() {
	ProgressBar progressBar = (ProgressBar) findViewById(R.id.app_register_prog_bar);
	progressBar.setVisibility(ProgressBar.VISIBLE);
	TextView textView = (TextView) findViewById(R.id.app_register_prog_text);
	textView.setVisibility(ProgressBar.VISIBLE);

	Button disconnectButton = (Button) findViewById(R.id.app_disconnect);
	disconnectButton.setEnabled(false);

	C2DMessaging.unregister(this);
    }

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
	} else {
	    ProgressBar progressBar = (ProgressBar) findViewById(R.id.app_register_prog_text);
	    progressBar.setVisibility(ProgressBar.INVISIBLE);
	    TextView textView = (TextView) findViewById(R.id.app_register_prog_text);
	    textView.setText(status == DeviceRegistrar.AUTH_ERROR_STATUS ? R.string.auth_error_text
		    : R.string.connect_error_text);

	    Button backButton = (Button) findViewById(R.id.setup_back);
	    backButton.setEnabled(true);

	    Button nextButton = (Button) findViewById(R.id.setup_next);
	    nextButton.setEnabled(true);
	}
    }

    private void handleConnectingUpdate(int status) {
	//	ProgressBar progressBar = (ProgressBar) findViewById(R.id.app_register_prog_bar);
	//	progressBar.setVisibility(ProgressBar.INVISIBLE);

	//	TextView progTextView = (TextView) findViewById(R.id.app_register_prog_text);
	if (status == DeviceRegistrar.REGISTERED_STATUS) {
	    //	    progTextView.setText(R.string.register_progress_text_reg);
	    Log.d(Config.LOGTAG, "GeatteCanvas:handleConnectingUpdate() status : registered");
	} else if (status == DeviceRegistrar.UNREGISTERED_STATUS) {
	    //	    progTextView.setText(R.string.register_progress_text_unreg);
	    Log.d(Config.LOGTAG, "GeatteCanvas:handleConnectingUpdate() status : unregistered");
	} else {
	    //	    progTextView.setText(R.string.register_progress_text_error);
	    Log.d(Config.LOGTAG, "GeatteCanvas:handleConnectingUpdate() status : error");
	}
    }

    private void handleDisconnectingUpdate(int status) {
	if (status == DeviceRegistrar.UNREGISTERED_STATUS) {
	    setScreenContent(R.layout.intro);
	} else {
	    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
	    progressBar.setVisibility(ProgressBar.INVISIBLE);

	    TextView textView = (TextView) findViewById(R.id.disconnecting_text);
	    textView.setText(R.string.disconnect_error_text);

	    Button disconnectButton = (Button) findViewById(R.id.disconnect);
	    disconnectButton.setEnabled(true);
	}
    }

    private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    if (mScreenId == R.layout.select_account) {
		handleConnectingUpdate(intent.getIntExtra(DeviceRegistrar.STATUS_EXTRA, DeviceRegistrar.ERROR_STATUS));
	    } else if (mScreenId == R.layout.connected) {
		handleDisconnectingUpdate(intent
			.getIntExtra(DeviceRegistrar.STATUS_EXTRA, DeviceRegistrar.ERROR_STATUS));
	    }
	}
    };

    private final BroadcastReceiver mAuthPermissionReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    Bundle extras = intent.getBundleExtra("AccountManagerBundle");
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
