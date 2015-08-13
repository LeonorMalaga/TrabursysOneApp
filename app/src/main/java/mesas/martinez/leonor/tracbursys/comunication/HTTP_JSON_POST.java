package mesas.martinez.leonor.tracbursys.comunication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;

import mesas.martinez.leonor.tracbursys.R;
import mesas.martinez.leonor.tracbursys.Services.GPSservice;
import mesas.martinez.leonor.tracbursys.model.Constants;
import mesas.martinez.leonor.tracbursys.model.Device;
import mesas.martinez.leonor.tracbursys.model.DeviceDAO;
import mesas.martinez.leonor.tracbursys.model.OrionJsonManager;
import mesas.martinez.leonor.tracbursys.model.Project;
import mesas.martinez.leonor.tracbursys.model.ProjectDAO;


/**
 * Created by Leonor Martinez Mesas on 24/07/15.
 * Entries <context, Header, body>
 */
public class HTTP_JSON_POST extends AsyncTask<String,Void,String>{
    //---------------------------Variables/Structures---------------------------------------------//
   public enum Gender{
      UPDATE_CREATE("/ngsi10/updateContext",0),
      GET_MESSAGE("/ngsi10/queryContext",1),
      GET("/ngsi10/queryContext",2);
        private String query;
        private int index;
        private Gender(String query, int index){
            this.query=query;
            this.index=index;
        }

        @Override
        public String toString() {
            return query;
        }

    }


    private URL url;
    private String body;
    private String stringUrl;
    private String error;
    private Gender gender;
    private JSONObject json;
    TextView data_validation;
    private String address="0";
    private String message;
    //Variables to work with Database
    private Project projectaux;
    private ProjectDAO projectDAO;
    private int project_id;
    private Device deviceaux;
    private DeviceDAO deviceDAO;
    private OrionJsonManager objectJsonManager;
    private Context context;

    //--------------------------Constructor-------------------------------//
    public HTTP_JSON_POST(Context context, OrionJsonManager object, String address){

        this.address=address;
        this.context=context;
        this.gender=object.JsonGender;
        this.objectJsonManager=object;
        this.body=objectJsonManager.getStringJson();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.stringUrl = sharedPrefs.getString(Constants.SERVER, "@string/default_import_editText");
        //Http petition to create a new instance in Orion
        //query="/ngsi10/updateContext";
        stringUrl="http://"+stringUrl+gender.query;
      // Log.i("-----HTTP_JSON_POST url:----",stringUrl);
        try{
            url=new URL(stringUrl);
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
    }
    public HTTP_JSON_POST(Context context, OrionJsonManager object,TextView data_validation){
        this.context=context;
        this.gender=object.JsonGender;
        this.objectJsonManager=object;
        this.body=objectJsonManager.getStringJson();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.stringUrl = sharedPrefs.getString(Constants.SERVER, "@string/default_import_editText");
        this.data_validation=data_validation;
        //Http petition to create a new instance in Orion
        //query="/ngsi10/updateContext";
        stringUrl="http://"+stringUrl+gender.query;
        //Log.i("-----HTTP_JSON_POST url:----",stringUrl);
        try{
            url=new URL(stringUrl);
        }catch(MalformedURLException e){
            e.printStackTrace();
        }

    }
    //-------------------------Override-Methods-------------------------------//
    @Override
    protected String doInBackground(String... params) {
        String result="0";
        try{
            DefaultHttpClient client=new DefaultHttpClient();
            HttpPost httpPostFinal=new HttpPost(url.toURI());
            //Build the header
            Header[] headers=new Header[2];
            headers[0]=new BasicHeader("Content-Type","application/json");
            headers[1]=new BasicHeader("Accept","application/json");
            httpPostFinal.setHeaders(headers);
            //Build the body
            httpPostFinal.setEntity(new StringEntity(body));
            //Log.i("HTTP_JSON_POST body:----",body);
            //execute and get response
            HttpResponse response=client.execute(httpPostFinal);
            result=inputStreamToString(response.getEntity().getContent());
            if(gender.index >0) {
                this.onPostExecute(result);
            }
        }catch(URISyntaxException e){
            error=e.getMessage();
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            error=e.getMessage();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            error=e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            error=e.getMessage();
        }catch(Exception e){
            e.printStackTrace();
            error=e.getMessage();
        }finally {
           return result;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        //Log.i("-------------HTTP_JSON_POST:--------------","Do onPostExecute");
        super.onPostExecute(s);
        //Log.i("HTTP_JSON_POST onPostExecute",s);
        String message=" ";
        Device mdevice;
        //If all go well, save the device in the Database
        if(s!="0"){
            switch(gender.index) {
                case 0:
                //obtain values from object OrionJsonManager
                String address = objectJsonManager.getId();
                String mlatitude = objectJsonManager.getLatitude();
                String mlongitude = objectJsonManager.getLongitude();
                String name = objectJsonManager.getDeviceName();
                 message= objectJsonManager.getMessage();
                String rssi = objectJsonManager.getCoverageAlert();
                String project_name = objectJsonManager.getProjectName();
                //Work with Database
                projectDAO = new ProjectDAO(context);
                projectDAO.open();
                projectaux = projectDAO.getProjectByName(project_name);
                projectDAO.close();
                deviceaux = new Device(projectaux.get_id(), address, mlatitude, mlongitude, name, message, rssi);
                deviceDAO = new DeviceDAO(context);
                deviceDAO.open();
                int device_id = deviceDAO.create(deviceaux);
                deviceDAO.close();
                deviceaux.set_id(device_id);
                if (device_id == -1) {
                    data_validation.setText("ERROR:Saved device =" + address + ", with associate text= " + message + "in remote Server but it can save in local dataBase ");
                } else {
                    data_validation.setText("Saved device =" + address + ", with associate text= " + message + "in remote Server and local dataBase");
                }
                    break;
                case 1:
                    try {
                        JSONObject json = new JSONObject(s);
                        if(json.has("errorCode")){
                            //find text in database
                          message=getFromDatabase(s);
                        }else {
                            Log.i("JSON", json.toString());
                            message = objectJsonManager.getMessageFromStringJson(json.toString());
                            if (existDevice()) {
                                //update text in database
                                this.updateMessage(message);
                            } else {
                                //Create Device
                                //--------1 create mdevice------------
                                double latitude = 0;
                                double longitude = 0;
                                GPSservice gps = new GPSservice(context);
                                // check if GPS enabled
                                if (gps.canGetLocation()) {
                                    latitude = gps.getLatitude();
                                    longitude = gps.getLongitude();
                                    Log.d("--LocationOk---", "---");
                                    mlatitude = String.valueOf(latitude);
                                    mlongitude = String.valueOf(longitude);
                                }
                                mdevice = new Device(-1, this.address, String.valueOf(latitude),String.valueOf(longitude), "Anonimous", message, "-78");
                                //--------------Fin create mdevice---------//
                                this.updateProject(mdevice);
                            }
                        }
                        }catch(JSONException e){
                        Log.i("-----------ERROR Convirtiendo a JSON-------------",s);
                        message=getFromDatabase(s);
                        //e.printStackTrace();
                    }finally {
                        //Log.i("-----------SPECIFICATION-TEXT-------------",message);
                        //Send to Service
                        Intent intent = new Intent(Constants.DEVICE_MESSAGE);
                        intent.putExtra("message", message);
                        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
                        Log.i("-----------INTENT Was SEND-------------",message);
                    }
                    break;
                case 2:
                    try {
                        JSONObject json = new JSONObject(s);
                        if(json.has("errorCode")){
                            //find text in database
                            message=getFromDatabase(s);
                        }else{
                            Log.i("JSON",json.toString());
                            mdevice=objectJsonManager.getDeviceFromStringJson(json.toString());
                            project_id=mdevice.getprojecto_id();
                            if(existDevice(project_id)){
                                //update text in database
                                this.updateDevice(mdevice);
                            }else{
                                this.updateProject(mdevice);

                            }
                            message=mdevice.getDeviceSpecification();
                        }
                    }catch(JSONException e){
                        Log.i("-----------ERROR Convirtiendo a JSON-------------",s);
                        message=getFromDatabase(s);
                        //e.printStackTrace();
                    }catch(Exception e) {
                        Log.i("JSON:", json.toString() + "ERROR CODE Database ");

                    }finally {
                        //Log.i("-----------SPECIFICATION-TEXT-------------",message);
                        //Send to Service
                        Intent intent = new Intent(Constants.DEVICE_MESSAGE);
                        intent.putExtra("message", message);
                        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
                        Log.i("-----------INTENT Was SEND-------------",message);
                    }
                    break;

            }//fin switch
        }else{
            if(gender.index==0){
            data_validation.setText("Remote Server error:"+error);}else{
                Log.e("Remote Server error:",error);
            }
        }
    }
//-------------------Mi-Methods-------------------------------//
    private String inputStreamToString(InputStream is) throws IOException {
        String line;
        StringBuilder total=new StringBuilder();
        //Wrap a BufferedReader around the InputStream
        BufferedReader rd= new BufferedReader (new InputStreamReader(is));
        //Read response until the end
       while((line=rd.readLine())!=null){
           total.append(line);
       }
        //Return full string
        return total.toString();
    }
    //return the text associate to a device address
    private String getFromDatabase(String s){
        String text=" ";
        try {
            deviceDAO = new DeviceDAO(context);
            deviceDAO.open();
            deviceaux = deviceDAO.getDeviceByAddress(this.address);
            text = deviceaux.getDeviceSpecification();
        }catch(Exception e) {
            Log.i("JSON:",s+"ERROR CODE Database "+e.getMessage());
            text = " ";
        }finally {
            deviceDAO.close();
        }
        return text;
    }
    //
    private int updateProject(Device mydevice){
        int result=-1;
        int project_id=mydevice.getprojecto_id();
        //update text in database
        try {
            deviceDAO = new DeviceDAO(context);
            deviceDAO.open();
            //is Device in my Database?
            if(project_id==-1){
                deviceaux = deviceDAO.getDeviceByAddress(this.address);
                project_id=deviceaux.getprojecto_id();
                mydevice.setprojecto_id(project_id);
            }else{
                deviceaux=deviceDAO.getDeviceByAddressAndProject(this.address,mydevice.getprojecto_id());
            }

            if (deviceaux.get_id() != -1) {
                //The device exists
                mydevice.set_id(deviceaux.get_id());
                deviceDAO.update(mydevice);
                Log.i("JSON:","DEVICE UPDATE"+this.address+" "+message);
            } else {
                //Be careful the project id  exits, int other case, create a new project
                projectDAO=new ProjectDAO(context);
                projectDAO.open();
                projectaux= projectDAO.getProjectByID(project_id);
                if(projectaux.get_id()==-1){
                    //Create new project
                    projectaux=projectDAO.getProjectByName("Default");
                    project_id=projectaux.get_id();
                    if(project_id==-1){
                    projectaux=new Project("Default","0034667442487");
                    project_id=projectaux.get_id();
                    }
                    mydevice.setprojecto_id(project_id);
                }
                result=deviceDAO.create(mydevice);
                Log.i("JSON:","NEW DEVICE SAVE:"+this.address+" "+message+", in Project:"+projectaux.getmprojectName());
            }

        }finally {
            projectDAO.close();
            deviceDAO.close();
        }
        return result;
    }

    private boolean existDevice(){
        boolean exist=false;
        deviceDAO = new DeviceDAO(context);
        deviceDAO.open();
        deviceaux = deviceDAO.getDeviceByAddress(this.address);
        project_id=deviceaux.getprojecto_id();
        if (deviceaux.get_id() != -1) {
            exist=true;
        }
        deviceDAO.close();
        return exist;
    }
    private boolean existDevice(int id_project){
        boolean exist=false;
        deviceDAO = new DeviceDAO(context);
        deviceDAO.open();
        deviceaux = deviceDAO.getDeviceByAddressAndProject(this.address, id_project);
        project_id=deviceaux.getprojecto_id();
        if (deviceaux.get_id() != -1) {
            exist=true;
        }
        deviceDAO.close();
        return exist;
    }
    private int updateMessage(String message){
        int result=-1;
        deviceDAO = new DeviceDAO(context);
        deviceDAO.open();
        deviceaux = deviceDAO.getDeviceByAddress(this.address);
        deviceaux.setDeviceSpecification(message);
        project_id=deviceaux.getprojecto_id();
        deviceDAO.update(deviceaux);
        deviceDAO.close();
        return deviceaux.get_id();
    }
    private int updateDevice(Device device){
        int result=-1;
        deviceDAO = new DeviceDAO(context);
        deviceDAO.open();
        deviceaux = deviceDAO.getDeviceByAddress(this.address);
        device.set_id(deviceaux.get_id());
        deviceDAO.update(device);
        deviceDAO.close();
        return deviceaux.get_id();
    }

}
