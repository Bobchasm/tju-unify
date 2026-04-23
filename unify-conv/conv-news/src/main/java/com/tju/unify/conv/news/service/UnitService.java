package com.tju.unify.conv.news.service;

import cn.hutool.json.JSONObject;
import com.tju.unify.conv.news.config.UnitConfig;
import com.tju.unify.conv.news.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@Service
public class UnitService {

    @Autowired
    private UnitConfig unitConfig;

    public String utterance(String query, String botId, String userId) {
        String talkUrl = unitConfig.getUrl();
        String logId = UUID.randomUUID().toString();
        try {
            String params = "{\"bot_session\":\"\",\"log_id\":\"" + logId
                    + "\",\"request\":{\"bernard_level\":1,\"client_session\":\"{\\\"client_results\\\":\\\"\\\", \\\"candidate_options\\\":[]}\",\"query\":\""
                    + query
                    + "\",\"query_info\":{\"asr_candidates\":[],\"source\":\"KEYBOARD\",\"type\":\"TEXT\"},\"updates\":\"\",\"user_id\":\""
                    + userId + "\"},\"bot_id\":\"" + botId + "\",\"version\":\"2.0\"}";
            String accessToken = this.getAuth();
            return HttpUtil.post(talkUrl, accessToken, "application/json", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAuth() {
        String clientId = unitConfig.getApi_key();
        String clientSecret = unitConfig.getSecret_key();
        return getAuth(clientId, clientSecret);
    }

    public String getAuth(String ak, String sk) {
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                + "grant_type=client_credentials"
                + "&client_id=" + ak
                + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            in.close();
            JSONObject jsonObject = new JSONObject(result.toString());
            return jsonObject.getStr("access_token");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return null;
    }
}
