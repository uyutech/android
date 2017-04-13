package net.xiguo.test.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import net.xiguo.test.LoginActivity;
import net.xiguo.test.R;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/4/13.
 */

public class ForgetFragment extends Fragment {
    private EditText userName;
    private EditText userPass;
    private EditText userValid;
    private ImageView switchShowPass;
    private boolean showPass;
    private Button sendValid;
    private Button ok;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forget, container, false);

        userName = (EditText) view.findViewById(R.id.userName);
        userPass = (EditText) view.findViewById(R.id.userPass);
        userValid = (EditText) view.findViewById(R.id.userValid);
        switchShowPass = (ImageView) view.findViewById(R.id.switchShowPass);
        sendValid = (Button) view.findViewById(R.id.sendValid);
        ok = (Button) view.findViewById(R.id.ok);

        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkLoginButton();
            }
        });
        userPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkLoginButton();
            }
        });

        showPass = false;
        switchShowPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showPass) {
                    userPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    switchShowPass.setBackgroundResource(R.drawable.pass_visible);
                }
                else {
                    userPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    switchShowPass.setBackgroundResource(R.drawable.pass_invisible);
                }
                userPass.setSelection(userPass.getText().toString().length());
                showPass = !showPass;
            }
        });

        userValid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                sendValid.setEnabled(userValid.getText().length() == 6);
            }
        });
        sendValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        checkLoginButton();
        checkSendButton();

        return view;
    }

    private void checkLoginButton() {
        if (userName.getText().length() != 11 || userPass.getText().length() < 8 || userValid.getText().length() != 6) {
            ok.setEnabled(false);
        } else {
            ok.setEnabled(true);
        }
    }
    private void checkSendButton() {
        sendValid.setEnabled(userName.getText().length() == 11);
    }
}
