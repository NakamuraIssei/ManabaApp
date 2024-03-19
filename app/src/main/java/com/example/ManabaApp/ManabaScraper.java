package com.example.ManabaApp;

import android.os.Build;
import android.util.Log;
import android.util.Pair;

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
    private static ArrayList<String> classURL;
    private static HashMap<String, String> cookiebag;
    public static ArrayList<String> taskInfor;
    private static ArrayList<String> classInfor;


    public static void setCookie(HashMap<String, String> cookiebag) {
        ManabaScraper.cookiebag = cookiebag;
        urlList = new ArrayList<>(Arrays.asList(
                "https://ct.ritsumei.ac.jp/ct/home_summary_query",
                "https://ct.ritsumei.ac.jp/ct/home_summary_survey",
                "https://ct.ritsumei.ac.jp/ct/home_summary_report"));
        classURL = new ArrayList<>(Arrays.asList(
                "https://ct.ritsumei.ac.jp/ct/home_course_past?chglistformat=timetable",
                "https://ct.ritsumei.ac.jp/ct/home_course_past?chglistformat=list"));
    }

    public static ArrayList<String> scrapeTaskDataFromManaba() throws ExecutionException, InterruptedException {//ここのメゾッドで未提出課題、小テスト、アンケートの欄からスクレーピング
        taskInfor = new ArrayList<>();
        CompletableFuture<ArrayList<String>> scrapingTask = null;//非同期処理をするために、CompletableFuture型のデータを使う。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//おまじない。swiftなら無くても多分大丈夫！
            scrapingTask = CompletableFuture.supplyAsync(() -> {//非同期処理をするためのfuture1変数の設定
                try {
                    // Jsoupで対象URLの情報を取得する
                    for (String url : urlList) {//URLのArrayListの中身でループを回す
                        //Log.d("Thread",Thread.currentThread().getName());
                        Document doc = Jsoup.connect(url).cookies(cookiebag).get();//jsoupでHTMLを取得する。
                        //System.err.println(doc);
                        Elements doc2 = doc.select("#container > div.pagebody > div > table.stdlist tbody tr");//取得したHTMLから課題のテーブル部分を切り取る。
                        //Log.d("bbb", String.valueOf(doc2));
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
                                    taskInfor.add(taskName + "???" + deadLine + "???" + belongedClassName + "???" + taskURL);//「課題名???締め切り」の形の文字列を作る。
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

    public static ArrayList<String> getRegisteredClassDataFromManaba() throws ExecutionException, InterruptedException, IOException {//ここでManabaの時間割表にある授業情報をスクレーピング
        classInfor = new ArrayList<>();
        HashMap<Pair<Integer, Integer>, Integer> shiftBag = new HashMap<>();
        CompletableFuture<ArrayList<String>> scrapingTask = null;//非同期処理をするために、CompletableFuture型のデータを使う。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            scrapingTask = CompletableFuture.supplyAsync(() -> {//非同期処理をするためのfuture1変数の設定
                try {
                    Document doc = Jsoup.connect(classURL.get(0)).cookies(cookiebag).get();//jsoupでHTMLを取得する。
                    //System.err.println(doc);
                    Elements doc2 = doc.select("#courselistweekly > table > tbody");//取得したHTMLから課題のテーブル部分を切り取る。
                    Elements rows = doc2.select("tr");
                    classInfor.clear();
                    for (int i = 1; i < rows.size(); i++) {
                        int shiftNum = 0;
                        Element row = rows.get(i);
                        Elements cells = row.select("td"); // <td>要素を取得
                        for (int j = 1; j < cells.size(); j++) {
                            shiftNum += shiftBag.getOrDefault(new Pair<>(i, j), 0);

                            Log.d("aaa", j + "曜日目の" + i + "時間目右シフト" + shiftNum);
                            Element cell = cells.get(j);
                            Elements divs = cell.select("div > div > div");
                            Elements divs2 = cell.select("div > a:nth-child(1)");
                            Element divs3 = cell.select("div > a:nth-child(1)").first();

                            int rowspanNum = 0;
                            if (!cell.attr("rowspan").isEmpty())
                                rowspanNum = Integer.parseInt(cell.attr("rowspan"));
                            if (rowspanNum > 1) {
                                for (int k = 0; k < rowspanNum - 1; k++) {
                                    Pair<Integer, Integer> key = new Pair<>(i + k + 1, j);
                                    shiftBag.put(key, shiftBag.getOrDefault(key, 0) + 1);
                                }
                            }
                            String classRoom = "", className = "次は空きコマです", classURL = "";
                            if (!divs.isEmpty() && !divs2.isEmpty()) {
                                classRoom = Objects.requireNonNull(divs.first()).text();
                                className = Objects.requireNonNull(Objects.requireNonNull(divs2.first()).select("a")).text();
                                assert divs3 != null;
                                classURL = Objects.requireNonNull(divs3.attr("href"));

                                classInfor.add((7 * (j - 1 + shiftNum) + i - 1) + "???" + className + "???" + classRoom + "???" + classURL);
                                for (int k = 1; k < rowspanNum; k++)
                                    classInfor.add((7 * (j - 1 + shiftNum) + i - 1 + k) + "???" + className + "???" + classRoom + "???" + classURL);//番号、授業名、教室名、URLの順番
                            }
                        }
                    }
                    for (String k : classInfor) Log.d("aaa", k + "ManabaScraper 152");
                    return classInfor;

                } catch (IOException e) {//tryの中でうまくいかなかった時の処理。
                    Log.d("aaa", "授業スクレーピング失敗しました。ManabaScraper 120");
                    e.printStackTrace();
                }
                return classInfor;
            });

            return scrapingTask.get();
        }
        return classInfor;
    }

    public static ArrayList<String> getUnRegisteredClassDataFromManaba() throws ExecutionException, InterruptedException, IOException {//ここでManabaの時間割表にない時間割(卒研とか)をスクレーピング
        classInfor = new ArrayList<>();
        CompletableFuture<ArrayList<String>> scrapingTask = null;//非同期処理をするために、CompletableFuture型のデータを使う。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//おまじない。swiftなら無くても多分大丈夫！
            scrapingTask = CompletableFuture.supplyAsync(() -> {//非同期処理をするためのfuture1変数の設定
                try {
                    Document doc = Jsoup.connect(classURL.get(0)).cookies(cookiebag).get();//jsoupでHTMLを取得する。
                    Elements doc2 = doc.select("#courselistweekly > div > table > tbody");//取得したHTMLから課題のテーブル部分を切り取る。
                    Elements rows = doc2.select("tr");

                    classInfor.clear();
                    for (int i = 1; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cells = row.select("td"); // <td>要素を取得

                        Elements divs = cells.select("td:nth-child(1)");
                        Elements divs2 = cells.select("td.center");
                        Elements divs3 = cells.select("td:nth-child(3)");
                        Elements divs4 = cells.select("td:nth-child(4)");
                        Elements divs5 = cells.select("td:nth-child(1) > span > a[href]");

                        String className = "", year = "", classRoom = "", professorName = "", classURL;

                        className = Objects.requireNonNull(divs.first()).text();
                        year = Objects.requireNonNull(divs2.first()).text();
                        classRoom = Objects.requireNonNull(divs3.first()).text();
                        professorName = Objects.requireNonNull(divs4.first()).text();
                        classURL = Objects.requireNonNull(divs5.attr("href"));

                        if (!className.equals("") && !year.equals("") && !classRoom.equals("") && !professorName.equals("") && !classURL.equals("")) {
                            classInfor.add(className + "???" + professorName + "???" + classURL);//授業名、教授名、URLの順番
                        }
                    }
                    return classInfor;

                } catch (IOException e) {//tryの中でうまくいかなかった時の処理。
                    Log.d("ppp", "授業スクレーピング失敗しました。ManabaScraper 169");
                    e.printStackTrace();
                }
                return classInfor;
            });

            return scrapingTask.get();
        }
        return classInfor;
    }

    public static ArrayList<String> getProfessorNameFromManaba() throws ExecutionException, InterruptedException, IOException {//ここで授業担当の教授名をスクレーピング、授業名と教授名をセットで取得
        classInfor = new ArrayList<>();
        CompletableFuture<ArrayList<String>> scrapingTask = null;//非同期処理をするために、CompletableFuture型のデータを使う。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//おまじない。swiftなら無くても多分大丈夫！
            scrapingTask = CompletableFuture.supplyAsync(() -> {//非同期処理をするためのfuture1変数の設定
                try {
                    Document doc = Jsoup.connect(classURL.get(1)).cookies(cookiebag).get();//jsoupでHTMLを取得する。
                    Elements doc2 = doc.select("#container > div.pagebody > div > div.contentbody-left > div.my-infolist.my-infolist-mycourses > div.mycourses-body > div > table > tbody");//取得したHTMLから課題のテーブル部分を切り取る。
                    Elements rows = doc2.select("tr");
                    classInfor.clear();
                    for (int i = 1; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements div1, div2;

                        div1 = row.select("td:nth-child(1) > span > a");
                        div2 = row.select("td:nth-child(4)");

                        String className = "", professorName = "";

                        className = Objects.requireNonNull(Objects.requireNonNull(div1.text()));
                        professorName = Objects.requireNonNull(Objects.requireNonNull(div2.text()));
                        Log.d("aaa",professorName);

                        classInfor.add(className + "???" + professorName);
                    }

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
