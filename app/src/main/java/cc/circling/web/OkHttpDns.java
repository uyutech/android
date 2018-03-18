package cc.circling.web;

import android.content.Context;

import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cc.circling.BaseApplication;
import cc.circling.utils.LogUtil;
import okhttp3.Dns;

/**
 * Created by army8735 on 2018/2/1.
 */

public class OkHttpDns implements Dns {
    private HttpDnsService httpDns;//httpdns 解析服务
    private static OkHttpDns instance = null;
    private OkHttpDns(Context context) {
        httpDns = HttpDns.getService(context, "149110");
        httpDns.setHTTPSRequestEnabled(true);
        ArrayList<String> hostList = new ArrayList<>(Arrays.asList("circling.net.cn", "circling.cc", "zhuanquan.net.cn"));
        httpDns.setPreResolveHosts(hostList);
    }
    public static OkHttpDns getInstance() {
        if(instance == null) {
            instance = new OkHttpDns(BaseApplication.getContext());
        }
        return instance;
    }
    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        LogUtil.d("lookup", hostname);
        //通过异步解析接口获取ip
        String ip = httpDns.getIpByHostAsync(hostname);
        if(ip != null) {
            //如果ip不为null，直接使用该ip进行网络请求
            List<InetAddress> inetAddresses = Arrays.asList(InetAddress.getAllByName(ip));
            LogUtil.d("OkHttpDns", "inetAddresses:" + inetAddresses);
            return inetAddresses;
        }
        //如果返回null，走系统DNS服务解析域名
        return Dns.SYSTEM.lookup(hostname);
    }
}
