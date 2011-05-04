package com.geatte.android.app;

public class Config {

    public static final String LOGTAG = "GeatteApp";
    public static final String LOGTAG_C2DM = "C2DM";

    public static final String INTENT_ACTION_VIEW_DETAIL = "com.geatte.android.app.VIEW_DETAIL";
    public static final String INTENT_ACTION_VIEW_LIST = "com.geatte.android.app.VIEW_LIST";
    public static final String INTENT_ACTION_UPDATE_UI = "com.geatte.android.app.UPDATE_UI";
    public static final String INTENT_ACTION_AUTH_PERMISSION = "com.geatte.android.app.AUTH_PERMISSION";

    public static final String EXTRA_KEY_ACCOUNT_BUNDLE = "account_manager_bundle";

    /**
     * C2DM Configs
     */
    public static final String C2DM_SENDER = "geatte@gmail.com";
    public static final String C2DM_ACCOUNT_EXTRA = "account_name";
    public static final String C2DM_MESSAGE_EXTRA = "message";
    public static final String C2DM_MESSAGE_SYNC = "sync";
    public static final String GOOGLE_ACCOUNT_TYPE = "com.google";

    // prefs
    public static final String PREFERENCE_KEY = "com.geatte.android.app";
    public static final String PREF_USER_EMAIL = "userEmail";
    public static final String PREF_REGISTRATION_ID = "registrationId";

}
