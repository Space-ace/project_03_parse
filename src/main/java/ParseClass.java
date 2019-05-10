import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Класс который парсит страницу с товаром
 */
class ParseClass {
    static boolean isFirst = true;
    private String title;
    private String brand;
    private String scale;
    private String released;
    private String type;
    private String status;
    private String scalematesUrl;
    private String description;
    private String boxartUrl;
    private String number;

    //Метод принимающий url и вынимающий из него необходимые записи
    ParseClass(String url) throws IOException {
        Document doc = Jsoup.connect("https://www.scalemates.com" + url).timeout(15000).get();
        Elements table = doc.select("div#cont table tr");
        Elements img = doc.select(" div.resp-ar img[src$=.jpg]");
        boxartUrl = img.attr("src");
        scalematesUrl = url;
        //парсинг ссылки на img, убираем конечные неизменные элементы ~.png
        String[] hyphenPars = boxartUrl.split("-");
        boxartUrl ="https://www.scalemates.com" + boxartUrl.replace("-"+ hyphenPars[hyphenPars.length-1],"");
        for (Element link : table) {
            if (link.select("td").first().text().startsWith("Brand"))
                brand = link.select("td").get(1).text();
            if (link.select("td").first().text().startsWith("Title"))
                title = link.select("td").get(1).text();
            if (link.select("td").first().text().startsWith("Scale")) {
                String[] splitPer = link.select("td").get(1).text().split(":");
                if(splitPer.length == 1)
                scale = link.select("td").get(1).text();
                if(splitPer.length == 2)
                    scale =splitPer[1];
            }
            if (link.select("td").first().text().startsWith("Number"))
                number = link.select("td").get(1).text().replace(" ","").toUpperCase();
            if (link.select("td").first().text().startsWith("Released")) {
                String[] relSplit = link.select("td").get(1).text().split("\\|");
                if(relSplit.length == 2)
                {
                released = relSplit[0];
                description = relSplit[1];}
                else released = link.select("td").get(1).text();
            }
            if (link.select("td").first().text().startsWith("Type"))
                type = link.select("td").get(1).text();
            if (link.select("td").first().text().startsWith("Status"))
                status = link.select("td").get(1).text();
        }
    }

    //запись в файл
    void witeJson(String path) {
        JSONObject obj = new JSONObject();
        obj.put("title "  , title);
        obj.put("brand "  , brand);
        obj.put("number "  , number);
        obj.put("status "  , status);
        obj.put("scale "  , scale);
        obj.put("released "  , released);
        obj.put("description "  , description);
        obj.put("type "  , type);
        obj.put("scalematesUrl" , scalematesUrl);
        obj.put("image "  , boxartUrl);
        try (FileWriter file = new FileWriter(path,true)) {
            if (isFirst) {
                file.write(obj.toString());
                isFirst = false;
            } else
                file.write("," +"\r\n" + obj.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
