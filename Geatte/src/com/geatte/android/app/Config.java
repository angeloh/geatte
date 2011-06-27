package com.geatte.android.app;

public class Config {

    public static final String LOGTAG = "GeatteApp";
    public static final String LOGTAG_C2DM = "C2DM";

    public static final String ENCODE_UTF8 = "UTF-8";

    public static final Long IMAGE_BLOB_UPLOAD_BACKOFF = 300L;

    public static enum LIKE {
	YES, NO, MAYBE
    };

    private static final int LOG_LEVEL_DEBUG = 4;
    private static final int LOG_LEVEL_INFO = 3;
    private static final int LOG_LEVEL_WARNING = 2;
    private static final int LOG_LEVEL_ERROR = 1;
    private static final int LOG_LEVEL_NONE = 0;

    /**
     * Set this flag to {@link Config#GD_LOG_LEVEL_NONE} when releasing
     */
    private static final int GD_LOG_LEVEL = LOG_LEVEL_INFO;

    public static final boolean LOG_DEBUG_ENABLED = (GD_LOG_LEVEL == LOG_LEVEL_DEBUG);
    public static final boolean LOG_INFO_ENABLED = LOG_DEBUG_ENABLED || (GD_LOG_LEVEL == LOG_LEVEL_INFO);
    public static final boolean LOG_WARNING_LOGS_ENABLED = LOG_INFO_ENABLED || (GD_LOG_LEVEL == LOG_LEVEL_WARNING);
    public static final boolean LOG_ERROR_LOGS_ENABLED = LOG_WARNING_LOGS_ENABLED || (GD_LOG_LEVEL == LOG_LEVEL_ERROR);

    // public static final String INTENT_ACTION_VIEW_DETAIL =
    // "com.geatte.android.app.VIEW_DETAIL";
    // public static final String INTENT_ACTION_VIEW_LIST =
    // "com.geatte.android.app.VIEW_LIST";
    public static final String INTENT_ACTION_UPDATE_UI = "com.geatte.android.app.UPDATE_UI";
    public static final String INTENT_ACTION_AUTH_PERMISSION = "com.geatte.android.app.AUTH_PERMISSION";
    public static final String INTENT_ACTION_UPDATE_CONTACTS = "com.geatte.android.app.UPDATE_CONTACTS";

    public static final String EXTRA_MYGEATTE_STARTFROM = "mygeatte.startfrom";
    public static final String EXTRA_FRIENDGEATTE_STARTFROM = "friendgeatte.startfrom";
    public static final String EXTRA_KEY_ACCOUNT_BUNDLE = "account_manager_bundle";
    public static final String EXTRA_KEY_GEATTE_MESSAGE = "geatte_message";
    public static final String EXTRA_KEY_VOTING_COMMENT = "voting_comment";

    public static final String EXTRA_IMAGE_PATH = "image_path";
    public static final String EXTRA_IMAGE_RANDOM_ID = "image_random_id";

    public static final String EXTRA_CURRENT_TAB = "0";

    // intent actions
    public static final String ACTION_VOTING = "action_voting";
    public static final String ACTION_FEEDBACK = "action_feedback";

    // setup configs
    public static final String SAVED_SCREEN_ID = "saved_screen_id";

    // C2DM Configs
    public static final String C2DM_SENDER = "geatte@gmail.com";
    public static final String C2DM_ACCOUNT_EXTRA = "account_name";
    public static final String C2DM_MESSAGE_EXTRA = "message";
    public static final String C2DM_MESSAGE_SYNC = "sync";
    public static final String C2DM_MESSAGE_PAYLOAD = "payload";
    public static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    public static final String C2DM_MESSAGE_GEATTE_ID = "geatteid";
    // public static final String C2DM_MESSAGE_GEATTE_MESSAGE =
    // "geatte_message";
    public static final String C2DM_MESSAGE_GEATTEID_VOTE = "geatteid_vote";
    public static final String C2DM_MESSAGE_GEATTE_VOTER = "geatte_voter";
    public static final String C2DM_MESSAGE_GEATTE_VOTE_RESP = "geatte_vote_resp";
    public static final String C2DM_MESSAGE_GEATTE_VOTE_FEEDBACK = "geatte_vote_feedback";

    // prefs
    public static final String PREFERENCE_KEY = "com.geatte.android.app";
    public static final String PREF_USER_EMAIL = "userEmail";
    public static final String PREF_REGISTRATION_ID = "registrationId";
    public static final String PREF_SERVER_HAS_REG_ID = "hasRegIdOnServer";
    public static final String PREF_SELECTED_CONTACTS = "selectedContacts";
    public static final String PREF_VOTING_COMMENT = "votingComment";
    public static final String PREF_PHONE_NUMBER = "myPhoneNumber";

    // app server
    public static final String BASE_SERVER = "geatte.appspot.com";
    public static final String BASE_URL = "https://geatte.appspot.com";
    public static final String GEATTE_INFO_GET_URL = "/geatteinfoget";
    public static final String GEATTE_IMAGE_GET_URL = "/geatteimageget";
    public static final String GEATTE_REG_CHECK_URL = "/geatteregidcheck";
    public static final String GEATTE_IMAGE_BLOB_UPLOAD_URL = "/geatteimageblobupload";

    public static final String DEV_REG_ID_PARAM = "devRegId";
    public static final String DEVICE_ID_PARAM = "deviceId";
    public static final String DEV_PHONE_NUMBER_PARAM = "phoneNumber";
    public static final String DEV_PHONE_COUNTRY_ISO_PARAM = "countryCode";
    public static final String DEVICE_NAME_PARAM = "deviceName";
    public static final String DEVICE_TYPE_PARAM = "deviceType";

    public static final String GEATTE_ID_PARAM = "geatteId";
    public static final String GEATTE_FROM_NUMBER_PARAM = "fromNumber";
    public static final String GEATTE_COUNTRY_ISO_PARAM = "fromCountryCode";
    public static final String GEATTE_TO_NUMBER_PARAM = "toNumber";
    public static final String GEATTE_TITLE_PARAM = "title";
    public static final String GEATTE_DESC_PARAM = "desc";
    public static final String GEATTE_IMAGE_PARAM = "image";
    public static final String GEATTE_CREATED_DATE_PARAM = "createdDate";

    public static final String FRIEND_GEATTE_COUNTRY_ISO = "friendGeatteVoterCountryIso";
    public static final String FRIEND_GEATTE_VOTER = "friendGeatteVoter";
    public static final String FRIEND_GEATTE_VOTE_RESP = "friendGeatteVoteResp";
    public static final String FRIEND_GEATTE_FEEDBACK = "friendGeatteFeedback";

    public static final String GEATTE_IMAGE_RANDOM_ID_PARAM = "random_id";
    public static final String GEATTE_IMAGE_BLOB_PARAM = "image_blob";
    public static final String GEATTE_IMAGE_BLOB_RESP = "image_blob_resp";

    public static final String CONTACT_LIST = "contactList";
    public static final String CONTACT_DEFAULT_COUNTRY_CODE = "countryCode";
    public static final String CONTACT_PHONE_NUMBER = "phoneNumber";
    public static final String CONTACT_ID = "contactId";

    public static final String INVITE_EMAIL_SUBJECT = "Come join Geatte to share shopping feedback with me";
    public static final String INVITE_EMAIL_TEXT = "Geatte is a shopping feedback app on mobile for friends " +
    "to share information and help each other to make decision. We can share photos of want-to-have items to " +
    "anyone on your contact list who has installed Geatte. Go download Geatte free on your Android or iPhone market.";

}
