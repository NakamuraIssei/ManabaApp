package com.example.scrapingtest2;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class TaskDataManager extends DataManager{

    private HashMap<Integer,NotificationCustomAdapter> notificationAdapterBag;
    private ArrayList<TaskData> allTaskDataList;
    private DateTimeFormatter formatter;

    TaskDataManager(String dataName){
        prepareForWork(dataName);
        notificationAdapterBag =new HashMap<>();
        allTaskDataList =new ArrayList<TaskData>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //2024-01-19 13:30
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.JAPAN);
        }
        //bellButtonList=new ArrayList<>();
    }
    public void loadTaskData(){//データベースからデータを読み込んで、allTaskDataListに入れる
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.d("aaa","今からタスクデータをロードします。TaskDataManager 38");
            while (cursor.moveToNext()) {
                @SuppressLint("Range")int taskId = cursor.getInt(cursor.getColumnIndex("taskId"));
                Log.d("aaa", "taskId= "+String.valueOf(taskId));
                @SuppressLint("Range")String belongedClassName = cursor.getString(cursor.getColumnIndex("belongedClassName"));
                Log.d("aaa", "belongedClassName= "+belongedClassName);
                @SuppressLint("Range")String taskName = cursor.getString(cursor.getColumnIndex("taskName"));
                Log.d("aaa",taskName);
                @SuppressLint("Range")String dueDate = cursor.getString(cursor.getColumnIndex("dueDate"));
                Log.d("aaa","提出期限は"+dueDate);
                @SuppressLint("Range")String notificationTiming = cursor.getString(cursor.getColumnIndex("notificationTiming"));
                Log.d("aaa","通知タイミングは"+notificationTiming);
                @SuppressLint("Range")String taskURL = cursor.getString(cursor.getColumnIndex("taskURL"));
                Log.d("aaa",taskURL);
                @SuppressLint("Range")int hasSubmitted = cursor.getInt(cursor.getColumnIndex("hasSubmitted"));
                Log.d("aaa", String.valueOf(hasSubmitted));
                LocalDateTime temp=LocalDateTime.parse(dueDate, formatter);
                Log.d("aaa","締め切り日時は"+temp+"です。　TaskDataManager 56");
                if(LocalDateTime.parse(dueDate, formatter).isAfter(LocalDateTime.now(ZoneId.of("Asia/Tokyo")))&&((isExistInClassDataList(belongedClassName))||isExistInUnRegisteredClassDataList(belongedClassName))){
                    //締め切りを過ぎていない、かつ、所属クラスが存在すれば、allTaskListに登録
                    Log.d("aaa",taskName+"は締め切りを過ぎていないので登録します。TaskDataManager 50");

                    TaskData taskData =new TaskData(taskId,belongedClassName,taskName,LocalDateTime.parse(dueDate, formatter),taskURL,hasSubmitted);
                    dataCount=(taskId+1)%99999999;
                    Log.d("aaa",taskId+"番目の"+ taskData.getTaskName()+"をロードしました。TaskDataManager 42");
                    if(!Objects.equals(notificationTiming, "")){
                        String[] parts = notificationTiming.split("\\?"); // ? をエスケープして使用
                        for (String time : parts) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Log.d("aaa",notificationTiming+"を設定します。TskDataManager 57");
                                taskData.addNotificationTiming(LocalDateTime.parse(time, formatter));
                            }
                        }
                    }
                    for(LocalDateTime nt: taskData.getNotificationTiming()){
                        requestSettingNotification(dataName, taskData.getTaskId(), taskData.getTaskName(), dueDate,nt);
                    }
                    allTaskDataList.add(taskData);
                }else{
                    //締め切りを過ぎていれば、データベースから削除
                    Log.d("aaa",taskName+"は締め切りを過ぎていたか所属クラスがなかったので削除します。TaskDataManager 68");
                    String[] whereArgs = { String.valueOf(taskId) };
                    db.delete(dataName, "taskId = ?", whereArgs);
                }

            }
            Log.d("aaa","タスクデータロード完了!。TaskDataManager 59");
        }
    }
    public void setTaskDataIntoRegisteredClassData(){
        for(TaskData taskData: allTaskDataList){
            if(taskData.getHasSubmitted()==0) {
                for(int i=0;i<classDataList.size();i++) {
                    if (Objects.equals(classDataList.get(i).getClassName(), taskData.getBelongedClassName())) {
                        classDataList.get(i).addTaskData(taskData);
                        break;
                    }
                }
            }
        }
    }
    public void setTaskDataIntoUnRegisteredClassData(){
        for(TaskData taskData: allTaskDataList){
            if(taskData.getHasSubmitted()==0) {
                for(int i=0;i<unRegisteredClassDataList.size();i++) {
                    if (Objects.equals(unRegisteredClassDataList.get(i).getClassName(), taskData.getBelongedClassName())) {
                        unRegisteredClassDataList.get(i).addTaskData(taskData);
                        break;
                    }
                }
            }
        }
    }
    public void sortAllTaskDataList(){
        Comparator<TaskData> taskDataComparator = new Comparator<TaskData>() {
            @Override
            public int compare(TaskData task1, TaskData task2) {
                // hasSubmittedが大きい順に比較
                int submittedComparison = Integer.compare(task2.getHasSubmitted(), task1.getHasSubmitted());
                if (submittedComparison != 0) {
                    return submittedComparison;
                }

                // hasSubmittedが同じ場合はdueDateが小さい順に比較
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return task1.getDueDate().compareTo(task2.getDueDate());
                }

                return 0;
            }
        };

        Collections.sort(allTaskDataList, taskDataComparator);
    }
    public ArrayList<TaskData> getAllTaskDataList(){
        return allTaskDataList;
    }
    public void addAdapter(int num, NotificationCustomAdapter adapter){
        notificationAdapterBag.put(num,adapter);
    }
    public void addTaskData(String taskName, String dueDate,String belongedClassName,String taskURL) {//ここではデータベース登録、allTaskDataListへの登録、classDataへの登録を行う、
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(isExist(taskName)){//既に持ってる場合は
                makeTaskNotSubmitted(taskName);//未提出判定に
            }else{
                if(LocalDateTime.parse(dueDate, formatter).isAfter(LocalDateTime.now(ZoneId.of("Asia/Tokyo")))) {//提出期限が過ぎていなければ
                    try {
                        LocalDateTime deadLine = LocalDateTime.parse(dueDate, formatter);
                        Log.d("aaa", dataCount + "晩年の課題として" + taskName + "を作成します。授業番号は" + searchClassId(belongedClassName) + "です。TaskDataManager 146");
                        TaskData taskData = new TaskData(dataCount, belongedClassName, taskName, deadLine, taskURL, 0);//スクレーピングしてきたデータだからhasSubmittedは0
                        dataCount = (dataCount + 1) % 99999999;
                        LocalDateTime defaultTiming = deadLine.plusHours(-1);
                        Log.d("aaa", deadLine + "の一時間前は" + defaultTiming + "です。TaskdataManager 142");
                        taskData.addNotificationTiming(defaultTiming);
                        Log.d("aaa", taskData.getNotificationTiming().toString() + "デフォルトの通知を依頼します。TaskDataManager 149");
                        requestSettingNotification(dataName, taskData.getTaskId(), taskName, dueDate, defaultTiming);
                        Log.d("aaa", taskData.getNotificationTiming().toString() + "デフォルトの通知を依頼しました。TadskDataManager 151");
                        allTaskDataList.add(taskData);
                        insertTaskDataIntoDB(taskData);
                    } catch (DateTimeParseException e) {
                        Log.d("aaa", "デフォルトの通知タイミングを設定できませんでした。TaskDataManager 147");
                        TaskData taskData = new TaskData(dataCount, belongedClassName, taskName, LocalDateTime.MAX, taskURL, 0);//スクレーピングしてきたデータだからhasSubmittedは0
                        dataCount = (dataCount + 1) % 99999999;
                        allTaskDataList.add(taskData);
                        insertTaskDataIntoDB(taskData);
                    }
                }else{//提出期限が過ぎていれば
                    Log.d("aaa","スクレーピングした"+taskName+"は提出期限を過ぎていたので追加しません　TaskDataManager 179");
                }
                sortAllTaskDataList();
            }
        }
    }
    public Boolean isExist(String name){
        for(TaskData taskData : allTaskDataList)if(Objects.equals(taskData.getTaskName(), name))return true;
        return false;
    }
    public int searchClassId(String belongedClassName){
        for(ClassData classData:classDataList){
            if(Objects.equals(belongedClassName, classData.getClassName()))return classData.getClassId();
        }
        Log.d("aaa",belongedClassName+"はありませんでした。TaskDataManager 182");
        return -1;
    }
    public void deleteTaskNotification(int dataNum,int notificationNum){
        deleteNotification(dataNum,notificationNum);
        //changeBellButton(dataNum);
    }
    public void deleteFinishedTaskNotification(String title,String subTitle){
        subTitle = subTitle.substring(0, 10) + " " + subTitle.substring(11);//2024-06-12T03:10のTを消す。
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int num= 0;
            num = deleteFinishedNotification(title, LocalDateTime.parse(subTitle,formatter));
            //changeBellButton(num);
            if(notificationAdapterBag.get(num)!=null){
                notificationAdapterBag.get(num).notifyDataSetChanged();
            }else{
                Log.d("aaa",num+"番目の課題の通知リストを更新できませんでした。");
            }

        }
    }
    public void getTaskDataFromManaba(){//ここで課題の提出、未提出判定も行う。
        try {
            ArrayList<String> taskList;
            taskList=ManabaScraper.scrapeTaskDataFromManaba();
            Log.d("aaa","課題スクレーピング完了！　TaskDataManager 104");
            for(String k:taskList){
                Log.d("aaa",k+"TaskDataManager　106");
                String[] str = k.split("\\?\\?\\?");//切り分けたクッキーをさらに=で切り分ける

                if(!isExist(str[0])){//取得した課題を持っていなかったら追加する
                    Log.d("aaa",k+"持ってないから追加するよー！TaskDataManager　110");
                    addTaskData(str[0],str[1],str[2],str[3]);//str[0]は課題名、str[1]は締め切り、str[2]は課題が出ている授業名、str[3]は課題提出URL
                    Log.d("aaa",k+"追加したよー！TaskDataManager　112");
                }else{//取得した課題を持っていたら提出していない判定(hassubmittedを0)にする。
                    for(int i=0;i<allTaskDataList.size();i++){
                        if(Objects.equals(allTaskDataList.get(i).getTaskName(), str[0])){
                            allTaskDataList.get(i).changeSubmitted(0);
                            break;
                        }
                    }
                }
            }
        } catch (ExecutionException e) {
            Log.d("aaa","課題スクレーピングみすった！　TaskDataManager　116");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.d("aaa","課題スクレーピングみすった！　TaskDataManager　119");
            throw new RuntimeException(e);
        }
    }
    public void makeAllTasksSubmitted(){
        for(int i=0;i<allTaskDataList.size();i++){
            allTaskDataList.get(i).changeSubmitted(1);
        }
    }
    public void makeTaskNotSubmitted(String taskName){
        for(int i=0;i<allTaskDataList.size();i++){
            if(Objects.equals(taskName, allTaskDataList.get(i).getTaskName())){
                allTaskDataList.get(i).changeSubmitted(0);
                break;
            }
        }
    }
    public void insertTaskDataIntoDB(TaskData taskData){
        if (db == null) {
            Log.d("aaa", "db空っぽです。(TaskDataManager 239)");
        }
        String dueDate=taskData.getDueDate().toString();
        dueDate = dueDate.substring(0, 10) + " " + dueDate.substring(11);
        ContentValues values = new ContentValues();
        values.put("taskId", taskData.getTaskId());
        values.put("belongedClassName", taskData.getBelongedClassName());
        values.put("taskName", taskData.getTaskName());
        values.put("dueDate", dueDate);
        values.put("taskURL", taskData.getTaskURL());
        if(taskData.getHasSubmitted()==0)values.put("hasSubmitted", "0");
        else values.put("hasSubmitted", "1");
        String notificationtime="";
        if (taskData.getNotificationTiming().size() > 0) {
            String before = String.valueOf(taskData.getNotificationTiming().get(0));
            char[] charArray2 = before.toCharArray();
            charArray2[10] = ' ';
            notificationtime = new String(charArray2);
        } else notificationtime = "";
        values.put("notificationTiming", notificationtime);
        Log.d("aaa", values+" TaskDataManager 256");
        Log.d("aaa", db+" TaskDataManager 257");
        long newRowId = db.insert(dataName, null, values);
        if (newRowId != -1) {
            Log.d("aaa", dataName+"に"+ taskData.getTaskName()+"追加しました。TaskDataManager 268");
        } else {
            Log.d("aaa", dataName+"に追加失敗。TaskDataManager 262");
        }
    }
    public void addNotificationTiming(int num,LocalDateTime notificationTiming){
        allTaskDataList.get(num).addNotificationTiming(notificationTiming);
        requestSettingNotification(dataName, allTaskDataList.get(num).getTaskId(), allTaskDataList.get(num).getTaskName(), allTaskDataList.get(num).getDueDate().toString(),notificationTiming);
        updateNotificationTimingFromDB(allTaskDataList.get(num));
    }
    public void updateNotificationTimingFromDB(TaskData taskData){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            StringBuilder sb = new StringBuilder();
            for (LocalDateTime nt : taskData.getNotificationTiming()) {
                String dateTimeStr = nt.format(formatter);
                // StringBuilderを使って文字列に追加
                sb.append(dateTimeStr);
                // エントリ間に '?' を追加
                sb.append('?');
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            // 最終的な文字列を取得
            String finalString = sb.toString();

            ContentValues values = new ContentValues();
            values.put("notificationTiming", finalString);

            // WHERE 句を設定（どのレコードを更新するか）
            String whereClause = "taskId=?";
            String[] whereArgs = { String.valueOf(taskData.getTaskId()) };

            // レコードを更新
            Log.d("aaa",finalString+"を書き込みます(TaskDataManager 298)");
            db.update(dataName, values, whereClause, whereArgs);
            Cursor cursor = db.query(dataName, new String[]{"notificationTiming"}, whereClause, whereArgs, null, null, null);
            if(cursor==null)Log.d("aaa","taskId="+String.valueOf(taskData.getTaskId())+"のやつが見つかりませんでした(TaskDataManager 301)");
            String result = null;
            assert cursor != null;
            if (cursor.moveToFirst()) { // カーソルを最初の位置に移動
                // カラムのインデックスを取得
                int columnIndex = cursor.getColumnIndex("notificationTiming");
                // 値を取得
                result = cursor.getString(columnIndex);
            }
            Log.d("aaa", "通知情報を更新しました(TaskDataManager 310)" + result);
        }
    }
    public void requestSettingNotification(String dataName, int taskId, String taskName, String dueDate, LocalDateTime notificationTiming){
        NotifyManager2.setTaskNotificationAlarm(dataName,taskId,taskName,dueDate,notificationTiming);
    }
    public void requestCancelNotification(String dataName, String taskName, LocalDateTime dueDate, LocalDateTime notificationTiming){
        NotifyManager2.cancelTaskNotificationAlarm(dataName,taskName,dueDate.toString(),notificationTiming);
    }
    public int deleteFinishedNotification(String taskName,LocalDateTime subTitle){
        //dbの更新は呼び出し元で行うので、ここでは行わない。ここではメモリ上の通知情報のみ更新。
        for(int i=0;i<allTaskDataList.size();i++){
            if(Objects.equals(allTaskDataList.get(i).getTaskName(), taskName) && Objects.equals(allTaskDataList.get(i).getDueDate(), subTitle)){
                allTaskDataList.get(i).deleteFinishedNotification();
                return i;
            }
        }
        return -1;
    }
    public void deleteNotification(int dataNum,int notificationNum){
        //dbの更新もここで行う。
        requestCancelNotification(dataName, allTaskDataList.get(dataNum).getTaskName(), allTaskDataList.get(dataNum).getDueDate(), allTaskDataList.get(dataNum).getNotificationTiming().get(notificationNum));
        allTaskDataList.get(dataNum).deleteNotificationTiming(notificationNum);
        updateNotificationTimingFromDB(allTaskDataList.get(dataNum));
    }
    public Boolean isExistInClassDataList(String className){
        for(ClassData classData:classDataList){
            if(Objects.equals(className, classData.getClassName()))return true;
        }
        return false;
    }
    public Boolean isExistInUnRegisteredClassDataList(String className){
        for(ClassData classData:unRegisteredClassDataList){
            if(Objects.equals(className, classData.getClassName()))return true;
        }
        return false;
    }
}
