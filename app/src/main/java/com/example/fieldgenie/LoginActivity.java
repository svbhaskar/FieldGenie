package com.example.fieldgenie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    EditText inputMobile;

    Button sendOtp;

    SignInButton signInButton;

    FirebaseAuth mAuth;

    GoogleSignInClient client;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        signInButton = findViewById(R.id.sign_in_button);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        client = GoogleSignIn.getClient(this, options);

        signInButton.setOnClickListener(view -> {
            Intent googleIntent = client.getSignInIntent();
            startActivityForResult(googleIntent, 200);
        });

        mAuth = FirebaseAuth.getInstance();


        inputMobile = findViewById(R.id.phoneNumber);
        sendOtp = findViewById(R.id.sendOtp);

        sendOtp.setOnClickListener(view -> {
            if (inputMobile.getText().toString().trim().isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
            } else if (inputMobile.getText().toString().trim().length() != 10) {
                Toast.makeText(LoginActivity.this, "Invalid Number", Toast.LENGTH_SHORT).show();
            } else {
                otpSend();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(task1 -> {
                            if (task.isSuccessful()) {
                                navigateToMainActivity();
                            } else {
                                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            navigateToMainActivity();
        }
    }


    private void otpSend() {

        sendOtp.setVisibility(View.INVISIBLE);
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                sendOtp.setVisibility(View.VISIBLE);
                Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                sendOtp.setVisibility(View.VISIBLE);
                Intent intent = new Intent(LoginActivity.this, OtpVerifyActivity.class);
                intent.putExtra("phoneNumber", inputMobile.getText().toString().trim());
                intent.putExtra("verificationId", verificationId);
                startActivity(intent);
            }
        };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + inputMobile.getText().toString().trim())
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void navigateToMainActivity() {
        finish();
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
    }

}