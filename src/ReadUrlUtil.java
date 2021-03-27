////import java.io.BufferedReader;
////import java.io.IOException;
////import java.io.InputStream;
////import java.io.InputStreamReader;
////import java.net.URL;
////import java.nio.charset.Charset;
//
//package json;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//public class Demo2
//{
//    public static void main(String[] args) throws Exception
//    {
//        //参数字符串，如果拼接在请求链接之后，需要对中文进行 URLEncode   字符集 UTF-8
//        String param = "location=108.62 ,21.95&key=写你自己的key";
//        StringBuilder sb = new StringBuilder();
//        InputStream is = null;
//        BufferedReader br = null;
//        PrintWriter out = null;
//        try {
//            //接口地址
//            String url = "https://api.heweather.net/s6/weather/grid-minute";
//            URL    uri = new URL(url);
//            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setReadTimeout(5000);
//            connection.setConnectTimeout(10000);
//            connection.setRequestProperty("accept", "*/*");
//            //发送参数
//            connection.setDoOutput(true);
//            out = new PrintWriter(connection.getOutputStream());
//            out.print(param);
//            out.flush();
//            //接收结果
//            is = connection.getInputStream();
//            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//            String line;
//            //缓冲逐行读取
//            while ( ( line = br.readLine() ) != null )
//            {
//                sb.append(line);
//            }
//            String backStr=sb.toString();
//            System.out.println(backStr);
//            JSONObject jsonObject = JSONObject.parseObject(backStr);
//            JSONArray heWeather6 = jsonObject.getJSONArray("HeWeather6");
//            System.out.println(heWeather6);
//            JSONObject jsonObject1 = heWeather6.getJSONObject(0);
//            System.out.println(jsonObject1);
//            JSONObject basic = jsonObject1.getJSONObject("basic");
//            System.out.println(basic);
//            String parent_city = basic.getString("parent_city");
//            System.out.println(parent_city);
//            String lon = basic.getString("lon");
//            System.out.println(lon);
//            String lat = basic.getString("lat");
//            System.out.println(lat);
//
//            System.out.println("--------------");
//            JSONArray pcpn_5m = jsonObject1.getJSONArray("pcpn_5m");
//            for (int i = 0; i < pcpn_5m.size(); i++)
//            {
//                JSONObject jsonObject2 = pcpn_5m.getJSONObject(i);
//                System.out.println(jsonObject2);
//
//            }
//        } catch ( Exception e )
//        {
//            System.out.println(e);
//        } finally
//        {
//            //关闭流
//            try
//            {
//                if(is!=null)
//                {
//                    is.close();
//                }
//                if(br!=null)
//                {
//                    br.close();
//                }
//                if (out!=null)
//                {
//                    out.close();
//                }
//            } catch ( Exception ignored )
//            {
//                System.out.println(ignored);
//            }
//        }
//
//
//    }
//}
//
//
