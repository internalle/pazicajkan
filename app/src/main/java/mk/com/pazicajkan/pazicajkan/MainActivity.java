package mk.com.pazicajkan.pazicajkan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import mk.com.pazicajkan.pazicajkan.utilities.NetworkUtils;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public LoginButton loginButton;
    TextView textView;
    CallbackManager callbackManager;
    public static final String TAG = MainActivity.class.getSimpleName();
    public ProgressBar progressBar;
    NetworkUtils networkUtils;
    public Integer userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userId = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void init() {
        loginButton = (LoginButton) findViewById(R.id.fb_login_button_id);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        callbackManager = CallbackManager.Factory.create();
        initFacebookSDK();
        networkUtils = new NetworkUtils(getResources().getString(R.string.api_base_url));

        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if (getUserId() > 0) {
            openLocationInMap();
        } else {
            if (!isLoggedIn) {
                loginButton.setVisibility(View.VISIBLE);
            }
        }

    }

    private void initFacebookSDK() {
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                textView.setText("Login success");
            }

            @Override
            public void onCancel() {
                textView.setText("Login canceled");
            }

            @Override
            public void onError(FacebookException exception) {
                textView.setText("Login error");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        setUserData();
    }

    public void  handleButtonclick(View view) {

    }

    public void openLocationInMap() {
        Log.e(TAG, "LOAD MAP");
    }

    private void setUserData() {
        Log.e(TAG, "SET DATA CALLED");
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.INVISIBLE);

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {

                        Log.e(TAG, response.toString());
                        try {
                            loginButton.setVisibility(View.INVISIBLE);
                            String url = getResources().getString(R.string.api_base_url) + "check-and-login";

                            Map<String, String> data = new HashMap<String, String>();
                            data.put("platform_id", response.getJSONObject().getString("id"));
                            data.put("platform", "FACEBOOK");
                            data.put("email", response.getJSONObject().getString("email"));
                            data.put("name", response.getJSONObject().getString("name"));

                            Log.e(TAG, "BEFORE CALLING API");
                            networkUtils.post(url, data, getApplicationContext(), MainActivity.this);
                        } catch (Exception $e) {
                            Log.e(TAG, $e.toString());
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,link,email,first_name,last_name,name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
        SharedPreferences sharedPref = this.getPreferences(getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getResources().getString(R.string.user_id), userId);
        editor.apply();
    }

    public int getUserId() {
        SharedPreferences sharedPref = this.getPreferences(getApplicationContext().MODE_PRIVATE);

        return sharedPref.getInt(getString(R.string.user_id), 0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
