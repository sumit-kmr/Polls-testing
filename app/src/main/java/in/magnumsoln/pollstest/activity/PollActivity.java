package in.magnumsoln.pollstest.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.model.Poll;
import in.magnumsoln.pollstest.util.BlurBuilder;
import in.magnumsoln.pollstest.util.InternetChecker;
import jp.wasabeef.blurry.Blurry;

public class PollActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private Toolbar toolbar;
    private TextView nCoins, ques, a, b, c, d, submit_button_text, A, B, C, D, videoAdCreditDisplayer;
    private RelativeLayout adButton, coinButton, coinButton2;
    private SharedPreferences mSharedPreference;
    private RelativeLayout progressBar, submitButton, middleLayer, topLayer, bottomLayer;
    private FirebaseFirestore mFirestore;
    private Poll currentPoll;
    private Context context;
    private long availableCoins = 0;
    private boolean isPollUnlocked;
    private String userId;
    private int adLimitsLeft;
    private LinearLayout opA, opB, opC, opD;
    private int disabled_foreground,disabled_foreground_round;
    private ImageView imgPollImage, tickA, tickB, tickC, tickD, imgTopLayer, imgMiddleLayer;
    private ScrollView scrollView;
    private RewardedVideoAd mAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll);
        mSharedPreference = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        adButton = findViewById(R.id.adButton);
        coinButton = findViewById(R.id.coinButton);
        coinButton2 = findViewById(R.id.coinButton2);
        progressBar = findViewById(R.id.poll_progress_bar);
        opA = findViewById(R.id.option_a_container);
        opB = findViewById(R.id.option_b_container);
        opC = findViewById(R.id.option_c_container);
        opD = findViewById(R.id.option_d_container);
        a = findViewById(R.id.a);
        b = findViewById(R.id.b);
        c = findViewById(R.id.c);
        d = findViewById(R.id.d);
        ques = findViewById(R.id.txtQuestion);
        imgPollImage = findViewById(R.id.imgPollImage);

        // layers
        bottomLayer = findViewById(R.id.bottomLayer);
        middleLayer = findViewById(R.id.middleLayer);
        topLayer = findViewById(R.id.topLayer);
        imgTopLayer = findViewById(R.id.imgTopLayer);
        imgMiddleLayer = findViewById(R.id.imgMiddleLayer);

        submitButton = findViewById(R.id.submitButton);
        submit_button_text = findViewById(R.id.txtsubmit);
        scrollView = findViewById(R.id.scrollView);
        A = findViewById(R.id.A);
        B = findViewById(R.id.B);
        C = findViewById(R.id.C);
        D = findViewById(R.id.D);
        tickA = findViewById(R.id.ticka);
        tickB = findViewById(R.id.tickb);
        tickC = findViewById(R.id.tickc);
        tickD = findViewById(R.id.tickd);
        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);
        mAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
        videoAdCreditDisplayer = findViewById(R.id.video_ad_credits_left);
        disabled_foreground = R.drawable.foreground_disabled;
        disabled_foreground_round = R.drawable.foreground_disabled_round;
        mFirestore = FirebaseFirestore.getInstance();
        currentPoll = (Poll) getIntent().getSerializableExtra("poll");
        isPollUnlocked = mSharedPreference.getBoolean(currentPoll.getPOLL_ID(), false);
        adLimitsLeft = mSharedPreference.getInt("limit_remaining", 10);
        context = this;
        setupToolbar();
        setUpPoll();
        fetchCoinsAndUserId();
        setCoinAndAdButtonListener();
        setOptionsListener();
    }

    void hideLayers() {
        middleLayer.setVisibility(View.GONE);
        topLayer.setVisibility(View.GONE);
    }

    void showLayers() {
        middleLayer.setVisibility(View.VISIBLE);
        topLayer.setVisibility(View.VISIBLE);
    }

    int option_selected = -1;

    private void setOptionsListener() {
        opA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option_selected = 0;
                opA.setBackground(getDrawable(R.drawable.green_round_background));
                opA.setEnabled(false);

                opB.setEnabled(true);
                opB.setBackground(getDrawable(R.drawable.option_button));
                opC.setEnabled(true);
                opC.setBackground(getDrawable(R.drawable.option_button));
                opD.setEnabled(true);
                opD.setBackground(getDrawable(R.drawable.option_button));
                submitButton.setVisibility(View.VISIBLE);
                submitButton.setBackground(getDrawable(R.drawable.white_ripple_on_green_background));
                submitButton.setForeground(null);
                submitButton.setEnabled(true);
                submit_button_text.setText("SUBMIT");
                submit_button_text.setTextColor(Color.WHITE);
            }
        });
        opB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option_selected = 1;
                opB.setBackground(getDrawable(R.drawable.green_round_background));
                opB.setEnabled(false);

                opA.setEnabled(true);
                opA.setBackground(getDrawable(R.drawable.option_button));
                opC.setEnabled(true);
                opC.setBackground(getDrawable(R.drawable.option_button));
                opD.setEnabled(true);
                opD.setBackground(getDrawable(R.drawable.option_button));
                submitButton.setVisibility(View.VISIBLE);
                submitButton.setBackground(getDrawable(R.drawable.white_ripple_on_green_background));
                submitButton.setForeground(null);
                submitButton.setEnabled(true);
                submit_button_text.setText("SUBMIT");
                submit_button_text.setTextColor(Color.WHITE);
            }
        });
        opC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option_selected = 2;
                opC.setBackground(getDrawable(R.drawable.green_round_background));
                opC.setEnabled(false);

                opA.setEnabled(true);
                opA.setBackground(getDrawable(R.drawable.option_button));
                opB.setEnabled(true);
                opB.setBackground(getDrawable(R.drawable.option_button));
                opD.setEnabled(true);
                opD.setBackground(getDrawable(R.drawable.option_button));
                submitButton.setVisibility(View.VISIBLE);
                submitButton.setBackground(getDrawable(R.drawable.white_ripple_on_green_background));
                submitButton.setForeground(null);
                submitButton.setEnabled(true);
                submit_button_text.setText("SUBMIT");
                submit_button_text.setTextColor(Color.WHITE);
            }
        });
        opD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                option_selected = 3;
                opD.setBackground(getDrawable(R.drawable.green_round_background));
                opD.setEnabled(false);

                opA.setEnabled(true);
                opA.setBackground(getDrawable(R.drawable.option_button));
                opC.setEnabled(true);
                opC.setBackground(getDrawable(R.drawable.option_button));
                opB.setEnabled(true);
                opB.setBackground(getDrawable(R.drawable.option_button));
                submitButton.setVisibility(View.VISIBLE);
                submitButton.setBackground(getDrawable(R.drawable.white_ripple_on_green_background));
                submitButton.setForeground(null);
                submitButton.setEnabled(true);
                submit_button_text.setText("SUBMIT");
                submit_button_text.setTextColor(Color.WHITE);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!InternetChecker.isInternetAvailable(context)) {
                    Toast.makeText(context, "You're offline", Toast.LENGTH_SHORT).show();
                    return;
                }
                // check if poll is closed or not (close time may arrive while poll is open)
                mFirestore.collection("POLL").document(currentPoll.getPOLL_ID()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (!documentSnapshot.exists())
                                    Toast.makeText(context, "This poll does not exist", Toast.LENGTH_SHORT).show();
                                else {
                                    Date currentTime = Calendar.getInstance().getTime();
                                    Timestamp close_time = (Timestamp) documentSnapshot.get("CLOSE_TIME");
                                    if (close_time != null && close_time.toDate().before(currentTime)) {
                                        // poll is closed, do not proceed
                                        Toast.makeText(context, "This poll is closed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // proceed to submit response
                                        if (option_selected < 0)
                                            Toast.makeText(context, "Select a option", Toast.LENGTH_SHORT).show();
                                        else {
                                            submitResponse();
                                            submitButton.setEnabled(false);
                                            opA.setEnabled(false);
                                            opB.setEnabled(false);
                                            opC.setEnabled(false);
                                            opD.setEnabled(false);
                                        }
                                    }
                                }
                            }
                        });
            }
        });

    }

    private void submitResponse() {
        try {
            HashMap<String, Object> m = new HashMap<>();
            m.put("SELECTED_OPTION", option_selected);
            mFirestore.collection("USER")
                    .document(userId)
                    .collection("POLL")
                    .document(currentPoll.getPOLL_ID())
                    .set(m)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            submitButton.setBackground(getDrawable(R.drawable.gray));
                            submitButton.setEnabled(false);
                            submit_button_text.setText("Response submited");
                            submit_button_text.setTextColor(Color.RED);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            errorOccured();
                            onBackPressed();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    boolean adsEnabled;

    private void setUpPoll() {
        blurBackground();
        ques.setText(currentPoll.getQUESTION());
        List<String> options = currentPoll.getOPTIONS();
        int size = options.size();
        if (size > 0)
            a.setText(currentPoll.getOPTIONS().get(0));
        else
            opA.setVisibility(View.GONE);
        if (size > 1)
            b.setText(currentPoll.getOPTIONS().get(1));
        else
            opB.setVisibility(View.GONE);
        if (size > 2)
            c.setText(currentPoll.getOPTIONS().get(2));
        else
            opC.setVisibility(View.GONE);
        if (size > 3)
            d.setText(currentPoll.getOPTIONS().get(3));
        else
            opD.setVisibility(View.GONE);
        //////////
        Picasso.with(context)
                .load(currentPoll.getIMAGE_URL())
                .resize(2048, 1600).onlyScaleDown()
                .networkPolicy(NetworkPolicy.OFFLINE)
                .error(R.drawable.sample)
                .placeholder(R.drawable.sample)
                .into(imgPollImage, new Callback() {
                    @Override
                    public void onSuccess() { }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(currentPoll.getIMAGE_URL())
                                .resize(2048, 1600).onlyScaleDown()
                                .error(R.drawable.sample)
                                .placeholder(R.drawable.sample)
                                .into(imgPollImage, new Callback() {
                                    @Override
                                    public void onSuccess() { }

                                    @Override
                                    public void onError() { }
                                });
                    }
                });
        //////////
        Picasso.with(context)
                .load(currentPoll.getIMAGE_URL())
                .resize(2048, 1600).onlyScaleDown()
                .networkPolicy(NetworkPolicy.OFFLINE)
                .error(R.drawable.sample)
                .placeholder(R.drawable.sample)
                .into(imgTopLayer, new Callback() {
                    @Override
                    public void onSuccess() { }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(currentPoll.getIMAGE_URL())
                                .resize(2048, 1600).onlyScaleDown()
                                .error(R.drawable.sample)
                                .placeholder(R.drawable.sample)
                                .into(imgTopLayer, new Callback() {
                                    @Override
                                    public void onSuccess() { }

                                    @Override
                                    public void onError() { }
                                });
                    }
                });
        //////////

        //Picasso.get().load(currentPoll.getIMAGE_URL()).error(R.drawable.sample).into(imgPollImage);
        //Picasso.get().load(currentPoll.getIMAGE_URL()).error(R.drawable.sample).into(imgTopLayer);

        availableCoins = mSharedPreference.getInt("available_coins", 10);
        nCoins.setText("X  " + availableCoins);
        // setup coin buttons
        if (availableCoins <= 0) {
            coinButton2.setForeground(getDrawable(disabled_foreground_round));
            coinButton2.setEnabled(false);
            coinButton.setForeground(getDrawable(disabled_foreground_round));
            coinButton.setEnabled(false);
        }
        //check for ads enabled
        adsEnabled = mSharedPreference.getBoolean("ads_enabled", false);
        if (!adsEnabled) {
            coinButton.setVisibility(View.GONE);
            adButton.setVisibility(View.GONE);
            coinButton2.setVisibility(View.VISIBLE);
            return;
        }
        // update video ad limits
        Date c = Calendar.getInstance().getTime();
        String todaysDate = new SimpleDateFormat("dd-MM-yyyy").format(c);
        String savedDate = mSharedPreference.getString("date", null);
        if (todaysDate.equalsIgnoreCase(savedDate)) {
            System.out.println("DATE EQUAL.......");
        } else {
            mSharedPreference.edit().putString("date", todaysDate).apply();
            adLimitsLeft = 10;
            mSharedPreference.edit().putInt("limit_remaining", 10).apply();
        }
        videoAdCreditDisplayer.setText(adLimitsLeft + " credits left today");
        adButton.setEnabled(false);
        adButton.setForeground(getDrawable(disabled_foreground_round));
        if (adLimitsLeft == 0) {
            adButton.setEnabled(false);
            adButton.setForeground(getDrawable(disabled_foreground_round));
            videoAdCreditDisplayer.setText("No credits for today");
        }
    }

    void blurBackground(){
        middleLayer.post(new Runnable() {
            @Override
            public void run() {
                bottomLayer.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bottomLayer.setDrawingCacheEnabled(true);
                            bottomLayer.buildDrawingCache();
                            Bitmap bm = bottomLayer.getDrawingCache();
                            final Bitmap resultBitmap = BlurBuilder.blur(context, bm);
                            middleLayer.post(new Runnable() {
                                @Override
                                public void run() {
                                    imgMiddleLayer.setImageBitmap(resultBitmap);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void setCoinAndAdButtonListener() {
        coinButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performCoinButtonUnlock();
            }
        });

        coinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performCoinButtonUnlock();
            }
        });

        adButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                popupCardView.setVisibility(View.GONE);
//                enableScrollView();
//                imgPollImage.setForeground(null);
//                ques.setForeground(null);
//                opA.setEnabled(true);
//                opB.setEnabled(true);
//                opC.setEnabled(true);
//                opD.setEnabled(true);
//                opA.setForeground(null);
//                opB.setForeground(null);
//                opC.setForeground(null);
//                opD.setForeground(null);
//                submitButton.setBackground(getDrawable(R.drawable.gray));
//                submitButton.setForeground(null);
//                submit_button_text.setText("Win 4 coins on correct prediction");
//                submit_button_text.setTextColor(Color.WHITE);
                if(!InternetChecker.isInternetAvailable(context)){
                    Toast.makeText(context, "You're offline", Toast.LENGTH_SHORT).show();
                }
                else {
                    mAd.show();
                }
            }
        });
    }

    private void performCoinButtonUnlock() {
        try {
            availableCoins--;
            mSharedPreference.edit().putInt("available_coins", (int) availableCoins).apply();
            nCoins.setText("X  " + availableCoins);
            mFirestore.collection("USER").document(userId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            long coins_used = (long) documentSnapshot.get("COINS_USED");
                            mFirestore.collection("USER").document(userId).update("COINS_USED", coins_used + 1)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            errorOccured();
                                            return;
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
                        }
                    });
            mSharedPreference.edit().putBoolean(currentPoll.getPOLL_ID(), true).apply();

            //hide layers
            hideLayers();

            enableScrollView();
            imgPollImage.setForeground(null);
            ques.setForeground(null);
            opA.setEnabled(true);
            opB.setEnabled(true);
            opC.setEnabled(true);
            opD.setEnabled(true);
            opA.setForeground(null);
            opB.setForeground(null);
            opC.setForeground(null);
            opD.setForeground(null);
            submitButton.setBackground(getDrawable(R.drawable.gray));
            submitButton.setForeground(null);
            submitButton.setEnabled(false);
            submit_button_text.setText("Win " + currentPoll.getREWARD_AMOUNT() + " coins on correct prediction");
            submit_button_text.setTextColor(Color.RED);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPollStatus() {
        try {
            mFirestore.collection("POLL")
                    .document(currentPoll.getPOLL_ID())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (!documentSnapshot.exists()) {
                                errorOccured();
                                return;
                            }
                            String poll_status = getIntent().getStringExtra("poll_status");
                            if (poll_status.equalsIgnoreCase("OPEN")) {
                                //OPEN
//                                submitButton.setVisibility(View.GONE);
                                if (isPollUnlocked) {
                                    //question unlocked == true

                                    //hide layers
                                    hideLayers();

                                    imgPollImage.setForeground(null);
                                    mFirestore.collection("USER")
                                            .document(userId)
                                            .collection("POLL")
                                            .document(currentPoll.getPOLL_ID())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.exists()) {
                                                        // question is attempted
                                                        long selected_option = (long) documentSnapshot
                                                                .get("SELECTED_OPTION");
                                                        opA.setEnabled(false);
                                                        opB.setEnabled(false);
                                                        opC.setEnabled(false);
                                                        opD.setEnabled(false);
                                                        opA.setForeground(null);
                                                        opB.setForeground(null);
                                                        opC.setForeground(null);
                                                        opD.setForeground(null);
                                                        ques.setForeground(null);
                                                        enableScrollView();
//                                                        submitButton.setEnabled(false);
//                                                        submitButton.setBackground(getDrawable(R.drawable.gray));
//                                                        submit_button_text.setText("Poll result not declared yet");
//                                                        submit_button_text.setTextColor(Color.RED);
                                                        switch ((int) selected_option) {
                                                            case 0:
                                                                opA.setBackground(getDrawable(R.drawable.green_round_background));
                                                                break;
                                                            case 1:
                                                                opB.setBackground(getDrawable(R.drawable.green_round_background));
                                                                break;
                                                            case 2:
                                                                opC.setBackground(getDrawable(R.drawable.green_round_background));
                                                                break;
                                                            case 3:
                                                                opD.setBackground(getDrawable(R.drawable.green_round_background));
                                                                break;
                                                        }
                                                        submitButton.setEnabled(false);
                                                        submitButton.setBackground(getDrawable(R.drawable.gray));
                                                        submit_button_text.setText("Response submitted");
                                                        submit_button_text.setTextColor(Color.RED);
                                                        progressBar.setVisibility(View.GONE);
                                                    } else {
                                                        // question not attempted
                                                        opA.setForeground(null);
                                                        opB.setForeground(null);
                                                        opC.setForeground(null);
                                                        opD.setForeground(null);
                                                        opA.setEnabled(true);
                                                        opB.setEnabled(true);
                                                        opC.setEnabled(true);
                                                        opD.setEnabled(true);
                                                        ques.setForeground(null);
                                                        enableScrollView();
                                                        submitButton.setBackground(getDrawable(R.drawable.gray));
                                                        submitButton.setEnabled(false);
                                                        submit_button_text.setText("Win " + currentPoll.getREWARD_AMOUNT() + " coins on correct prediction");
                                                        submit_button_text.setTextColor(Color.RED);
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    errorOccured();
                                                }
                                            });
                                } else {
                                    // question unlocked == false
                                    // blurry UI
                                    imgPollImage.setForeground(getDrawable(disabled_foreground));
                                    opA.setForeground(getDrawable(disabled_foreground));
                                    opA.setEnabled(false);
                                    opB.setForeground(getDrawable(disabled_foreground));
                                    opB.setEnabled(false);
                                    opC.setForeground(getDrawable(disabled_foreground));
                                    opC.setEnabled(false);
                                    opD.setForeground(getDrawable(disabled_foreground));
                                    opD.setEnabled(false);
                                    ques.setForeground(getDrawable(disabled_foreground));
                                    disableScrollView();

                                    //show layers
                                    showLayers();

//                                    progressBar.setVisibility(View.GONE);
                                }
                            } else if (poll_status.equalsIgnoreCase("CLOSED")) {
                                //CLOSED

                                //hide layers
                                hideLayers();

                                opA.setForeground(getDrawable(disabled_foreground));
                                opA.setEnabled(false);
                                opB.setForeground(getDrawable(disabled_foreground));
                                opB.setEnabled(false);
                                opC.setForeground(getDrawable(disabled_foreground));
                                opC.setEnabled(false);
                                opD.setForeground(getDrawable(disabled_foreground));
                                opD.setEnabled(false);
                                submitButton.setBackground(getDrawable(R.drawable.gray));
                                submitButton.setEnabled(false);
                                if (isPollUnlocked) {
//                                isQuestionAttempted("close");
                                    mFirestore.collection("USER")
                                            .document(userId)
                                            .collection("POLL")
                                            .document(currentPoll.getPOLL_ID())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.exists()) {
                                                        // response == true
                                                        long selected_option = (long) documentSnapshot.get("SELECTED_OPTION");
                                                        switch ((int) selected_option) {
                                                            case 0:
                                                                opA.setBackground(getDrawable(R.drawable.green_round_background));
                                                                opA.setForeground(null);
                                                                break;
                                                            case 1:
                                                                opB.setBackground(getDrawable(R.drawable.green_round_background));
                                                                opB.setForeground(null);
                                                                break;
                                                            case 2:
                                                                opC.setBackground(getDrawable(R.drawable.green_round_background));
                                                                opC.setForeground(null);
                                                                break;
                                                            case 3:
                                                                opD.setBackground(getDrawable(R.drawable.green_round_background));
                                                                opD.setForeground(null);
                                                                break;
                                                        }
                                                        submit_button_text.setText("Poll result not declared yet");
                                                        submit_button_text.setTextColor(Color.RED);
                                                        progressBar.setVisibility(View.GONE);
                                                    } else {
                                                        // response == false
                                                        submit_button_text.setText("This poll is closed");
                                                        submit_button_text.setTextColor(Color.RED);
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    errorOccured();
                                                }
                                            });
                                } else {
                                    // question not attempted

                                    submit_button_text.setText("This poll is closed");
                                    submit_button_text.setTextColor(Color.RED);
                                    progressBar.setVisibility(View.GONE);
                                }

                            } else if (poll_status.equalsIgnoreCase("DECLARED")) {
                                //DECLARED
                                opA.setEnabled(false);
                                opB.setEnabled(false);
                                opC.setEnabled(false);
                                opD.setEnabled(false);
                                submitButton.setEnabled(false);
                                submitButton.setBackground(getDrawable(R.drawable.gray));
                                submit_button_text.setTextColor(Color.RED);

                                //hide layers
                                hideLayers();

                                if (isPollUnlocked) {
                                    // poll unlocked
                                    mFirestore.collection("USER")
                                            .document(userId)
                                            .collection("POLL")
                                            .document(currentPoll.getPOLL_ID())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.exists()) {
                                                        // response == true
                                                        int redBackground = R.drawable.red_round_background;
                                                        int greenBackground = R.drawable.green_round_background;
                                                        long selected_option = (long) documentSnapshot.get("SELECTED_OPTION");
                                                        long correct_option = currentPoll.getCORRECT_OPTION();
                                                        long reward_amount = currentPoll.getREWARD_AMOUNT();
                                                        int requiredBackground =
                                                                (selected_option == correct_option) ? greenBackground : redBackground;

                                                        switch ((int) selected_option) {
                                                            case 0:
                                                                opA.setBackground(getDrawable(requiredBackground));
                                                                break;
                                                            case 1:
                                                                opB.setBackground(getDrawable(requiredBackground));
                                                                break;
                                                            case 2:
                                                                opC.setBackground(getDrawable(requiredBackground));
                                                                break;
                                                            case 3:
                                                                opD.setBackground(getDrawable(requiredBackground));
                                                                break;
                                                        }
                                                        switch ((int) correct_option) {
                                                            case 0:
                                                                A.setVisibility(View.GONE);
                                                                tickA.setVisibility(View.VISIBLE);
                                                                break;
                                                            case 1:
                                                                B.setVisibility(View.GONE);
                                                                tickB.setVisibility(View.VISIBLE);
                                                                break;
                                                            case 2:
                                                                C.setVisibility(View.GONE);
                                                                tickC.setVisibility(View.VISIBLE);
                                                                break;
                                                            case 3:
                                                                D.setVisibility(View.GONE);
                                                                tickD.setVisibility(View.VISIBLE);
                                                                break;
                                                        }
                                                        int statusTextColor = (selected_option == correct_option) ?
                                                                getColor(R.color.colorAccent) : getColor(R.color.red);
                                                        submit_button_text.setTextColor(statusTextColor);
                                                        String status = (selected_option == correct_option) ?
                                                                "Congrats!! You won " + reward_amount + " coins " + getEmojiByUnicode(0x1F603) :
                                                                "Better luck next time " + getEmojiByUnicode(0x2639);
                                                        submit_button_text.setText(status);
                                                        progressBar.setVisibility(View.GONE);
                                                    } else {
                                                        // response == false
                                                        opA.setForeground(getDrawable(disabled_foreground));
                                                        opB.setForeground(getDrawable(disabled_foreground));
                                                        opC.setForeground(getDrawable(disabled_foreground));
                                                        opD.setForeground(getDrawable(disabled_foreground));
                                                        int correct_op = (int) currentPoll.getCORRECT_OPTION();
                                                        switch (correct_op) {
                                                            case 0:
                                                                A.setVisibility(View.GONE);
                                                                tickA.setVisibility(View.VISIBLE);
                                                                break;
                                                            case 1:
                                                                B.setVisibility(View.GONE);
                                                                tickB.setVisibility(View.VISIBLE);
                                                                break;
                                                            case 2:
                                                                C.setVisibility(View.GONE);
                                                                tickC.setVisibility(View.VISIBLE);
                                                                break;
                                                            case 3:
                                                                D.setVisibility(View.GONE);
                                                                tickD.setVisibility(View.VISIBLE);
                                                                break;
                                                        }
                                                        submit_button_text.setText("Result for this poll is declared");
                                                        submit_button_text.setTextColor(Color.RED);
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    errorOccured();
                                                }
                                            });
                                } else {
                                    // poll not unlocked
                                    opA.setForeground(getDrawable(disabled_foreground));
                                    opB.setForeground(getDrawable(disabled_foreground));
                                    opC.setForeground(getDrawable(disabled_foreground));
                                    opD.setForeground(getDrawable(disabled_foreground));
                                    int correct_op = (int) currentPoll.getCORRECT_OPTION();
                                    switch (correct_op) {
                                        case 0:
                                            A.setVisibility(View.GONE);
                                            tickA.setVisibility(View.VISIBLE);
                                            break;
                                        case 1:
                                            B.setVisibility(View.GONE);
                                            tickB.setVisibility(View.VISIBLE);
                                            break;
                                        case 2:
                                            C.setVisibility(View.GONE);
                                            tickC.setVisibility(View.VISIBLE);
                                            break;
                                        case 3:
                                            D.setVisibility(View.GONE);
                                            tickD.setVisibility(View.VISIBLE);
                                            break;
                                    }
                                    submit_button_text.setText("Result for this poll is declared");
                                    submit_button_text.setTextColor(Color.RED);
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            errorOccured();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchCoinsAndUserId() {
        try {
            mFirestore.collection("USER")
                    .whereEqualTo("PHONE_NUMBER", mSharedPreference.getString("phone_no", null))
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.isEmpty()) {
                                errorOccured();
                                return;
                            }
                            DocumentSnapshot currentDocument = queryDocumentSnapshots.getDocuments().get(0);
                            long used_coins = (long) currentDocument.get("COINS_USED");
                            long share_coins = (long) currentDocument.get("SHARE_COINS");
                            long poll_coins = (long) currentDocument.get("POLL_COINS");
                            long coins_redeemed = (long) currentDocument.get("COINS_REDEEMED");
                            availableCoins = poll_coins + share_coins - used_coins - coins_redeemed;
                            nCoins.setText("X  " + availableCoins);
                            mSharedPreference.edit().putInt("available_coins", (int) availableCoins).apply();
                            userId = currentDocument.getId();
                            checkPollStatus();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            errorOccured();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableScrollView() {
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
    }

    private void disableScrollView() {
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    private void errorOccured() {
        Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.pollToolbar);
        nCoins = findViewById(R.id.txtCoins_poll);
        nCoins.setText("X  " + mSharedPreference.getInt("available_coins", 0));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        nCoins.setText("X  " + mSharedPreference.getInt("available_coins", 10));
    }

    public String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        if (adLimitsLeft > 0) {
            adButton.setEnabled(true);
            adButton.setForeground(null);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        mAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        adLimitsLeft--;
        mSharedPreference.edit().putInt("limit_remaining", adLimitsLeft).apply();
        mSharedPreference.edit().putBoolean(currentPoll.getPOLL_ID(), true).apply();
        videoAdCreditDisplayer.setText(adLimitsLeft + " credits left for today");

        //hide layers
        hideLayers();

        enableScrollView();
        imgPollImage.setForeground(null);
        ques.setForeground(null);
        opA.setEnabled(true);
        opB.setEnabled(true);
        opC.setEnabled(true);
        opD.setEnabled(true);
        opA.setForeground(null);
        opB.setForeground(null);
        opC.setForeground(null);
        opD.setForeground(null);
        submitButton.setBackground(getDrawable(R.drawable.gray));
        submitButton.setForeground(null);
        submitButton.setEnabled(false);
        submit_button_text.setText("Win " + currentPoll.getREWARD_AMOUNT() + " coins on correct prediction");
        submit_button_text.setTextColor(Color.RED);
        Toast.makeText(this, "Poll unlocked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Toast.makeText(this, "You are offline", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onRewardedVideoCompleted() {

    }
}