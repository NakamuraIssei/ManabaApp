package com.example.scrapingtest2;

import android.os.Build;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ManabaScraper {
    private static ArrayList<String> urlList;
    private static HashMap<String, String> cookiebag;
    public static ArrayList<String> taskInfor;
    private static ArrayList<String> classInfor;
    private static String classURL;

    public static ArrayList<String> receiveRequest(String dataName) throws ExecutionException, InterruptedException, IOException {
        taskInfor =new ArrayList<>();
        classInfor=new ArrayList<>();
        switch (dataName){
            case "TaskData":
                return scrapeTaskDataFromManaba();
            case "ClassData":
                return getClassDataFromManaba();
        }
        return null;
    }
    public static void setCookie(HashMap<String, String> cookiebag){
        ManabaScraper.cookiebag =cookiebag;
        urlList= new ArrayList<>(Arrays.asList(
                "https://ct.ritsumei.ac.jp/ct/home_summary_query",
                "https://ct.ritsumei.ac.jp/ct/home_summary_survey",
                "https://ct.ritsumei.ac.jp/ct/home_summary_report"));
        classURL="https://ct.ritsumei.ac.jp/ct/home_course";
    }
    private static ArrayList<String> scrapeTaskDataFromManaba() throws ExecutionException, InterruptedException {
        taskInfor.clear();
        CompletableFuture<ArrayList<String>> scrapingTask = null;//非同期処理をするために、CompletableFuture型のデータを使う。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//おまじない。swiftなら無くても多分大丈夫！
            scrapingTask = CompletableFuture.supplyAsync(() -> {//非同期処理をするためのfuture1変数の設定
                try {
                    // Jsoupで対象URLの情報を取得する
                    for(String url:urlList){//URLのArrayListの中身でループを回す
                        //Log.d("Thread",Thread.currentThread().getName());
                        Document doc = Jsoup.connect(url).cookies(cookiebag).get();//jsoupでHTMLを取得する。
                        //System.err.println(doc);
                        Elements doc2 = doc.select("#container > div.pagebody > div > table.stdlist tbody tr");//取得したHTMLから課題のテーブル部分を切り取る。
                        //Log.d("bbb", String.valueOf(doc2));
                        for(Element row : doc2){//切り取ったテーブルの中身でループを回す。
                            //Log.d("aaa", String.valueOf(row));
                            if(!row.text().equals("タイトル コース名 受付終了日時")){//rowのHTMLのテキストが"タイトル コース名 受付終了日時"で無ければ
                                Element taskNameElement = row.selectFirst("h3.myassignments-title > a");//課題名の部分を切り取る。
                                Element deadLineElement = row.selectFirst("td:last-child");//締め切りの部分を切り取る。
                                Element belongedClassElement = row.select("td:nth-child(2)").first();
                                Element taskURLElement =row.selectFirst("td h3.myassignments-title a");

                                if(taskNameElement!=null&&deadLineElement!=null&&belongedClassElement!=null&&taskURLElement!=null){
                                    String taskName = taskNameElement.text();//課題名を文字列で取得。
                                    String deadLine = deadLineElement.text();//締め切りを文字列で取得。
                                    String belongedClassName = belongedClassElement.text();//締め切りを文字列で取得。
                                    String taskURL =taskURLElement.attr("href");
                                    taskInfor.add(taskName+"???"+deadLine+"???"+belongedClassName+"???"+taskURL);//「課題名???締め切り」の形の文字列を作る。
                                }
                            }
                        }
                    }
                } catch (IOException e) {//tryの中でうまくいかなかった時の処理。
                    e.printStackTrace();
                }
                return taskInfor;
            });

            return scrapingTask.get();
        }
        return taskInfor;
    }
    private static ArrayList<String> getClassDataFromManaba() throws ExecutionException, InterruptedException, IOException {
        classInfor.clear();
        CompletableFuture<ArrayList<String>> scrapingTask = null;//非同期処理をするために、CompletableFuture型のデータを使う。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//おまじない。swiftなら無くても多分大丈夫！
            scrapingTask = CompletableFuture.supplyAsync(() -> {//非同期処理をするためのfuture1変数の設定
                try {
                    Document doc = Jsoup.connect(classURL).cookies(cookiebag).get();//jsoupでHTMLを取得する。
                    //System.err.println(doc);
                    Elements doc2 = doc.select("#courselistweekly > table > tbody");//取得したHTMLから課題のテーブル部分を切り取る。
                    //Log.d("bbb", String.valueOf(doc2));

                    Elements rows = doc2.select("tr");

                    classInfor.clear();
                    for (int i = 1; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cells = row.select("td"); // <td>要素を取得
                        for (int j = 1; j < cells.size(); j++) {
                            //Log.d("aaa",j+"曜日目の"+i+"時間目");
                            Element cell = cells.get(j);
                            Elements divs = cell.select("div.couraselocationinfo.couraselocationinfoV2");
                            Elements divs2 = cell.select("div.courselistweekly-nonborder.courselistweekly-c");
                            Element divs3 = cell.select("div.courselistweekly-nonborder.courselistweekly-c a[href]").first();

                            String classRoom="",className="次は空きコマです",url="";
                            if(!divs.isEmpty()&&!divs2.isEmpty()){
                                classRoom = Objects.requireNonNull(divs.first()).text();
                                className = Objects.requireNonNull(Objects.requireNonNull(divs2.first()).select("a")).text();
                                className = className.substring(0, className.length() - 1);//最後に空白が入ってるから、それを消す。
                                assert divs3 != null;
                                url= Objects.requireNonNull(divs3.attr("href"));
                                classInfor.add((7*(j-1)+i)-1+"???"+className+"???"+classRoom+"???"+url);//番号、授業名、教室名、URLの順番
                            }
                        }
                    }
                    for(String k:classInfor)Log.d("aaa",k+"ManabaScraper 152");
                    Log.d("aaa",classInfor.size()+"ManabaScraper 153");

                    return classInfor;

                } catch (IOException e) {//tryの中でうまくいかなかった時の処理。
                    e.printStackTrace();
                }
                return classInfor;
            });

            return scrapingTask.get();
        }
        return classInfor;
    }
}
