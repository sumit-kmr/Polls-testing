package in.magnumsoln.pollstest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.magnumsoln.pollstest.BuildConfig;
import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.adapter.LoginRecyclerAdapter;

public class LoginActivity extends AppCompatActivity {

    String TAG = "Login Activity log: ";
    private EditText edtMobile, edtOtp;
    private Button btnMobile, btnOtp;
    private ImageView btnResendOtp;
    private Context context;
    private LoginActivity currentActivity;
    private PhoneAuthProvider mAuth;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressBar progressBarMobile, progressBarOtp;
    private RelativeLayout mainProgressBar;
    private TextView txtTimer;
    private FirebaseFirestore mFirestore;
    private String mMobileNumber;
    private SharedPreferences mSharedPreferences;
    boolean isLoggedIn;
    private RecyclerView loginRecyclerView;
    private int[] images = {R.drawable.im1, R.drawable.im2, R.drawable.im3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtMobile = findViewById(R.id.edtMobile);
        edtOtp = findViewById(R.id.edtOtp);
        btnMobile = findViewById(R.id.btnOkMobile);
        btnOtp = findViewById(R.id.btnOkOtp);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        progressBarMobile = findViewById(R.id.progress_bar_mobile);
        progressBarOtp = findViewById(R.id.progress_bar_otp);
        txtTimer = findViewById(R.id.txtTimer);
        mainProgressBar = findViewById(R.id.mainProgressBar);
        loginRecyclerView = findViewById(R.id.loginRecyclerView);
        context = this;
        currentActivity = this;
        mAuth = PhoneAuthProvider.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mSharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        isLoggedIn =  mSharedPreferences.getBoolean("login", false);

        setupRecyclerView();
        disableMobileOkButton();
        disableOtpTextField();
        disableOtpOkButton();
        disableResendButton();

        editTextTextChangeListener();
        setupMobileOkButton();
        setupOtpOkButton();
        setupCallback();
    }

    private void setupRecyclerView() {
        int[] images = {R.drawable.im1,R.drawable.im2,R.drawable.im3};
        LoginRecyclerAdapter adapter = new LoginRecyclerAdapter(this,images);
        loginRecyclerView.setAdapter(adapter);
        loginRecyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
    }

    /*private void checkSupportedVersion() {
        try {
            if(!InternetChecker.isInternetAvailable(context)){
                Toast.makeText(context, "No internet ", Toast.LENGTH_SHORT).show();
                showInternetDialog();
                return;
            }

            mFirestore.collection("PROJECT_DATA").document("ANDROID").get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String minSupportedVersion = (String) documentSnapshot.get("LAST_SUPPORTED_VERSION");
                            String currentAppVersion = BuildConfig.VERSION_NAME;
                            boolean adEnabled = (boolean) documentSnapshot.get("ADS_ENABLED");
                            mSharedPreferences.edit().putBoolean("ads_enabled", adEnabled).apply();
                            if (isCompatibleVersion(minSupportedVersion, currentAppVersion)) {
                                setupNotificationChannel();
                                initializeAds();
                                if (isLoggedIn) {
                                    startActivity(new Intent(currentActivity, MainActivity.class));
                                    finish();
                                } else {
                                    mainProgressBar.setVisibility(View.GONE);
                                }
                            } else {
                                showVersionAlertDialog();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
                            mainProgressBar.setVisibility(View.GONE);
                        }
                    });
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }*/

    private void setupOtpOkButton() {
        btnOtp.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                try {
                    if (edtOtp.getText().toString().length() == 0) {
                        Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PhoneAuthCredential credential = mAuth.getCredential(mVerificationId, edtOtp.getText().toString());
                    signInWithPhoneAuthCredential(credential);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                    reset();
                    edtMobile.setText(null);
                }
            }
        });
    }

    private void setupCallback() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                hideMobileProgressBar();
                showMobileOkButton();
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(context, "Invalid Phone number", Toast.LENGTH_SHORT).show();
                    reset();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Log.w(TAG, "Firebase request exeeded limits");
                    Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
                    reset();
                }else if(e instanceof FirebaseNetworkException){
                    Toast.makeText(context, "Internet unavailable", Toast.LENGTH_SHORT).show();
                    reset();
                }
            }
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull final PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
                enableOtpOkButton();
                enableOtpTextField();
                hideMobileProgressBar();
                showMobileOkButton();
                txtTimer.setVisibility(View.VISIBLE);
                //txtResendOtp.setTextColor(getColor(R.color.grey));
                //txtResendOtp.setEnabled(false);
                btnResendOtp.setVisibility(View.GONE);    ////
                btnResendOtp.setEnabled(false);
                new CountDownTimer(30000,1000){

                    @Override
                    public void onTick(long l) {
                        l /= 1000;
                        String seconds = ""+l;
//                        seconds = (seconds.length()==1) ? "0"+seconds : seconds;
                        txtTimer.setText(seconds);
                    }

                    @Override
                    public void onFinish() {
                        txtTimer.setVisibility(View.GONE);   ////
                       /* txtResendOtp.setEnabled(true);
                        txtResendOtp.setTextColor(Color.parseColor("#228B22"));
                        txtResendOtp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mAuth.verifyPhoneNumber("+91" + mMobileNumber, 30,
                                        TimeUnit.SECONDS, currentActivity, mCallbacks,token);
                                txtResendOtp.setEnabled(false);
                                txtResendOtp.setTextColor(getColor(R.color.grey));
                            }
                        });  */
                        btnResendOtp.setVisibility(View.VISIBLE);
                        btnResendOtp.setEnabled(true);
                        btnResendOtp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mAuth.verifyPhoneNumber("+91" + mMobileNumber, 30,
                                        TimeUnit.SECONDS, currentActivity, mCallbacks,token);
                                btnResendOtp.setVisibility(View.INVISIBLE);
                                btnResendOtp.setEnabled(false);
                            }
                        });
                    }
                }.start();
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        hideOtpOkButton();
        disableOtpTextField();
        showOtpProgressBar();
        try {
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(currentActivity, new OnCompleteListener<AuthResult>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                loginOrSignup();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(context, "Some error occuredABC", Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show();
                            hideOtpProgressBar();
                            showOtpOkButton();
                            enableOtpOkButton();
                            enableOtpTextField();
                        }
                    });
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginOrSignup() {
        try {
            mFirestore.collection("USER")
                    .whereEqualTo("PHONE_NUMBER", mMobileNumber).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                                // signup
                                signup();
                            } else {
                                // login
                                String mob_no = (String) queryDocumentSnapshots.getDocuments().get(0).getString("PHONE_NUMBER");
                                long poll_coins = (long) queryDocumentSnapshots.getDocuments().get(0).get("POLL_COINS");
                                long share_coins = (long) queryDocumentSnapshots.getDocuments().get(0).get("SHARE_COINS");
                                long used_coins = (long) queryDocumentSnapshots.getDocuments().get(0).get("COINS_USED");
                                long coins_redeemed = (long) queryDocumentSnapshots.getDocuments().get(0).get("COINS_REDEEMED");
                                int available_coins = (int) poll_coins + (int) share_coins - (int) used_coins-(int)coins_redeemed;
                                mSharedPreferences.edit().putInt("available_coins", available_coins).apply();
                                mSharedPreferences.edit().putString("phone_no", mob_no).apply();
                                mSharedPreferences.edit().putBoolean("login", true).apply();
                                FirebaseMessaging.getInstance().subscribeToTopic(mob_no);
                                updateDeviceIdAndLaunchActivity(queryDocumentSnapshots.getDocuments().get(0).getId());

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Some error occured. Try again", Toast.LENGTH_SHORT).show();
                            reset();
                        }
                    });
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDeviceIdAndLaunchActivity(String docId) {
        try {
            String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            mFirestore.collection("USER").document(docId).update("DEVICE_ID", deviceId).
                    addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.w(TAG, "Device id updated successfully");
                            Bundle splashBundle = getIntent().getBundleExtra("splash_bundle");
                            Intent intent = new Intent(currentActivity, MainActivity.class);
                            if(splashBundle != null)
                                intent.putExtra("splash_bundle",splashBundle);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Failed to update device id");
                        }
                    });
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }


    private void signup() {
        try {
            String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("COINS_REDEEMED", 0);
            userData.put("POLL_COINS", 10);
            userData.put("SHARE_COINS", 0);
            userData.put("DEVICE_ID", deviceId);
            userData.put("PAYTM_NUMBER", mMobileNumber);
            userData.put("PHONE_NUMBER", mMobileNumber);
            userData.put("REFERRED_BY", "");
            userData.put("MONEY_REQUESTED", false);
            userData.put("COINS_USED", 0);
            mFirestore.collection("USER")
                    .add(userData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            FirebaseMessaging.getInstance().subscribeToTopic(mMobileNumber);
                            Intent intent = new Intent(currentActivity, MainActivity.class);
                            mSharedPreferences.edit().putInt("available_coins", 10).apply();
                            mSharedPreferences.edit().putString("phone_no", mMobileNumber).apply();
                            mSharedPreferences.edit().putBoolean("login", true).apply();
                            Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show();
                            Bundle splashBundle = getIntent().getBundleExtra("splash_bundle");
                            if(splashBundle != null)
                                intent.putExtra("splash_bundle",splashBundle);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "onFailure: Failed to store data during signup");
                            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
                            reset();
                        }
                    });
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    private void reset() {
        enableMobileTextField();
        disableMobileOkButton();
        showMobileOkButton();
        hideMobileProgressBar();

        disableResendButton();

        disableOtpTextField();
        disableOtpOkButton();
        showOtpOkButton();
        hideOtpProgressBar();
        edtOtp.setText(null);
        edtMobile.setText(null);
    }

    private void setupMobileOkButton() {
        btnMobile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                disableMobileOkButton();
                disableMobileTextField();
                hideMobileOkButton();
                showMobileProgressBar();
                mMobileNumber = edtMobile.getText().toString();
                verifyPhone();
            }
        });
    }

    private void verifyPhone() {
        try {
            mAuth.verifyPhoneNumber("+91" + mMobileNumber, 30, TimeUnit.SECONDS, currentActivity, mCallbacks);
        }catch(Exception e){
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    private void editTextTextChangeListener() {
        edtMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 10) {
                    enableMobileOkButton();
                } else {
                    disableMobileOkButton();
                    disableOtpTextField();
                    disableOtpOkButton();
                    disableResendButton();
                }
            }
        });
    }

    void disableMobileTextField() {
        edtMobile.setEnabled(false);
        edtMobile.setForeground(getDrawable(R.drawable.foreground_disabled));
    }

    void disableMobileOkButton() {
        btnMobile.setEnabled(false);
        btnMobile.setForeground(getDrawable(R.drawable.foreground_disabled));
    }

    void disableOtpTextField() {
        edtOtp.setEnabled(false);
        edtOtp.setForeground(getDrawable(R.drawable.foreground_disabled));
    }

    void disableOtpOkButton() {
        btnOtp.setEnabled(false);
        btnOtp.setForeground(getDrawable(R.drawable.foreground_disabled));
    }

    void disableResendButton() {
       // txtResendOtp.setEnabled(false);
        //txtResendOtp.setForeground(getDrawable(R.drawable.foreground_disabled));
        btnResendOtp.setVisibility(View.INVISIBLE);
        btnResendOtp.setEnabled(false);
    }

    void enableMobileTextField() {
        edtMobile.setEnabled(true);
        edtMobile.setForeground(null);
    }

    void enableMobileOkButton() {
        btnMobile.setEnabled(true);
        btnMobile.setForeground(null);
    }

    void enableOtpTextField() {
        edtOtp.setEnabled(true);
        edtOtp.setForeground(null);
    }

    void enableOtpOkButton() {
        btnOtp.setEnabled(true);
        btnOtp.setForeground(null);
    }

    void enableResendButton() {
       // txtResendOtp.setEnabled(true);
        //txtResendOtp.setForeground(null);
        btnResendOtp.setVisibility(View.VISIBLE);
        btnResendOtp.setEnabled(true);
    }

    void hideMobileOkButton() {
        btnMobile.setVisibility(View.GONE);
    }

    void showMobileOkButton() {
        btnMobile.setVisibility(View.VISIBLE);
    }

    void hideMobileProgressBar() {
        progressBarMobile.setVisibility(View.GONE);
    }

    void showMobileProgressBar() {
        progressBarMobile.setVisibility(View.VISIBLE);
    }

    void hideOtpOkButton() {
        btnOtp.setVisibility(View.GONE);
    }

    void showOtpOkButton() {
        btnOtp.setVisibility(View.VISIBLE);
    }

    void hideOtpProgressBar() {
        progressBarOtp.setVisibility(View.GONE);
    }

    void showOtpProgressBar() {
        progressBarOtp.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        ActivityCompat.finishAffinity(this);
    }
}


