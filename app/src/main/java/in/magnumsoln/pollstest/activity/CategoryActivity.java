package in.magnumsoln.pollstest.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.adapter.TabFragmentAdapter;

public class CategoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    public String title;
    private Context context;
    private TextView txtTitle,coins;
    private SharedPreferences mSharedPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        toolbar = findViewById(R.id.categoryToolbar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewPager);
        txtTitle = findViewById(R.id.category_title);
        coins = findViewById(R.id.txtCoins_category);
        mSharedPreference = getSharedPreferences("user_details",Context.MODE_PRIVATE);
        context = this;
        coins.setText(" X "+mSharedPreference.getInt("available_coins",10));
        title = getIntent().getStringExtra("category_name");
        txtTitle.setText(title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        setupViewPager();
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager() {
        TabFragmentAdapter adapter = new TabFragmentAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT);
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        coins.setText(" X "+mSharedPreference.getInt("available_coins",10));
    }

}
