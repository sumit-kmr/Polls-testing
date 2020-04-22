package in.magnumsoln.poll.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import in.magnumsoln.poll.R;
import in.magnumsoln.poll.model.Poll;

public class PollActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView nCoins, ques, a, b, c, d, submit_button_text, A, B, C, D, videoAdCreditDisplayer;
    private RelativeLayout adButton, coinButton, coinButton2;
    private SharedPreferences mSharedPreference;
    private RelativeLayout progressBar, submitButton;
    private FirebaseFirestore mFirestore;
    private Poll currentPoll;
    private Context context;
    private long pollCoins = 0;
    private boolean isPollUnlocked;
    private String userId;
    private int adLimitsLeft;
    private LinearLayout opA, opB, opC, opD;
    private int disabled_foreground;
    private CardView popupCardView;
    private ImageView imgPollImage, tickA, tickB, tickC, tickD;
    private ScrollView scrollView;

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
        popupCardView = findViewById(R.id.popup_card_view);
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
        videoAdCreditDisplayer = findViewById(R.id.video_ad_credits_left);
        disabled_foreground = R.drawable.foreground_disabled;
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
        });

    }

    private void submitResponse() {
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
    }

    private void setUpPoll() {
        ques.setText(currentPoll.getQUESTION());
        a.setText(currentPoll.getOPTIONS().get(0));
        b.setText(currentPoll.getOPTIONS().get(1));
        c.setText(currentPoll.getOPTIONS().get(2));
        d.setText(currentPoll.getOPTIONS().get(3));
        Picasso.get().load(currentPoll.getIMAGE_URL()).error(R.drawable.sample).into(imgPollImage);

        pollCoins = mSharedPreference.getLong("poll_coins", 10);
        nCoins.setText(" X " + pollCoins);
        // setup coin buttons
        if (pollCoins == 0) {
            coinButton2.setForeground(getDrawable(R.drawable.foreground_disabled));
            coinButton2.setEnabled(false);
            coinButton.setForeground(getDrawable(disabled_foreground));
            coinButton.setEnabled(false);
        }
        //check for ads enabled
        boolean adsEnabled = mSharedPreference.getBoolean("ads_enabled", false);
        if (!adsEnabled) {
            coinButton.setVisibility(View.GONE);
            adButton.setVisibility(View.GONE);
            coinButton2.setVisibility(View.VISIBLE);
            return;
        }
        // update video ad limits
        Date c = Calendar.getInstance().getTime();
        String todaysDate = new SimpleDateFormat("dd-MM-yyyy").format(c);
        String savedDate = mSharedPreference.getString("date", todaysDate);
        System.out.println("todays date: " + todaysDate);
        System.out.println("saved date: " + savedDate);
        if (todaysDate.equalsIgnoreCase(savedDate)) {

        } else {
            mSharedPreference.edit().putString("date", todaysDate).apply();
            adLimitsLeft = 10;
            mSharedPreference.edit().putInt("limit_remaining", 10).apply();
        }
        videoAdCreditDisplayer.setText(adLimitsLeft + " credits left today");
        if (adLimitsLeft == 0) {
            adButton.setEnabled(false);
            adButton.setForeground(getDrawable(disabled_foreground));
            videoAdCreditDisplayer.setText("No credits for today");
        }
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
                adLimitsLeft--;
                mSharedPreference.edit().putInt("limit_remaining", 10).apply();
                mSharedPreference.edit().putBoolean(currentPoll.getPOLL_ID(), true).apply();
                popupCardView.setVisibility(View.GONE);
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
                submit_button_text.setText("Win 4 coins on correct prediction");
                submit_button_text.setTextColor(Color.WHITE);
            }
        });
    }

    private void performCoinButtonUnlock() {
        pollCoins--;
        mSharedPreference.edit().putLong("poll_coins", pollCoins).apply();
        nCoins.setText(" X " + pollCoins);
        mFirestore.collection("USER").document(userId).update("POLL_COINS", pollCoins)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        errorOccured();
                        return;
                    }
                });
        mSharedPreference.edit().putBoolean(currentPoll.getPOLL_ID(), true).apply();
        popupCardView.setVisibility(View.GONE);
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
        submit_button_text.setText("Win 4 coins on correct prediction");
        submit_button_text.setTextColor(Color.RED);
    }

    private void checkPollStatus() {
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
                        String poll_status = (String) documentSnapshot.get("STATUS");
                        if (poll_status.equalsIgnoreCase("OPEN")) {
                            //OPEN
                            if (isPollUnlocked) {
                                //question unlocked == true
//                                isQuestionAttempted("open");
                                popupCardView.setVisibility(View.GONE);
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
                                                    submitButton.setEnabled(false);
                                                    submitButton.setBackground(getDrawable(R.drawable.gray));
                                                    submit_button_text.setText("Poll result not declared yet");
                                                    submit_button_text.setTextColor(Color.RED);
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
                                                    submit_button_text.setText("Win 4 coins on correct prediction");
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
                                // blurry UI and status: close timer
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
                                submitButton.setBackground(getDrawable(R.drawable.gray));
                                submitButton.setEnabled(false);
                                submit_button_text.setText("Close timer");
                                submit_button_text.setTextColor(Color.RED);
                                popupCardView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }
                        } else if (poll_status.equalsIgnoreCase("CLOSED")) {
                            //CLOSED
                            popupCardView.setVisibility(View.GONE);
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
                            popupCardView.setVisibility(View.GONE);
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
                                                    long correct_option = (long) currentPoll.getCORRECT_OPTION();
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
                                                            "Congrats!! You won 4 coins " + getEmojiByUnicode(0x1F603) :
                                                            "Better luck next time " + getEmojiByUnicode(0x2639);
                                                    submit_button_text.setText(status);
                                                    progressBar.setVisibility(View.GONE);
                                                } else {
                                                    // response == false
                                                    opA.setForeground(getDrawable(disabled_foreground));
                                                    opB.setForeground(getDrawable(disabled_foreground));
                                                    opC.setForeground(getDrawable(disabled_foreground));
                                                    opD.setForeground(getDrawable(disabled_foreground));
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
                                // poll not unlocked
                                opA.setForeground(getDrawable(disabled_foreground));
                                opB.setForeground(getDrawable(disabled_foreground));
                                opC.setForeground(getDrawable(disabled_foreground));
                                opD.setForeground(getDrawable(disabled_foreground));
                                submit_button_text.setText("This poll is closed");
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
    }

    private void fetchCoinsAndUserId() {
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
                        pollCoins = (long) currentDocument.get("POLL_COINS");
                        nCoins.setText(" X " + pollCoins);
                        mSharedPreference.edit().putLong("poll_coins", pollCoins).apply();
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
        nCoins.setText(" X " + mSharedPreference.getLong("poll_coins", 0));
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
        nCoins.setText(" X " + mSharedPreference.getLong("poll_coins", 10));
    }

    public String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

}