package com.example.fieldgenie;

import android.annotation.SuppressLint;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpVerifyActivity extends AppCompatActivity {

    EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;

    TextView textMobile, resendButton;

    String verificationId;

    Button verifyButton;

    Boolean resendEnabled = false;

    int resendTime = 60;

    int selectedETPosition = 0;
    private final TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }


        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() > 0) {
                if (selectedETPosition == 0) {
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
    OtpReceiver otpReceiver;

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

        autoOtpReceiver();

        verifyButton.setOnClickListener(view -> {
            if (inputCode1.getText().toString().trim().isEmpty() ||
                    inputCode2.getText().toString().trim().isEmpty() ||
                    inputCode3.getText().toString().trim().isEmpty() ||
                    inputCode4.getText().toString().trim().isEmpty() ||
                    inputCode5.getText().toString().trim().isEmpty() ||
                    inputCode6.getText().toString().trim().isEmpty()) {
                Toast.makeText(OtpVerifyActivity.this, "OTP not Valid", Toast.LENGTH_SHORT).show();
            } else {
                if (verificationId != null) {
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
                                if (task.isSuccessful()) {
                                    verifyButton.setVisibility(View.INVISIBLE);
                                    Intent intent = new Intent(OtpVerifyActivity.this, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {
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
            if (resendEnabled) {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+91" + getIntent().getStringExtra("phoneNumber"),
                        60,
                        TimeUnit.SECONDS,
                        OtpVerifyActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(OtpVerifyActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                verificationId = newVerificationId;
                                Toast.makeText(OtpVerifyActivity.this, "OTP Sent Again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });


    }

    private void autoOtpReceiver() {
        otpReceiver = new OtpReceiver();
        this.registerReceiver(otpReceiver, new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION));
        otpReceiver.initListener(new OtpReceiver.OtpReceiverListener() {
            @Override
            public void onOtpSuccess(String otp) {
                int o1 = Character.getNumericValue(otp.charAt(0));
                int o2 = Character.getNumericValue(otp.charAt(1));
                int o3 = Character.getNumericValue(otp.charAt(2));
                int o4 = Character.getNumericValue(otp.charAt(3));
                int o5 = Character.getNumericValue(otp.charAt(4));
                int o6 = Character.getNumericValue(otp.charAt(5));

                inputCode1.setText(String.valueOf(o1));
                inputCode2.setText(String.valueOf(o2));
                inputCode3.setText(String.valueOf(o3));
                inputCode4.setText(String.valueOf(o4));
                inputCode5.setText(String.valueOf(o5));
                inputCode6.setText(String.valueOf(o6));
            }

            @Override
            public void onOtpTimeout() {
                Toast.makeText(OtpVerifyActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCountDownTimer() {
        resendEnabled = false;
        resendButton.setTextColor(Color.parseColor("#99000000"));

        new CountDownTimer(resendTime * 1000L, 1000) {

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long l) {
                resendButton.setText("Resend Code (" + (l / 1000) + ")");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                resendEnabled = true;
                resendButton.setText("Resend Code");
                resendButton.setTextColor(getResources().getColor(R.color.textColor));
            }
        }.start();
    }

    private void showKeyboard(EditText OtpET) {
        OtpET.requestFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        inputMethodManager.showSoftInput(OtpET, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (selectedETPosition == 5) {
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

        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpReceiver != null) {
            unregisterReceiver(otpReceiver);
        }
    }
}