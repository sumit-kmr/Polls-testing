package in.magnumsoln.poll.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
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
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import in.magnumsoln.poll.BuildConfig;
import in.magnumsoln.poll.R;

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
    private TextView txtResendOtp,txtTimer;
    private FirebaseFirestore mFirestore;
    private String mMobileNumber;
    private SharedPreferences mSharedPreferences;
    boolean isLoggedIn;
    private CarouselView carouselView;
   private int[] images = {R.drawable.im1, R.drawable.im2, R.drawable.im3};

    @RequiresApi(api = Build.VERSION_CODES.M)
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
        txtResendOtp = findViewById(R.id.txtresendOtp);
        txtTimer = findViewById(R.id.txtTimer);
        mainProgressBar = findViewById(R.id.mainProgressBar);
        carouselView = findViewById(R.id.loginCarouselView);
        context = this;
        currentActivity = this;
        mAuth = PhoneAuthProvider.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mSharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        isLoggedIn =  mSharedPreferences.getBoolean("login", false);

        // check min_support_version of the app
        checkSupportedVersion();

        setupCarouselView();
        disableMobileOkButton();
        disableOtpTextField();
        disableOtpOkButton();
        disableResendButton();

        editTextTextChangeListener();
        setupMobileOkButton();
        setupOtpOkButton();
        setupCallback();
    }

    private void setupCarouselView() {
        carouselView.setViewListener(new ViewListener() {
            @Override
            public View setViewForPosition(int position) {
                View view = getLayoutInflater().inflate(R.layout.carouselview_layout,null);
                ImageView img = view.findViewById(R.id.carouselImg);
                img.setImageDrawable(getDrawable(images[position]));
                return view;
            }
        });
        carouselView.setPageCount(images.length);
    }

    private void checkSupportedVersion() {
        mFirestore.collection("PROJECT_DATA").document("ANDROID").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String minSupportedVersion = (String) documentSnapshot.get("LAST_SUPPORTED_VERSION");
                        String currentAppVersion = BuildConfig.VERSION_NAME;
                        boolean adEnabled = (boolean) documentSnapshot.get("ADS_ENABLED");
                        mSharedPreferences.edit().putBoolean("ads_enabled",adEnabled).apply();
                        if(isCompatibleVersion(minSupportedVersion,currentAppVersion)){

                            if (isLoggedIn) {
                                startActivity(new Intent(currentActivity, MainActivity.class));
                                finish();
                            }else{
                                mainProgressBar.setVisibility(View.GONE);
                            }
                        }else{
                            showAlertDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showAlertDialog();
                    }
                });
    }

    private boolean isCompatibleVersion(String supportedVersion, String currentVersion) {
        int x=supportedVersion.indexOf('.');
        int y=currentVersion.indexOf('.');
        int majorSupportedVersion=Integer.parseInt(supportedVersion.substring(0,x));
        int majorCurrentVersion=Integer.parseInt(currentVersion.substring(0,y));
        int sprintCurrentVersion=Integer.parseInt(currentVersion.substring(x+1));
        int sprintSupportedVersion=Integer.parseInt(supportedVersion.substring(y+1));

        if(majorCurrentVersion<majorSupportedVersion)
            return false;
        else if(sprintCurrentVersion<sprintSupportedVersion)
            return false;

        return true;
    }

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
                    Toast.makeText(context, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                    reset();
                    edtMobile.setText(null);
                }
            }
        });
    }

    private void setupCallback() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                hideMobileProgressBar();
                showMobileOkButton();
                signInWithPhoneAuthCredential(credential);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(context, "Invalid Phone number", Toast.LENGTH_SHORT).show();
                    reset();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Log.w(TAG, "Firebase request exeeded limits");
                    Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
                }
                reset();
                edtMobile.setText("");
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
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
                txtResendOtp.setTextColor(getColor(R.color.grey));
                txtResendOtp.setEnabled(false);
                new CountDownTimer(30000,1000){

                    @Override
                    public void onTick(long l) {
                        l /= 1000;
                        String seconds = ""+l;
                        seconds = (seconds.length()==1) ? "0"+seconds : seconds;
                        txtTimer.setText("00:"+seconds);
                    }

                    @Override
                    public void onFinish() {
                        txtTimer.setVisibility(View.GONE);
                        txtResendOtp.setEnabled(true);
                        txtResendOtp.setTextColor(Color.parseColor("#228B22"));
                        txtResendOtp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mAuth.verifyPhoneNumber("+91" + mMobileNumber, 30,
                                        TimeUnit.SECONDS, currentActivity, mCallbacks,token);
                                txtResendOtp.setEnabled(false);
                                txtResendOtp.setTextColor(getColor(R.color.grey));
                            }
                        });
                    }
                }.start();
            }
        };
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        hideOtpOkButton();
        disableOtpTextField();
        showOtpProgressBar();
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
                        System.out.println("B");
                        hideOtpProgressBar();
                        showOtpOkButton();
                        enableOtpOkButton();
                        enableOtpTextField();
                    }
                });
    }

    private void loginOrSignup() {
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
                            mSharedPreferences.edit().putLong("poll_coins", poll_coins).apply();
                            mSharedPreferences.edit().putString("phone_no", mob_no).apply();
                            mSharedPreferences.edit().putBoolean("login", true).apply();
                            updateDeviceId(queryDocumentSnapshots.getDocuments().get(0).getId());
                            startActivity(new Intent(currentActivity, MainActivity.class));
                            finish();
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
    }

    private void updateDeviceId(String docId) {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        mFirestore.collection("USER").document(docId).update("DEVICE_ID",deviceId).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.w(TAG,"Device id updated successfully");
                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG,"Failed to update device id");
            }
        });
    }


    private void signup() {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("COINS_REDEEMED", 0);
        userData.put("POLL_COINS", 10);
        userData.put("SHARE_COIN", 0);
        userData.put("DEVICE_ID", deviceId);
        userData.put("PAYTM_NUMBER", mMobileNumber);
        userData.put("PHONE_NUMBER", mMobileNumber);
        userData.put("REFERRED_BY", "Nobody");
        userData.put("MONEY_REQUESTED", false);
        mFirestore.collection("USER")
                .add(userData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Intent intent = new Intent(currentActivity, MainActivity.class);
                        mSharedPreferences.edit().putLong("poll_coins", 10).apply();
                        mSharedPreferences.edit().putString("phone_no", mMobileNumber).apply();
                        mSharedPreferences.edit().putBoolean("login", true).apply();
                        Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show();
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
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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
        mAuth.verifyPhoneNumber("+91" + mMobileNumber, 30, TimeUnit.SECONDS, currentActivity, mCallbacks);
    }

    private void showAlertDialog(){
        new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage("The app you are using is no longer supported.Please update and try again")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store")));
                            finishAffinity();
                            System.exit(0);
                        }
                        catch (Exception e) {
                            Toast.makeText(context,"Unable to open play store",Toast.LENGTH_SHORT).show();
                            finishAffinity();
                            System.exit(0);
                        }
                    }
                }).create().show();
    }

    private void editTextTextChangeListener() {
        edtMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    void disableMobileTextField() {
        edtMobile.setEnabled(false);
        edtMobile.setForeground(getDrawable(R.drawable.foreground_disabled));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void disableMobileOkButton() {
        btnMobile.setEnabled(false);
        btnMobile.setForeground(getDrawable(R.drawable.foreground_disabled));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void disableOtpTextField() {
        edtOtp.setEnabled(false);
        edtOtp.setForeground(getDrawable(R.drawable.foreground_disabled));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void disableOtpOkButton() {
        btnOtp.setEnabled(false);
        btnOtp.setForeground(getDrawable(R.drawable.foreground_disabled));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void disableResendButton() {
        txtResendOtp.setEnabled(false);
        txtResendOtp.setForeground(getDrawable(R.drawable.foreground_disabled));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void enableMobileTextField() {
        edtMobile.setEnabled(true);
        edtMobile.setForeground(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void enableMobileOkButton() {
        btnMobile.setEnabled(true);
        btnMobile.setForeground(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void enableOtpTextField() {
        edtOtp.setEnabled(true);
        edtOtp.setForeground(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void enableOtpOkButton() {
        btnOtp.setEnabled(true);
        btnOtp.setForeground(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void enableResendButton() {
        txtResendOtp.setEnabled(true);
        txtResendOtp.setForeground(null);
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

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }
}


