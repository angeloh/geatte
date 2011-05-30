package com.geatte.android.app;

public class Config {

    public static final String LOGTAG = "GeatteApp";
    public static final String LOGTAG_C2DM = "C2DM";

    //    public static final String INTENT_ACTION_VIEW_DETAIL = "com.geatte.android.app.VIEW_DETAIL";
    //    public static final String INTENT_ACTION_VIEW_LIST = "com.geatte.android.app.VIEW_LIST";
    public static final String INTENT_ACTION_UPDATE_UI = "com.geatte.android.app.UPDATE_UI";
    public static final String INTENT_ACTION_AUTH_PERMISSION = "com.geatte.android.app.AUTH_PERMISSION";

    public static final String EXTRA_MYGEATTE_STARTFROM = "mygeatte.startfrom";
    public static final String EXTRA_FRIENDGEATTE_STARTFROM = "friendgeatte.startfrom";
    public static final String EXTRA_KEY_ACCOUNT_BUNDLE = "account_manager_bundle";
    public static final String EXTRA_KEY_GEATTE_MESSAGE = "geatte_message";

    public static final String EXTRA_CURRENT_TAB = "0";

    //C2DM Configs
    public static final String C2DM_SENDER = "geatte@gmail.com";
    public static final String C2DM_ACCOUNT_EXTRA = "account_name";
    public static final String C2DM_MESSAGE_EXTRA = "message";
    public static final String C2DM_MESSAGE_SYNC = "sync";
    public static final String C2DM_MESSAGE_PAYLOAD = "payload";
    public static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    public static final String C2DM_MESSAGE_GEATTE_ID = "geatteid";
    //public static final String C2DM_MESSAGE_GEATTE_MESSAGE = "geatte_message";
    public static final String C2DM_MESSAGE_GEATTEID_VOTE = "geatteid_vote";
    public static final String C2DM_MESSAGE_GEATTE_VOTER = "geatte_voter";
    public static final String C2DM_MESSAGE_GEATTE_VOTE_RESP = "geatte_vote_resp";
    public static final String C2DM_MESSAGE_GEATTE_VOTE_FEEDBACK = "geatte_vote_feedback";

    // prefs
    public static final String PREFERENCE_KEY = "com.geatte.android.app";
    public static final String PREF_USER_EMAIL = "userEmail";
    public static final String PREF_REGISTRATION_ID = "registrationId";

    // app server
    public static final String BASE_URL = "https://geatte.appspot.com";
    public static final String GEATTE_INFO_GET_URL = "/geatteinfoget";
    public static final String GEATTE_IMAGE_GET_URL = "/geatteimageget";

    public static final String DEV_REG_ID_PARAM = "devRegId";
    public static final String DEVICE_ID_PARAM = "deviceId";
    public static final String DEV_PHONE_NUMBER_PARAM = "phoneNumber";
    public static final String DEVICE_NAME_PARAM = "deviceName";
    public static final String DEVICE_TYPE_PARAM = "deviceType";

    public static final String GEATTE_ID_PARAM = "geatteId";
    public static final String GEATTE_FROM_NUMBER_PARAM = "fromNumber";
    public static final String GEATTE_TO_NUMBER_PARAM = "toNumber";
    public static final String GEATTE_TITLE_PARAM = "title";
    public static final String GEATTE_DESC_PARAM = "desc";
    public static final String GEATTE_IMAGE_PARAM = "image";
    public static final String GEATTE_CREATED_DATE_PARAM = "createDate";

    public static final String FRIEND_GEATTE_VOTER = "friendGeatteVoter";
    public static final String FRIEND_GEATTE_VOTE_RESP = "friendGeatteVoteResp";
    public static final String FRIEND_GEATTE_FEEDBACK = "friendGeatteFeedback";

}
