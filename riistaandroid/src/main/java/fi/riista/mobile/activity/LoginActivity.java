package fi.riista.mobile.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import fi.riista.mobile.R;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.network.LoginTask;
import fi.riista.mobile.utils.AppPreferences;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.activity.WorkActivity;

/**
 * Login screen is skipped if user has stored credentials
 */
public class LoginActivity extends WorkActivity {

    public static String EVENT_YEARS_EXTRA = "gameDiaryYears";
    private Button mLoginButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLocaleFromSetting();
        setContentView(R.layout.activity_loginscreen);
        setupButtons(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean googleServicesEnabled = Utils.checkPlayServices(this);
        mLoginButton.setEnabled(googleServicesEnabled);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setupButtons(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.loginscreen, menu);
        return true;
    }

    public void attemptLogin(final String username, final String password) {
        LoginTask log = new LoginTask(getWorkContext(), username, password, false) {
            @Override
            public void loginFailed() {
                mLoginButton.setEnabled(true);
            }

            @Override
            public void onFinishText(String text) {
                GameDatabase.getInstance().storeCredentials(LoginActivity.this, username, password);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                try {
                    JSONObject jObject = new JSONObject(text);
                    JSONArray yearArray = jObject.getJSONArray("gameDiaryYears");
                    ArrayList<Integer> eventYears = new ArrayList<>();
                    for (int i = 0; i < yearArray.length(); i++) {
                        eventYears.add(yearArray.getInt(i));
                    }
                    intent.putIntegerArrayListExtra(EVENT_YEARS_EXTRA, eventYears);

                    AppPreferences.setUserInfo(getWorkContext().getContext(), text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        };
        log.start();
        mLoginButton.setEnabled(false);
    }

    public void setupLocaleFromSetting() {
        String languageSelected = AppPreferences.getLanguageCodeSetting(this);
        Locale locale = new Locale(languageSelected);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    void setupButtons(boolean restarted) {
        mLoginButton = (Button) findViewById(R.id.loginbutton);
        if (Utils.checkPlayServices(this)) {

            // Login automatically if credentials are stored
            if (GameDatabase.getInstance().credentialsStored(this)) {
                if (restarted) {
                    finish();
                    return;
                }
                GameDatabase.getInstance().loginWithStoredCredentials(getWorkContext(), false);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                return;
            }

            mLoginButton.setEnabled(true);
            mLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText usernameField = (EditText) findViewById(R.id.username);
                    EditText passwordField = (EditText) findViewById(R.id.password);
                    attemptLogin(usernameField.getText().toString(), passwordField.getText().toString());
                }
            });
        }
        findViewById(R.id.layout).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                Utils.hideKeyboard(LoginActivity.this, view);
                return false;
            }
        });
    }
}
