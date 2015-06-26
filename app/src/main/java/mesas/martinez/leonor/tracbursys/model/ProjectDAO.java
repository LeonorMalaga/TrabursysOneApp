package mesas.martinez.leonor.tracbursys.model;

/**
 * Created by leonormartinezmesas on 28/01/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {
    // Database fields
    private SQLiteDatabase database;

    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_DATE,
            MySQLiteHelper.COLUMN_PROJECT_NAME,
            MySQLiteHelper.COLUMN_PROJECT_SPECIFICATION
    };
    private android.util.Log Lod;

    private Project cursorTo(Cursor cursor) {
        Project project = new Project();
        project.set_id(cursor.getInt(0));
        project.setDate(cursor.getString(1));
        project.setmprojectName(cursor.getString(2));
        project.setprojectSpecification(cursor.getString(3));
        return project;
    }
    
    public ProjectDAO(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public int create(Project project) {
        int insertId=-1;
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_DATE, project.getDate());
        values.put(MySQLiteHelper.COLUMN_PROJECT_NAME, project.getmprojectName());
        values.put(MySQLiteHelper.COLUMN_PROJECT_SPECIFICATION, project.getprojectSpecification());
        insertId = (int) database.insert(MySQLiteHelper.TABLE_PROJECTS, null, values);
        return insertId;
    }

    public void update(Project project) {
        String filter = MySQLiteHelper.COLUMN_ID + " = " + project.get_id();
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_DATE, project.getDate());
        values.put(MySQLiteHelper.COLUMN_PROJECT_NAME, project.getmprojectName());
        values.put(MySQLiteHelper.COLUMN_PROJECT_SPECIFICATION, project.getprojectSpecification());
        database.update(MySQLiteHelper.TABLE_PROJECTS, values, filter, null);
    }

    public boolean delete(int id) {
        return database.delete(MySQLiteHelper.TABLE_PROJECTS, MySQLiteHelper.COLUMN_ID + " = " + id, null) > 0;
    }

    public Project getProjectByID(int id) {
        Project project = new Project();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_PROJECTS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            project = cursorTo(cursor);
        }
        cursor.close();
        return project;
    }

    public Project getProjectByName(String name) {
        Project project = new Project();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PROJECTS,
                allColumns, MySQLiteHelper.COLUMN_PROJECT_NAME+ " = " + "\'" + name + "\'", null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            project = cursorTo(cursor);
        }
        cursor.close();
        return project;
    }
    public Project getProjectByDate(String date) {
        Project project = new Project();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PROJECTS,
                allColumns, MySQLiteHelper.COLUMN_DATE+ " = " + "\'" + date + "\'", null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            project = cursorTo(cursor);
        }
        cursor.close();
        return project;
    }

     public Project getLastInsert(){
        Project project = new Project();
        String selectQuery= "SELECT * FROM " + MySQLiteHelper.TABLE_PROJECTS+" ORDER BY "+MySQLiteHelper.COLUMN_ID+" DESC LIMIT 1";
        Cursor cursor = database.rawQuery(selectQuery, null);
         if (cursor != null) {
             cursor.moveToFirst();
             project = cursorTo(cursor);
         }
         cursor.close();
         return project;
    }
    public Project getLastDate(){
        Project project = new Project();
        String selectQuery= "SELECT * FROM " + MySQLiteHelper.TABLE_PROJECTS+" ORDER BY "+MySQLiteHelper.COLUMN_DATE+" DESC LIMIT 1";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
            project = cursorTo(cursor);
        }
        cursor.close();
        return project;
    }

    public List<Project> getAll() {
        List<Project> projectList = new ArrayList<Project>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_PROJECTS,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Project project = cursorTo(cursor);
            projectList.add(project);
            cursor.moveToNext();
        }
        cursor.close();
        return projectList;
    }


}
