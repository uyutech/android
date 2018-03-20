package cc.circling.utils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by army8735 on 2018/3/20.
 */

public class QueryParser {
    public static HashMap<String, String> parse(String query) {
        if(query != null && !query.isEmpty()) {
            HashMap<String, String> hashMap = new HashMap<>();
            String[] params = query.split("&");
            for(String param : params) {
                String[] kv = param.split("=");
                String k = kv[0];
                String v = kv.length > 1 ? kv[1] : "";
                hashMap.put(k, v);
            }
            return hashMap;
        }
        return null;
    }
}
