package in.magnumsoln.poll.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import in.magnumsoln.poll.R;
import in.magnumsoln.poll.fragment.DashboardFragment;
import in.magnumsoln.poll.fragment.ProfileFragment;
import in.magnumsoln.poll.model.Poll;

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
        nCoins.setText(" X " + mSharedPreferences.getInt("available_coins", 0) + "");
        fragmentManager.beginTransaction().replace(R.id.frame, dashboardFragment).commit();
        handleDynamicLinks();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        nCoins.setText(" X " + mSharedPreferences.getInt("available_coins", 10));
    }

    @Override
    public void onBackPressed() {
        if (!(fragmentManager.findFragmentById(R.id.frame) instanceof DashboardFragment)) {
            fragmentManager.beginTransaction().replace(R.id.frame, dashboardFragment).commit();
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
        } else {
            super.onBackPressed();
        }
    }

    void handleDynamicLinks() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        if (pendingDynamicLinkData != null) {
                            Uri deepLink = null;
                            if (pendingDynamicLinkData != null) {
                                deepLink = pendingDynamicLinkData.getLink();
                            }
                            final String pollid = deepLink.getQueryParameter("pollid");
                            final String topic = deepLink.getQueryParameter("topic");
                            if(pollid != null) {
                                FirebaseFirestore.getInstance().collection("POLL")
                                        .document(pollid).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                Log.w("MainActivity", "poll loaded using deep link");
                                                Poll poll = new Poll(pollid,
                                                        (String) documentSnapshot.get("QUESTION"),
                                                        (String) documentSnapshot.get("TOPIC"),
                                                        null,
                                                        null,
                                                        null,
                                                        "OPEN",
                                                        (String) documentSnapshot.get("IMAGE_URL"),
                                                        null,
                                                        (List<String>) documentSnapshot.get("OPTIONS"),
                                                        0, 0);
                                                Intent intent = new Intent(MainActivity.this, PollActivity.class);
                                                intent.putExtra("poll", poll);
                                                intent.putExtra("poll_status", "OPEN");
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("MainActivity:: ", "Failed to load poll using deep link");
                                            }
                                        });
                            }else if(topic != null){
                                Intent intent = new Intent(MainActivity.this,CategoryActivity.class);
                                intent.putExtra("category_name",topic);
                                startActivity(intent);
                            }
                            // Handle the deep link. For example, open the linked
                            // content, or apply promotional credit to the user's
                            // account.
                            // ...

                            // ...
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MainActivity::", "getDynamicLink:onFailure", e);
                    }
                });
    }

}
