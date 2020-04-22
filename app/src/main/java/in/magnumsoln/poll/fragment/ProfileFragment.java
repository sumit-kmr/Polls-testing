package in.magnumsoln.poll.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import in.magnumsoln.poll.R;
import in.magnumsoln.poll.util.CustomDialogChangePaytmNumber;
import in.magnumsoln.poll.util.CustomDialogReferredId;

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
        return view;
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
                        txtPaytmNo.setText(paytm_no);
                        amountEarned.setText("Rs. "+coin_redeemed);
                        editButtonListener();
                        referIdListener();
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
