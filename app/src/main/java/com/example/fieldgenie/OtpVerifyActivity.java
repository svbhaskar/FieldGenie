package com.example.fieldgenie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtpVerifyActivity extends AppCompatActivity {

    EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;

    TextView textMobile, resendButton;

    String verificationId;

    Button verifyButton;

    Boolean resendEnabled = false;

    int resendTime = 60;

    int selectedETPosition = 0;

    FirebaseAuth mAuth;

    int REQ_USER_CONSENT = 200;

    SmsBroadcast smsBroadcast;


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        verificationId = getIntent().getStringExtra("verificationId");

        resendButton = findViewById(R.id.resendButton);

        verifyButton = findViewById(R.id.verifyButton);

        textMobile = findViewById(R.id.textMobile);

        textMobile.setText(String.format(
                "+91-%s", getIntent().getStringExtra("phoneNumber")
        ));

        verifyButton.setOnClickListener(view -> {
            if(inputCode1.getText().toString().trim().isEmpty() ||
                    inputCode2.getText().toString().trim().isEmpty() ||
                    inputCode3.getText().toString().trim().isEmpty() ||
                    inputCode4.getText().toString().trim().isEmpty() ||
                    inputCode5.getText().toString().trim().isEmpty() ||
                    inputCode6.getText().toString().trim().isEmpty()){
                Toast.makeText(OtpVerifyActivity.this, "OTP not Valid", Toast.LENGTH_SHORT).show();
            }else{
                if (verificationId != null){
                    String code = inputCode1.getText().toString().trim() +
                            inputCode2.getText().toString().trim() +
                            inputCode3.getText().toString().trim() +
                            inputCode4.getText().toString().trim() +
                            inputCode5.getText().toString().trim() +
                            inputCode6.getText().toString().trim();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    FirebaseAuth.getInstance()
                            .signInWithCredential(credential)
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    verifyButton.setVisibility(View.INVISIBLE);
                                    Intent intent = new Intent(OtpVerifyActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }else{
                                    verifyButton.setVisibility(View.VISIBLE);
                                    Toast.makeText(OtpVerifyActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        inputCode1 = findViewById(R.id.inputCode1);
        inputCode2 = findViewById(R.id.inputCode2);
        inputCode3 = findViewById(R.id.inputCode3);
        inputCode4 = findViewById(R.id.inputCode4);
        inputCode5 = findViewById(R.id.inputCode5);
        inputCode6 = findViewById(R.id.inputCode6);

        inputCode1.addTextChangedListener(textWatcher);
        inputCode2.addTextChangedListener(textWatcher);
        inputCode3.addTextChangedListener(textWatcher);
        inputCode4.addTextChangedListener(textWatcher);
        inputCode5.addTextChangedListener(textWatcher);
        inputCode6.addTextChangedListener(textWatcher);

        showKeyboard(inputCode1);

        startCountDownTimer();



        resendButton.setOnClickListener(view -> {
            if(resendEnabled){
                mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {


                        Toast.makeText(OtpVerifyActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = newVerificationId;
                        Toast.makeText(OtpVerifyActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                    }
                };

                PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(mAuth)
                                .setPhoneNumber("+91"+ getIntent().getStringExtra("phoneNumber"))
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(OtpVerifyActivity.this)
                                .setCallbacks(mCallbacks)
                                .build();

                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });
        
        startSmartUserConsent();
        
    }

    private void startSmartUserConsent() {
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        client.startSmsUserConsent(null);
    }

    private void registerBroadcastReceiver(){
        smsBroadcast = new SmsBroadcast();

        smsBroadcast.smsBroadcastListener = new SmsBroadcast.SmsBroadcastListener() {
            @Override
            public void onSuccess(Intent intent) {
                startActivityForResult(intent, REQ_USER_CONSENT);
            }

            @Override
            public void onFailure() {

            }
        };

        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(smsBroadcast, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(smsBroadcast);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_USER_CONSENT){
            if((requestCode == RESULT_OK) && (data != null)){
                String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
                getOtpFromMessage(message);
            }
        }
    }

    private void getOtpFromMessage(String message) {
        Pattern otpPattern = Pattern.compile("(|^)\\d{6}");
        Matcher matcher = otpPattern.matcher(message);
        if(matcher.find()){
            inputCode1.setText(matcher.group(0));
        }
    }

    private void startCountDownTimer(){
        resendEnabled = false;
        resendButton.setTextColor(Color.parseColor("#99000000"));

        new CountDownTimer(resendTime * 1000, 1000){

            @Override
            public void onTick(long l) {
                resendButton.setText("Resend Code (" + (l/1000)+")");
            }

            @Override
            public void onFinish() {
                resendEnabled = true;
                resendButton.setText("Resend Code");
                resendButton.setTextColor(getResources().getColor(R.color.textColor));
            }
        }.start();
    }

    private void showKeyboard(EditText OtpET){
        OtpET.requestFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        inputMethodManager.showSoftInput(OtpET, InputMethodManager.SHOW_IMPLICIT);
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(editable.length()>0){
                if(selectedETPosition == 0){
                    selectedETPosition = 1;
                    showKeyboard(inputCode2);
                    
                } else if (selectedETPosition == 1) {
                    selectedETPosition = 2;
                    showKeyboard(inputCode3);
                    
                } else if (selectedETPosition == 2) {
                    selectedETPosition = 3;
                    showKeyboard(inputCode4);
                } else if (selectedETPosition == 3) {
                    selectedETPosition = 4;
                    showKeyboard(inputCode5);
                } else if (selectedETPosition == 4) {
                    selectedETPosition = 5;
                    showKeyboard(inputCode6);
                }
            }
        }
    };

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_DEL){
            if(selectedETPosition == 5){
                selectedETPosition = 4;
                showKeyboard(inputCode5);
            } else if (selectedETPosition == 4) {
                selectedETPosition = 3;
                showKeyboard(inputCode4);
            } else if (selectedETPosition == 3) {
                selectedETPosition = 2;
                showKeyboard(inputCode3);
            } else if (selectedETPosition == 2) {
                selectedETPosition = 1;
                showKeyboard(inputCode2);
            } else if (selectedETPosition == 1) {
                selectedETPosition = 0;
                showKeyboard(inputCode1);
            }
            return true;

        }
        else{
        return super.onKeyUp(keyCode, event);}
    }
}