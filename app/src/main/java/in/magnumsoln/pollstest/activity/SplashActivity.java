package in.magnumsoln.pollstest.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import in.magnumsoln.pollstest.BuildConfig;
import in.magnumsoln.pollstest.R;
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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                init();
            }
        }, 1000);

    }

    private void init() {
        if (!InternetChecker.isInternetAvailable(context)) {
            showNoInternetDialog();
        } else {
            if (!isLoggedIn) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();

            } else {
                setupAndJumpToMainActivity();
            }
        }
    }

    private void setupAndJumpToMainActivity() {
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
                            startActivity(new Intent(context,MainActivity.class));
                            finish();

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

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }
}
