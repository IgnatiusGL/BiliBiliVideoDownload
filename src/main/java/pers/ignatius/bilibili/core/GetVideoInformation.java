package pers.ignatius.bilibili.core;

import pers.ignatius.bilibili.exception.AnalyzeUrlException;
import pers.ignatius.bilibili.exception.InternetException;
import pers.ignatius.bilibili.exception.UnknownException;
import pers.ignatius.bilibili.exception.WebsiteNotEndWithAvException;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    public List<VideoInformation> getVideoInformationFromUrl(String url, VideoQuality videoQuality) throws InternetException, WebsiteNotEndWithAvException, UnknownException, AnalyzeUrlException {
        List<VideoInformation> videoInformations = new ArrayList<>();
        //检查网址是否符合标准
        url = findText("https://www.bilibili.com/video/BV[\\d\\w]*", url);
        if (url == null) {
            throw new WebsiteNotEndWithAvException();
        }
        try {
            url = "https://www.bilibili.com/video/av" + bvToAv(url.substring(33));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(url);
        //检查视频清晰度
        if (videoQuality == null)
            videoQuality = VideoQuality.HIGHEST_QUALITY;

        //获取网页源代码
        String page = getWebsiteSource(url);
        //获取视频的全部视频
        getVideosInformation(url,page,videoInformations);
        //分析所有视频
        for (int i=0;i<videoInformations.size();i++){
            analyzeUrl(videoQuality,videoInformations.get(i));
        }
        return videoInformations;
    }

    /**
     * 分析网页获取视频和音频信息
     * @param quality   视频质量
     * @param videoInformation  视频信息
     * @throws InternetException    网络异常
     * @throws AnalyzeUrlException  分析网址异常
     */
    private void analyzeUrl(VideoQuality quality,VideoInformation videoInformation) throws InternetException, AnalyzeUrlException {
        System.out.println(videoInformation.getUrl());
        //获取网页源代码
        String page = getWebsiteSource(videoInformation.getUrl());

        //匹配视频地址
        int code = quality.getCode();
        if (quality == VideoQuality.HIGHEST_QUALITY){
            //找到质量最好
            String q = findText("\"accept_quality\":\\[[0-9,]*\\]",page);
            if (q == null)
                throw new AnalyzeUrlException();
            if (q.contains(VideoQuality.P1080.getCode() + ""))
                code = VideoQuality.P1080.getCode();
            else if (q.contains(VideoQuality.P720.getCode() + ""))
                code = VideoQuality.P720.getCode();
            else if (q.contains(VideoQuality.P480.getCode() + ""))
                code = VideoQuality.P480.getCode();
            else if (q.contains(VideoQuality.P360.getCode() + ""))
                code = VideoQuality.P360.getCode();
        }
        System.out.println("视频质量:" + code);
        String videoUrl = findText("\"id\":"+code+",\"baseUrl\":\"[\\s\\S]+?\"",page);
        if (videoUrl == null)//判空
            throw new AnalyzeUrlException();
        videoInformation.setVideoUrl(videoUrl.substring(19,videoUrl.length()-1));

        //匹配音频地址
        String audioUrl = findText("\"audio\":\\[\\{\"id\":\\d*,\"baseUrl\":\"[\\s\\S]+?\"",page);
        if (audioUrl == null)//判空
            throw new AnalyzeUrlException();
        videoInformation.setAudioUrl(audioUrl.substring(32,audioUrl.length()-1));
    }

    /**
     * 获取视频信息,包括标题,地址
     * @param page 页面
     * @param videoInformations 视频信息
     * @throws UnknownException 未知异常
     */
    private void getVideosInformation(String url,String page,List<VideoInformation> videoInformations) throws UnknownException, InternetException {
        String np = findText("<ul class=\"list-box\">[\\s\\S]+?</ul>", page);
        if (np != null){//多P视频
            System.out.println("多视频");
            //获取视频列表的json
            url = findText("(https://)?www.bilibili.com/video/av\\d*", url);
            //匹配信息
            Pattern pattern = Pattern.compile("\"part\":\"[\\s\\S]+?\"");
            Matcher matcher = pattern.matcher(page);
            for (int i=0;matcher.find();i++){
                String title = matcher.group();
                VideoInformation videoInformation = new VideoInformation();
                videoInformation.setTitle(("P" + (i + 1) + "-" + title.substring(8,title.length()-1)).replaceAll(" ", ""));
                videoInformation.setUrl(url + "?p=" + (i + 1));
                videoInformations.add(videoInformation);
            }
        }else {//单独视频
            System.out.println("单视频");
            //匹配标题
            String title = findText("(\"title\":\").+(\",\"pubdate\")",page);
            if(title == null)//判空
                throw new UnknownException();
            VideoInformation videoInformation = new VideoInformation();
            videoInformation.setTitle(title.substring(9,title.length()-11).replaceAll(" ", ""));
            videoInformation.setUrl(url);
            videoInformations.add(videoInformation);
        }
    }

    /**
     * 正则匹配找第一个
     * @param reger 正则表达式
     * @param page  匹配的页面
     * @return  匹配的文字
     */
    private String findText(String reger,String page){
        Pattern pattern = Pattern.compile(reger);
        Matcher matcher = pattern.matcher(page);
        if(matcher.find()){
            return matcher.group(0);
        }
        return null;
    }

    /**
     * 获取网页源代码
     * @param url   网址
     * @return  源代码
     * @throws InternetException    网络异常
     */
    private String getWebsiteSource(String url) throws InternetException {
        //连接网站
        HttpsURLConnection httpURLConnection;
        //自动携带cookie
        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);
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
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
            httpURLConnection.setRequestProperty("Content-Type", "text/html; charset=utf-8");
            httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            httpURLConnection.setRequestProperty("Accept", "*/*");
            httpURLConnection.setRequestProperty("Connection", "close");
            httpURLConnection.setRequestProperty("Host", "www.bilibili.com");
            httpURLConnection.setSSLSocketFactory(ssf);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() != 200){
                System.out.println(httpURLConnection.getResponseCode() + ":" + httpURLConnection.getResponseMessage());
                throw new InternetException();
            }
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            throw new InternetException();
        }
        String page;
        try {
            InputStream in = httpURLConnection.getInputStream();

            GZIPInputStream gzipInputStream = new GZIPInputStream(in);
            BufferedReader bf = new BufferedReader(new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8));
            String t;
            StringBuilder res = new StringBuilder();
            while((t = bf.readLine()) != null){
                res.append(t).append("\n");
            }
            page = res.toString();
            //防止和谐 100~300 ms
            Thread.sleep((new Random().nextInt(2) + 1)*100);
            bf.close();
            in.close();
            httpURLConnection.disconnect();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new InternetException();
        }
        return page;
    }

    private String bvToAv(String bv) throws IOException {
        URL url;
        url = new URL("https://api.bilibili.com/x/web-interface/view?bvid=BV" + bv);
        System.out.println(url);
        HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
        InputStream is = httpUrl.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            line = line.replaceAll("</?a[^>]*>", "");
            line = line.replaceAll("<(\\w+)[^>]*>", "<$1>");
            sb.append(line);
        }
        is.close();
        br.close();
        String data = sb.toString();
        System.out.println(data);
        data = data.substring(data.indexOf("aid") + 5);
        return data.substring(0, data.indexOf(",\""));
    }
}