package com.geatte.android.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

public class CommentPopupWidget extends PopupWindow {

    private static final int MEASURE_AND_LAYOUT_DONE = 1 << 1;

    private final int[] mLocation = new int[2];
    private final Rect mRect = new Rect();

    private int mPrivateFlags;

    private Context mContext;

    private int mArrowOffsetY;

    private int mPopupY;
    private boolean mIsOnTop;

    private int mScreenHeight;
    private int mScreenWidth;

    /**
     * Creates a new CommentPopupWidget for the given context.
     * 
     * @param context The context in which the CommentPopupWidget is running in
     */
    public CommentPopupWidget(Context context) {
	super(context);

	mContext = context;

	initializeDefault();

	setContentView(LayoutInflater.from(mContext).inflate(R.layout.geatte_vote_comment_edit_view, null));

	setFocusable(true);
	setTouchable(true);
	setOutsideTouchable(true);
	setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
	setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

	final WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
	mScreenWidth = windowManager.getDefaultDisplay().getWidth();
	mScreenHeight = windowManager.getDefaultDisplay().getHeight();
    }

    private void initializeDefault() {
	mArrowOffsetY = mContext.getResources().getDimensionPixelSize(R.dimen.gd_arrow_offset);
    }

    /**
     * Returns the arrow offset for the Y axis.
     * 
     * @see {@link #setArrowOffsetY(int)}
     * @return The arrow offset.
     */
    public int getArrowOffsetY() {
	return mArrowOffsetY;
    }

    /**
     * Sets the arrow offset to a new value. Setting an arrow offset may be
     * particular useful to warn which view the QuickActionWidget is related to.
     * By setting a positive offset, the arrow will overlap the view given by
     * {@link #show(View)}. The default value is 5dp.
     * 
     * @param offsetY The offset for the Y axis
     */
    public void setArrowOffsetY(int offsetY) {
	mArrowOffsetY = offsetY;
    }

    /**
     * Returns the width of the screen.
     * 
     * @return The width of the screen
     */
    protected int getScreenWidth() {
	return mScreenWidth;
    }

    /**
     * Returns the height of the screen.
     * 
     * @return The height of the screen
     */
    protected int getScreenHeight() {
	return mScreenHeight;
    }

    /**
     * Call that method to display the {@link CommentPopupWidget} anchored to the
     * given view.
     * 
     * @param anchor The view the {@link CommentPopupWidget} will be anchored to.
     */
    public void show(View anchor) {
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, " CommentPopupWidget show() START");
	}

	final View contentView = getContentView();
	final SharedPreferences prefs = mContext.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);

	if (contentView == null) {
	    throw new IllegalStateException("You need to set the content view using the setContentView method");
	}

	// Replaces the background of the popup with a cleared background
	setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

	final int[] loc = mLocation;
	anchor.getLocationOnScreen(loc);
	mRect.set(loc[0], loc[1], loc[0] + anchor.getWidth(), loc[1] + anchor.getHeight());

	Button btnCancel = (Button)contentView.findViewById(R.id.voting_cancel_button);
	btnCancel.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View vv) {
		// close the popup
		dismiss();
	    }
	});
	final EditText commentEditText = (EditText) contentView.findViewById(R.id.voting_comment);
	String savedComment = prefs.getString(Config.PREF_VOTING_COMMENT, null);
	if (savedComment != null) {
	    commentEditText.setText(savedComment);
	}
	//	commentEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
	//
	//	    public void onFocusChange(View v, boolean hasFocus) {
	//		if (hasFocus == true){
	//		    update(0, 0, -1, -1);
	//		}
	//	    }
	//	});

	Button btnOk = (Button)contentView.findViewById(R.id.voting_ok_button);
	btnOk.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View vv) {
		String comment = commentEditText.getText().toString();
		if (comment != null && !comment.trim().equals("")) {
		    if(Config.LOG_DEBUG_ENABLED) {
			Log.d(Config.LOGTAG, " CommentPopupWidget return from comment typing, comment = " + comment);
		    }

		    SharedPreferences.Editor editor = prefs.edit();
		    editor.putString(Config.PREF_VOTING_COMMENT, comment);
		    editor.commit();
		}
		dismiss();
	    }
	});

	onMeasureAndLayout(mRect, contentView);

	if ((mPrivateFlags & MEASURE_AND_LAYOUT_DONE) != MEASURE_AND_LAYOUT_DONE) {
	    throw new IllegalStateException("onMeasureAndLayout() did not set the widget specification by calling"
		    + " setWidgetSpecs()");
	}

	showArrow();
	prepareAnimationStyle();
	showAtLocation(anchor, Gravity.NO_GRAVITY, 0, 18);
	//showAtLocation(anchor, Gravity.NO_GRAVITY, 0, mPopupY);
	if(Config.LOG_DEBUG_ENABLED) {
	    Log.d(Config.LOGTAG, " CommentPopupWidget show() END");
	}
    }

    protected void onMeasureAndLayout(Rect anchorRect, View contentView) {

	contentView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	contentView.measure(MeasureSpec.makeMeasureSpec(getScreenWidth(), MeasureSpec.EXACTLY),
		LayoutParams.WRAP_CONTENT);

	int rootHeight = contentView.getMeasuredHeight();

	int offsetY = getArrowOffsetY();
	int dyTop = anchorRect.top;
	int dyBottom = getScreenHeight() - anchorRect.bottom;

	boolean onTop = (dyTop > dyBottom);
	int popupY = (onTop) ? anchorRect.top - rootHeight + offsetY : anchorRect.bottom - offsetY;

	setWidgetSpecs(popupY, onTop);
    }

    protected void setWidgetSpecs(int popupY, boolean isOnTop) {
	mPopupY = popupY;
	mIsOnTop = isOnTop;

	mPrivateFlags |= MEASURE_AND_LAYOUT_DONE;
    }

    private void showArrow() {

	final View contentView = getContentView();
	final int arrowId = mIsOnTop ? R.id.gdi_arrow_down : R.id.gdi_arrow_up;
	final View arrow = contentView.findViewById(arrowId);
	final View arrowUp = contentView.findViewById(R.id.gdi_arrow_up);
	final View arrowDown = contentView.findViewById(R.id.gdi_arrow_down);

	if (arrowId == R.id.gdi_arrow_up) {
	    arrowUp.setVisibility(View.VISIBLE);
	    arrowDown.setVisibility(View.INVISIBLE);
	} else if (arrowId == R.id.gdi_arrow_down) {
	    arrowUp.setVisibility(View.INVISIBLE);
	    arrowDown.setVisibility(View.VISIBLE);
	}

	ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) arrow.getLayoutParams();
	param.leftMargin = mRect.centerX() - (arrow.getMeasuredWidth()) / 2;
    }

    private void prepareAnimationStyle() {

	final int screenWidth = mScreenWidth;
	final boolean onTop = mIsOnTop;
	final int arrowPointX = mRect.centerX();

	if (arrowPointX <= screenWidth / 4) {
	    setAnimationStyle(onTop ? R.style.GreenDroid_Animation_PopUp_Left
		    : R.style.GreenDroid_Animation_PopDown_Left);
	} else if (arrowPointX >= 3 * screenWidth / 4) {
	    setAnimationStyle(onTop ? R.style.GreenDroid_Animation_PopUp_Right
		    : R.style.GreenDroid_Animation_PopDown_Right);
	} else {
	    setAnimationStyle(onTop ? R.style.GreenDroid_Animation_PopUp_Center
		    : R.style.GreenDroid_Animation_PopDown_Center);
	}
    }

    protected Context getContext() {
	return mContext;
    }

}
