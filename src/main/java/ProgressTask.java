import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ProgressTask extends Task {
    private static boolean firstWrite  = true;
    static int goodCount;
    static String filePath;
    static long timeSpent;
    ArrayList<ParseClass> parse;
    int i = 0;
    int allGoods;
    int allPage;

    @Override
    protected Integer call() throws Exception {
        updateProgress(0, goodCount);
        //получаем путь к исполняемому файлу
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        filePath = URLDecoder.decode(path, "UTF-8").substring(1).replace("web.jar" , "").replace("/","\\") + "goodsInfo.txt";
        long startTime = System.currentTimeMillis();
        int pageCode  = 0; //код в запросе с помощью которого можно осуществлять переход по подгружаемым ресурсам
        int goodCountDuplicate = goodCount - 125; // Переменная необходимая для поиска количества pageCountForLoad
        allGoods = goodCount;
        int pageCountForLoad = 1;// количество страниц необходимых для парсинга
        ArrayList<Thread> threads = new ArrayList<>();
        //находим количество страниц, на одной странице умещается 125 товаров, следовательно уменьшая количество товаров на 125 можно вычислить количество страниц
        while(goodCountDuplicate > 0) {
            goodCountDuplicate = goodCountDuplicate - 125;
            pageCountForLoad++;
        }
        allPage = pageCountForLoad;
        fileWork(filePath);
        Document doc;
        parse = new ArrayList<>();
        while (pageCountForLoad > 0) {
            doc = Jsoup.connect("https://www.scalemates.com/search.php?q=*&sortby=date&page=news&fkSECTION[]=Kits&fkYEAR[]=2017&fkYEAR[]=2018&fkYEAR[]=2019&mode=ajax&start=" + pageCode + "&df=ok").get();
            pageCode = pageCode + 125;// прибовлем к коду 125 для перехода к следующим товра
            Elements links = doc.select("div.ac  div.ar a[href]");//достаем элемент <a> содержащий сылку на товар
            if (pageCountForLoad == 1) {
                for (int j = 0; j < allGoods - (allPage - 1) * 125; j++) {
                    Runnable rannable = new ParseThread(links.get(j));
                    threads.add(new Thread(rannable));
                    threads.get(threads.size() - 1).start();
                }
            } else {
                for (int j = 0; j < 125; j++) {
                    Runnable rannable = new ParseThread(links.get(j));
                    threads.add(new Thread(rannable));
                    threads.get(threads.size() - 1).start();
                }
            }
            pageCountForLoad--;
        }
        for (int j = 0; j < threads.size(); j++) {
            threads.get(j).join();
        }
        fileWork(filePath);
        timeSpent = System.currentTimeMillis() - startTime;
        Platform.runLater((() -> {
            Stage stage = new Stage();
            Parent root = null;
            try {
                root = FXMLLoader.load(getClass().getResource("Message.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (root != null) {
                stage.setScene(new Scene(root));
            }
            stage.setTitle("Файл записан");
            stage.show();
        }));
        return i;
    }

    //метод для работы с файлом( предварительная запись results:{[ и ]}
    private static void fileWork(String path) {
        if(firstWrite) {
            try (FileWriter file = new FileWriter(path,false)) {
                file.write("{\"results\": [ \r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            firstWrite = false;
        } else {
            try (FileWriter file = new FileWriter(path,true)) {
                file.write("\r\n" + "]}");
            } catch (IOException e) {
                e.printStackTrace();
            }
            firstWrite = true;
            ParseClass.isFirst = true;
        }
    }

    class ParseThread implements Runnable
    {
        Element element;

        public ParseThread(Element element) {
            this.element = element;
        }

        public void run()
        {
            if (goodCount > 0) {
                goodCount--;
                try {
                    parse.add(new ParseClass(element.attr("href")));//достаем ссылку на товар в списке
                }
                catch ( SocketTimeoutException exp){
                    return;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                parse.get(parse.size() - 1).witeJson(filePath);//запись в файл вытянутых тегов товара
                i++;
                updateProgress(i, allGoods);
            }
        }
    }
}