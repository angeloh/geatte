package com.geatte.android.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.geatte.android.c2dm.C2DMessaging;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GeatteCanvas extends GDActivity {
    private static final String CLASSTAG = GeatteCanvas.class.getSimpleName();
    private static final int ACTIVITY_SNAP = 0;
    private static final int ACTIVITY_CREATE = 1;
    private static final int ACTIVITY_ALL_FEEDBACK = 2;
    private static final int ACTIVITY_SHOW_GEATTETAB = 3;
    private boolean mPendingAuth = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	// setContentView(R.layout.geatte_app);

	// setup google account and save to context prefs
	getGoogleAccount();

	setActionBarContentView(R.layout.geatte_canvas);
	//setActionBarContentView(R.layout.geatte_app);

	addActionBarItem(Type.Info);

	Button cameraButton = (Button) findViewById(R.id.app_snap_button);
	cameraButton.setOnClickListener( new OnClickListener(){
	    public void onClick(View v ){
		//Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
		//intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
		startActivityForResult(intent, ACTIVITY_SNAP);
	    }
	});

	Button showAllFeedbackButton = (Button) findViewById(R.id.app_show_all_feedback_btn);
	showAllFeedbackButton.setOnClickListener( new OnClickListener(){
	    public void onClick(View v ){
		Intent i = new Intent(GeatteCanvas.this.getApplicationContext(), GeatteAllFeedbackActivity.class);
		startActivity(i);
	    }
	});

	Button showGeatteTab = (Button) findViewById(R.id.app_show_geattetab_btn);
	showGeatteTab.setOnClickListener( new OnClickListener(){
	    public void onClick(View v ){
		Intent i = new Intent(GeatteCanvas.this, GeatteTabActivity.class);
		startActivity(i);
	    }
	});

	Button registerButton = (Button) findViewById(R.id.app_register_btn);
	registerButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		setRegisterView();
	    }
	});

	registerReceiver(mUpdateUIReceiver, new IntentFilter(Config.INTENT_ACTION_UPDATE_UI));
	registerReceiver(mAuthPermissionReceiver, new IntentFilter(Config.INTENT_ACTION_AUTH_PERMISSION));
    }

    //TODO show information
    public void onShowInfo(View v) {

    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

	switch (position) {
	case 0:
	    onShowInfo(item.getItemView());
	    break;

	default:
	    return super.onHandleActionBarItemClick(item, position);
	}

	return true;
    }

    @Override
    protected void onResume() {
	Log.d(Config.LOGTAG_C2DM, "GeatteApp:onResume() START");
	super.onResume();
	if (mPendingAuth) {
	    mPendingAuth = false;
	    String regId = C2DMessaging.getRegistrationId(this);
	    if (regId != null && !regId.equals("")) {
		DeviceRegistrar.registerWithServer(this, regId);
	    } else {
		C2DMessaging.register(this, Config.C2DM_SENDER);
	    }
	}
	Log.d(Config.LOGTAG_C2DM, "GeatteApp:onResume() END");
    }

    @Override
    public void onDestroy() {
	unregisterReceiver(mUpdateUIReceiver);
	unregisterReceiver(mAuthPermissionReceiver);
	super.onDestroy();
    }


    private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    handleConnectingUpdate(intent.getIntExtra(
		    DeviceRegistrar.STATUS_EXTRA, DeviceRegistrar.ERROR_STATUS));
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

    private void handleConnectingUpdate(int status) {
	ProgressBar progressBar = (ProgressBar) findViewById(R.id.app_register_prog_bar);
	progressBar.setVisibility(ProgressBar.INVISIBLE);

	TextView progTextView = (TextView) findViewById(R.id.app_register_prog_text);
	if (status == DeviceRegistrar.REGISTERED_STATUS) {
	    progTextView.setText(R.string.register_progress_text_reg);
	} else if (status == DeviceRegistrar.UNREGISTERED_STATUS) {
	    progTextView.setText(R.string.register_progress_text_unreg);
	} else {
	    progTextView.setText(R.string.register_progress_text_error);

	}
    }

    private boolean getGoogleAccount() {
	// Display accounts
	final String accounts[] = getGoogleAccounts();
	if (accounts.length == 0) {
	    //	    TextView promptText = (TextView) findViewById(R.id.select_text);
	    //	    promptText.setText(R.string.no_accounts);
	    //	    TextView nextText = (TextView) findViewById(R.id.click_next_text);
	    //	    nextText.setVisibility(TextView.INVISIBLE);
	    Log.d(Config.LOGTAG_C2DM, " " + GeatteCanvas.CLASSTAG + " No google accounts available!!");
	    return false;

	} else {
	    //	    ListView listView = (ListView) findViewById(R.id.select_account);
	    //	    listView.setAdapter(new ArrayAdapter<String>(this,
	    //		    R.layout.account, accounts));
	    //	    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    //	    listView.setItemChecked(mAccountSelectedPosition, true);
	    Log.d(Config.LOGTAG_C2DM, " " + GeatteCanvas.CLASSTAG + " google account available : " + accounts[0]);

	    Context context = getApplicationContext();
	    final SharedPreferences prefs = context.getSharedPreferences(
		    Config.PREFERENCE_KEY,
		    Context.MODE_PRIVATE);

	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(Config.PREF_USER_EMAIL, accounts[0]);
	    editor.commit();
	    return true;
	}
    }

    private void setRegisterView() {
	final Button regButton = (Button) findViewById(R.id.app_register_btn);

	final SharedPreferences prefs = getApplicationContext().getSharedPreferences(Config.PREFERENCE_KEY,
		Context.MODE_PRIVATE);
	String accountName = prefs.getString(Config.PREF_USER_EMAIL, null);

	if (accountName == null) {
	    regButton.setEnabled(false);
	    Log.d(Config.LOGTAG_C2DM, " " + GeatteCanvas.CLASSTAG + ":setRegisterView() : No google account '"
		    + Config.PREF_USER_EMAIL + "' in prefs");
	    //TODO forward user to add account

	} else {
	    Log.d(Config.LOGTAG_C2DM, " " + GeatteCanvas.CLASSTAG + ":setRegisterView() : user prefs google account "
		    + Config.PREF_USER_EMAIL + " = " + accountName);

	    regButton.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		    register();
		}
	    });
	}

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

    private void register() {
	ProgressBar progressBar = (ProgressBar) findViewById(R.id.app_register_prog_bar);
	progressBar.setVisibility(ProgressBar.VISIBLE);
	TextView textView = (TextView) findViewById(R.id.app_register_prog_text);
	textView.setVisibility(ProgressBar.VISIBLE);
	C2DMessaging.register(this, Config.C2DM_SENDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode == ACTIVITY_SNAP && resultCode == Activity.RESULT_OK){
	    Bitmap x = (Bitmap) data.getExtras().get("data");
	    String path = saveToFile(x);
	    Intent i = new Intent(this, GeatteEdit.class);
	    i.putExtra(GeatteDBAdapter.KEY_IMAGE_PATH, path);
	    startActivityForResult(i, ACTIVITY_CREATE);
	}
    }

    public String saveToFile(Bitmap bitmap) {
	String filename;
	Date date = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	filename = sdf.format(date);

	try {
	    String path = Environment.getExternalStorageDirectory().toString();
	    OutputStream fOut = null;
	    File dir = new File(path, "/geatte/images/");
	    if (!dir.isDirectory()) {
		dir.mkdirs();
	    }

	    File file = new File(dir, filename + ".jpg");

	    fOut = new FileOutputStream(file);

	    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
	    fOut.flush();
	    fOut.close();

	    MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file
		    .getName());
	    return file.getAbsolutePath();
	} catch (Exception e) {
	    Log.w(Config.LOGTAG, " " + GeatteCanvas.CLASSTAG + " Exception :" , e);
	}
	return null;
    }
}