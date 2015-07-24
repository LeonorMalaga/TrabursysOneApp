package mesas.martinez.leonor.tracbursys.model;



/**
 * Created by root on 16/01/15.
 */
public class Constants {
    //----------------------- KNOW FIRST TIME -------------------------------------//
    public static final String FIRST="com.m2m.ericsson.controller.activity.FIRST";
    public static final String FIRSTINSTALLER="com.m2m.ericsson.controller.activity.FIRSTINSTALLER";
    //---------------------------SETTINGS---------------------------------------//
    public static final String SERVER="server";
   // public static final String URLGET="getUrl";
    public static final String WORKMODE="work_mode_list";
    public static final String SAVEMODE="work_save_list";
    //----------------------------------Constans for DMGETPOSTIntentService-----------------------------------//
    /*Defines a custom Intent action in DMGETPOSTIntenService class to update the status*/
    public static final String INTENTSERVICE_BROADCAST_POSTHEADER=
            "com.presencecontrol.m2m.m2m_presencecontrol.extra.INTENTSERVICE_ACTION_POSTHEADER";
    /*Defines a custom Intent action in DMGETPOSTIntenService class to update the status*/
    public static final String INTENTSERVICE_BROADCAST_GET=
            "com.presencecontrol.m2m.m2m_presencecontrol.extra.INTENTSERVICE_ACTION_GET";
    /*Defines the key for the status "extra" in DMGETPOSTIntenService Intent*/
    public static final String INTENTSERVICE_EXTRA =
            "com.presencecontrol.m2m.m2m_presencecontrol.extra.INTENTSERVICE_EXTRA";
//---------------------------------Constans For InstallerMode---------------------------------------------//
public static final String SPINNER_NAME="com.m2m.ericsson.controller.activity.SPINNER_INDEX";
//---------------------------------Constans For UserMode---------------------------------------------//
public static final String SERVICE_STATE="com.m2m.ericsson.controller.activity.SERVICE_STATE";
public static final String DEVICE_ADDRESS="com.m2m.ericsson.controller.activity.DEVICE_ADDRESS";
public static final String DEVICE_RSSI="com.m2m.ericsson.controller.activity.DEVICE_RSSI";
}
