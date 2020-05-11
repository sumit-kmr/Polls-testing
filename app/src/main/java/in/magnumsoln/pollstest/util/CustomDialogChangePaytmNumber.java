package in.magnumsoln.pollstest.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import in.magnumsoln.pollstest.R;

public class CustomDialogChangePaytmNumber extends Dialog implements
        android.view.View.OnClickListener {

    private Activity c;
    public Dialog d;
    private Button update, cancel;
    private EditText paytmNum;
    private FirebaseFirestore mFirestore;
    private SharedPreferences mSharedPreferences;
    private ChangedPaytmNumberCallback callback;
    private String currentPaytm;

    public CustomDialogChangePaytmNumber(Activity a, ChangedPaytmNumberCallback callback, String currentPaytm) {
        super(a);
        this.c = a;
        this.callback = callback;
        this.currentPaytm = currentPaytm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_paytm_number);
        update = (Button) findViewById(R.id.btnUpdate);
        cancel = (Button) findViewById(R.id.btnCancel);
        paytmNum = findViewById(R.id.edtChangePaytmNo);
        mFirestore = FirebaseFirestore.getInstance();
        mSharedPreferences = c.getSharedPreferences("user_details", Context.MODE_PRIVATE);
        update.setOnClickListener(this);
        cancel.setOnClickListener(this);
        paytmNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {
                if (text.length() == 10 && !(text.toString().equalsIgnoreCase(currentPaytm))) {
                    update.setEnabled(true);
                    update.setForeground(null);
                } else {
                    if (text.toString().equalsIgnoreCase(currentPaytm))
                        Toast.makeText(c, "Same as previous paytm number", Toast.LENGTH_SHORT).show();
                    update.setEnabled(false);
                    update.setForeground(c.getDrawable(R.drawable.foreground_disabled));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btnUpdate:
                    if(!InternetChecker.isInternetAvailable(c))
                    {
                        Toast.makeText(c, "You're offline", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final String enteredNumber = paytmNum.getText().toString();
                    String registeredNumber = mSharedPreferences.getString("phone_no", null);
                    if (currentPaytm.equalsIgnoreCase(enteredNumber)) {
                        Toast.makeText(c, "Enter a different number", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    update.setText("Wait...");
                    update.setEnabled(false);
                    mFirestore.collection("USER").whereEqualTo("PHONE_NUMBER", registeredNumber)
                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            DocumentSnapshot curr = queryDocumentSnapshots.getDocuments().get(0);
                            String doc_id = curr.getId();
                            mFirestore.collection("USER").document(doc_id).update("PAYTM_NUMBER", enteredNumber)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(c, "Some error occured. Try again", Toast.LENGTH_SHORT).show();
                                            update.setEnabled(true);
                                            update.setForeground(null);
                                        }
                                    })
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(c, "Paytm number updated", Toast.LENGTH_SHORT).show();
                                            callback.changedPaytmNum(enteredNumber);
                                            dismiss();
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(c, "Some error occured. Try again", Toast.LENGTH_SHORT).show();
                            update.setEnabled(true);
                            update.setForeground(null);
                        }
                    });
                    break;
                case R.id.btnCancel:
                    dismiss();
                    break;
                default:
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(c, "Some error occured", Toast.LENGTH_SHORT).show();
        }
    }


    public interface ChangedPaytmNumberCallback {
        public void changedPaytmNum(String paytmNum);
    }
}