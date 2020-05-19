package in.magnumsoln.pollstest.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import in.magnumsoln.pollstest.BuildConfig;
import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.model.Poll;
import in.magnumsoln.pollstest.util.InternetChecker;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static in.magnumsoln.pollstest.util.VersionChecker.isCompatibleVersion;

public class SplashActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private SharedPreferences mSharedPreferences;
    private Context context;
    private boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mSharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        mFirestore = FirebaseFirestore.getInstance();
        context = this;
        isLoggedIn = mSharedPreferences.getBoolean("login", false);
        boolean fromNotification = getIntent().getBooleanExtra("fromNotification",false);
        if(fromNotification){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    handleNotificationRedirection();
                }
            }, 1000);
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                init();
            }
        }, 1000);
    }

    private void handleNotificationRedirection() {
        Bundle bundle = getIntent().getBundleExtra("bundle");
        bundle.putString("type",getIntent().getStringExtra("type"));
        bundle.putBoolean("fromNotification",true);
        if(isLoggedIn){
            Intent intent = new Intent(context,MainActivity.class);
            intent.putExtra("splash_bundle",bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(context,LoginActivity.class);
            intent.putExtra("splash_bundle",bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void init() {
        if (!InternetChecker.isInternetAvailable(context)) {
            showNoInternetDialog();
        } else {
            checkVersionAndProceed();
        }
    }

    private void checkVersionAndProceed() {
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
                            handleDynamicLinks();

                        } else {
                            showVersionAlertDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(SplashActivity.this, "Some error occured", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void setupNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("notification", "notification", importance);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
            FirebaseMessaging.getInstance().subscribeToTopic("newPoll");
            FirebaseMessaging.getInstance().subscribeToTopic("customNotification");
            MobileAds.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNoInternetDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_no_internet);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button retry = dialog.findViewById(R.id.btnofflinedialog);
        dialog.setCancelable(false);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showVersionAlertDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_version);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button update = dialog.findViewById(R.id.btnversiondialog);
        dialog.setCancelable(false);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store")));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    void handleDynamicLinks() {
        try {
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(getIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                        @Override
                        public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                            // Get deep link from result (may be null if no link is found)
                            if (pendingDynamicLinkData == null){
                                if (isLoggedIn) {
                                    Intent intent = new Intent(context,MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Intent intent = new Intent(context, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                            else {
                                Uri deepLink = pendingDynamicLinkData.getLink();
                                final String pollid = deepLink.getQueryParameter("pollid");
                                final String topic = deepLink.getQueryParameter("topic");
                                if (pollid != null) {
                                    FirebaseFirestore.getInstance().collection("POLL")
                                            .document(pollid).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    Timestamp start_time = (Timestamp) documentSnapshot.get("START_TIME");
                                                    Timestamp close_time = (Timestamp) documentSnapshot.get("CLOSE_TIME");
                                                    Timestamp declare_time = (Timestamp) documentSnapshot.get("DECLARE_TIME");
                                                    Date start_ = (start_time==null)? null : start_time.toDate();
                                                    Date close_ = (close_time==null)? null : close_time.toDate();
                                                    Date declare_ = (declare_time==null)? null : declare_time.toDate();
                                                    String status = "";
                                                    Date current_time = Calendar.getInstance().getTime();
                                                    if(close_time==null ||close_time.toDate().after(current_time)){
                                                        status = "OPEN";
                                                    }else if(close_time.toDate().before(current_time)){
                                                        if(declare_time==null || declare_time.toDate().after(current_time))
                                                            status = "CLOSED";
                                                        else if(declare_time.toDate().before(current_time)){
                                                            status = "DECLARED";
                                                        }
                                                    }
                                                    long correct_option = 0,reward_amount = 4;
                                                    if(documentSnapshot.get("CORRECT_OPTION") != null){
                                                        correct_option =(long) documentSnapshot.get("CORRECT_OPTION");
                                                    }
                                                    if(documentSnapshot.get("REWARD_AMOUNT") != null){
                                                        reward_amount =(long) documentSnapshot.get("REWARD_AMOUNT");
                                                    }
                                                    Poll poll = new Poll(pollid,
                                                            (String) documentSnapshot.get("QUESTION"),
                                                            (String) documentSnapshot.get("TOPIC"),
                                                            start_,
                                                            close_,
                                                            declare_,
                                                            status,
                                                            (String) documentSnapshot.get("IMAGE_URL"),
                                                            (String) documentSnapshot.get("SHARE_URL"),
                                                            (List<String>) documentSnapshot.get("OPTIONS"),
                                                            correct_option, reward_amount);
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("type","poll");
                                                    bundle.putSerializable("poll",poll);
                                                    bundle.putString("poll_status",status);
                                                    bundle.putBoolean("fromDL",true);
                                                    if(isLoggedIn){
                                                        Intent intent = new Intent(context,MainActivity.class);
                                                        intent.putExtra("splash_bundle",bundle);
                                                        startActivity(intent);
                                                        finish();
                                                    }else{
                                                        Intent intent = new Intent(context,LoginActivity.class);
                                                        intent.putExtra("splash_bundle",bundle);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    e.printStackTrace();
                                                    Log.w("MainActivity:: ", "Failed to load poll using deep link");
                                                }
                                            });
                                } else if (topic != null) {
                                    mFirestore.collection("TOPIC").document(topic).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.exists()) {
                                                        String topicName = documentSnapshot.getId();
                                                        String shareUrl = (String) documentSnapshot.get("SHARE_URL");
                                                        Bundle bundle = new Bundle();
                                                        bundle.putString("type","topic");
                                                        bundle.putString("category_name", topicName);
                                                        bundle.putString("topic_share_url",shareUrl);
                                                        bundle.putBoolean("fromDL",true);
                                                        if(isLoggedIn){
                                                            Intent intent = new Intent(context, MainActivity.class);
                                                            intent.putExtra("splash_bundle",bundle);
                                                            startActivity(intent);
                                                            finish();
                                                        }else{
                                                            Intent intent = new Intent(context,LoginActivity.class);
                                                            intent.putExtra("splash_bundle",bundle);
                                                            startActivity(intent);
                                                            finish();
                                                        }

                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                }
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("MainActivity::", "getDynamicLink:onFailure", e);
                        }
                    });
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }
}
