package mesas.martinez.leonor.tracbursys.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by paco on 22/07/2015.
 */
public class OrionJsonManager {
    private String type;
    private String id;
    private String latitude;
    private String longitude;
    private String message;
    private String coberageAlert;
    private String projectName;
    private String installerDNIorNIF;
    private String date;
    private String deviceName;
   // private JSONObject json;
   private String json;

    public OrionJsonManager(String type, String id, String latitude, String longitude, String message, String coberageAlert, String installerDNIorNIF, String projectName, String deviceName) {
        this.type = type;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.message = message;
        this.coberageAlert = coberageAlert;
        this.projectName = projectName;
        this.installerDNIorNIF = installerDNIorNIF;
        this.deviceName=deviceName;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
        Date d=new Date();
        this.date = sdf.format(d);
        String shortDate=s.format(d);
               json="{  \n" +
                    "\"contextElements\": [\n" +
                    "  {\n" +
                    "    \"type\": \"" + type + "\",\n" +
                    "    \"isPattern\": \"false\",\n" +
                    "    \"id\": \"" + id + "\",\n" +
                    "    \"attributes\": [\n" +
                    "    {\n" +
                    "      \"name\": \"position\",\n" +
                    "      \"type\": \"coords\",\n" +
                    "      \"value\": \""+latitude+","+longitude+"\",\n" +
                    "      \"metadatas\": [\n" +
                    "      {\n" +
                    "        \"name\": \"location\",\n" +
                    "        \"type\": \"string\",\n" +
                    "        \"value\": \"WGS84\"\n" +
                    "      }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"message\",\n" +
                    "      \"type\": \"text\",\n" +
                    "      \"value\": \""+message+"\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \"coberageAlert \",\n" +
                    "      \"type\": \"dBm\",\n" +
                    "      \"value\": \""+coberageAlert+"\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \""+shortDate+"\",\n" +
                    "      \"type\": \"Date\",\n" +
                    "      \"value\": \""+date+"\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \""+projectName+"\",\n" +
                    "      \"type\": \"text\",\n" +
                    "      \"value\": \"ProjectName\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"name\": \""+installerDNIorNIF+"\",\n" +
                    "      \"type\": \"text\",\n" +
                    "      \"value\": \"InstallerDNIorNIF\"\n" +
                    "    }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "  ],\n" +
                    "  \"updateAction\": \"APPEND\"\n" +
                    " }\n" ;

   }

    public String getStringJson() {
        return json;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getMessage() {
        return message;
    }

    public String getCoberageAlert() {
        return coberageAlert;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getInstallerDNIorNIF() {
        return installerDNIorNIF;
    }

    public String getDate() {
        return date;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
