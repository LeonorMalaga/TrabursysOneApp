package mesas.martinez.leonor.tracbursys.model;



/**
 * Created by root on 16/01/15.
 */
public class Constants {
    public static final String TAG="Filtrar";
    //----------------------- KNOW FIRST TIME -------------------------------------//
    public static final String FIRST="Leonor.martinez.mesas.controller.activity.FIRST";
    public static final String FIRSTINSTALLER="Leonor.martinez.mesas.controller.activity.FIRSTINSTALLER";
    //---------------------------SETTINGS---------------------------------------//
    public static final String SERVER="server";
   // public static final String URLGET="getUrl";
    public static final String WORKMODE="work_mode_list";
    public static final String SAVEMODE="work_save_list";
    //----------------------------------Constans for DMGETPOSTIntentService-----------------------------------//
    /*Defines a custom Intent action in DMGETPOSTIntenService class to update the status*/
    public static final String INTENTSERVICE_BROADCAST_POSTHEADER=
            "Leonor.martinez.mesas.extra.INTENTSERVICE_ACTION_POSTHEADER";
    /*Defines a custom Intent action in DMGETPOSTIntenService class to update the status*/
    public static final String INTENTSERVICE_BROADCAST_GET=
            "Leonor.martinez.mesas.extra.INTENTSERVICE_ACTION_GET";
    /*Defines the key for the status "extra" in DMGETPOSTIntenService Intent*/
    public static final String INTENTSERVICE_EXTRA =
            "Leonor.martinez.mesas.extra.INTENTSERVICE_EXTRA";
//---------------------------------Constans For InstallerMode---------------------------------------------//
public static final String SPINNER_NAME="Leonor.martinez.mesas.installer.activity.SPINNER_INDEX";
//---------------------------------Constans For UserMode---------------------------------------------//
public static final String SERVICE_STATE="Leonor.martinez.mesas.user.activity.SERVICE_STATE";
public static final String SERVICE_STOP="Leonor.martinez.mesas.user.activity.SERVICE_STOP";
public static final String DEVICE_ADDRESS="Leonor.martinez.mesas.user.activity.DEVICE_ADDRESS";
public static final String DEVICE_RSSI="Leonor.martinez.mesas.user.activity.DEVICE_RSSI";
public static final String DEVICE_MESSAGE="Leonor.martinez.mesas.user.activity.DEVICE_RSSI";
public static final String BLUETOOTH_OFF="Leonor.martinez.mesas.user.activity.BLUETOOTH_OFF";
}
