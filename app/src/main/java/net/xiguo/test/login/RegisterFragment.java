package net.xiguo.test.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import net.xiguo.test.R;
import net.xiguo.test.utils.LogUtil;

/**
 * Created by army on 2017/3/26.
 */

public class RegisterFragment extends Fragment {
    private EditText userName;
    private EditText userPass;
    private EditText userValid;
    private ImageView switchShowPass;
    private boolean showPass;
    private Button sendValid;
    private Button register;
    private TextView registerSchema;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        userName = (EditText) view.findViewById(R.id.userName);
        userPass = (EditText) view.findViewById(R.id.userPass);
        userValid = (EditText) view.findViewById(R.id.userValid);
        switchShowPass = (ImageView) view.findViewById(R.id.switchShowPass);
        sendValid = (Button) view.findViewById(R.id.sendValid);
        register = (Button) view.findViewById(R.id.register);
        registerSchema = (TextView) view.findViewById(R.id.registerSchema);

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

        registerSchema.setText(getClickableSpan(registerSchema.getText().toString()));
        registerSchema.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

    private void checkLoginButton() {
        if (userName.getText().length() == 0 || userPass.getText().length() == 0) {
            register.setEnabled(false);
        } else {
            register.setEnabled(true);
        }
    }
    private SpannableString getClickableSpan(String s) {
        SpannableString spannableString = new SpannableString(s);
        int start = 11;
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                LogUtil.i("点击协议");
            }
        }, start, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.loginBg)), start, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
}
