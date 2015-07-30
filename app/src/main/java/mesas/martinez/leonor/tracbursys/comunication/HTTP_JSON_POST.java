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
      GET("/ngsi10/queryContext",1);
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

    //Variables to work with Database
    private Project projectaux;
    private ProjectDAO projectDAO;
    private Device deviceaux;
    private DeviceDAO deviceDAO;
    private OrionJsonManager objectJsonManager;
    private Context context;
    TextView data_validation;
    //--------------------------Constructor-------------------------------//
    public HTTP_JSON_POST(Context context, OrionJsonManager object){

        this.context=context;
        this.gender=gender.GET;
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
        this.gender=gender.UPDATE_CREATE;
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
            //Log.i("HTTP_JSON_POST result:",result);
        }catch(URISyntaxException e){
            error=e.getMessage();
            e.printStackTrace();
            return error;
        } catch (UnsupportedEncodingException e) {
            error=e.getMessage();
            e.printStackTrace();
            return error;
        } catch (ClientProtocolException e) {
            error=e.getMessage();
            e.printStackTrace();
            return error;
        } catch (IOException e) {
            error=e.getMessage();
            e.printStackTrace();
            return error;
        }
     if(gender.index ==1) {
           this.onPostExecute(result);
      }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        //Log.i("-------------HTTP_JSON_POST:--------------","Do onPostExecute");
        super.onPostExecute(s);
        //Log.i("HTTP_JSON_POST onPostExecute",s);
        String specifications_text;
        //If all go well, save the device in the Database
        if(s!="0"){
            switch(gender.index) {
                case 0:
                //obtain values from object OrionJsonManager
                String address = objectJsonManager.getId();
                String mlatitude = objectJsonManager.getLatitude();
                String mlongitude = objectJsonManager.getLongitude();
                String name = objectJsonManager.getDeviceName();
                 specifications_text= objectJsonManager.getMessage();
                String rssi = objectJsonManager.getCoverageAlert();
                String project_name = objectJsonManager.getProjectName();

                //Work with Database
                projectDAO = new ProjectDAO(context);
                projectDAO.open();
                projectaux = projectDAO.getProjectByName(project_name);
                projectDAO.close();
                deviceaux = new Device(projectaux.get_id(), address, mlatitude, mlongitude, name, specifications_text, rssi);
                deviceDAO = new DeviceDAO(context);
                deviceDAO.open();
                int device_id = deviceDAO.create(deviceaux);
                deviceDAO.close();
                deviceaux.set_id(device_id);
                if (device_id == -1) {
                    data_validation.setText("ERROR:Saved device =" + address + ", with associate text= " + specifications_text + "in remote Server but it can save in local dataBase ");
                } else {
                    data_validation.setText("Saved device =" + address + ", with associate text= " + specifications_text + "in remote Server and local dataBase");
                }
                    break;
                case 1:
                    try {
                        JSONObject json = new JSONObject(s);
                        if(json.has("errorCode")){
                            //Log.i("JSON:","ERROR CODE"+json.toString());
                            specifications_text ="-1";
                        }else{
                            Log.i("JSON",json.toString());
                            specifications_text=objectJsonManager.getMessageFromStringJson(json.toString());

                        }
                        //Log.i("-----------SPECIFICATION-TEXT-------------",specifications_text);
                         //Send to Service
                        Intent intent = new Intent(Constants.DEVICE_MESSAGE);
                        intent.putExtra("message", specifications_text);
                        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
                        Log.i("-----------INTENT Was SEND-------------",specifications_text);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                    break;
            }//fin switch
        }else{
            data_validation.setText("Remote Server error:"+error);
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
}
