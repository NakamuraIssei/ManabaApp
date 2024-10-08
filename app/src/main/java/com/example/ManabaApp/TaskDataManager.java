package com.example.ManabaApp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

public class TaskDataManager {

    private final HashMap<Integer, NotificationCustomAdapter> notificationAdapterBag;
    private final ArrayList<TaskData> allTaskDataList;
    private DateTimeFormatter formatter;
    private static SQLiteDatabase db;
    private static Cursor cursor;
    private String dataName="TaskData";

    TaskDataManager() {
        notificationAdapterBag = new HashMap<>();
        allTaskDataList = new ArrayList<TaskData>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.JAPAN);
        }
    }
    public void loadTaskData() {//データベースからデータを読み込んで、allTaskDataListに入れる
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") Long taskId = cursor.getLong(cursor.getColumnIndex("taskId"));
                @SuppressLint("Range") String belongedClassName = cursor.getString(cursor.getColumnIndex("belongedClassName"));
                @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex("taskName"));
                @SuppressLint("Range") String dueDate = cursor.getString(cursor.getColumnIndex("dueDate"));
                @SuppressLint("Range") String notificationTiming = cursor.getString(cursor.getColumnIndex("notificationTiming"));
                @SuppressLint("Range") String taskURL = cursor.getString(cursor.getColumnIndex("taskURL"));
                @SuppressLint("Range") int hasSubmitted = cursor.getInt(cursor.getColumnIndex("hasSubmitted"));
                Log.d("taskId", String.valueOf(taskId));
                if (LocalDateTime.parse(dueDate, formatter).isAfter(LocalDateTime.now(ZoneId.of("Asia/Tokyo"))) && ((isExistInClassDataList(belongedClassName)) || isExistInUnRegisteredClassDataList(belongedClassName))) {
                    //締め切りを過ぎていない、かつ、所属クラスが存在すれば、allTaskListに登録

                    TaskData taskData = new TaskData(taskId, belongedClassName, taskName, LocalDateTime.parse(dueDate, formatter), taskURL, hasSubmitted);
                    if (!Objects.equals(notificationTiming, "")) {
                        String[] parts = notificationTiming.split("\\?"); // ? をエスケープして使用
                        for (String time : parts) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                taskData.addNotificationTiming(LocalDateTime.parse(time, formatter));
                            }
                        }
                    }
                    for (LocalDateTime nt : taskData.getNotificationTiming()) {
                        requestSettingNotification(dataName, taskData.getTaskId(), taskData.getTaskName(), dueDate, nt);
                    }
                    allTaskDataList.add(taskData);
                } else {
                    //締め切りを過ぎていれば、データベースから削除
                    Log.d("aaa", taskName + "は締め切りを過ぎていたか所属クラスがなかったので削除します。TaskDataManager 68");
                    String[] whereArgs = {String.valueOf(taskId)};
                    db.delete(dataName, "taskId = ?", whereArgs);
                }
            }
        }
    }
    public int getTaskGroupId(int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            TaskData task = allTaskDataList.get(position);
            if (task.getHasSubmitted() == 1) return 0;//hasSubmittedは提出済みなら1そうでないなら0
            else if (task.getDueDate().isBefore(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withMinute(0))) {
                return 1;
            } else if (task.getDueDate().isBefore(LocalDateTime.now().plusDays(2).withHour(0).withMinute(0).withMinute(0))) {
                return 2;
            } else return 3;
        }
        return 3;
    }
    public void setTaskDataIntoRegisteredClassData() {
        for (TaskData taskData : allTaskDataList) {
            if (taskData.getHasSubmitted() == 0) {
                for (int i = 0; i < ClassDataManager.classDataList.size(); i++) {
                    if (Objects.equals(ClassDataManager.classDataList.get(i).getClassName(), taskData.getBelongedClassName())) {
                        ClassDataManager.classDataList.get(i).addTaskData(taskData);
                        break;
                    }
                }
            }
        }
    }
    public void setTaskDataIntoUnRegisteredClassData() {
        for (TaskData taskData : allTaskDataList) {
            if (taskData.getHasSubmitted() == 0) {
                for (int i = 0; i < ClassDataManager.unRegisteredClassDataList.size(); i++) {
                    if (Objects.equals(ClassDataManager.unRegisteredClassDataList.get(i).getClassName(), taskData.getBelongedClassName())) {
                        ClassDataManager.unRegisteredClassDataList.get(i).addTaskData(taskData);
                        break;
                    }
                }
            }
        }
    }
    public void sortAllTaskDataList() {
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
    public ArrayList<TaskData> getAllTaskDataList() {
        return allTaskDataList;
    }
    public void addAdapter(int num, NotificationCustomAdapter adapter) {
        notificationAdapterBag.put(num, adapter);
    }
    public void addTaskData(Long taskId,String taskName, String dueDate, String belongedClassName, String taskURL) {//ここではデータベース登録、allTaskDataListへの登録、classDataへの登録を行う、
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isExist(taskId)) {//既に持ってる場合は
                makeTaskNotSubmitted(taskName);//未提出判定に
            } else {
                if (LocalDateTime.parse(dueDate, formatter).isAfter(LocalDateTime.now(ZoneId.of("Asia/Tokyo")))) {//提出期限が過ぎていなければ
                    try {
                        LocalDateTime deadLine = LocalDateTime.parse(dueDate, formatter);
                        TaskData taskData = new TaskData(taskId, belongedClassName, taskName, deadLine, taskURL, 0);//スクレーピングしてきたデータだからhasSubmittedは0
                        LocalDateTime defaultTiming = deadLine.plusHours(-1);
                        taskData.addNotificationTiming(defaultTiming);
                        requestSettingNotification(dataName, taskData.getTaskId(), taskName, dueDate, defaultTiming);
                        allTaskDataList.add(taskData);
                        insertTaskDataIntoDB(taskData);
                    } catch (DateTimeParseException e) {
                        Log.d("aaa", "デフォルトの通知タイミングを設定できませんでした。TaskDataManager 147");
                        TaskData taskData = new TaskData(0L, belongedClassName, taskName, LocalDateTime.MAX, taskURL, 0);//スクレーピングしてきたデータだからhasSubmittedは0
                        allTaskDataList.add(taskData);
                        insertTaskDataIntoDB(taskData);
                    }
                } else {//提出期限が過ぎていれば
                    Log.d("aaa", "スクレーピングした" + taskName + "は提出期限を過ぎていたので追加しません　TaskDataManager 179");
                }
                sortAllTaskDataList();
            }
        }
    }
    public Boolean isExist(Long taskId) {
        //既に持っている課題か判定
        for (TaskData taskData : allTaskDataList)
            if (Objects.equals(taskData.getTaskId(), taskId)) return true;
        return false;
    }
    public void deleteTaskNotification(int dataNum, int notificationNum) {
        deleteNotification(dataNum, notificationNum);
    }
    public void deleteFinishedTaskNotification(String title, String subTitle) {
        subTitle = subTitle.substring(0, 10) + " " + subTitle.substring(11);//2024-06-12T03:10のTを消す。
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int num = 0;
            num = deleteFinishedNotification(title, LocalDateTime.parse(subTitle, formatter));
            //changeBellButton(num);
            if (notificationAdapterBag.get(num) != null) {
                notificationAdapterBag.get(num).notifyDataSetChanged();
            } else {
                Log.d("aaa", num + "番目の課題の通知リストを更新できませんでした。");
            }

        }
    }
    public void getTaskDataFromManaba() {
        try {
            ArrayList<String> taskList = ManabaScraper.scrapeTaskDataFromManaba();
            if (taskList == null) {
                // 例外を作成して投げる
                throw new RuntimeException("Task list is null. Unable to retrieve task data.");
            }
            for (String k : taskList) {
                String[] str = k.split("\\?\\?\\?"); // 切り分けたクッキーをさらに=で切り分ける
                if (!isExist(Long.parseLong(str[0]))) { // 取得した課題を持っていなかったら追加する
                    if (str[1].length() > 15) {
                        str[1] = str[1].substring(0, 15);
                        str[1] += "...";
                    }
                    str[1] += " ";
                    addTaskData(Long.parseLong(str[0]), str[1], str[2], str[3], str[4]); // str[0]はTaskId, str[1]は課題名, str[2]は締め切り, str[3]は課題が出ている授業名, str[4]は課題提出URL
                    Log.d("aaa", k + "追加したよー！TaskDataManager　getTaskDataFromManaba");
                } else { // 取得した課題を持っていたら提出していない判定(hassubmittedを0)にする。
                    for (int i = 0; i < allTaskDataList.size(); i++) {
                        if (Objects.equals(allTaskDataList.get(i).getTaskId(), Long.parseLong(str[0]))) {
                            allTaskDataList.get(i).changeSubmitted(0);
                            break;
                        }
                    }
                }
            }
        } catch (ExecutionException e) {
            // 例外を作成して投げる
            throw new RuntimeException("課題スクレーピング中にエラーが発生しました。", e);
        } catch (InterruptedException e) {
            // 例外を作成して投げる
            throw new RuntimeException("課題スクレーピング中にスレッドが中断されました。", e);
        }
    }

    public void makeAllTasksSubmitted() {//ここでDBのhasSubmittedも更新
        for (int i = 0; i < allTaskDataList.size(); i++) {
            allTaskDataList.get(i).changeSubmitted(1);
            changeHasSubmittedIntoDB(allTaskDataList.get(i).getTaskId(),true);
        }
    }
    public void makeTaskNotSubmitted(String taskName) {
        for (int i = 0; i < allTaskDataList.size(); i++) {
            if (Objects.equals(taskName, allTaskDataList.get(i).getTaskName())) {
                allTaskDataList.get(i).changeSubmitted(0);
                break;
            }
        }
    }
    public void insertTaskDataIntoDB(TaskData taskData) {
        if (db == null) {
            Log.d("aaa", "db空っぽです。(TaskDataManager 239)");
        }
        String dueDate = taskData.getDueDate().toString();
        dueDate = dueDate.substring(0, 10) + " " + dueDate.substring(11);
        ContentValues values = new ContentValues();
        values.put("taskId", taskData.getTaskId());
        values.put("belongedClassName", taskData.getBelongedClassName());
        values.put("taskName", taskData.getTaskName());
        values.put("dueDate", dueDate);
        values.put("taskURL", taskData.getTaskURL());
        if (taskData.getHasSubmitted() == 0) values.put("hasSubmitted", "0");
        else values.put("hasSubmitted", "1");
        String notificationtime = "";
        if (taskData.getNotificationTiming().size() > 0) {
            String before = String.valueOf(taskData.getNotificationTiming().get(0));
            char[] charArray2 = before.toCharArray();
            charArray2[10] = ' ';
            notificationtime = new String(charArray2);
        } else notificationtime = "";
        values.put("notificationTiming", notificationtime);
        long newRowId = db.insert(dataName, null, values);
        if (newRowId != -1) {
            Log.d("aaa", dataName + "に" + taskData.getTaskName() + "追加しました。TaskDataManager 268");
        } else {
            Log.d("aaa", dataName + "に追加失敗。TaskDataManager 262");
        }
    }
    public void changeHasSubmittedIntoDB(Long taskId, Boolean flag) {
        ContentValues values = new ContentValues();
        values.put("hasSubmitted", flag);

        // WHERE 句を設定（どのレコードを更新するか）
        String whereClause = "taskId=?";
        String[] whereArgs = {String.valueOf(taskId)};

        // レコードを更新
        db.update(dataName, values, whereClause, whereArgs);
        Cursor cursor = db.query(dataName, new String[]{"hasSubmitted"}, whereClause, whereArgs, null, null, null);
        String result = null;
        assert cursor != null;
        if (cursor.moveToFirst()) { // カーソルを最初の位置に移動
            // カラムのインデックスを取得
            int columnIndex = cursor.getColumnIndex("hasSubmitted");
            // 値を取得
            result = cursor.getString(columnIndex);
        }
    }
    public void addNotificationTiming(int num, LocalDateTime notificationTiming) {
        Log.d("aaa", num + "番目の課題のつうちを追加します。TaskdataManager addnotificationTioming");
        allTaskDataList.get(num).addNotificationTiming(notificationTiming);
        requestSettingNotification(dataName, allTaskDataList.get(num).getTaskId(), allTaskDataList.get(num).getTaskName(), allTaskDataList.get(num).getDueDate().toString(), notificationTiming);
        updateNotificationTimingFromDB(allTaskDataList.get(num));
    }
    public void updateNotificationTimingFromDB(TaskData taskData) {
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
            String[] whereArgs = {String.valueOf(taskData.getTaskId())};

            // レコードを更新
            db.update(dataName, values, whereClause, whereArgs);
            Cursor cursor = db.query(dataName, new String[]{"notificationTiming"}, whereClause, whereArgs, null, null, null);
            if (cursor == null)
                Log.d("aaa", "taskId=" + taskData.getTaskId() + "のやつが見つかりませんでした(TaskDataManager 301)");
            String result = null;
            assert cursor != null;
            if (cursor.moveToFirst()) { // カーソルを最初の位置に移動
                // カラムのインデックスを取得
                int columnIndex = cursor.getColumnIndex("notificationTiming");
                // 値を取得
                result = cursor.getString(columnIndex);
            }
        }
    }
    public void requestSettingNotification(String dataName, Long taskId, String taskName, String dueDate, LocalDateTime notificationTiming) {
        NotifyManager.setTaskNotificationAlarm(dataName, String.valueOf(taskId), taskName, dueDate, notificationTiming);
    }
    public void requestCancelNotification(String dataName, String taskName, LocalDateTime dueDate, LocalDateTime notificationTiming) {
        NotifyManager.cancelTaskNotificationAlarm(dataName, taskName, dueDate.toString(), notificationTiming);
    }
    public int deleteFinishedNotification(String taskName, LocalDateTime subTitle) {
        //dbの更新は呼び出し元で行うので、ここでは行わない。ここではメモリ上の通知情報のみ更新。
        for (int i = 0; i < allTaskDataList.size(); i++) {
            if (Objects.equals(allTaskDataList.get(i).getTaskName(), taskName) && Objects.equals(allTaskDataList.get(i).getDueDate(), subTitle)) {
                allTaskDataList.get(i).deleteFinishedNotification();
                return i;
            }
        }
        return -1;
    }
    public void deleteNotification(int dataNum, int notificationNum) {
        //dbの更新もここで行う。
        requestCancelNotification(dataName, allTaskDataList.get(dataNum).getTaskName(), allTaskDataList.get(dataNum).getDueDate(), allTaskDataList.get(dataNum).getNotificationTiming().get(notificationNum));
        allTaskDataList.get(dataNum).deleteNotificationTiming(notificationNum);
        updateNotificationTimingFromDB(allTaskDataList.get(dataNum));
    }
    public Boolean isExistInClassDataList(String className) {
        for (ClassData classData : ClassDataManager.classDataList) {
            if (Objects.equals(className, classData.getClassName())) return true;
        }
        return false;
    }
    public Boolean isExistInUnRegisteredClassDataList(String className) {
        for (ClassData classData : ClassDataManager.unRegisteredClassDataList) {
            if (Objects.equals(className, classData.getClassName())) return true;
        }
        return false;
    }
    public static void setDB(SQLiteDatabase DB, Cursor Cursor) {// データベースを渡す
        db = DB;
        cursor = Cursor;
    }
}