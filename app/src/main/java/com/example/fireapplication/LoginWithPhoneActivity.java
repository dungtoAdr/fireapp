package com.example.fireapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginWithPhoneActivity extends AppCompatActivity {
    private EditText et_sdt;
    private Button bt_verify;
    private FirebaseAuth firebaseAuth;
    private static final String TAG= RegisterActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_with_phone);
        et_sdt=findViewById(R.id.et_sdt);
        bt_verify=findViewById(R.id.bt_verify);
        firebaseAuth=FirebaseAuth.getInstance();
        bt_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthOptions options=
                        PhoneAuthOptions.newBuilder(firebaseAuth)
                                .setPhoneNumber(et_sdt.getText().toString().trim())
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(getParent())
                                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        signInWithPhoneAuthCredential(phoneAuthCredential);
                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        Toast.makeText(getApplicationContext(),"Verifycation Failed",Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        super.onCodeSent(s, forceResendingToken);
                                        goToEnterOtpActivity(et_sdt.getText().toString().trim(),s);
                                    }
                                })
                                .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });

    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("name",user.getUid());
                            startActivity(intent);
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }


    private void goToEnterOtpActivity(String phoneNumber, String s) {
        Intent intent=new Intent(this, EnterOtpActivity.class);
        intent.putExtra("phone_number",phoneNumber);
        intent.putExtra("verification_id",s);
        startActivity(intent);
    }
}