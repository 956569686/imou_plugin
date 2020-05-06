package xl.honggv.cameraimouplugin;

import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

public class OpenApiHelper {

    public final static String tag = "OpenApiHelper";


    public static void getAccessToken(final String host,
                                      final String phoneNumber, final String appid,
                                      final String appsecret, final Handler handler) {
        // 拼装url
        String url = "";
        if (host.endsWith(":443")) {
            url = "https://" + host + "/openapi/accessToken";
        } else {
            url = "http://" + host + "/openapi/accessToken";
        }
        Log.d(tag, url);
        getToken(url, phoneNumber, appid, appsecret, handler);
    }

    private static void getToken(final String host, final String phoneNumber,
                                 final String appid, final String appsecret, final Handler handler) {

        int code = -1;
        String result = null;
        try {
            JSONObject body = new JSONObject();
            String data = "{phone:\"" + phoneNumber + "\"}";
            body.put("params", new JSONObject(data));
            body.put("id", "1");// id号 随机值
            body.put(
                    "system",
                    new JSONObject(SignHelper.getSystem(data, appid, appsecret,
                            "1.1")));

            String response = HttpUtils.getInstance().postString(host, body.toString());

            if(response != null){
                JSONObject res = new JSONObject((String) response);
                if (res.getJSONObject("result").getString("code").equals("0")) {
                    code = 0;
                    if (res.getJSONObject("result").getJSONObject("data")
                            .has("accessToken")) {
                        result = res.getJSONObject("result")
                                .getJSONObject("data")
                                .getString("accessToken");
                    } else {
                        result = res.getJSONObject("result")
                                .getJSONObject("data")
                                .getString("userToken");
                    }
                } else {
                    code = -1;
                    result = res.getJSONObject("result").getString("msg");
                    // 界面展示和业务需要 特殊处理
                    if (res.getJSONObject("result").getString("code")
                            .equals("TK1004"))
                        code = 1;
                    if (res.getJSONObject("result").getString("code")
                            .equals("TK1006"))
                        code = 1;

                }
            }else{
                code = -1;
                result = "get Token failed,Response is null";
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        }

        handler.obtainMessage(code, result).sendToTarget();

    }

}
