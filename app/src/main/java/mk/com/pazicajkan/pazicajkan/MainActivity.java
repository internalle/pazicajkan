package mk.com.pazicajkan.pazicajkan;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import mk.com.pazicajkan.pazicajkan.utilities.NetworkUtils;

public class MainActivity extends AppCompatActivity {

    LoginButton loginButton;
    TextView textView;
    CallbackManager callbackManager;
    String TAG = "PAZICAJKAN";

    NetworkUtils networkUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void buttonClickedHandle(View button) {
        Log.e(TAG, "BUTTON clicked");

    }

    private void init() {
        loginButton = (LoginButton)findViewById(R.id.fb_login_button_id);
        textView = (TextView)findViewById(R.id.text_info);
        callbackManager = CallbackManager.Factory.create();
        initFacebookSDK();
        networkUtils = new NetworkUtils(getResources().getString(R.string.api_base_url));

        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends", "name"));

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();


        if (isLoggedIn) {
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {

                            try {
                                String url  = getResources().getString(R.string.api_base_url) + "check-and-login";

                                Map<String, String> data = new HashMap<String, String>();
                                data.put("email", response.getJSONObject().getString("email"));
                                data.put("name", response.getJSONObject().getString("name"));

                                networkUtils.post(url, data, getApplicationContext());
                            } catch (Exception $e) {

                            }



                            Log.e(TAG, "BEFORE REPONSE");
                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,link,email,first_name,last_name,name");
            request.setParameters(parameters);
            request.executeAsync();

          //  Log.e(TAG, "REQUEST SENT");
        }
    }

    private void initFacebookSDK() {
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e(TAG, loginResult.toString());
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
    }
}
