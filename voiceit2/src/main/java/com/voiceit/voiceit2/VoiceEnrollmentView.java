package com.voiceit.voiceit2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;

public class VoiceEnrollmentView extends AppCompatActivity {

    private final String mTAG = "VoiceEnrollmentView";
    private Context mContext;

    private RadiusOverlayView mOverlay;
    private MediaRecorder mMediaRecorder = null;

    private VoiceItAPI2 mVoiceIt2;
    private String mUserID = "usr_c41fa437eb314a9e96cf56cec8599dc0";
    private String mContentLanguage = "";
    private String mPhrase = "";

    private int mEnrollmentCount = 0;
    private final int mNeededEnrollments = 3;
    private int mFailedAttempts = 0;
    private final int mMaxFailedAttempts = 3;
    private boolean mContinueEnrolling = false;

    private boolean displayWaveform = true;
    private final long REFRESH_WAVEFORM_INTERVAL_MS = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_enrollment_view);

        Intent intent = getIntent();
        // Grab data from parent activity
        Bundle bundle = intent.getExtras();
        if(bundle != null) {

            mVoiceIt2 = new VoiceItAPI2(bundle.getString("apiKey"), bundle.getString("apiToken"));
            System.out.println(bundle.getString("apiKey") + " " + bundle.getString("apiToken"));

            mUserID = bundle.getString("userID");
            System.out.println(bundle.getString("userID"));

            mContentLanguage = bundle.getString("contentLanguage");
            System.out.println(bundle.getString("contentLanguage"));

            mPhrase = bundle.getString("phrase");
            System.out.println(bundle.getString("phrase"));
        }
        System.out.println("VoiceEmrollmentView point3");

        // Hide action bar
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
            Log.d(mTAG, "Cannot hide action bar");
        }

        // Set context
        mContext = this;

        System.out.println("VoiceEnrollmentView point4");

        // Text output on mOverlay
        mOverlay = findViewById(R.id.overlay);

        // Lock orientation
        if (Build.VERSION.SDK_INT >= 18) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        } else {
            setRequestedOrientation(Utils.lockOrientationCode(getWindowManager().getDefaultDisplay().getRotation()));
        }
    }

    private void startEnrollmentFlow() {
        mContinueEnrolling = true;
        System.out.println("i am inside start Enrollment Flow...");
        //recordVoice();

        // Delete enrollments and re-enroll
        mVoiceIt2.deleteAllEnrollmentsForUser(mUserID, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject Response) {
                // Record voice and enroll
                recordVoice();
                System.out.println("record voice finish....");
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    exitViewWithJSON("voiceit-failure", errorResponse);
                    System.out.println("delete user fail, Error Code = " + " " + mUserID + " " + errorResponse.toString());
                } else {
                    Log.e(mTAG, "No response from server");
                    mOverlay.updateDisplayTextAndLock(getString(R.string.CHECK_INTERNET));
                    System.out.println("No response from server");
                    // Wait for 2.0 seconds
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exitViewWithMessage("voiceit-failure","No response from server");
                        }
                    }, 2000);
                }
                System.out.println("delete user fail....");
            }
        });
    }

    private void requestHardwarePermissions() {
        final int PERMISSIONS_REQUEST_RECORD_AUDIO = 0;
        final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 1;

        System.out.println("i am inside requestHardwarePermissions");
        // MY_PERMISSIONS_REQUEST_* is an app-defined int constant. The callback method gets the
        // result of the request.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ Manifest.permission.RECORD_AUDIO},
                        ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
                System.out.println("ask multiple permission.....");
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                            PERMISSIONS_REQUEST_RECORD_AUDIO);
                    System.out.println("ask for record audio permission");
                }
            }
        } else {
            // Permissions granted, so continue with view
            startEnrollmentFlow();
            System.out.println("Permission Granted and Finish Enrollment Flow.....");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(mTAG, "Hardware Permissions not granted");
            exitViewWithMessage("voiceit-failure", "Hardware Permissions not granted");
        } else {
            // Permissions granted, so continue with view
            startEnrollmentFlow();
        }
    }

    private void exitViewWithMessage(String action, String message) {
        mContinueEnrolling = false;
        stopRecording();
        Intent intent = new Intent(action);
        JSONObject json = new JSONObject();
        try {
            json.put("message", message);
        } catch(JSONException e) {
            Log.d(mTAG,"JSON Exception : " + e.getMessage());
        }
        intent.putExtra("Response", json.toString());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        finish();
    }

    private void exitViewWithJSON(String action, JSONObject json) {
        mContinueEnrolling = false;
        stopRecording();
        Intent intent = new Intent(action);
        intent.putExtra("Response", json.toString());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        exitViewWithMessage("voiceit-failure", "User Canceled");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Confirm permissions and start enrollment flow
        System.out.println("onStart start....");
        requestHardwarePermissions();

        System.out.println("onStart finished...");

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mContinueEnrolling) {
            exitViewWithMessage("voiceit-failure", "User Canceled");
        }
    }

    private void stopRecording() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                Log.d(mTAG, "Error trying to stop MediaRecorder");
            }
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void failEnrollment(final JSONObject response) {
        mOverlay.setProgressCircleColor(getResources().getColor(R.color.failure));
        mOverlay.updateDisplayText(getString(R.string.ENROLL_FAIL));

        // Wait for ~1.5 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // Report error to user
                    if (response.getString("responseCode").equals("PDNM")) {
                        mOverlay.updateDisplayText(getString((getResources().getIdentifier(response.
                                getString("responseCode"), "string", getPackageName())), mPhrase));
                    } else {
                        mOverlay.updateDisplayText(getString((getResources().getIdentifier(response.
                                getString("responseCode"), "string", getPackageName()))));
                    }
                } catch (JSONException e) {
                    Log.d(mTAG, "JSON exception : " + e.toString());
                }
                // Wait for ~4.5 seconds
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFailedAttempts++;

                        // User failed too many times
                        if (mFailedAttempts > mMaxFailedAttempts) {
                            mOverlay.updateDisplayText(getString(R.string.TOO_MANY_ATTEMPTS));
                            // Wait for ~2 seconds then exit
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    exitViewWithJSON("voiceit-failure", response);
                                }
                            }, 2000);
                        } else if (mContinueEnrolling) {
                            // Try again
                            recordVoice();
                        }
                    }
                }, 4500);
            }
        }, 1500);
    }

    private long redrawWaveform(){
        final long currentTime = System.currentTimeMillis();

        runOnUiThread(new Runnable() {
            public void run() {
                if (mMediaRecorder != null) {
                    mOverlay.setWaveformMaxAmplitude(mMediaRecorder.getMaxAmplitude());
                }
            }
        });

        return System.currentTimeMillis() - currentTime;
    }

    // Enroll after recording voice
    private void recordVoice() {
        if (mContinueEnrolling) {
            System.out.println("i am inside record voice....");
            mOverlay.updateDisplayText(getString(getResources().getIdentifier("ENROLL_" + (mEnrollmentCount + 1) + "_PHRASE", "string", getPackageName()), mPhrase));
            System.out.println(getString(getResources().getIdentifier("ENROLL_" + (mEnrollmentCount + 1) + "_PHRASE", "string", getPackageName()), mPhrase));
            try {
                // Create file for audio
                final File audioFile = Utils.getOutputMediaFile(".wav");
                if (audioFile == null) {
                    exitViewWithMessage("voiceit-failure", "Creating audio file failed");
                    System.out.println("Creating audio file failed....");
                }

                // Setup device and capture Audio
                mMediaRecorder = new MediaRecorder();
                Utils.startMediaRecorder(mMediaRecorder, audioFile);

                // Start displaying waveform
                displayWaveform = true;
                new Thread(new Runnable() {
                    public void run() {
                        while (displayWaveform) {
                            try {
                                Thread.sleep(Math.max(0, REFRESH_WAVEFORM_INTERVAL_MS - redrawWaveform()));
                            } catch (Exception e) {
                                Log.d(mTAG, "MediaRecorder getMaxAmplitude Exception: " + e.getMessage());
                            }
                        }
                    }
                }).start();

                // Record and update amplitude display for ~5 seconds, then send data
                // 4800ms to make sure recording is not over 5 seconds
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // Stop waveform
                        displayWaveform = false;

                        if (mContinueEnrolling) {
                            stopRecording();

                            // Reset sine wave
                            mOverlay.setWaveformMaxAmplitude(1);

                            mOverlay.updateDisplayText(getString(R.string.WAIT));
                            mVoiceIt2.createVoiceEnrollment(mUserID, mContentLanguage, mPhrase, audioFile, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                                    try {
                                        if (response.getString("responseCode").equals("SUCC")) {
                                            mOverlay.setProgressCircleColor(getResources().getColor(R.color.success));
                                            mOverlay.updateDisplayText(getString(R.string.ENROLL_SUCCESS));
                                            // Wait for ~2 seconds
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    audioFile.deleteOnExit();
                                                    mEnrollmentCount++;

                                                    if (mEnrollmentCount == mNeededEnrollments) {
                                                        mOverlay.updateDisplayText(getString(R.string.ALL_ENROLL_SUCCESS));
                                                        // Wait for ~2.5 seconds
                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                exitViewWithJSON("voiceit-success", response);
                                                            }
                                                        }, 2500);
                                                    } else {
                                                        // Continue Enrolling
                                                        recordVoice();
                                                    }
                                                }
                                            }, 2000);

                                            // Fail
                                        } else {
                                            audioFile.deleteOnExit();
                                            failEnrollment(response);
                                        }
                                    } catch (JSONException e) {
                                        Log.d(mTAG, "JSON exception : " + e.toString());
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, final JSONObject errorResponse) {
                                    if (errorResponse != null) {
                                        Log.d(mTAG, "JSONResult : " + errorResponse.toString());

                                        audioFile.deleteOnExit();
                                        failEnrollment(errorResponse);
                                    } else {
                                        Log.e(mTAG, "No response from server");
                                        mOverlay.updateDisplayTextAndLock(getString(R.string.CHECK_INTERNET));
                                        // Wait for 2.0 seconds
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                exitViewWithMessage("voiceit-failure", "No response from server");
                                            }
                                        }, 2000);
                                    }
                                }
                            });
                        }
                    }
                }, 4800);
            } catch (Exception ex) {
                Log.d(mTAG, "Recording Error: " + ex.getMessage());
                exitViewWithMessage("voiceit-failure", "Recording Error");
            }
        }
    }
}