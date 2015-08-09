package mesas.martinez.leonor.tracbursys.Services;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mesas.martinez.leonor.tracbursys.comunication.HTTP_JSON_POST;
import mesas.martinez.leonor.tracbursys.model.Constants;
import mesas.martinez.leonor.tracbursys.model.Device;
import mesas.martinez.leonor.tracbursys.model.DeviceDAO;
import mesas.martinez.leonor.tracbursys.model.OrionJsonManager;

/**
 * Created by root on 5/08/15.
 */
public class SpeechBluService extends IntentService implements BluetoothAdapter.LeScanCallback, TextToSpeech.OnInitListener{

//------------------Variables---------------------//
private static boolean start;
private SharedPreferences sharedPrefs;
private OrionJsonManager jsonManager;
//--to-speak-Variables/Contans,enums---//
private String toSpeak;
private TextToSpeech tts=null;
    
//----Bluetooth-Variables/Contans,enums--//    
public static enum State {
    UNKNOWN,
    WAIT,
    SCANNING,
    BLUETOOTH_OFF,
    CONNECTING,
    DISCONNECTING
}

    private static final long SCAN_TIMEOUT = 5000;
    private static final long WAIT_PERIOD = 10000;
    private final List<Messenger> mClients = new LinkedList<Messenger>();
    private final Map<String, BluetoothDevice> mDevices = new HashMap<String, BluetoothDevice>();

    private String address;
    private String string_rssi;
    private String old_address;
    private String old_string_rssi;
    private BluetoothAdapter mBluetoothAdapter= null;
    private State mState;
    private Handler mHandler;

   //-----DataBase-Variables--//
   private DeviceDAO deviceDAO;
   private Device deviceaux;

    public SpeechBluService() {
        super(SpeechBluService.class.getName());
       // this.setState(State.UNKNOWN);
        mHandler = new Handler();
        start = true;
    }

    //-----------------------------------------------Main-Method---------------------------//
    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.DEVICE_MESSAGE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.SERVICE_STOP));
//to can reproduce the messages
        toSpeak = " ";
        //if(tts==null){
        tts = new TextToSpeech(this, this);
        tts.setSpeechRate(0.5f);//}
        
 //Start detect i-beacons
        while (start) {
            try {
                synchronized (this) {
                    Log.d("onHandleIntent WHILE", "Start Scan");
                    this.startScan();
                    this.wait(WAIT_PERIOD);
                }
            } catch (InterruptedException e) {
                Log.d("InterrupteException in While", "------------STOP----------");
                start = false;
            }
        }
        
    }
    //---------------------------------------Methods and subclass--------------------------------//
  public void setState(State state){
      mState = state;
      //Log.d("--Service--Set State--",mState.name());
      String share=Constants.SERVICE_STATE.toString();
      SharedPreferences config=this.getSharedPreferences(share,MODE_MULTI_PROCESS);
      config.edit()
              .putString(Constants.SERVICE_STATE, mState.name())
              .commit();

  }

    protected void mstop(){
        Log.d("---STOP SERVICE--"," "+start);

        if(start!=false){
            start = false;
            mBluetoothAdapter.stopLeScan(SpeechBluService.this);
            this.setState(State.DISCONNECTING);
            // Do Not forget to Stop the TTS Engine when you do not require it
            if (tts != null) {
                tts.stop();
                tts.shutdown();
                tts=null;
            }
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            this.stopSelf();//Stop service
            this.onDestroy();
        }
        
    }

    private BroadcastReceiver mReceiver=new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            Log.i("-----------INTENT received-------------", action);
            switch(action) {
                case Constants.DEVICE_MESSAGE:
                    toSpeak = intent.getStringExtra("message");
                    speakTheText(toSpeak);
                    break;
                case Constants.SERVICE_STOP:
                    SpeechBluService.this.mstop();
                    Log.e("-----------INTENT received-------------","---STOP SERVICE--");                    
                    break;
                default:
                    Log.e("-----------INTENT received-------------", action+"---not catched--");
                    break;
            }
        }
    };
    
    //------------------to-Speak--Methods---------------------------//
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "This Language is not supported", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Ready to Speak", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(this, "Can Not Speak", Toast.LENGTH_LONG).show();
        }

    }

    private void speakTheText(String textToSpeak) {
        //Log.v("--SPEAKtheTEXT---", textToSpeak);
        if(!textToSpeak.equals(" ")){
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);}
    }

    @Override
    public void onDestroy() {
        Log.d("--OnDetroy---", "NewSpeechBluService");
        this.mstop();
        super.onDestroy();
    }

    //------------------------BLU--Methods---------------------------//


    private void start() {
           // Log.d("startScan:", "------------------Star-----------------------\n\n\n ");
// scan for SCAN_TIMEOUT
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SpeechBluService.this.setState(State.WAIT);
                    mBluetoothAdapter.stopLeScan(SpeechBluService.this);
                }
            }, SCAN_TIMEOUT);
            SpeechBluService.this.setState(State.SCANNING);
            mBluetoothAdapter.startLeScan(SpeechBluService.this);

     }


    private void startScan() throws InterruptedException {
        mDevices.clear();

        if (mBluetoothAdapter== null) {
            final BluetoothManager BluetoothManager = (android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = BluetoothManager.getAdapter();
        }
        if (mBluetoothAdapter== null || !mBluetoothAdapter.isEnabled()) {
            this.setState(State.BLUETOOTH_OFF);
            Toast.makeText(this, "Please turn On your Bluetooth", Toast.LENGTH_LONG).show();
            this.wait(100);
            //please trun on Bluetthoth sensor
            Intent intent = new Intent(Constants.BLUETOOTH_OFF);
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
            start=false;
        } else {
            SpeechBluService.this.start();
        }
    }

//Method from LeScanCallBack
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        address = device.getAddress().toString();
        int auxint=rssi;
        string_rssi = String.valueOf(auxint);
        // Log.v("---DEVICE FAUND---", address + ", rss1=" + string_rssi);


//        deviceDAO = new DeviceDAO(getApplicationContext());
//        deviceDAO.open();
//        //try {

        jsonManager=new OrionJsonManager() ;
        //Log.d("-OnLeScan:-----","After new OrionJsonManager");
        String jsonString=jsonManager.SetJSONtoGetMessage("BLE", address);
       // Log.d("-OnLeScan:-----","After SetJsonManager");
        old_address = sharedPrefs.getString(Constants.DEVICE_ADDRESS, "0");
       // Log.d("-OnLeScan:-----","Compare"+old_address+"=="+address+"-->"+(old_address.equals(address)));
            //If It detected a new device
            if (!old_address.equals(address)) {
                Log.d("-OnLeScan:-----","If");
                //deviceaux = deviceDAO.getDeviceByAddress(device.getAddress().toString());
                //Obtain text to server

                //String query="/ngsi10/updateContext";

                new HTTP_JSON_POST(this, jsonManager,address).execute();
               // Log.d("--OnLeScan:----HTTP_JSON_POST.execute----:", toSpeak);
//                //The message will be receiver in the BroadcastReceiver.
//                //toSpeak = deviceaux.getDeviceSpecification();
//
//                //Update database
//                //if The server not respond, tray to obtain tex to database
//                //toSpeak = deviceaux.getDeviceSpecification();
//
//                //speakTheText(toSpeak);
//                //set the value of the last device detected
//                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
//                        .edit()
//                        .putString(Constants.DEVICE_ADDRESS, address)
//                        .commit();
//                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
//                        .edit()
//                        .putString(Constants.DEVICE_RSSI, string_rssi)
//                        .commit();
//
//                //If It detected the same device again
            } else {
                Log.d("-OnLeScan:-----","Else");

                old_string_rssi = sharedPrefs.getString(Constants.DEVICE_RSSI, "0");
                Integer auxold = Integer.decode(old_string_rssi);
                Integer aux = Integer.decode(string_rssi);
                Integer rest = auxold - aux;
//                //int primitiverest = rest.intValue();
                //Log.d("-OnLeScan:------"+auxold+"-"+aux+"--->: "," "+rest);
                if (rest > 10 || rest < -10) {
////                    Log.d(TAG, "------device Detecte AGAIN---to Speak---:" + toSpeak);
////
////                    deviceaux = deviceDAO.getDeviceByAddress(device.getAddress().toString());
////                    toSpeak = deviceaux.getDeviceSpecification();
                   // speakTheText(toSpeak);
                    new HTTP_JSON_POST(this, jsonManager,address).execute();
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putString(Constants.DEVICE_ADDRESS, address)
                            .commit();
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putString(Constants.DEVICE_RSSI, string_rssi)
                            .commit();
                }
            }
//
//        //} //catch (Exception e) {
//          //  Log.d("OnLeScan:", "------BAD ADREESS--------");
//        //}
//        deviceDAO.close();
//        //}
    }

}