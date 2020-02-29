package pers.ignatius.bilibili.core;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @ClassName : GetVideoInformation
 * @Description : 处理网页,获取视频和音频链接
 * @Author : IgnatiusGL
 * @Date : 2020-02-28 15:58
 */
public class GetVideoInformation {
    private String videoUrl;//视频url
    private String audioUrl;//音频url
    private String av;//av号
    private String title;//标题

    /**
     * 分析URL,然后通过get方法获取结果
     * @param url   url
     */
    public void analyzeUrl(String url,VideoQuality videoQuality){
        this.av = url.substring(url.indexOf("av") + 2);
        System.out.println(av);
        HttpsURLConnection httpURLConnection = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManager[] tm = {new X509TrustManager(){

                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            sslContext.init(null, tm, new java.security.SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            httpURLConnection = (HttpsURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
            httpURLConnection.setRequestProperty("Content-Type", "text/html; charset=utf-8");
            httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            httpURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            httpURLConnection.setSSLSocketFactory(ssf);
            httpURLConnection.connect();
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            return;
        }
        try {
            InputStream in = httpURLConnection.getInputStream();
            GZIPInputStream gzipInputStream = new GZIPInputStream(in);
            BufferedReader bf = new BufferedReader(new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8));
            String t;
            StringBuilder res = new StringBuilder();
            while((t = bf.readLine()) != null){
                res.append(t).append("\n");
            }
            //匹配视频地址
            Pattern pattern;
            Matcher matcher;
            //选择视频清晰度
            String quality = "64";
            switch (videoQuality){
                case FHD:
                    quality = "80";
                    break;
                case HD:
                    quality = "64";
                    break;
                case SD:
                    quality = "32";
                    break;
            }
            pattern = Pattern.compile("\"id\":"+quality+",\"baseUrl\":\"[a-zA-Z0-9:/.\\-\\?=&_\\+]*\"");
            matcher = pattern.matcher(res);
            if (matcher.find()){
                String videoUrl = matcher.group(0);
                this.videoUrl = videoUrl.substring(19,videoUrl.length()-1);
                System.out.println("视频地址" + this.videoUrl);
            }
            //匹配音频地址
            pattern = Pattern.compile("\"audio\":\\[\\{\"id\":[0-9]*,\"baseUrl\":\"[a-zA-Z0-9:/.\\-\\?=&_\\+]*\"");
            matcher = pattern.matcher(res);
            if(matcher.find()){
                String audioUrl = matcher.group(0);
                this.audioUrl = audioUrl.substring(32,audioUrl.length()-1);
                System.out.println("音频地址" + this.audioUrl);
            }
            //匹配标题
            pattern = Pattern.compile("(\"title\":\").+(\",\"pubdate\")");
            matcher = pattern.matcher(res);
            if(matcher.find()){
                String audioUrl = matcher.group(0);
                this.title = audioUrl.substring(9,audioUrl.length()-11).replaceAll(" ", "");
                System.out.println("标题" + this.title);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getAv() {
        return av;
    }

    public String getTitle() {
        return title;
    }
}
