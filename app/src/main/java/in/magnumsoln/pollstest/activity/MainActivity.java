package in.magnumsoln.pollstest.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.fragment.DashboardFragment;
import in.magnumsoln.pollstest.fragment.ProfileFragment;
import in.magnumsoln.pollstest.model.Poll;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView toolbarTitle, nCoins;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences mSharedPreferences;
    private FragmentManager fragmentManager;
    private DashboardFragment dashboardFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        nCoins = findViewById(R.id.txtCoins);
        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        fragmentManager = getSupportFragmentManager();
        dashboardFragment = new DashboardFragment();
        profileFragment = new ProfileFragment(nCoins);
        setupNavigationView();
        toolbarTitle.setText("Polls");
        nCoins.setText("X " + mSharedPreferences.getInt("available_coins", 0) + "");
        Bundle splashBundle = getIntent().getBundleExtra("splash_bundle");
        // data from dynamic link/notification
        if (splashBundle != null) {
//            boolean fromDl = splashBundle.getBoolean("fromDL", false);
            String type = splashBundle.getString("type", null);
            if (type != null) {
                if (type.equalsIgnoreCase("poll")) {
                    Intent intent = new Intent(MainActivity.this, PollActivity.class);
                    intent.putExtra("poll", splashBundle.getSerializable("poll"));
                    intent.putExtra("poll_status", splashBundle.getString("poll_status"));
                    intent.putExtra("redirected", true);
                    startActivity(intent);
                } else if (type.equalsIgnoreCase("topic")) {
                    Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                    intent.putExtra("category_name", splashBundle.getString("category_name"));
                    intent.putExtra("topic_share_url", splashBundle.getString("topic_share_url"));
                    intent.putExtra("redirected", true);
                    startActivity(intent);
                }
            }

        } else {
            fragmentManager.beginTransaction().replace(R.id.frame, dashboardFragment).commit();
        }
    }

    private void setupNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        fragmentManager.beginTransaction().replace(R.id.frame, dashboardFragment).commit();
                        break;
                    case R.id.profile:
                        fragmentManager.beginTransaction()
                                .replace(R.id.frame, profileFragment).commit();
                }
                return true;
            }
        });
        bottomNavigationView.setItemIconTintList(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!(fragmentManager.findFragmentById(R.id.frame) instanceof ProfileFragment))
            fragmentManager.beginTransaction().replace(R.id.frame, dashboardFragment).commit();
        nCoins.setText("X " + mSharedPreferences.getInt("available_coins", 10));
    }

    @Override
    public void onBackPressed() {
        if (!(fragmentManager.findFragmentById(R.id.frame) instanceof DashboardFragment)) {
            fragmentManager.beginTransaction().replace(R.id.frame, dashboardFragment).commit();
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
        } else {
            ActivityCompat.finishAffinity(this);
        }
    }

}
