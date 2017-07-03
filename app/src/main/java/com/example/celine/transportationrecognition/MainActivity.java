package com.example.celine.transportationrecognition;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity {

    // Declare variables for pending intent and fence receiver.
    private PendingIntent myPendingIntent;
    private MyFenceReceiver myFenceReceiver;
    private GoogleApiClient mGoogleApiClient;
    private static final String FENCE_RECEIVER_ACTION = "FENCE_RECEIVE";
    private static final String TAG = "Awareness";

    //TODO: test
    private TextView walkingTextView;
    private TextView cyclingTextView;
    private TextView drivingTextView;
    private int walkingTimes = 0;
    private int cyclingTimes = 0;
    private int drivingTimes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        mGoogleApiClient.connect();

        // Initialize myPendingIntent and fence receiver
        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        myPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        myFenceReceiver = new MyFenceReceiver();
        registerReceiver(myFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));

        registerFences();

        walkingTextView = (TextView) findViewById(R.id.walkingText);
        cyclingTextView = (TextView) findViewById(R.id.cyclingText);
        drivingTextView = (TextView) findViewById(R.id.drivingText);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unregisterFences();
        //unregisterReceiver(myFenceReceiver);
    }

    private void registerFences() {
        //Create fences
        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);
        AwarenessFence cyclingFence = DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE);
        AwarenessFence drivingFence = DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE);

        // Register the fence to receive callbacks.
        // The fence key uniquely identifies the fence.
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence("walkingFence", walkingFence, myPendingIntent)
                        .addFence("cyclingFence", cyclingFence, myPendingIntent)
                        .addFence("drivingFence", drivingFence, myPendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.");
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    }
                });
    }

    private void unregisterFences() {
        unregisterFence("walkingFence");
        unregisterFence("cyclingFence");
        unregisterFence("drivingFence");
    }

    private void unregisterFence(final String fenceKey) {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(fenceKey)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " successfully removed.");
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " could NOT be removed.");
            }
        });
    }

    // Handle the callback on the Intent.
    public class MyFenceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            FenceState fenceState = FenceState.extract(intent);

            switch(fenceState.getFenceKey()) {
                case "walkingFence":
                    switch(fenceState.getCurrentState()) {
                        case FenceState.TRUE:
                            Log.i(TAG, "You are walking.");
                            walkingTimes += 1;
                            walkingTextView.setText("Walking: " + String.valueOf(walkingTimes));
                            break;
                        case FenceState.FALSE:
                            Log.i(TAG, "You are not walking.");
                            break;
                        case FenceState.UNKNOWN:
                            Log.i(TAG, "You may be walking.");
                            break;
                    }
                    break;
                case "cyclingFence":
                    switch(fenceState.getCurrentState()) {
                        case FenceState.TRUE:
                            Log.i(TAG, "You are cycling.");
                            cyclingTimes += 1;
                            cyclingTextView.setText("Cycling: " + String.valueOf(cyclingTimes));
                            break;
                        case FenceState.FALSE:
                            Log.i(TAG, "You are not cycling.");
                            break;
                        case FenceState.UNKNOWN:
                            Log.i(TAG, "You may be cycling.");
                            break;
                    }
                    break;
                case "drivingFence":
                    switch(fenceState.getCurrentState()) {
                        case FenceState.TRUE:
                            Log.i(TAG, "You are driving.");
                            drivingTimes += 1;
                            drivingTextView.setText("Driving: " + String.valueOf(drivingTimes));
                            break;
                        case FenceState.FALSE:
                            Log.i(TAG, "You are not driving.");
                            break;
                        case FenceState.UNKNOWN:
                            Log.i(TAG, "You may be driving.");
                            break;
                    }
                    break;

            }
        }
    }
}
