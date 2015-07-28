package mesas.martinez.leonor.tracbursys.phoneSensor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mesas.martinez.leonor.tracbursys.model.Constants;
import mesas.martinez.leonor.tracbursys.model.Device;
import mesas.martinez.leonor.tracbursys.model.DeviceDAO;

public class Beta_BleService extends Service implements BluetoothAdapter.LeScanCallback, TextToSpeech.OnInitListener {
    public static final String TAG = "-----------------BleService------------";
    public static final int MSG_REGISTER = 1;
    public static final int MSG_UNREGISTER = 2;
    public static final int MSG_START_SCAN = 3;
    public static final int MSG_STATE_CHANGED = 4;
    public static final int MSG_DEVICE_FOUND = 5;
    public static final int MSG_STOP_SCAN = 6;
    private static final long SCAN_PERIOD = 1000;
    private static final long WAIT_PERIOD = 20000;
    private String toSpeak;
    private String address;
    private String string_rssi;
    private String old_address;
    private String old_string_rssi;
    public static final String KEY_MAC_ADDRESSES = "KEY_MAC_ADDRESSES";
    private Device deviceaux;
    private DeviceDAO deviceDAO;
    int first;
    private static boolean start = true;
    private TextToSpeech tts;
    private final IncomingHandler mHandler;
    private final Messenger mMessenger;
    private final List<Messenger> mClients = new LinkedList<Messenger>();
    private final Map<String, BluetoothDevice> mDevices = new HashMap<String, BluetoothDevice>();

    public enum State {
        UNKNOWN,
        IDLE,
        SCANNING,
        BLUETOOTH_OFF,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    private BluetoothAdapter mBluetooth = null;
    private State mState = State.UNKNOWN;

    public Beta_BleService() {

        mHandler = new IncomingHandler(this);
        mMessenger = new Messenger(mHandler);
    }

    @Override
    public void onCreate() {

        tts = new TextToSpeech(this, this);
        tts.setSpeechRate(0.5f);
        Log.v(TAG, "---oncreate_service--------------");
        toSpeak = "dangerous point ";
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        start = false;
        // Do Not forget to Stop the TTS Engine when you do not require it
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private static class IncomingHandler extends Handler {
        private final WeakReference<Beta_BleService> mService;

        public IncomingHandler(Beta_BleService service) {
            mService = new WeakReference<Beta_BleService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            Beta_BleService service = mService.get();
            if (service != null) {
                switch (msg.what) {
                    case MSG_REGISTER:
                        service.mClients.add(msg.replyTo);
                        Log.d(TAG, "Registered");
                        break;
                    case MSG_UNREGISTER:
                        service.mClients.remove(msg.replyTo);
                        Log.d(TAG, "Unegistered");
                        break;
                    case MSG_STOP_SCAN:
                        service.notifyAll();
                        start = false;
                        Log.d(TAG, "-------------->Stop Service----------------");
                        break;
                    case MSG_START_SCAN:
                        while (start) {
                            try {
                                synchronized (service) {
                                    Log.d(TAG, "Start Scan");
                                    service.startScan();
                                    service.wait(WAIT_PERIOD);
                                }
                            } catch (InterruptedException e) {
                                Log.d("InterrupteException in While", "------------STOP----------");
                                start = false;
                            }
                        }
                        break;

                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }

    private void startScan() {
        mDevices.clear();
        setState(State.SCANNING);
        if (mBluetooth == null) {
            BluetoothManager bluetoothMgr = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            mBluetooth = bluetoothMgr.getAdapter();
        }
        if (mBluetooth == null || !mBluetooth.isEnabled()) {
            setState(State.BLUETOOTH_OFF);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Beta_BleService.this.start();
                }
            }, SCAN_PERIOD);
            //seconf find
            mBluetooth.stopLeScan(Beta_BleService.this);
            Beta_BleService.this.start();
            Log.d(TAG, "------------------Start second time-----------------------\n\n\n ");
        }
    }

    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

        address = device.getAddress();
        string_rssi = String.valueOf(rssi);
        Log.d(TAG, "------------------Added---------------\n\n\n " + device.getName() + "address: " + address + "-->rssi: " + string_rssi);
        deviceDAO = new DeviceDAO(getApplicationContext());
        deviceDAO.open();
        try {
            old_address = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.DEVICE_ADDRESS, "0");
            old_string_rssi = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.DEVICE_RSSI, "0");
            //If It detected a new device
            if (!old_address.equals(address)) {
                deviceaux = deviceDAO.getDeviceByAddress(device.getAddress().toString());
                //Obtain text to server
                //if The server not respond, tray to obtain tex to database
                toSpeak = deviceaux.getDeviceSpecification();
                Log.d(TAG, "------device is save---- at date----:" + toSpeak);

                speakTheText(toSpeak);
                //set the value of the last device detected
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .edit()
                        .putString(Constants.DEVICE_ADDRESS, address)
                        .commit();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .edit()
                        .putString(Constants.DEVICE_RSSI, string_rssi)
                        .commit();

            //If It detected the same device again
            } else {
                Integer auxold = Integer.decode(old_string_rssi);
                Integer aux = Integer.decode(string_rssi);
                Integer rest = auxold - aux;
                int primitiverest = rest.intValue();
                Log.d(TAG, "-----------Integer valur--->: " + rest + "-----------Int valur--->: " + primitiverest);
                if (primitiverest > 10) {
                    deviceaux = deviceDAO.getDeviceByAddress(device.getAddress().toString());
                    toSpeak = deviceaux.getDeviceSpecification();
                    Log.d(TAG, "------device is save---- at date----:" + toSpeak);
                    speakTheText(toSpeak);
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

        } catch (Exception e) {
            Log.d(TAG, "------BAD ADREESS--------");
        }
        deviceDAO.close();
        //}
    }

    private void setState(State newState) {
        if (mState != newState) {
            mState = newState;
            Message msg = getStateMessage();
            if (msg != null) {
                sendMessage(msg);
            }
        }
    }

    private Message getStateMessage() {
        Message msg = Message.obtain(null, MSG_STATE_CHANGED);
        if (msg != null) {
            msg.arg1 = mState.ordinal();
        }
        return msg;
    }

    private void sendMessage(Message msg) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            Messenger messenger = mClients.get(i);
            if (!sendMessage(messenger, msg)) {
                mClients.remove(messenger);
            }
        }
    }

    private void start() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mState == State.SCANNING) {
                    mBluetooth.stopLeScan(Beta_BleService.this);
                    setState(State.IDLE);
                    Message msg = Message.obtain(null, MSG_DEVICE_FOUND);
                    if (msg != null) {
                        Bundle bundle = new Bundle();
                        String[] addresses = mDevices.keySet().toArray(new String[mDevices.size()]);
                        bundle.putStringArray(KEY_MAC_ADDRESSES, addresses);
                        msg.setData(bundle);
                        sendMessage(msg);
                    }
                }
            }
        }, SCAN_PERIOD);
        mBluetooth.startLeScan(this);
        Log.d(TAG, "------------------Start Scan-----------------------\n\n\n ");

    }

    private boolean sendMessage(Messenger messenger, Message msg) {
        boolean success = true;
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            Log.w(TAG, "Lost connection to client", e);
            success = false;
        }
        return success;
    }

    @Override
    public void onInit(int status) {
        Log.v(TAG, "---------------oninit----------------");
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
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

}
