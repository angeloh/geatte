package com.geatte.android.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.geatte.android.c2dm.C2DMessaging;
import com.geatte.android.app.R;
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

public class GeatteApp extends Activity {

    private static final String CLASSTAG = GeatteApp.class.getSimpleName();
    private static final int ACTIVITY_SNAP = 0;
    private static final int ACTIVITY_CREATE = 1;

    private boolean mPendingAuth = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.geatte_app);

	Button cameraButton = (Button) findViewById(R.id.app_snap_button);
	cameraButton.setOnClickListener( new OnClickListener(){
	    public void onClick(View v ){
		//Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
		//intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
		startActivityForResult(intent, ACTIVITY_SNAP);
	    }
	});

	Button nextButton = (Button) findViewById(R.id.app_register_btn);
	nextButton.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		setRegisterView();
	    }
	});

	registerReceiver(mUpdateUIReceiver, new IntentFilter(Config.INTENT_ACTION_UPDATE_UI));
	registerReceiver(mAuthPermissionReceiver, new IntentFilter(Config.INTENT_ACTION_AUTH_PERMISSION));

	/*	        // custom button
	        final Button button = (Button) findViewById(R.id.button);
	        button.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                // Perform action on clicks
	                Toast.makeText(GeatteApp.this, "Beep Bop", Toast.LENGTH_SHORT).show();
	            }
	        });

	        // edit text
	        final EditText edittext = (EditText) findViewById(R.id.edittext);
	        edittext.setOnKeyListener(new OnKeyListener() {
	            public boolean onKey(View v, int keyCode, KeyEvent event) {
	                // If the event is a key-down event on the "enter" button
	                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
	                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
	                  // Perform action on key press
	                  Toast.makeText(GeatteApp.this, edittext.getText(), Toast.LENGTH_SHORT).show();
	                  return true;
	                }
	                return false;
	            }
	        });

	        // check box
	        final CheckBox checkbox = (CheckBox) findViewById(R.id.checkbox);
	        checkbox.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                // Perform action on clicks, depending on whether it's now checked
	                if (((CheckBox) v).isChecked()) {
	                    Toast.makeText(GeatteApp.this, "Selected", Toast.LENGTH_SHORT).show();
	                } else {
	                    Toast.makeText(GeatteApp.this, "Not selected", Toast.LENGTH_SHORT).show();
	                }
	            }
	        });

	        // toggle button
	        final ToggleButton togglebutton = (ToggleButton) findViewById(R.id.togglebutton);
	        togglebutton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                // Perform action on clicks
	                if (togglebutton.isChecked()) {
	                    Toast.makeText(GeatteApp.this, "Checked", Toast.LENGTH_SHORT).show();
	                } else {
	                    Toast.makeText(GeatteApp.this, "Not checked", Toast.LENGTH_SHORT).show();
	                }
	            }
	        });

	        //rating
	        final RatingBar ratingbar = (RatingBar) findViewById(R.id.ratingbar);
	        ratingbar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
	            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
	                Toast.makeText(GeatteApp.this, "New Rating: " + rating, Toast.LENGTH_SHORT).show();
	            }
	        });*/

    }

    @Override
    protected void onResume() {
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
    }

    @Override
    public void onDestroy() {
	unregisterReceiver(mUpdateUIReceiver);
	super.onDestroy();
    }

    private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    handleConnectingUpdate(intent.getIntExtra(
		    DeviceRegistrar.STATUS_EXTRA, DeviceRegistrar.ERROR_STATUS));
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

    private void setRegisterView() {
	final Button nextButton = (Button) findViewById(R.id.app_register_btn);

	// Display accounts
	final String accounts[] = getGoogleAccounts();
	if (accounts.length == 0) {
	    //	    TextView promptText = (TextView) findViewById(R.id.select_text);
	    //	    promptText.setText(R.string.no_accounts);
	    //	    TextView nextText = (TextView) findViewById(R.id.click_next_text);
	    //	    nextText.setVisibility(TextView.INVISIBLE);
	    nextButton.setEnabled(false);
	    Log.d(Config.LOGTAG_C2DM, " " + GeatteApp.CLASSTAG + " No google accounts available!!");

	} else {
	    //	    ListView listView = (ListView) findViewById(R.id.select_account);
	    //	    listView.setAdapter(new ArrayAdapter<String>(this,
	    //		    R.layout.account, accounts));
	    //	    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    //	    listView.setItemChecked(mAccountSelectedPosition, true);
	    Log.d(Config.LOGTAG_C2DM, " " + GeatteApp.CLASSTAG + " google account available : " + accounts[0]);

	    nextButton.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		    register((String) accounts[0]);
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

    private void register(String userEmail) {
	ProgressBar progressBar = (ProgressBar) findViewById(R.id.app_register_prog_bar);
	progressBar.setVisibility(ProgressBar.VISIBLE);
	TextView textView = (TextView) findViewById(R.id.app_register_prog_text);
	textView.setVisibility(ProgressBar.VISIBLE);

	Context context = getApplicationContext();
	final SharedPreferences prefs = context.getSharedPreferences(
		Config.PREFERENCE_KEY,
		Context.MODE_PRIVATE);

	SharedPreferences.Editor editor = prefs.edit();
	editor.putString(Config.PREF_USER_EMAIL, userEmail);
	editor.commit();

	C2DMessaging.register(this, Config.C2DM_SENDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode== 0 && resultCode == Activity.RESULT_OK){
	    Bitmap x = (Bitmap) data.getExtras().get("data");
	    //((ImageView)findViewById(R.id.edit_img)).setImageBitmap(x);
	    /*	    ContentValues values = new ContentValues();
	    values.put(Images.Media.TITLE, "title");
	    values.put(Images.Media.BUCKET_ID, "test");
	    values.put(Images.Media.DESCRIPTION, "test Image taken");
	    values.put(Images.Media.MIME_TYPE, "image/jpeg");
	    Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
	    OutputStream outstream;
	    try {
		outstream = getContentResolver().openOutputStream(uri);
		x.compress(Bitmap.CompressFormat.JPEG, 70, outstream);
		outstream.close();
	    } catch (FileNotFoundException e) {
		//
	    }catch (IOException e){
		//
	    }*/
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
	    Log.w(Config.LOGTAG, " " + GeatteApp.CLASSTAG + " Exception :" , e);
	}
	return null;
    }
}
