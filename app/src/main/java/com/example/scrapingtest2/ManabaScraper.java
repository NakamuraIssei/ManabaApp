package com.example.scrapingtest2;

import android.os.Build;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalTime;
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
        switch (dataName){
            case "TaskData":
                return scrapeTaskDataFromManaba();
            case "ClassData":
                return getClassInforFromManaba();

        }
        return null;
    }
    public static void setCookie(HashMap<String, String> cookiebag){
        ManabaScraper.cookiebag =cookiebag;
        taskInfor =new ArrayList<>();
        classInfor=new ArrayList<>();
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
                                Element titleElement = row.selectFirst("h3.myassignments-title > a");//課題名の部分を切り取る。
                                assert titleElement != null;
                                String taskName = titleElement.text();//課題名を文字列で取得。
                                Element dateElement = row.selectFirst("td:last-child");//締め切りの部分を切り取る。
                                assert dateElement != null;
                                String deadLine = dateElement.text();//締め切りを文字列で取得。
                                taskInfor.add(taskName+"???"+deadLine);//「課題名???締め切り」の形の文字列を作る。
                                //Log.d("aaa",extractedText);
                                //Log.d("aaa",dateText);
                                //TaskData context;
                                //context = new TaskData(extractedText,1,"hoegohoge",dateText);//締め切り日時と課題名をペアにする。
                                //if(TaskData.isExist(context.name))TaskData.addTask(context,1);//ペアにした締め切り日時と課題名をtaskDataに入れる。第二引数にはdbに書き込むので1を入れる。書き込まないときは0
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
    private static ArrayList<String> getClassInforFromManaba() throws ExecutionException, InterruptedException, IOException {

        for (int i = 0; i < 7; i++) {
            ClassData.classData.add(new ArrayList<ClassData>());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                for(int j=0;j<9;j++){
                    //Log.d("aaa",i+"曜日"+j+"時間目追加するよー");
                    ClassData.classData.get(i).add(new ClassData());
                    switch (j){
                        case 0:
                            //Log.d("aaa",i+"曜日"+j+"時間目初期化するよー");
                            ClassData.classData.get(i).get(j).nextTiming= LocalTime.of(8, 30);
                            //Log.d("aaa",i+"曜日"+j+"時間目初期化したよー");
                            break;
                        case 1:
                            ClassData.classData.get(i).get(j).nextTiming= LocalTime.of(10, 10);
                            break;
                        case 2:
                            ClassData.classData.get(i).get(j).nextTiming= LocalTime.of(12, 30);
                            break;
                        case 3:
                            ClassData.classData.get(i).get(j).nextTiming= LocalTime.of(14, 10);
                            break;
                        case 4:
                            ClassData.classData.get(i).get(j).nextTiming= LocalTime.of(15, 50);
                            break;
                        case 5:
                            ClassData.classData.get(i).get(j).nextTiming= LocalTime.of(17, 30);
                            break;
                        case 6:
                            ClassData.classData.get(i).get(j).nextTiming= LocalTime.of(19, 10);
                            break;
                        case 7:
                            ClassData.classData.get(i).get(j).nextTiming= LocalTime.of(20, 50);
                            break;
                        case 8:
                            ClassData.classData.get(i).get(j).nextTiming= LocalTime.of(8, 30);
                            ClassData.classData.get(i).get(j).judge=true;
                            break;
                    }
                }
            }
        }
        Log.d("aaa","時間割表初期化完了!");
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
                String name="次は空きコマです",room="";
                if(!divs.isEmpty()&&!divs2.isEmpty()){
                    name = Objects.requireNonNull(divs.first()).text();
                    room = Objects.requireNonNull(divs2.first()).text();
                    classInfor.add((7*(j-1)+i)-1+"???"+room+"???"+name);
                }

                //Log.d("class",name);
                //Log.d("class",room);

                ClassData.classData.get(j-1).get(i).classRoom=room;
                ClassData.classData.get(j-1).get(i).className=name;
            }
        }
        for(String k:classInfor)Log.d("aaa",k+"ManabaScraper 152");
        Log.d("aaa",classInfor.size()+"ManabaScraper 153");
        /*for(int i=0;i<ClassData.classData.size();i++){
            for(int j=0;j<ClassData.classData.get(i).size();j++){
                Log.d("ttt",i+"曜日");
                Log.d("ttt",j+"時間目");
                Log.d("ttt",ClassData.classData.get(i).get(j).className);
                Log.d("ttt",ClassData.classData.get(i).get(j).classRoom);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("ttt", String.valueOf(ClassData.classData.get(i).get(j).nextTiming));
                }
            }
        }*/
        return classInfor;
    }


}
