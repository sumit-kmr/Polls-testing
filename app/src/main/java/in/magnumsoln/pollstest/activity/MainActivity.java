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

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
        try {
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(getIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                        @Override
                        public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                            // Get deep link from result (may be null if no link is found)
                            if (pendingDynamicLinkData == null)
                                System.out.println("Dynamic link s null");
                            if (pendingDynamicLinkData != null) {
                                Uri deepLink = pendingDynamicLinkData.getLink();
                                final String pollid = deepLink.getQueryParameter("pollid");
                                final String topic = deepLink.getQueryParameter("topic");
                                if (pollid != null) {
                                    FirebaseFirestore.getInstance().collection("POLL")
                                            .document(pollid).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    Log.w("MainActivity", "poll loaded using deep link");
                                                    Timestamp close_time = (Timestamp) documentSnapshot.get("CLOSE_TIME");
                                                    Timestamp declare_time = (Timestamp) documentSnapshot.get("DECLARE_TIME");
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
                                                            null,
                                                            null,
                                                            null,
                                                            status,
                                                            (String) documentSnapshot.get("IMAGE_URL"),
                                                            null,
                                                            (List<String>) documentSnapshot.get("OPTIONS"),
                                                            correct_option, reward_amount);
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
                                } else if (topic != null) {
                                    Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                                    intent.putExtra("category_name", topic);
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
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

}
