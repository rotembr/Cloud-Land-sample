/*
 * Copyright 2016, 2017 IBM Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.bluemix.appid.cloud.directory.android.sample.appid;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AppIDAuthorizationManager;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.LoginWidget;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;

/**
 * This is the App front page activity.
 * It demonstrates the use of AppID for authentication using cloud directory APIs.
 * Using the loginWidget for sign up user and for forgot password flow.
 * AppID generates and returns Access and Identity tokens. The Identity token provides information
 * about the user which come from cloud directory Identity Provider and the access token can be used
 * to access the user profile attributes.
 */
public class MainActivity extends AppCompatActivity {

    private final static String region = ".stage1.eu-gb.mybluemix.net";//AppID.REGION_US_SOUTH; //AppID.REGION_SYDNEY or AppID.REGION_UK
    private AppID appId;
    private AppIDAuthorizationManager appIdAuthorizationManager;
    private ProgressManager progressManager;
    private AppIdSampleAuthorizationListener appIdSampleAuthorizationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        appId = AppID.getInstance();
        appId.initialize(this, getResources().getString(R.string.authTenantId), region);
        this.progressManager = new ProgressManager(this);
        this.appIdSampleAuthorizationListener = new AppIdSampleAuthorizationListener(this, progressManager);
        appIdAuthorizationManager = new AppIDAuthorizationManager(appId);
        setKeyboardLoginListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        AccessToken accessToken = appIdAuthorizationManager.getAccessToken();
        IdentityToken identityToken = appIdAuthorizationManager.getIdentityToken();
        if (identityToken != null && !identityToken.isExpired()){
            //user already authenticated move to AfterLoginActivity
            appIdSampleAuthorizationListener.onAuthorizationSuccess(accessToken, identityToken);
        }
    }
    /**
     * Log in using resource owner password
     * @param v
     */
    public void onLoginClick(View v) {
        Log.d(logTag("onLoginClick"),"Attempting identified authorization");
        progressManager.showProgress();
        String inputEmail = ((EditText) findViewById(R.id.email)).getText().toString();
        String inputPassword = ((EditText) findViewById(R.id.password)).getText().toString();
        if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
            appIdSampleAuthorizationListener.onAuthorizationFailure(new AuthorizationException("Something didn't work out.\nPlease try entering your email and password again."));
        } else {
            appId.obtainTokensWithROP(getApplicationContext(), inputEmail, inputPassword, appIdSampleAuthorizationListener);
        }
    }

    /**
     * launching cloud directory sign up form
     * @param v
     */
    public void onSignUpClick(View v) {
        Log.d(logTag("onSignUpClick"),"Attempting sign up a new user");
        progressManager.showProgress();
        LoginWidget loginWidget = appId.getLoginWidget();
        loginWidget.launchSignUp(this, appIdSampleAuthorizationListener);
    }

    /**
     * launching cloud directory forgot password from
     * @param v
     */
    public void onForgotPasswordClick(View v) {
        Log.d(logTag("onForgotPasswordClick"),"forgot password triggered");
        progressManager.showProgress();
        appId.getLoginWidget().launchForgotPassword(this, appIdSampleAuthorizationListener);
    }

    /*
     * Setting listener for the user keyboard after entering the password
     */
    private void setKeyboardLoginListener() {
        final EditText editText = (EditText) findViewById(R.id.password);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    //dismiss the keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    onLoginClick(v);
                    handled = true;
                }
                return handled;
            }
        });
    }

    private String logTag(String methodName){
        return getClass().getCanonicalName() + "." + methodName;
    }

}

