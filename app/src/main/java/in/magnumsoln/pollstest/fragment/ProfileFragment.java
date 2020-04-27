package in.magnumsoln.pollstest.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import in.magnumsoln.pollstest.R;
import in.magnumsoln.pollstest.util.CustomDialogChangePaytmNumber;
import in.magnumsoln.pollstest.util.CustomDialogReferredId;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment
        implements CustomDialogChangePaytmNumber.ChangedPaytmNumberCallback,
        CustomDialogReferredId.ReferredDialogCallback {

    private TextView nCoins,txtRegNum,amountEarned;
    public TextView txtPaytmNo;
    private Button btnReedem,btnReferId,btnRateUs,btnShareApp;
    private ImageView imgShare,imgEditPaytmNum;
    private Context context;
    CustomDialogChangePaytmNumber.ChangedPaytmNumberCallback current;
    CustomDialogReferredId.ReferredDialogCallback referredCallback;
    private SharedPreferences mSharedPreference;
    private FirebaseFirestore mFirestore;
    private String reg_ph,paytm_no,referred_by;

    public ProfileFragment(TextView textView) { nCoins = textView; }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        txtRegNum = view.findViewById(R.id.regNo);
        txtPaytmNo = view.findViewById(R.id.paytmNo);
        amountEarned = view.findViewById(R.id.amountEarned);
        btnReedem = view.findViewById(R.id.btnReedem);
        btnReferId = view.findViewById(R.id.btnRefer);
        btnRateUs = view.findViewById(R.id.btnRate);
        btnShareApp = view.findViewById(R.id.btnShare);
        imgShare = view.findViewById(R.id.imgShare);
        imgEditPaytmNum = view.findViewById(R.id.imgEditPaytmNo);
        context = getActivity();
        current = this;
        referredCallback = this;
        mFirestore = FirebaseFirestore.getInstance();
        mSharedPreference = context.getSharedPreferences("user_details",Context.MODE_PRIVATE);
        reg_ph = mSharedPreference.getString("phone_no",null);
        setupFragment();
        referAndEarnButtonListener();
        shareAppListener();
        rateButtonListener();
        return view;
    }

    private void rateButtonListener() {
        btnRateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store")));
            }
        });
    }
    String userId;
    private void redeemButtonListener() {
        int available_coins = mSharedPreference.getInt("available_coins",0);
        if(available_coins >= 100){
            btnReedem.setForeground(null);
            btnReedem.setEnabled(true);
        }
        btnReedem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                int av_cns = mSharedPreference.getInt("available_coins",0);
                btnReedem.setEnabled(false);
                btnReedem.setText("Wait...");
               mFirestore.collection("USER").document(userId).get()
                       .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                           @Override
                           public void onSuccess(DocumentSnapshot documentSnapshot) {
                               final long coin_redeemed = (long) documentSnapshot.get("COINS_REDEEMED");
                               long share_coin = (long) documentSnapshot.get("SHARE_COIN");
                               long poll_coin = (long) documentSnapshot.get("POLL_COINS");
                               final long used_coin = (long) documentSnapshot.get("COINS_USED");
                               final long available_coin = share_coin + poll_coin - used_coin ;
                               mFirestore.collection("USER").document(userId)
                                       .update("COINS_REDEEMED",coin_redeemed+100)
                                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void aVoid) {
                                               mFirestore.collection("USER").document(userId)
                                                       .update("COINS_USED",used_coin+100)
                                                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                           @Override
                                                           public void onSuccess(Void aVoid) {
                                                               mFirestore.collection("USER")
                                                                       .document(userId)
                                                                       .update("MONEY_REQUESTED",true)
                                                                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                           @Override
                                                                           public void onSuccess(Void aVoid) {
                                                                               mSharedPreference.edit()
                                                                                       .putInt("available_coins",(int)available_coin-100)
                                                                                       .apply();
                                                                               amountEarned.setText("Rs. "+(coin_redeemed+100));
                                                                               nCoins.setText(" X "+(available_coin-100));
                                                                               btnReedem.setText("Redeem 100");
                                                                               if((available_coin-100) >= 100){
                                                                                   btnReedem.setEnabled(true);
                                                                                   btnReedem.setForeground(null);
                                                                               }else{
                                                                                   btnReedem.setEnabled(false);
                                                                                   btnReedem.setForeground(context
                                                                                           .getDrawable(R.drawable.foreground_disabled));
                                                                               }
                                                                               showSuccessDialog();
                                                                           }
                                                                       })
                                                                       .addOnFailureListener(new OnFailureListener() {
                                                                           @Override
                                                                           public void onFailure(@NonNull Exception e) {
                                                                               Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
                                                                               btnReedem.setEnabled(true);
                                                                               btnReedem.setText("Redeem 100");
                                                                           }
                                                                       });
                                                           }
                                                       })
                                                       .addOnFailureListener(new OnFailureListener() {
                                                   @Override
                                                   public void onFailure(@NonNull Exception e) {
                                                       Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
                                                       btnReedem.setEnabled(true);
                                                       btnReedem.setText("Redeem 100");
                                                   }
                                               });
                                           }
                                       })
                                       .addOnFailureListener(new OnFailureListener() {
                                           @Override
                                           public void onFailure(@NonNull Exception e) {
                                               Toast.makeText(context, "Some error occured", Toast.LENGTH_SHORT).show();
                                               btnReedem.setEnabled(true);
                                               btnReedem.setText("Redeem 100");
                                           }
                                       });
                           }
                       })
                       .addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               Toast.makeText(context,"Some error occured.",Toast.LENGTH_SHORT).show();
                               btnReedem.setEnabled(true);
                               btnReedem.setText("Redeem 100");
                           }
                       });

            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Success")
                .setMessage("Your Requested Is Submitted. You will receive the amount within 7 working days.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss
                    }
                })
                .create()
                .show();
    }

    private void shareAppListener() {
        btnShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Polls");
                    String shareMessage = "Predict and win Paytm cash. Download Polls\n\n https://play.google.com/store/ \n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Share using:"));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void referAndEarnButtonListener() {
        imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Polls");
                    String shareMessage= "Predict and win Paytm cash. Download Polls\n\n https://play.google.com/store/ \n\n";
                    shareMessage = shareMessage + "Use refer ID "+ reg_ph +" to get referral bonus.";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Share using:"));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupFragment() {

        txtRegNum.setText(reg_ph);
        txtPaytmNo.setText("Fetching data...");
        amountEarned.setText("Fetching data...");
        mFirestore.collection("USER").whereEqualTo("PHONE_NUMBER",reg_ph).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        DocumentSnapshot curr = queryDocumentSnapshots.getDocuments().get(0);
                        paytm_no = (String) curr.get("PAYTM_NUMBER");
                        long coin_redeemed = (long) curr.get("COINS_REDEEMED");
                        referred_by = (String) curr.get("REFERRED_BY");
                        long coins_used = (long) curr.get("COINS_USED");
                        long share_coins = (long) curr.get("SHARE_COIN");
                        long poll_coins = (long) curr.get("POLL_COINS");
                        int available_coins = (int)share_coins + (int)poll_coins - (int)coins_used;
                        txtPaytmNo.setText(paytm_no);
                        amountEarned.setText("Rs. "+coin_redeemed);
                        mSharedPreference.edit().putInt("available_coins",available_coins).apply();
                        editButtonListener();
                        referIdListener();
                        userId = curr.getId();
                        redeemButtonListener();
//                        redeemButtonListener(available_coins,coin_redeemed,coins_used,curr.getId());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"Some error occured",Toast.LENGTH_SHORT);
            }
        });
    }

    private void referIdListener() {
        btnReferId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CustomDialogReferredId(getActivity(),reg_ph,paytm_no,referred_by,referredCallback).show();
            }
        });
    }

    private void editButtonListener() {
        imgEditPaytmNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialogChangePaytmNumber changeDialog =
                        new CustomDialogChangePaytmNumber(getActivity(),current,txtPaytmNo.getText().toString());
                changeDialog.show();
            }
        });
    }

    @Override
    public void changedPaytmNum(String paytmNum) {
        txtPaytmNo.setText(paytmNum);
        txtPaytmNo.setTextColor(context.getColor(R.color.colorAccent));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                txtPaytmNo.setTextColor(Color.parseColor("#000000"));
            }
        },1000);
    }


    @Override
    public void referComplete(long shareCoins,String referredBy) {
        referred_by = referredBy;
    }
}
