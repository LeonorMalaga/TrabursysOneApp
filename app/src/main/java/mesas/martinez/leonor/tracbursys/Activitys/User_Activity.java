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
import mesas.martinez.leonor.tracbursys.Services.SpeechBluService;
import mesas.martinez.leonor.tracbursys.model.Constants;


/**
 * Created by leonor martinez mesas on 21/01/15.
 */
public class User_Activity extends ActionBarActivity {
    private static final int RESULT_SETTINGS = 1;
    private final int ENABLE_BT = 2;
    //attributes
    int first;
    private Button stop_start;
    private TextView user_first_text;
    private TextView user_secon_text;
    private TextView user_third_text;
    private TextView user_fourth_text;
    private TextView user_text;
    private String workMode;
    private String state;
    private String stateaux;
    public static final String TAG = "------BluetoothLE----";

    private final Messenger mMessenger;
    private Intent mServiceIntent;
    private Messenger mService = null;
    private SpeechBluService.State mState = SpeechBluService.State.UNKNOWN;
    private MenuItem mRefreshItem = null;
    private ImageView back_button;
    private SpeechBluService service;

    //builder
    public User_Activity() {
        super();
        ////////////////////////////////-------------------///////////////////////////////////////////
        mMessenger = new Messenger(new IncomingHandler(this));
        ////////////////////////////////-------------------///////////////////////////////////////////
    }
    ////////////////////////////////-------------------///////////////////////////////////////////
    //Service Create and conection
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, SpeechBluService.MSG_REGISTER);
                if (msg != null) {
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } else {
                    mService = null;
                }
            } catch (Exception e) {
                Log.w(TAG, "Error connecting to SpeechBluService", e);
                mService = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            //unbindService(mConnection);
            Log.i("Lost Connection with ",name.toString());
        }
    };
    ////////////////////////////////-------------------///////////////////////////////////////////
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
            user_text=(TextView) this.findViewById(R.id.user_textView);
            stop_start = (Button) this.findViewById(R.id.user_button);
            mServiceIntent = new Intent(this, SpeechBluService.class);
            stateaux = SpeechBluService.State.DISCONNECTING.toString();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putString(Constants.SERVICE_STATE, stateaux)
                    .commit();
////////////////////////////////-------------------///////////////////////////////////////////
            stop_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user_secon_text.setVisibility(View.VISIBLE);
                                    ////////////////////////////////-------------------///////////////////////////////////////////
                    state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.SERVICE_STATE, stateaux);
                    Log.d("------STATE---: ", state);
                    stateaux = SpeechBluService.State.DISCONNECTING.toString();//Para el servicio de busqueda
                    Log.d("-----STATE----: ", stateaux + "equals" + (state.equals(stateaux)));
                    if (!state.equals(stateaux)) {
                        stop_start.setVisibility(View.INVISIBLE);
                        //star ble service
                        //stop ble service
//                        stopScan();
//                        stateaux=SpeechBluService.State.DISCONNECTING.toString();
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
                        stateaux = SpeechBluService.State.CONNECTED.toString();
                        Log.d("------STATE----: ", stateaux);
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                .edit()
                                .putString(Constants.SERVICE_STATE, stateaux)
                                .commit();
                        user_first_text.setText(getString(R.string.can_detect_Ibeacon));
                        // stop_start.setText(getString(R.string.stop_service));
                        user_secon_text.setText(getString(R.string.user_second_textView));
                    }
          ////////////////////////////////-------------------///////////////////////////////////////////
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
                super.onActivityResult(requestCode, resultCode, data);
                //showUserSettings();
                break;
            case ENABLE_BT:
               if(resultCode!=RESULT_OK){finish();}
               break;
           default:
                super.onActivityResult(requestCode, resultCode, data);
               break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        workMode = sharedPrefs.getString(Constants.WORKMODE, "1");
        if (workMode.equals("1")) {
            startActivity(new Intent(getApplicationContext(), Installer_Activity.class));
        }else{
        bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
        Log.d("------DENTRO------", "----ON--Start----");}
    }

//    @Override
//    public void onBackPressed() {
//        unbindService(mConnection);
//        Log.d(TAG, "-----BACK BUTTON------------");
//        Intent setIntent = new Intent(Intent.ACTION_MAIN);
//        setIntent.addCategory(Intent.CATEGORY_HOME);
//        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(setIntent);//Falta Parar servicio
//    }

    //------------------------------METHOD--------------------------//
    private void startScan() {
        Message msg = Message.obtain(null, SpeechBluService.MSG_START_SCAN);
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
        Message msg = Message.obtain(null, SpeechBluService.MSG_STOP_SCAN);
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
    private void stateChanged(SpeechBluService.State newState) {
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
                    case SpeechBluService.MSG_STATE_CHANGED:
                        activity.stateChanged(SpeechBluService.State.values()[msg.arg1]);
                        break;
                    case SpeechBluService.MSG_DEVICE_FOUND:
                        Bundle data = msg.getData();
                        if (data != null && data.containsKey(SpeechBluService.KEY_MAC_ADDRESSES)) {
                            String[] n = data.getStringArray(SpeechBluService.KEY_MAC_ADDRESSES);
                            for (int s = 0; s < n.length; s++) {
                                String ns = n[s];
                                Log.d("----DEVICELIST---", ns + "-------");
                            }
                            //activity.mDeviceList.setDevices(activity, data.getStringArray(SpeechBluService.KEY_MAC_ADDRESSES));
                        }
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }
    //Method to stop the service
    public void stopService(){

        if (mService != null) {

            //if service exit, turn off it
            try {
                //No stop the service
                Message msg = Message.obtain(null, SpeechBluService.MSG_UNREGISTER);
                if (msg != null) {
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error unregistering with SpeechBluService", e);
                mService = null;
            } finally {
                unbindService(mConnection);
            }
        }

    }

}
