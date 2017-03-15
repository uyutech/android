package net.xiguo.test;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private List<Fragment> fragments = new ArrayList<Fragment>();
    private ViewPager viewPager;
    private LinearLayout llFollow, llZhuanquan, llFind, llMy, llCurrent;
    private ImageView ivFollow, ivZhuanquan, ivFind, ivMy;
    private TextView tvFollow, tvZhuanquan, tvFind, tvMy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        llFollow = (LinearLayout) findViewById(R.id.follow);
        llZhuanquan = (LinearLayout) findViewById(R.id.zhuanquan);
        llFind = (LinearLayout) findViewById(R.id.find);
        llMy = (LinearLayout) findViewById(R.id.my);
        ivFollow = (ImageView) findViewById(R.id.followIcon);
        ivZhuanquan = (ImageView) findViewById(R.id.zhuanquanIcon);
        ivFind = (ImageView) findViewById(R.id.findIcon);
        ivMy = (ImageView) findViewById(R.id.myIcon);
        tvFollow = (TextView) findViewById(R.id.followTxt);
        tvZhuanquan = (TextView) findViewById(R.id.zhuanquanTxt);
        tvFind = (TextView) findViewById(R.id.findTxt);
        tvMy = (TextView) findViewById(R.id.myTxt);

        llFollow.setOnClickListener(this);
        llZhuanquan.setOnClickListener(this);
        llFind.setOnClickListener(this);
        llMy.setOnClickListener(this);

        llFollow.setSelected(true);
        llCurrent = llFollow;

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                changeTab(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }
    private void initData() {
        Fragment followFragment = new FollowFragment();
        Fragment zhuanquanFragment = new ZhuanquanFragment();
        Fragment findFragment = new FindFragment();
        Fragment myFragment = new MyFragment();

        fragments.add(followFragment);
        fragments.add(zhuanquanFragment);
        fragments.add(findFragment);
        fragments.add(myFragment);

        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        changeTab(id);
    }

    private void changeTab(int id) {
        if(llCurrent.getId() == id) {
            return;
        }
        Log.d("MainActivity", "changeTab " + id);

        llCurrent.setSelected(false);
        switch(id) {
            case R.id.follow:
                viewPager.setCurrentItem(0);
            case 0:
                llCurrent = llFollow;
                break;
            case R.id.zhuanquan:
                viewPager.setCurrentItem(1);
            case 1:
                llCurrent = llZhuanquan;
                break;
            case R.id.find:
                viewPager.setCurrentItem(2);
            case 2:
                llCurrent = llFind;
                break;
            case R.id.my:
                viewPager.setCurrentItem(3);
            case 3:
                llCurrent = llMy;
                break;
        }
        llCurrent.setSelected(true);
    }
}
