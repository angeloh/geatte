package com.geatte.mobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GeatteApp extends Activity {
    private static final int ACTIVITY_SNAP = 0;
    private static final int ACTIVITY_CREATE = 1;
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
		startActivityForResult(intent, ACTIVITY_SNAP);
	    }
	});

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

	    Intent i = new Intent(this, GeatteEdit.class);
	    i.putExtra(GeatteDBAdapter.KEY_IMAGE_IMAGE, x);
	    startActivityForResult(i, ACTIVITY_CREATE);
	}
    }
}
