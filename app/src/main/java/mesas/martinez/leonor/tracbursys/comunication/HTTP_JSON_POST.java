package mesas.martinez.leonor.tracbursys.comunication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
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

import mesas.martinez.leonor.tracbursys.model.Constants;


/**
 * Created by Leonor Martinez Mesas on 24/07/15.
 * Entries <context, Header, body>
 */
public class HTTP_JSON_POST extends AsyncTask<String,Void,String>{
     URL url;
     String query;
     String body;
    String stringUrl;
    public HTTP_JSON_POST(Context context, String query, String stringBody){
        body=stringBody;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        stringUrl = sharedPrefs.getString(Constants.SERVER, "@string/default_import_editText");
        //Http petition to create a new instance in Orion
        //query="/ngsi10/updateContext";
        stringUrl="http://"+stringUrl+query;
        try{
            url=new URL(stringUrl);
        }catch(MalformedURLException e){
            e.printStackTrace();
        }

    }
    @Override
    protected String doInBackground(String... params) {
        String result="@string/error";
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
            //execute and get response
            HttpResponse response=client.execute(httpPostFinal);
            result=inputStreamToString(response.getEntity().getContent());
        }catch(URISyntaxException e){
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.i("OrionResponse",s);
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
