package in.magnumsoln.pollstest.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import in.magnumsoln.pollstest.R;

public class DemoDialog extends Dialog {

    Activity activity;
    public DemoDialog(@NonNull Context context, Activity activity) {
        super(context);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.unlock_dialog);
    }

    @Override
    public void onBackPressed() {
        activity.onBackPressed();
        dismiss();
    }
}
