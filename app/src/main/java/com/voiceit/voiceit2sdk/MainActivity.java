package com.voiceit.voiceit2sdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;

import com.voiceit.voiceit2.VoiceEnrollmentView;
import com.voiceit.voiceit2.VoiceItAPI2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private VoiceItAPI2 myVoiceIt;
    private String userId="";
    private String userName="";
    private String phrase = "Never forget tomorrow is a new day";
    private String contentLanguage = "en-US";
    private boolean doLivenessCheck = false; // Liveness detection is not used for enrollment views

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myVoiceIt = new VoiceItAPI2("key_85952e14f77b4d6eb4f6df20c578004d","tok_1dc13da43844428391585c4b35375fad");



    }


    public void encapsulatedVoiceEnrollment(View view) {

        userName = findViewById(R.id.userName).toString();


            myVoiceIt.createUser(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    //retrieve the value of user id
                    try {
                        userId = (String)response.get("userId");
                        System.out.println("Create User Successful : " + response.toString() + " " + userName + " " + userId);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //store the user name;user id pair into txt
                    saveInfo(getBaseContext(),userName,userId);

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (errorResponse != null) {
                        System.out.println("Create User Failed : " + errorResponse.toString());
                    }
                }

            });

        myVoiceIt.encapsulatedVoiceEnrollment(this, userId, contentLanguage, phrase, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedVoiceEnrollment Success Result : " + response.toString() + " " + userId + " " + contentLanguage + " " + phrase);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedVoiceEnrollment Failure Result : " + errorResponse.toString()+ " " + userId + " " + contentLanguage + " " + phrase);
                }
            }
        });
     //   }else {Toast.makeText(this,"Please enter user name", Toast.LENGTH_LONG).show();}


    }

    public void encapsulatedVoiceVerification(View view) {
        myVoiceIt.encapsulatedVoiceVerification(this, userId, contentLanguage, phrase, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedVoiceVerification Result : " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedVoiceVerification Result : " + errorResponse.toString());
                }
            }
        });
    }

    public void encapsulatedVideoEnrollment(View view) {
        myVoiceIt.encapsulatedVideoEnrollment(this, userId, contentLanguage, phrase, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedVideoEnrollment Result : " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedVideoEnrollment Result : " + errorResponse.toString());
                }
            }
        });
    }

    public void encapsulatedVideoVerification(View view) {
        myVoiceIt.encapsulatedVideoVerification(this, userId, contentLanguage, phrase, doLivenessCheck, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedVideoVerification Result : " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedVideoVerification Result : " + errorResponse.toString());
                }
            }
        });
    }

    public void encapsulatedFaceEnrollment(View view) {
        myVoiceIt.encapsulatedFaceEnrollment(this, userId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedFaceEnrollment Result : " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedFaceEnrollment Result : " + errorResponse.toString());
                }
            }
        });
    }

    public void encapsulatedFaceVerification(View view) {
        myVoiceIt.encapsulatedFaceVerification(this, userId, doLivenessCheck, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedFaceVerification Result : " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedFaceVerification Result : " + errorResponse.toString());
                }
            }
        });
    }

        //Write file

        public static boolean saveInfo(Context context,String name, String id){

            try {
                String info = name + ";" + id;
                System.out.println("start to write");
                FileOutputStream fos = context.openFileOutput("info.txt",0);
                fos.write(info.getBytes());
                fos.close();
                System.out.println("finish to write");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }


        //Read file
        public static Map<String,String> readInfo(Context context){

            Map<String,String> maps = new HashMap<>();

            try {
                FileInputStream fis = context.openFileInput("info.txt");
                BufferedReader bf = new BufferedReader(new InputStreamReader(fis));
                String info = bf.readLine();

                String split[] = info.split(";");

                String name = split[0];
                String password = split[1];
                maps.put("name",name);
                maps.put("id",password);

                return maps;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return maps;
        }


}
