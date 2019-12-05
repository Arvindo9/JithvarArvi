package org.ptindia.jithvar.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import org.ptindia.jithvar.R;

/**
 * Created by Arvindo Mondal on 3/7/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */
public class VerifyLogin extends DialogFragment {

    private EditText enterOtp;
    private Button login;
    private String title;

    public interface OtpVerifyDialog{
        void onFinishOtpDialog(String inputText);
    }

    public VerifyLogin(){

    }

    public void setDialogTitle(String title){
        this.title = title;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.verify_loging, container);
//        re_send = (Button) view.findViewById(R.id.re_send_otp);
        login = (Button) view.findViewById(R.id.enter_otp);
        enterOtp = (EditText) view.findViewById(R.id.enter_otp_et);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OtpVerifyDialog otpVerifyDialog = (OtpVerifyDialog) getActivity();
                otpVerifyDialog.onFinishOtpDialog(enterOtp.getText().toString());
                dismiss();
            }
        });

        enterOtp.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().setTitle(getResources().getString(R.string.enter_otp));

        return view;

    }
}
