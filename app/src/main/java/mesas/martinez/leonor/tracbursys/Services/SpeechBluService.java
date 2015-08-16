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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mesas.martinez.leonor.tracbursys.R;
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
 //-------------for accelerometer----------//
 private Accelerometer a;
 private int min_timesensitivity = 100000000;
 private int time_sensitivity;
 private float min_movement;
 private ArrayList<BluetoothDevice> mDevicesArray;
//----Bluetooth-Variables/Contans,enums--//    
public static enum State {
    UNKNOWN,
    WAIT,
    WAIT_RESPONSE,
    SCANNING,
    BLUETOOTH_OFF,
    CONNECTING,
    DISCONNECTING
}

    private static final long SCAN_TIMEOUT = 2000;
    private static final long WAIT_PERIOD = 5000;
    private final List<Messenger> mClients = new LinkedList<Messenger>();
    private final Map<String, BluetoothDevice> mDevices = new HashMap<String, BluetoothDevice>();

    private String address;
    private String device_name;
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
        mState=State.CONNECTING;
        start = true;
        tts=null;
    }

    //-----------------------------------------------Main-Method---------------------------//
    @Override
    protected void onHandleIntent(Intent intent) {
        //to can reproduce the messages
        toSpeak = " ";
        //if(tts==null){
            Log.i("Create TexToSpeech", "new tts");
            tts = new TextToSpeech(getBaseContext(),this);
            tts.setSpeechRate(0.5f);
    //}

        mDevicesArray= new ArrayList<BluetoothDevice>();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.DEVICE_MESSAGE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.SERVICE_STOP));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.SERVICE_UNKNOWN_STATE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.SERVICE_WAIT_RESPONSE));
        try {
        this.startScan();
        } catch (InterruptedException e) {
            Log.d("InterrupteException in While", "------------STOP----------");
            //start = false;
        }

 //Start detect i-beacons
        while (start) {
            try {
                synchronized (this) {
                    Log.d("---onHandleIntent WHILE---", "Start Scan");
                    this.startScan();
                    this.wait(WAIT_PERIOD*2);}
            } catch (InterruptedException e) {
                Log.d("InterruptedException in While", "------------STOP----------");
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
            // Stop the TTS Engine when you do not require it
            if (tts != null) {
                tts.stop();
                tts.shutdown();
                tts=null;
            }
            //Stop accelerometer

            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            this.stopSelf();//Stop service
            this.onDestroy();
        }
        
    }

    private BroadcastReceiver mReceiver=new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            switch(action) {
                case Constants.DEVICE_MESSAGE:
                    toSpeak = intent.getStringExtra("message");
                    Log.i("-----------INTENT received-------------","---DEVICE_MESSAGE--"+toSpeak);
                    if(!toSpeak.equals(null))
                        SpeechBluService.this.speakTheText( );
                    break;
                case Constants.SERVICE_STOP:
                    SpeechBluService.this.mstop();
                    Log.i("-----------INTENT received-------------","---STOP SERVICE--");
                    break;
                case Constants.SERVICE_WAIT_RESPONSE:
                    SpeechBluService.this.setState(State.WAIT_RESPONSE);
                    Log.i("-----------INTENT received-------------","--SERVICE_WAIT_RESPONSE--");
                    break;
                case Constants.SERVICE_UNKNOWN_STATE:
                    SpeechBluService.this.setState(State.UNKNOWN);
                    Log.i("-----------INTENT received-------------","---SERVICE_UNKNOWN_STATE--");
                    break;
                default:
                    Log.i("-----------INTENT received-------------", action+"---not catched--");
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

    protected void speakTheText( ) {
        //Log.v("--SPEAKtheTEXT---", textToSpeak);
        //if(!textToSpeak.equals(" ") && tts!=null){
        tts.speak(SpeechBluService.this.toSpeak, TextToSpeech.QUEUE_FLUSH, null);//}
    }

    @Override
    public void onDestroy() {
        Log.d("--OnDetroy---", "SpeechBluService");
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
                    //SpeechBluService.this.setState(State.WAIT);
                    mBluetoothAdapter.stopLeScan(SpeechBluService.this);
                }
            }, SCAN_TIMEOUT);
        //Wait for HTTP_JSON_POST end, before scan again
        if((!mState.equals(State.SCANNING)) && (!mState.equals(State.WAIT_RESPONSE)) ){
//                        Log.i("---onHandleIntent WHILE--", "WAIT FOR STATE CHANGE");
            SpeechBluService.this.setState(State.SCANNING);
            mBluetoothAdapter.startLeScan(SpeechBluService.this);
        }

     }


    private void startScan() throws InterruptedException {
        if (mBluetoothAdapter== null) {
            final BluetoothManager BluetoothManager = (android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = BluetoothManager.getAdapter();
        }
        if (mBluetoothAdapter== null || !mBluetoothAdapter.isEnabled()) {
            Log.i("starScan","BLUETOOH is OFF");
            this.setState(State.BLUETOOTH_OFF);
            String turn_on=getResources().getString(R.string.turn_on_Bluetooth);
            Toast.makeText(this, turn_on, Toast.LENGTH_LONG).show();
            this.wait(Constants.WAIT_TIME);
            //please trun on Bluetthoth sensor
            Intent intent = new Intent(Constants.BLUETOOTH_OFF);
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
            start=false;
        } else {
            mDevices.clear();
            SpeechBluService.this.start();
        }
    }

//Method from LeScanCallBack
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

        address = device.getAddress().toString();
        int auxint=rssi;
        string_rssi = String.valueOf(auxint);
        device_name=device.getName();
        jsonManager=new OrionJsonManager() ;
        //String jsonString=jsonManager.SetJSONtoGetMessage("BLE", address);
        String jsonString=jsonManager.SetJSONtoGetAttributes("BLE", address,getApplicationContext());
        jsonManager.setDeviceName(device_name);
        //Log.d("-OnLeScan:-----","After SetJsonManager "+jsonString);
       // old_address = sharedPrefs.getString(Constants.DEVICE_ADDRESS, "0");
       // Log.d("-OnLeScan:-----","Compare"+old_address+"=="+address+"-->"+(old_address.equals(address)));
            //If It detected a new device
//        if(!mDevicesArray.contains(device)){
//            mDevicesArray.add(device);
            //if (!old_address.equals(address)) {
                Log.d("OnLeScan","----New Device--- address: "+ address+ " rssi "+string_rssi);
                new HTTP_JSON_POST(this, jsonManager,address).execute();

//            } else {
//                Log.d("OnLeScan","----Detected device again----");
//
//                old_string_rssi = sharedPrefs.getString(Constants.DEVICE_RSSI, "0");
//                Integer auxold = Integer.decode(old_string_rssi);
//                Integer aux = Integer.decode(string_rssi);
//                Integer rest = auxold - aux;
////                //int primitiverest = rest.intValue();
//                //Log.d("-OnLeScan:------"+auxold+"-"+aux+"--->: "," "+rest);
//                if (rest > 10 || rest < -10) {
//////                    Log.d(TAG, "------device Detecte AGAIN---to Speak---:" + toSpeak);
//////
//////                    deviceaux = deviceDAO.getDeviceByAddress(device.getAddress().toString());
//////                    toSpeak = deviceaux.getDeviceSpecification();
//                   // speakTheText(toSpeak);
//                    new HTTP_JSON_POST(this, jsonManager,address).execute();
//                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
//                            .edit()
//                            .putString(Constants.DEVICE_ADDRESS, address)
//                            .commit();
//                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
//                            .edit()
//                            .putString(Constants.DEVICE_RSSI, string_rssi)
//                            .commit();
//                }
//            }

    }
    //---------------To now the movement direction-------------//
    class Accelerometer implements SensorEventListener {

        private long last_update = 0, last_movement = 0;
        private float prevX = 0, prevY = 0, prevZ = 0;
        private float curX = 0, curY = 0, curZ = 0;
        private float movement;
        private int min_timesensitivity = 100000000;
        private int time_sensitivity;
        //float base_movement = 1E-6f;
        private float min_movement=11;
        private long time_difference=2;
        private long current_time;

        public int getTime_sensitivity() {
            return time_sensitivity;
        }

        public void setTime_sensitivity(int time_sensitivity) {
            this.time_sensitivity = time_sensitivity;
        }

        public float getMin_movement() {
            return min_movement;
        }

        public void setMin_movement(float min_movement) {
            this.min_movement = min_movement;
        }

        public long getTime_difference() {
            return time_difference;
        }

        public long getCurrent_time() {
            return current_time;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(this);
            return super.clone();
        }

        Accelerometer() {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (sensors.size() > 0) {
                sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_UI);
            }
        }

        public boolean RangeOfTime() {
            boolean response = false;
            //*5.0 because discober devices is slow that accelerometer
            if (time_difference < (time_sensitivity)) {
                response = true;
            }
            Log.d(Constants.TAG, "-----RANGE OF TIMER------" + response + "--" + time_difference + " < " + (time_sensitivity));
            return response;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            synchronized (this) {
                current_time = event.timestamp;
                curX = event.values[0];
                curY = event.values[1];
                curZ = event.values[2];
//initialization of variables
                if (prevX == 0 && prevY == 0 && prevZ == 0) {
                    last_update = current_time;
                    last_movement = current_time;
                    prevX = curX;
                    prevY = curY;
                    prevZ = curZ;
                }
                time_difference = current_time - last_movement;

//           Log.v(Constants.TAG, " --Time--"+current_time+"---------DIFFER TIME----------"+time_difference);
                if (time_difference > time_sensitivity) {
                    movement = (Math.abs(curX - prevX) + Math.abs(curY - prevY) + Math.abs(curZ - prevZ));
                    // Log.v(Constants.TAG, "\n\n-----Movement0------"+movement+" > "+min_movement+" --Time--"+current_time+"---------DIFFER TIME----------"+time_difference);
                    if (movement > min_movement) {
                        last_movement = current_time;
                        Log.v(Constants.TAG, "\n\n-----Movement1------" + movement + " > " + min_movement + " --Time--" + current_time + "---------DIFFER TIME----------" + time_difference);
                        prevX = curX;
                        prevY = curY;
                        prevZ = curZ;
                    }
                }
            }
        }

    }

    //---------------------Fin Accelerometer-------------------//
    //---------------------Device Aux-------------------------//
    private class Deviceaux {
        private int state;
        private int count;
        private double outOfRegion;
        private double dBmAverage;
        private double lastdBmAverage;
        private String address;
        private String text;

        Deviceaux(double dBmAverage, String address, String text) {
            this.address = address;
            this.text = text;
            this.dBmAverage = dBmAverage;
            this.lastdBmAverage = -77.0;
            this.outOfRegion = -85.0;
            this.state = 0;
            this.count = 0;
        }

        Deviceaux(String address) {
            this.address = address;
            this.state = 0;
            this.count = 0;
            this.dBmAverage = -85.0;
            this.lastdBmAverage = -77.0;
            this.outOfRegion = -85.0;
            this.text = "Danger";
        }

        Deviceaux(String address,String text) {
            this.address = address;
            this.state = 0;
            this.count = 0;
            this.dBmAverage = -85.0;
            this.lastdBmAverage = -77.0;
            this.outOfRegion = -85.0;
            this.text = "Danger";
        }
        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getAddress() {
            return address;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public double getdBmAverage() {
            return dBmAverage;
        }

        public void setdBmAverage(double dBmAverage) {
            this.dBmAverage = dBmAverage;
        }

        public double getOutOfRegion() {
            return outOfRegion;
        }

        public void setOutOfRegion(double outOfRegion) {
            this.outOfRegion = outOfRegion;
        }

        public double getLastdBmAverage() {
            return lastdBmAverage;
        }

        public void setLastdBmAverage(double lastdBmAverage) {
            this.lastdBmAverage = lastdBmAverage;
        }

        @Override
        public boolean equals(Object o) {
            Deviceaux aux = (Deviceaux) o;
            String address = aux.getAddress();
            return this.address.equals(address);
        }
    }
    //------------------------------Device aux--------------------//

}