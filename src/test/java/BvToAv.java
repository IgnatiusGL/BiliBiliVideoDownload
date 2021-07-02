import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BvToAv {
    public static void main(String[] args) throws Exception {
        URL url;
        url = new URL("https://api.bilibili.com/x/web-interface/view?bvid=BV1k54y1J7vd");
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
        String data = sb.toString();
        System.out.println(data);
        data = data.substring(data.indexOf("aid") + 5);
        System.out.println(data.substring(0, data.indexOf(",\"")));
        is.close();
        br.close();
    }
}