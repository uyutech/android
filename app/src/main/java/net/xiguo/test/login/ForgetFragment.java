package net.xiguo.test.login;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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
import net.xiguo.test.widget.ErrorTip;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by army on 2017/4/13.
 */

public class ForgetFragment extends Fragment {
    private EditText userName;
    private EditText userPass;
    private EditText userValid;
    private ImageView switchShowPass;
    private boolean showPass;
    private ViewGroup validViewGroup;
    private Button sendValid;
    private int sendDelay;
    private Button ok;
    private LoginActivity loginActivity;
    private ErrorTip errorTip;
    private Handler handler;
    private Runnable runnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forget, container, false);
        loginActivity = (LoginActivity) getActivity();
        errorTip = loginActivity.getErrorTip();
        handler = new Handler();

        userName = (EditText) view.findViewById(R.id.userName);
        userPass = (EditText) view.findViewById(R.id.userPass);
        userValid = (EditText) view.findViewById(R.id.userValid);
        validViewGroup = (ViewGroup) userValid.getParent();
        switchShowPass = (ImageView) view.findViewById(R.id.switchShowPass);
        sendValid = (Button) view.findViewById(R.id.sendValid);
        sendDelay = 0;
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
                checkOkButton();
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
                checkOkButton();
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
                checkOkButton();
            }
        });
        sendValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendValid.setEnabled(false);
                userValid.setEnabled(true);
                validViewGroup.setAlpha(1);
                sendDelay = 60;
                sendValid.setText(sendDelay + "秒后重新发送");
                new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        sendValid.setText(--sendDelay + "秒后刷新");
                    }
                    @Override
                    public void onFinish() {
                        sendDelay = 0;
                        sendValid.setText("重新发送");
                        checkOkButton();
                    }
                }.start();
            }
        });

        return view;
    }

    public void clearDelayShowError() {
        if(handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }
    private void checkOkButton() {
        String userNameText = userName.getText().toString();
        String userPassText = userPass.getText().toString();
        String userValidText = userValid.getText().toString();
        // 清除上次可能的延迟校验
        clearDelayShowError();
        boolean valid = true;
        // 空则清除提示信息
        if(userNameText.equals("")) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    errorTip.showNeedUserName();
                }
            };
            handler.postDelayed(runnable, 500);
            valid = false;
        }
        else {
            // 手机格式校验
            Pattern pattern = Pattern.compile("^1[356789]\\d{9}$");
            Matcher matcher = pattern.matcher(userNameText);
            if(!matcher.matches()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        errorTip.showPhoneUnValid();
                    }
                };
                handler.postDelayed(runnable, 500);
                valid = false;
            }
            else {
                errorTip.hide();
            }
        }
        // 用户名如果正确合法，则判断密码输入状态
        if(valid) {
            // 空则清除提示信息
            if(userPassText.equals("")) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        errorTip.showNeedUserPass();
                    }
                };
                handler.postDelayed(runnable, 500);
                valid = false;
            }
            else if(userPassText.length() < 8) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        errorTip.showUserPassTooShort();
                    }
                };
                handler.postDelayed(runnable, 500);
                valid = false;
            }
            else {
                errorTip.hide();
            }
        }
        // 密码也正确，放开发送验证码按钮
        if(valid) {
            // 可能上次发送没结束，判断是否倒计时到0秒
            if(sendDelay == 0) {
                sendValid.setEnabled(true);
            }
        }
        else if(sendDelay == 0) {
            sendValid.setEnabled(false);
        }
        // 判断输入验证码里的内容
        if(valid) {
            // 是否可用
            if(userValid.isEnabled()) {
                // 空则清除提示信息
                if (userValidText.equals("")) {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            errorTip.showNeedUserValid();
                        }
                    };
                    handler.postDelayed(runnable, 500);
                    valid = false;
                } else if (userValidText.length() != 6) {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            errorTip.showUserValidError();
                        }
                    };
                    handler.postDelayed(runnable, 500);
                    valid = false;
                } else {
                    errorTip.hide();
                }
            }
            else if(sendDelay == 0){
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        errorTip.showNeedSendUserValid();
                    }
                };
                handler.postDelayed(runnable, 500);
                valid = false;
            }
        }
        // 设置按钮禁用状态
        ok.setEnabled(valid);
    }
}
