package com.example.ManabaApp;

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
    private static ArrayList<String> taskURL,classURL,taskInfor,unRegisteredclassInfor;
    private static HashMap<Integer,String> registerdClassInfor;
    private static HashMap<String, String> cookiebag;

    public static void setCookie(HashMap<String, String> cookiebag) {
        ManabaScraper.cookiebag = cookiebag;
        taskURL = new ArrayList<>(Arrays.asList(
                "https://ct.ritsumei.ac.jp/ct/home_summary_query",
                "https://ct.ritsumei.ac.jp/ct/home_summary_survey",
                "https://ct.ritsumei.ac.jp/ct/home_summary_report"));
        classURL = new ArrayList<>(Arrays.asList(
                "https://ct.ritsumei.ac.jp/ct/home_course?chglistformat=list"));
    }
    public static ArrayList<String> scrapeTaskDataFromManaba() throws ExecutionException, InterruptedException{//ここのメゾッドで未提出課題、小テスト、アンケートの欄からスクレーピング
        taskInfor=null;
        CompletableFuture<ArrayList<String>> scrapingTask = null;//非同期処理をするために、CompletableFuture型のデータを使う。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//おまじない。swiftなら無くても多分大丈夫！
            scrapingTask = CompletableFuture.supplyAsync(() -> {//非同期処理をするためのfuture1変数の設定
                try {
                    taskInfor = new ArrayList<>();
                    // Jsoupで対象URLの情報を取得する
                    for (String url : taskURL) {//URLのArrayListの中身でループを回す
                        Document doc = Jsoup.connect(url).cookies(cookiebag).get();//jsoupでHTMLを取得する。
                        Elements doc2 = doc.select("#container > div.pagebody > div > table.stdlist tbody tr");//取得したHTMLから課題のテーブル部分を切り取る。
                        for (Element row : doc2) {//切り取ったテーブルの中身でループを回す。
                            //Log.d("aaa", String.valueOf(row));
                            if (!row.text().equals("タイトル コース名 受付終了日時")) {//rowのHTMLのテキストが"タイトル コース名 受付終了日時"で無ければ
                                Element taskNameElement = row.selectFirst("h3.myassignments-title > a");//課題名の部分を切り取る。
                                Element deadLineElement = row.selectFirst("td:last-child");//締め切りの部分を切り取る。
                                Element belongedClassElement = row.select("td:nth-child(2)").first();
                                Element taskURLElement = row.selectFirst("td h3.myassignments-title a");

                                if (taskNameElement != null && deadLineElement != null && belongedClassElement != null && taskURLElement != null) {
                                    String taskName = taskNameElement.text();//課題名を文字列で取得。
                                    String deadLine = deadLineElement.text();//締め切りを文字列で取得。
                                    String belongedClassName = belongedClassElement.text();//締め切りを文字列で取得。
                                    String taskURL = taskURLElement.attr("href");
                                    String taskId = taskURL.split("_")[1]+taskURL.split("_")[3];
                                    taskInfor.add(taskId+"???"+taskName + "???" + deadLine + "???" + belongedClassName + "???" + taskURL);//「課題名???締め切り」の形の文字列を作る。
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
    public static HashMap<Integer,String> scrapeUnChangableClassDataFromManaba() throws ExecutionException, InterruptedException, IOException {//ここでManabaの時間割表にある授業情報をスクレーピング
        String message="授業が重複しています。??? ??? ??? ";
        registerdClassInfor = null;
        ArrayList<Character>days;
        days=new ArrayList<>(Arrays.asList(
                '月',
                '火',
                '水',
                '木',
                '金',
                '土',
                '日'));
        CompletableFuture<HashMap<Integer,String>> scrapingTask = null;//非同期処理をするために、CompletableFuture型のデータを使う。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//おまじない。swiftなら無くても多分大丈夫！
            scrapingTask = CompletableFuture.supplyAsync(() -> {//非同期処理をするためのfuture1変数の設定
                Document doc = null;//jsoupでHTMLを取得する。
                try {
                    doc = Jsoup.connect(classURL.get(0)).cookies(cookiebag).get();
                    Log.d("doc", String.valueOf(doc));
                    Elements doc2 = doc.select("#container > div.pagebody > div > div.contentbody-left > div.my-infolist.my-infolist-mycourses > div.mycourses-body > div > table > tbody");//取得したHTMLから課題のテーブル部分を切り取る。

                    Elements rows = doc2.select("tr");
                    registerdClassInfor=new HashMap<>();

                    Log.d("yyyyy","時間割分析します。getRegisteredClassDataFromManabaaaaaaa"+rows.size());
                    for (int i = 1; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cells = row.select("td"); // <td>要素を取得

                        Elements divs = cells.select("td:nth-child(1)");
                        Elements divs2 = cells.select("td:nth-child(1) > span > a[href]");
                        Elements divs3 = cells.select("td:nth-child(3)");
                        Elements divs4 = cells.select("td:nth-child(3) > span");
                        Elements divs5 = cells.select("td:nth-child(4)");

                        String classId="",className = "", classURL = "", classNum = "", classRoom="", professorName = "";

                        className = Objects.requireNonNull(divs.first()).text();
                        classURL = Objects.requireNonNull(divs2.attr("href"));
                        classRoom = Objects.requireNonNull(divs3.first()).text();
                        classNum = Objects.requireNonNull(divs4.first()).text();
                        professorName = Objects.requireNonNull(divs5.first()).text();


                        if (!className.equals("") && !classRoom.equals("") && !classRoom.equals("") && !professorName.equals("") && !classURL.equals("")) {
                            String[] urlParts=classURL.split("_");
                            for (String urlPart : urlParts) {
                                // urlPart が数字のみで構成されているかをチェックする正規表現
                                if (urlPart.matches("\\d+")) {
                                    classId=urlPart;
                                    break;  // classIdが見つかったらループを終了
                                }
                            }

                            classRoom=classRoom.substring(classNum.length());
                            String[] parts = classNum.split(" / ");
                            for(String dayNum:parts){
                                for(int j=0;j<dayNum.length();j++){
                                    for(int k=0;k< days.size();k++){
                                        if(dayNum.charAt(j)== days.get(k)){
                                            if(dayNum.charAt(j+2)=='('){
                                                int dayAndPeriod=(7*k)+Integer.parseInt(String.valueOf(dayNum.charAt(j+1)))-1;
                                                if (registerdClassInfor.containsKey(dayAndPeriod)) {
                                                    registerdClassInfor.remove(dayAndPeriod);
                                                    registerdClassInfor.put(dayAndPeriod,message);
                                                }else{
                                                    registerdClassInfor.put(dayAndPeriod,classId+"???"+className + "???" + classRoom + "???" + professorName + "???" + classURL);
                                                }
                                            }else{
                                                int startNum=(7*k)+Integer.parseInt(String.valueOf(dayNum.charAt(j+1)))-1;
                                                int endNum=(7*k)+Integer.parseInt(String.valueOf(dayNum.charAt(j+3)))-1;
                                                for(int l = startNum; l<=endNum;l++){
                                                    if (registerdClassInfor.containsKey(l)) {
                                                        registerdClassInfor.remove(l);
                                                        registerdClassInfor.put(l,message);
                                                    }else{
                                                        registerdClassInfor.put(l,classId+"???"+className + "???" + classRoom + "???" + professorName + "???" + classURL);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Log.d("yyyyy",className+"//"+classRoom+"aaaaaaaaaa");
                        }
                    }
                    return registerdClassInfor;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return scrapingTask.get();
        }
        return registerdClassInfor;
    }
    public static ArrayList<String> scrapeChangableClassDataFromManaba() throws ExecutionException, InterruptedException, IOException {//ここでManabaの時間割表にない時間割(卒研とか)をスクレーピング
        unRegisteredclassInfor = new ArrayList<>();
        CompletableFuture<ArrayList<String>> scrapingTask = null;//非同期処理をするために、CompletableFuture型のデータを使う。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//おまじない。swiftなら無くても多分大丈夫！
            scrapingTask = CompletableFuture.supplyAsync(() -> {//非同期処理をするためのfuture1変数の設定
                Document doc = null;//jsoupでHTMLを取得する。
                try {
                    doc = Jsoup.connect(classURL.get(0)).cookies(cookiebag).get();
                    Elements doc2 = doc.select("#container > div.pagebody > div > div.contentbody-left > div.my-infolist.my-infolist-mycourses > div.mycourses-body > div > table > tbody");//取得したHTMLから課題のテーブル部分を切り取る。

                    Elements rows = doc2.select("tr");

                    unRegisteredclassInfor.clear();
                    for (int i = 1; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cells = row.select("td"); // <td>要素を取得

                        Elements divs = cells.select("td:nth-child(1)");
                        Elements divs2 = cells.select("td:nth-child(1) > span > a[href]");
                        Elements divs3 = cells.select("td:nth-child(3)");
                        Elements divs4 = cells.select("td:nth-child(3) > span");
                        Elements divs5 = cells.select("td:nth-child(4)");

                        String classId="",className = "", classURL = "", classNum = "", classRoom="", professorName = "";

                        className = Objects.requireNonNull(divs.first()).text();
                        classURL = Objects.requireNonNull(divs2.attr("href"));
                        classRoom = Objects.requireNonNull(divs3.first()).text();
                        classNum = Objects.requireNonNull(divs4.first()).text();
                        professorName = Objects.requireNonNull(divs5.first()).text();


                        if (!className.equals("") && !classRoom.equals("") && !classRoom.equals("") && !professorName.equals("") && !classURL.equals("")) {
                            String[] urlParts=classURL.split("_");
                            for (String urlPart : urlParts) {
                                // urlPart が数字のみで構成されているかをチェックする正規表現
                                if (urlPart.matches("\\d+")) {
                                    classId=urlPart;
                                    break;  // classIdが見つかったらループを終了
                                }
                            }
                            classRoom=classRoom.substring(classNum.length());
                            if(classRoom.contains("--")){
                                unRegisteredclassInfor.add(classId+"???"+className+ "???" + professorName + "???" + classURL);
                            }
                        }
                    }
                    return unRegisteredclassInfor;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return scrapingTask.get();
        }
        return unRegisteredclassInfor;
    }
}