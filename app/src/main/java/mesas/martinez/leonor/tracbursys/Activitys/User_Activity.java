package mesas.martinez.leonor.tracbursys.Activitys;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import mesas.martinez.leonor.tracbursys.R;
import mesas.martinez.leonor.tracbursys.model.Constants;
import mesas.martinez.leonor.tracbursys.phoneSensor.Beta_BleService;


/**
 * Created by leonor martinez mesas on 21/01/15.
 */
public class User_Activity extends ActionBarActivity {
    private static final int RESULT_SETTINGS = 4;
    //attributes
    int first;
    private Button stop_start;
    private TextView user_first_text;
    private TextView user_secon_text;
    private TextView user_third_text;
    private TextView user_fourth_text;
    private String workMode;
    private String state;
    private String stateaux;
    public static final String TAG = "------BluetoothLE----";
    private final int ENABLE_BT = 1;
    private final Messenger mMessenger;
    private Intent mServiceIntent;
    private Messenger mService = null;
    private Beta_BleService.State mState = Beta_BleService.State.UNKNOWN;
    private MenuItem mRefreshItem = null;
    private ImageView back_button;

    //builder
    public User_Activity() {
        super();
        mMessenger = new Messenger(new IncomingHandler(this));
    }

    //Service Create and conection
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, Beta_BleService.MSG_REGISTER);
                if (msg != null) {
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } else {
                    mService = null;
                }
            } catch (Exception e) {
                Log.w(TAG, "Error connecting to Beta_BleService", e);
                mService = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        workMode = sharedPrefs.getString(Constants.WORKMODE, "1");
        Log.d("------------NOT FIRST--WOORK MODE----------: " + workMode.equals("0"), workMode);
        if (workMode.equals("1")) {
            startActivity(new Intent(getApplicationContext(), Installer_Activity.class));
        } else {
            Log.d("--------------WOORK MODE----------: ", "USER");
            user_first_text = (TextView) this.findViewById(R.id.user_first_textView);
            user_secon_text = (TextView) this.findViewById(R.id.user_second_textView);
            user_secon_text.setVisibility(View.INVISIBLE);
            user_third_text = (TextView) this.findViewById(R.id.user_third_textView);
            user_third_text.setVisibility(View.INVISIBLE);
            user_fourth_text = (TextView) this.findViewById(R.id.user_fourth_textView);
            user_fourth_text.setVisibility(View.INVISIBLE);
            stop_start = (Button) this.findViewById(R.id.user_button);
            back_button = (ImageView) this.findViewById(R.id.user_back_button_imageView);
            back_button.setVisibility(View.INVISIBLE);
            mServiceIntent = new Intent(this, Beta_BleService.class);
            //Star Ble service
            // startScan();
            stateaux = Beta_BleService.State.DISCONNECTING.toString();
            Log.d("-STATE--: ", stateaux);
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putString(Constants.SERVICE_STATE, stateaux)
                    .commit();

            stop_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user_secon_text.setVisibility(View.VISIBLE);
                    user_third_text.setVisibility(View.VISIBLE);
                    user_fourth_text.setVisibility(View.VISIBLE);
                    back_button.setVisibility(View.VISIBLE);
                    state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.SERVICE_STATE, stateaux);
                    Log.d("------STATE---: ", state);
                    stateaux = Beta_BleService.State.DISCONNECTING.toString();//Para el servicio de busqueda
                    Log.d("-----STATE----: ", stateaux + "equals" + (state.equals(stateaux)));
                    if (!state.equals(stateaux)) {
                        stop_start.setVisibility(View.INVISIBLE);
                        //star ble service
                        //stop ble service
//                        stopScan();
//                        stateaux=Beta_BleService.State.DISCONNECTING.toString();
//                        Log.d("--------------STATE----------: ",stateaux);
//                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
//                                .edit()
//                                .putString(Constants.SERVICE_STATE, stateaux)
//                                .commit();
//                        user_first_text.setText(getString(R.string.service_stopped));
//                        stop_start.setText(getString(R.string.start_service));
//                        user_secon_text.setText(getString(R.string.can_not_detect_Ibeacon));
                    } else {
                        stop_start.setVisibility(View.INVISIBLE);
                        startScan();
                        stateaux = Beta_BleService.State.CONNECTED.toString();
                        Log.d("------STATE----: ", stateaux);
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                .edit()
                                .putString(Constants.SERVICE_STATE, stateaux)
                                .commit();
                        user_first_text.setText(getString(R.string.can_detect_Ibeacon));
                        // stop_start.setText(getString(R.string.stop_service));
                        user_secon_text.setText(getString(R.string.user_second_textView));
                    }
                }
            });

        }
    }


    //--------------SETTINGS
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                //showUserSettings();
                break;
        }
    }

    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
////        switch (requestCode) {
////            case RESULT_SETTINGS:
////                super.onActivityResult(requestCode, resultCode, data);
////                //showUserSettings();
////                break;
////            case ENABLE_BT:
////                if(resultCode!=RESULT_OK){finish();}
////                break;
////            default:
////                super.onActivityResult(requestCode, resultCode, data);
////                break;
////        }
//    }
    @Override
    protected void onStop() {
        if (mService != null) {
            //if service exit, turn off it
//            try {
//                //No stop the service
//               // Message msg = Message.obtain(null, Beta_BleService.MSG_UNREGISTER);
//                if (msg != null) {
//                    msg.replyTo = mMessenger;
//                    mService.send(msg);
//                }
//            } catch (Exception e) {
//                Log.w(TAG, "Error unregistering with Beta_BleService", e);
//                mService = null;
//            } finally {
//                unbindService(mConnection);
//            }
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
        Log.d("------DENTRO------", "----ON--Start----");
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "-----BACK BUTTON------------");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);//Falta Parar servicio
    }

    //------------------------------METHOD--------------------------//
    private void startScan() {
        Message msg = Message.obtain(null, Beta_BleService.MSG_START_SCAN);
        if (msg != null) {
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Lost connection to service", e);
                unbindService(mConnection);
            }
        }
    }

    private void stopScan() {
        Message msg = Message.obtain(null, Beta_BleService.MSG_STOP_SCAN);
        if (msg != null) {
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Lost connection to service", e);
                unbindService(mConnection);
            }
        }
    }

    //get message from service
    private void stateChanged(Beta_BleService.State newState) {
        mState = newState;
        switch (mState) {
            case BLUETOOTH_OFF:
                //please trun on Bluetthoth sensor
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BT);
                break;
        }
    }

    private static class IncomingHandler extends Handler {
        private final WeakReference<User_Activity> mActivity;

        public IncomingHandler(User_Activity activity) {
            mActivity = new WeakReference<User_Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            User_Activity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case Beta_BleService.MSG_STATE_CHANGED:
                        activity.stateChanged(Beta_BleService.State.values()[msg.arg1]);
                        break;
                    case Beta_BleService.MSG_DEVICE_FOUND:
                        Bundle data = msg.getData();
                        if (data != null && data.containsKey(Beta_BleService.KEY_MAC_ADDRESSES)) {
                            String[] n = data.getStringArray(Beta_BleService.KEY_MAC_ADDRESSES);
                            for (int s = 0; s < n.length; s++) {
                                String ns = n[s];
                                Log.d("----DEVICELIST---", ns + "-------");
                            }
                            //activity.mDeviceList.setDevices(activity, data.getStringArray(Beta_BleService.KEY_MAC_ADDRESSES));
                        }
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

}
