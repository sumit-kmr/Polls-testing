package in.magnumsoln.poll.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import in.magnumsoln.poll.R;
import in.magnumsoln.poll.fragment.DashboardFragment;
import in.magnumsoln.poll.fragment.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView toolbarTitle,nCoins;
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences mSharedPreferences;
    private FragmentManager fragmentManager;
    private DashboardFragment dashboardFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences("user_details",Context.MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        nCoins = findViewById(R.id.txtCoins);
        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        fragmentManager = getSupportFragmentManager();
        dashboardFragment = new DashboardFragment();
        profileFragment = new ProfileFragment(nCoins);
        setupNavigationView();
        toolbarTitle.setText("Polls");
        nCoins.setText(" X "+mSharedPreferences.getLong("poll_coins",0)+"");
        fragmentManager.beginTransaction()
                .replace(R.id.frame,dashboardFragment).commit();
    }

    private void setupNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.home :
                        fragmentManager.beginTransaction()
                                .replace(R.id.frame,dashboardFragment).commit();
                        break;
                    case R.id.profile:
                        fragmentManager.beginTransaction()
                                .replace(R.id.frame,profileFragment).commit();
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        nCoins.setText(" X "+mSharedPreferences.getLong("poll_coins",10));
    }
}
