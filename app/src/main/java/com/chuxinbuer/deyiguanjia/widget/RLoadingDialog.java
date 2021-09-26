package com.chuxinbuer.deyiguanjia.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.chuxinbuer.deyiguanjia.R;

public class RLoadingDialog extends Dialog {

    private Context context;

    public RLoadingDialog(Context context, boolean cancelable) {
        super(context);
        this.context = context;
        setCancelable(cancelable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout linearLayout = new LinearLayout(getContext());
        ProgressBar progressBar = new ProgressBar(getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(context.getResources().getDimensionPixelSize(R.dimen.dp_36),
                context.getResources().getDimensionPixelSize(R.dimen.dp_36));
        params.setMargins(context.getResources().getDimensionPixelSize(R.dimen.dp_12),
                context.getResources().getDimensionPixelSize(R.dimen.dp_12),
                context.getResources().getDimensionPixelSize(R.dimen.dp_12),
                context.getResources().getDimensionPixelSize(R.dimen.dp_12));
        progressBar.setLayoutParams(params);
        linearLayout.addView(progressBar);

        setContentView(linearLayout);
    }

}
