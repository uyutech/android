package net.xiguo.test.login;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
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

import net.xiguo.test.LoginActivity;
import net.xiguo.test.R;
import net.xiguo.test.utils.LogUtil;
import net.xiguo.test.widget.ErrorTipText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by army on 2017/3/26.
 */

public class RegisterFragment extends Fragment {
    private EditText userName;
    private EditText userPass;
    private EditText userValid;
    private ImageView switchShowPass;
    private boolean showPass;
    private ViewGroup validViewGroup;
    private Button sendValid;
    private int sendDelay;
    private Button register;
    private LoginActivity loginActivity;
    private ErrorTipText errorTipText;
    private Handler handler;
    private Runnable runnable;
    private TextView registerSchema;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        loginActivity = (LoginActivity) getActivity();
        errorTipText = loginActivity.getErrorTipText();
        handler = new Handler();

        userName = (EditText) view.findViewById(R.id.userName);
        userPass = (EditText) view.findViewById(R.id.userPass);
        userValid = (EditText) view.findViewById(R.id.userValid);
        validViewGroup = (ViewGroup) userValid.getParent();
        switchShowPass = (ImageView) view.findViewById(R.id.switchShowPass);
        sendValid = (Button) view.findViewById(R.id.sendValid);
        sendDelay = 0;
        register = (Button) view.findViewById(R.id.register);
        registerSchema = (TextView) view.findViewById(R.id.registerSchema);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                ((ViewGroup) userValid.getParent()).setBackgroundResource(R.drawable.login_field_bg_disable);
//            }
//        }, 2000);

        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkRegButton();
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
                checkRegButton();
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
                checkRegButton();
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
                sendValid.setTextSize(12);
                new CountDownTimer(600000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        sendValid.setText(--sendDelay + "秒后刷新");
                    }
                    @Override
                    public void onFinish() {
                        sendDelay = 0;
                        sendValid.setText("重新发送");
                        checkRegButton();
                    }
                }.start();
            }
        });

        registerSchema.setText(getClickableSpan(registerSchema.getText().toString()));
        registerSchema.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

    public void clearDelayShowError() {
        if(handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
    private void checkRegButton() {
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
                    errorTipText.showNeedUserName();
                }
            };
            handler.postDelayed(runnable, 300);
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
                        errorTipText.showPhoneUnValid();
                    }
                };
                handler.postDelayed(runnable, 300);
                valid = false;
            }
            else {
                errorTipText.hide();
            }
        }
        // 用户名如果正确合法，则判断密码输入状态
        if(valid) {
            // 空则清除提示信息
            if(userPassText.equals("")) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        errorTipText.showNeedUserPass();
                    }
                };
                handler.postDelayed(runnable, 300);
                valid = false;
            }
            else if(userPassText.length() < 8) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        errorTipText.showUserPassTooShort();
                    }
                };
                handler.postDelayed(runnable, 300);
                valid = false;
            }
            else {
                errorTipText.hide();
            }
        }
        // 密码也正确，放开发送验证码按钮
        if(valid) {
            // 可能上次发送没结束，判断是否倒计时到0秒
            if(sendDelay == 0) {
                sendValid.setEnabled(true);
            }
            else {
                valid = false;
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
                            errorTipText.showNeedUserValid();
                        }
                    };
                    handler.postDelayed(runnable, 300);
                    valid = false;
                } else if (userValidText.length() != 6) {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            errorTipText.showUserValidError();
                        }
                    };
                    handler.postDelayed(runnable, 300);
                    valid = false;
                } else {
                    errorTipText.hide();
                }
            }
            else if(sendDelay == 0){
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        errorTipText.showNeedSendUserValid();
                    }
                };
                handler.postDelayed(runnable, 300);
                valid = false;
            }
        }
        // 设置按钮禁用状态
        register.setEnabled(valid);
    }
    private void checkSendButton() {
        sendValid.setEnabled(userName.getText().length() == 11);
    }
    private SpannableString getClickableSpan(String s) {
        SpannableString spannableString = new SpannableString(s);
        int start = 11;
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                LogUtil.i("点击协议");
            }
            @Override
            public void updateDrawState(TextPaint textPaint) {
                textPaint.setUnderlineText(false);
            }
        }, start, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.loginBg)), start, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
}
