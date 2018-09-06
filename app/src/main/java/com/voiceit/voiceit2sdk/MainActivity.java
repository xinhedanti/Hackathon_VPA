package com.voiceit.voiceit2sdk;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;

import com.voiceit.voiceit2.VoiceItAPI2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private VoiceItAPI2 myVoiceIt;
    private String userId="";
    private String userName="";
    private String phrase = "welcome to the hackathon it's going to be fun";
    private String contentLanguage = "en-US";
    private boolean doLivenessCheck = false; // Liveness detection is not used for enrollment views

    private boolean userCreated = false;

    public EditText _userName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myVoiceIt = new VoiceItAPI2("key_85952e14f77b4d6eb4f6df20c578004d","tok_1dc13da43844428391585c4b35375fad");

        //create userId in the beginning
        myVoiceIt.createUser(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //retrieve the value of user id
                try {
                    userId = (String)response.get("userId");
                    System.out.println("Create User Successful : " + response.toString() + " " + userName + " " + userId);
                    userCreated = true;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //store the user name;user id pair into txt
                //saveInfo(getBaseContext(),userName,userId);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("Create User Failed : " + errorResponse.toString());
                }
            }
        });
    }


    public void encapsulatedVoiceEnrollment(View view) {
        _userName = (EditText) this.findViewById(R.id.userName);
        userName = _userName.getText().toString();
        //if userName is found from file. override the userId with the value from the file.

        //ideally, the logic should be if the user not found from mapping file, create new user. but can't have two functions in one onclick.
        //this just workaround.

        myVoiceIt.encapsulatedVoiceEnrollment(this, userId, contentLanguage, phrase, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedVoiceEnrollment Success Result : " + response.toString() + " userName: " + userName + "userId: " + userId + " " + contentLanguage + " " + phrase);
                //append userName;userId mapping to a file.
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedVoiceEnrollment Failure Result : " + errorResponse.toString()+ " userName: " + userName + "userId: " + userId + " " + contentLanguage + " " + phrase);
                }
            }
        });

    }

    public void encapsulatedVoiceVerification(View view) {

        //get userName from editText
        _userName = (EditText) this.findViewById(R.id.userName);
        userName = _userName.getText().toString();

        //look for userId from user info mapping file
        //userId = FileInstreamxxx

        System.out.println("I am in verification method, userName : " + userName + " userId: " + userId);

        myVoiceIt.encapsulatedVoiceVerification(this, userId, contentLanguage, phrase, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedVoiceVerification Success Result : " + response.toString() + " userName: " + userName + "userId : " + userId);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedVoiceVerification Failure Result : userName : " + userName + " userId " + userId + errorResponse.toString());
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

        public void writeFile(String name, String id){

            FileOutputStream fos;

            try {
                String info = name + ";" + id;
                System.out.println("start to write");
                fos = openFileOutput("info.txt",MODE_APPEND);

                fos.write(info.getBytes());
                fos.close();
                System.out.println("finish to write");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Write file failed");
            }

        }


        //Read file
        public static Map<String,String> readFile(Context context){

            Map<String,String> maps = new HashMap<>();
            FileInputStream fis;

            try {
                fis = context.openFileInput("info.txt");
                BufferedReader bf = new BufferedReader(new InputStreamReader(fis));
                String info = bf.readLine();

                String split[] = info.split(";");

                String name = split[0];
                String password = split[1];
                maps.put("name",name);
                maps.put("id",password);

                fis.close();

                return maps;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return maps;
        }


}
