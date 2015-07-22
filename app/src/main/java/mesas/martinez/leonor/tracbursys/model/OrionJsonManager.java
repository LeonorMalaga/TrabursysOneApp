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
    private JSONObject json;

    public OrionJsonManager(String type, String id, String latitude, String longitude, String message, String coberageAlert, String installerDNIorNIF, String projectName) {
        this.type = type;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.message = message;
        this.coberageAlert = coberageAlert;
        this.projectName = projectName;
        this.installerDNIorNIF = installerDNIorNIF;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
        Date d=new Date();
        this.date = sdf.format(d);
        String shortDate=s.format(d);
        try {
            json = new JSONObject("{  \n" +
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
                    " }\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson() {
        return json;
    }
}
