package in.magnumsoln.pollstest.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    public String title, share_url;
    private Context context;
    private TextView txtTitle, coins;
    private SharedPreferences mSharedPreference;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        toolbar = findViewById(R.id.categoryToolbar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewPager);
        txtTitle = findViewById(R.id.category_title);
        coins = findViewById(R.id.txtCoins_category);
        floatingActionButton = findViewById(R.id.actionButtonCategory);
        mSharedPreference = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        context = this;
        coins.setText("X " + mSharedPreference.getInt("available_coins", 10));
        title = getIntent().getStringExtra("category_name");
        share_url = getIntent().getStringExtra("topic_share_url");
        txtTitle.setText(title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        setupViewPager();
        setupActionButton();
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager() {
        TabFragmentAdapter adapter = new TabFragmentAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT);
        viewPager.setAdapter(adapter);
    }

    private void setupActionButton() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (share_url != null) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share Topic");
                    String shareMessage = "Checkout this awesome topic:";
                    shareMessage += "\n\n"+share_url;
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Share using:"));
                } else {
                    Toast.makeText(context, "No share url found", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        coins.setText("X " + mSharedPreference.getInt("available_coins", 10));
    }

}
