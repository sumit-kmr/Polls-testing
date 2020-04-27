package in.magnumsoln.pollstest.util;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import in.magnumsoln.pollstest.R;

public class CustomDialogReferredId extends Dialog implements android.view.View.OnClickListener {

    private Activity activity;
    private String registeredNumber,paytmNumber,referredBy;
    private LinearLayout referIdContainer;
    private EditText edtReferId;
    private Button cancel_refer,cancel_error,submit_refer;
    private RelativeLayout progressBar,dialogErrorContainer;
    private TextView errorMessage;
    private FirebaseFirestore mFirestore;
    private ReferredDialogCallback referredDialogCallback;

    public CustomDialogReferredId(Activity activity,String reg_ph,String paytm_no,String referred_by,ReferredDialogCallback callback){
        super(activity);
        this.activity = activity;
        this.registeredNumber = reg_ph;
        this.paytmNumber = paytm_no;
        this.referredBy = referred_by;
        this.referredDialogCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_referred_id);
        referIdContainer = findViewById(R.id.referIdContainer);
        edtReferId = findViewById(R.id.edtReferId);
        cancel_error = findViewById(R.id.btnErrorDismiss);
        cancel_refer = findViewById(R.id.btnCancelReferId);
        submit_refer = findViewById(R.id.btnSubmitReferId);
        progressBar = findViewById(R.id.dialogProgressBar);
        dialogErrorContainer = findViewById(R.id.dialogErrorContainer);
        errorMessage = findViewById(R.id.errorMsg);
        mFirestore = FirebaseFirestore.getInstance();

        submit_refer.setOnClickListener(this);
        cancel_refer.setOnClickListener(this);
        cancel_error.setOnClickListener(this);
        if(!referredBy.equalsIgnoreCase("Nobody")){
            errorMessage.setText("You are already referred by "+referredBy);
            dialogErrorContainer.setVisibility(View.VISIBLE);
        }else{
            referIdContainer.setVisibility(View.VISIBLE);
            edtReferId.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence referId, int i, int i1, int i2) {
                    if(referId.length() == 10){
                        submit_refer.setEnabled(true);
                        submit_refer.setForeground(null);
                    }else{
                        submit_refer.setEnabled(false);
                        submit_refer.setForeground(activity.getDrawable(R.drawable.foreground_disabled));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) { }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCancelReferId:
                dismiss();
                break;
            case R.id.btnErrorDismiss:
                dismiss();
                break;
            case R.id.btnSubmitReferId:
                final String enteredReferId = edtReferId.getText().toString();
                if((enteredReferId.equalsIgnoreCase(registeredNumber)) ||
                    enteredReferId.equalsIgnoreCase(paytmNumber)){
                    Toast.makeText(activity,"Invalid refer id",Toast.LENGTH_SHORT).show();
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    referIdContainer.setVisibility(View.GONE);
                    mFirestore.collection("USER").whereEqualTo("PHONE_NUMBER",enteredReferId).get()
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(activity,"Some error occured. Try again",Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    referIdContainer.setVisibility(View.VISIBLE);
                                }
                            }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(queryDocumentSnapshots.isEmpty()){
                                Toast.makeText(activity,"Invalid refer id",Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                referIdContainer.setVisibility(View.VISIBLE);
                            }else{
                                DocumentSnapshot curr_doc = queryDocumentSnapshots.getDocuments().get(0);
                                String refer_id = (String) curr_doc.get("REFERRED_BY");
                                long share_coin = (long) curr_doc.get("SHARE_COIN");
                                if(refer_id.equalsIgnoreCase(registeredNumber) ||
                                   refer_id.equalsIgnoreCase(paytmNumber)){
                                    Toast.makeText(activity,"Invalid refer id",Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    referIdContainer.setVisibility(View.VISIBLE);
                                    return;
                                }
                                else if(share_coin > 100){
                                    Toast.makeText(activity,"Your friend has crossed max. refer limit",Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    referIdContainer.setVisibility(View.VISIBLE);
                                }else{
                                    mFirestore.collection("USER").document(curr_doc.getId())
                                            .update("SHARE_COIN",share_coin+5)
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.GONE);
                                                    referIdContainer.setVisibility(View.VISIBLE);
                                                    Toast.makeText(activity,"Some error occured. Try again",Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mFirestore.collection("USER")
                                                            .whereEqualTo("PHONE_NUMBER",registeredNumber).get()
                                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                    DocumentSnapshot curr_doc = queryDocumentSnapshots.getDocuments().get(0);
                                                                    final long share_coin = (long) curr_doc.get("SHARE_COIN");
                                                                    mFirestore.collection("USER").document(curr_doc.getId())
                                                                            .update("REFERRED_BY",enteredReferId)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    //coins added to wallet
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(activity,"Some error occured",Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                    mFirestore.collection("USER").document(curr_doc.getId())
                                                                            .update("SHARE_COIN",share_coin+5)
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(activity,"Some error occured",Toast.LENGTH_SHORT).show();
                                                                                    referIdContainer.setVisibility(View.VISIBLE);
                                                                                    progressBar.setVisibility(View.GONE);

                                                                                }
                                                                            })
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    referIdContainer.setVisibility(View.GONE);
                                                                                    errorMessage.setText("Congratulations!! 5 coins added to wallet");
                                                                                    progressBar.setVisibility(View.GONE);
                                                                                    dialogErrorContainer.setVisibility(View.VISIBLE);
                                                                                    referredDialogCallback.referComplete(share_coin+5,enteredReferId);
                                                                                }
                                                                            });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(activity, "Some error occured.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            });
                                }
                            }
                        }
                    });
                }
                break;
        }

    }

    public interface ReferredDialogCallback{void referComplete(long shareCoins,String referredBy);}
}
