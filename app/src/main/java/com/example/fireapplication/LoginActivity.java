package com.example.fireapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    private EditText et_email, et_password;
    private TextView tv_register, tv_phone, tv_forget;
    private Button btn_login;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth firebaseAuth;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this.getApplication());
        setContentView(R.layout.activity_login);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        tv_register = findViewById(R.id.tv_register);
        tv_phone = findViewById(R.id.tv_phone);
        tv_forget = findViewById(R.id.tv_forget);
        tv_forget.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ForgetActivity.class);
            startActivity(intent);
            finish();
        });

        tv_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginWithPhoneActivity.class);
                startActivity(intent);
            }
        });

        // Khởi tạo FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Đăng nhập với Facebook
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
                String facebookToken = loginResult.getAccessToken().getToken();
                Log.d("FacebookToken", "Token: " + facebookToken);
                Toast.makeText(getApplicationContext(), "Facebook Token: " + facebookToken, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Đăng nhập bị hủy", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Toast.makeText(getApplicationContext(), "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Đăng nhập với Google
        SignInButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnGoogleSignIn.setSize(SignInButton.SIZE_ICON_ONLY);
        btnGoogleSignIn.setOnClickListener(view -> signInWithGoogle());

        // Đăng nhập với email/mật khẩu
        btn_login.setOnClickListener(v -> {
            String email = et_email.getText().toString();
            String pass = et_password.getText().toString();
            firebaseAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user.isEmailVerified()) {
                                String email1 = user.getEmail();
                                Toast.makeText(getApplicationContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("name", "abc");
                                intent.putExtra("email", email1);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Email chưa được xác thực", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Chuyển đến trang đăng ký
        tv_register.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.e("GoogleSignIn", "Google sign-in failed", e);
                Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show();
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        String name = user.getDisplayName();
                        String email = user.getEmail();
                        Log.d("Google_token", account.getIdToken());
                        Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("name", name);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                String name = user.getDisplayName();
                String email = user.getEmail();
                String facebookToken = token.getToken();
                Toast.makeText(getApplicationContext(), "Đăng nhập thành công: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                Log.d("FacebookToken", "Token: " + facebookToken);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                intent.putExtra("facebookToken", facebookToken);
                startActivity(intent);
                finish();
            } else {
                Log.d("FirebaseAuth", "Lỗi đăng nhập Firebase: " + task.getException().getMessage());
                Toast.makeText(getApplicationContext(), "Lỗi đăng nhập Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
