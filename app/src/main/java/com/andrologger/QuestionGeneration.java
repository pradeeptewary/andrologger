package com.andrologger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import weka.classifiers.bayes.net.search.local.LocalScoreSearchAlgorithm;
import weka.core.*;
import weka.classifiers.misc.*;
import weka.filters.unsupervised.attribute.*;


/**
 * A login screen that offers login via email/password.
 */
public class QuestionGeneration extends Activity {

    AndroLoggerDatabase dbm;
    SQLiteDatabase ques_database;
    Cursor c;

    //Question related variables
    Cursor ques_c;
    ArrayList<Cursor> ques_array;

    //Running Answer Weight
    float runningAnswerWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_server);

        runningAnswerWeight =1;
        dbm = new AndroLoggerDatabase(QuestionGeneration.this);


        ques_database = openOrCreateDatabase("results.db",MODE_PRIVATE,null);

        SegregateCallRecords(dbm);
        CallRelatedPositiveQuestions(dbm);
        CallRelatedNegativeQuestions(dbm);
        CallRelatedMCQQuestions(dbm);

        ShowQuestion(dbm);
        //AlertDialog dialogBox = makeAndShowDialogBox();
        //dialogBox.show();
    }


    public void SegregateCallRecords(AndroLoggerDatabase db){
        long currentTime = System.currentTimeMillis();
        Log.d("Current Time",Long.toString(currentTime));
        String Query = "select * from events where detector='CallWatcher'";
        ArrayList<Cursor> alc = db.getData(Query);
        c = alc.get(0);
        c.moveToFirst();
        int count = c.getCount();
        while (count > 0) {
            count--;
            //Call Details
            Long callTime = c.getLong(4);
            String desc = c.getString(5);

            //Call additional info
            String addl_info = c.getString(6);
            //phone number
            String temp20 = addl_info.split(";")[0];
            String callId = temp20.split(":")[1].trim();
            int callIdInt = Integer.parseInt(callId);

            String temp21 = addl_info.split(";")[1];
            String phone_no = temp21.split(":")[1].trim();
            //Long ph_no= Long.parseLong(phone_no);

            String name1 = addl_info.split(";")[2];
            String name = name1.split(":")[1];

            String durationTemp = addl_info.split(";")[3];
            String durationTemp1 = durationTemp.split(" ")[1];
            String duration = durationTemp1.split(":")[1];
            int durationInt = Integer.parseInt(duration);

            String numTypeTemp = addl_info.split(";")[3];
            String numTypeTemp1 = numTypeTemp.split(";")[1];
            String numType = numTypeTemp1.split(":")[1];
            int numTypeInt = Integer.parseInt(numType);

            if(!name.equalsIgnoreCase("null")){
                try {
                    ques_database.execSQL("INSERT INTO cdr VALUES("+callTime+","+callId+",'"+desc+"','"+phone_no+"','"+name+"',"+durationInt+","+numTypeInt+");");
                } catch (SQLException s){
                    System.out.println("Inserted in CDR");
                }

            }
            c.moveToNext();
        }
        c.close();
    }


    public void CallOutlierDetection(AndroLoggerDatabase db){
        long currentTime = System.currentTimeMillis();
        String Query = "select * from cdr group by";
        ArrayList<Cursor> alc = db.getData(Query);
        c = alc.get(0);
        c.moveToFirst();
        int count = c.getCount();
        while (count > 0) {
            count--;
            //Event time
            Long eventTime = c.getLong(2);
            String eventStr = c.getString(2);
            Date dat = new Date(eventTime*1000);
            SimpleDateFormat sdfDay = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a");
            Log.d("Event Time",eventStr);
            //Event additional info
            String addl_info = c.getString(6);
            //phone number
            String temp10 = addl_info.split(";")[1];
            String phone_no = temp10.split(":")[1].trim();
            Log.d("Database",addl_info);
            Long ph_no= Long.parseLong(phone_no);
            //name
            String name1 = addl_info.split(";")[2];
            String name = name1.split(":")[1];
            //duration
            String durationTemp = addl_info.split(";")[3];
            String durationTemp1 = durationTemp.split(" ")[1];
            String duration = durationTemp1.split(":")[1];
            Log.d("Database", duration);
            int durationInt = Integer.parseInt(duration);
            if(!name.equalsIgnoreCase("null")){
                String question = "Did you speak to " + name + " at around " + sdfTime.format(dat) + " on " + sdfDay.format(dat) + " ?";
                try {
                    ques_database.execSQL("INSERT INTO questions VALUES('"+eventStr+"','"+eventStr+"',3,'"+question+"','Y',2);");
                } catch (SQLException s){
                    System.out.println("Related Question Present");
                }


                Log.d("Database", question);
            }
            /*
            Cursor c1;
            String Query1 = "select * from events where detector='CallWatcher' INNER JOIN {select * from contacts} ON events.additional_info CONTAINS contacts.number";
            try {
                ArrayList<Cursor> alc1 = db.getData(Query1);
                c1 = alc1.get(0);
                Log.d("Database", c1.getString(0));
            }
            catch (NullPointerException e){
                System.out.println(e);
            }
            */
            c.moveToNext();
        }
        c.close();
    }

    public void CallRelatedPositiveQuestions(AndroLoggerDatabase db){
        long currentTime = System.currentTimeMillis();
        Log.d("Current Time",Long.toString(currentTime));
        String Query = "select * from events where detector='CallWatcher'";
        ArrayList<Cursor> alc = db.getData(Query);
        c = alc.get(0);
        c.moveToFirst();
        int count = c.getCount();
        while (count > 0) {
            count--;
            //Event time
            Long eventTime = c.getLong(4);
            String eventStr = c.getString(4);
            Date dat = new Date(eventTime);
            SimpleDateFormat sdfDay = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a");
            Log.d("Event Time",eventStr);
            //Event additional info
            String addl_info = c.getString(6);
            //phone number
            String temp10 = addl_info.split(";")[1];
            String phone_no = temp10.split(":")[1].trim();
            Log.d("Database",addl_info);
            Long ph_no= Long.parseLong(phone_no);
            //name
            String name1 = addl_info.split(";")[2];
            String name = name1.split(":")[1];
            //duration
            String durationTemp = addl_info.split(";")[3];
            String durationTemp1 = durationTemp.split(" ")[1];
            String duration = durationTemp1.split(":")[1];
            Log.d("Database", duration);
            int durationInt = Integer.parseInt(duration);
            if(!name.equalsIgnoreCase("null")){
                String question = "Did you speak to " + name + " at around " + sdfTime.format(dat) + " on " + sdfDay.format(dat) + " ?";
                try {
                    ques_database.execSQL("INSERT INTO questions VALUES('"+eventStr+"','"+eventStr+"',3,'"+question+"','Y',2);");
                } catch (SQLException s){
                    System.out.println("Related Question Present");
                }


                Log.d("Database", question);
            }
            /*
            Cursor c1;
            String Query1 = "select * from events where detector='CallWatcher' INNER JOIN {select * from contacts} ON events.additional_info CONTAINS contacts.number";
            try {
                ArrayList<Cursor> alc1 = db.getData(Query1);
                c1 = alc1.get(0);
                Log.d("Database", c1.getString(0));
            }
            catch (NullPointerException e){
                System.out.println(e);
            }
            */
            c.moveToNext();
        }
        c.close();
    }

    public void CallRelatedNegativeQuestions(AndroLoggerDatabase db){
        String Query = "select * from events where detector='CallWatcher'";
        ArrayList<Cursor> alc = db.getData(Query);
        c = alc.get(0);
        c.moveToFirst();
        int count = c.getCount();
        while (count > 0) {
            count--;
            //Event time
            Long eventTime = c.getLong(2);
            Long eventTime1 = c.getLong(2)+1;
            String eventStr = Long.toString(eventTime1);
            Date dat = new Date(eventTime*1000);
            SimpleDateFormat sdfDay = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a");
            Log.d("Date",sdfTime.format(dat));
            //Event additional info
            String addl_info = c.getString(6);
            //phone number
            String temp10 = addl_info.split(";")[1];
            String phone_no = temp10.split(":")[1].trim();
            Log.d("Database",addl_info);
            Long ph_no= Long.parseLong(phone_no);
            //name
            String name1 = addl_info.split(";")[2];
            String name = name1.split(":")[1];
            //duration
            String durationTemp = addl_info.split(";")[3];
            String durationTemp1 = durationTemp.split(" ")[1];
            String duration = durationTemp1.split(":")[1];
            Log.d("Database", duration);
            int durationInt = Integer.parseInt(duration);
            if(!name.equalsIgnoreCase("null")){
                String Query2 = "SELECT * FROM contacts WHERE _id IN (SELECT _id FROM contacts ORDER BY RANDOM() LIMIT 1)";
                ArrayList<Cursor> alc2 = db.getData(Query2);
                Cursor c2 = alc2.get(0);
                String falseName = c2.getString(3);
                String question = "Did you speak to " + falseName + " at around " + sdfTime.format(dat) + " on " + sdfDay.format(dat) + " ?";
                try {
                    ques_database.execSQL("INSERT INTO questions VALUES('"+eventStr+"','"+eventStr+"',3,'"+question+"','N',2);");
                } catch (SQLException s){
                    System.out.println("Related Question Present");
                }
                c2.close();

                Log.d("Database", question);
            }
            /*
            Cursor c1;
            String Query1 = "select * from events where detector='CallWatcher' INNER JOIN {select * from contacts} ON events.additional_info CONTAINS contacts.number";
            try {
                ArrayList<Cursor> alc1 = db.getData(Query1);
                c1 = alc1.get(0);
                Log.d("Database", c1.getString(0));
            }
            catch (NullPointerException e){
                System.out.println(e);
            }
            */
            c.moveToNext();
        }
        c.close();
    }

    public void CallRelatedMCQQuestions(AndroLoggerDatabase db){
        String Query = "select * from events where detector='CallWatcher'";
        ArrayList<Cursor> alc = db.getData(Query);
        c = alc.get(0);
        c.moveToFirst();
        int count = c.getCount();
        while (count > 0) {
            count--;
            //Event time
            Long eventTime = c.getLong(2);
            Long eventTime1 = c.getLong(2)+3;
            String eventStr = Long.toString(eventTime1);
            Date dat = new Date(eventTime*1000);
            SimpleDateFormat sdfDay = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a");
            Log.d("Date",sdfTime.format(dat));
            //Event additional info
            String addl_info = c.getString(6);
            //phone number
            String temp10 = addl_info.split(";")[1];
            String phone_no = temp10.split(":")[1].trim();
            Log.d("Database",addl_info);
            Long ph_no= Long.parseLong(phone_no);
            //name
            String name1 = addl_info.split(";")[2];
            String name = name1.split(":")[1];
            //duration
            String durationTemp = addl_info.split(";")[3];
            String durationTemp1 = durationTemp.split(" ")[1];
            String duration = durationTemp1.split(":")[1];
            Log.d("Database", duration);
            int durationInt = Integer.parseInt(duration);
            if(!name.equalsIgnoreCase("null")){
                Random r = new Random();
                int random = r.nextInt(3);
                int chek = 0 , ch = 0;
                String Query2 = "SELECT * FROM contacts WHERE _id IN (SELECT _id FROM contacts ORDER BY RANDOM() LIMIT 4)";
                ArrayList<Cursor> alc2 = db.getData(Query2);
                Cursor c2 = alc2.get(0);
                String options = "";
                boolean loopCheck = true;
                while (loopCheck){
                    if (random == chek){
                        options = options + ";" + name;
                        ch = chek;
                    }
                    else {
                        options = options + ";"+c2.getString(3);
                    }
                    chek++;
                    loopCheck = c2.moveToNext();
                }
                //String falseName = c2.getString(3);
                String question = " Whom did you speak to at around " + sdfTime.format(dat) + " on " + sdfDay.format(dat) + " ? " + options;
                try {
                    ques_database.execSQL("INSERT INTO questions VALUES('"+eventStr+"','"+eventStr+"',1,'"+question+"',"+Integer.toString(ch)+",3);");
                } catch (SQLException s){
                    System.out.println("Related Question Present");
                }
                c2.close();

                Log.d("Database", question);
            }
            /*
            Cursor c1;
            String Query1 = "select * from events where detector='CallWatcher' INNER JOIN {select * from contacts} ON events.additional_info CONTAINS contacts.number";
            try {
                ArrayList<Cursor> alc1 = db.getData(Query1);
                c1 = alc1.get(0);
                Log.d("Database", c1.getString(0));
            }
            catch (NullPointerException e){
                System.out.println(e);
            }
            */
            c.moveToNext();
        }
        c.close();
    }

    void Questions (){
        final int ques_id = ques_c.getInt(0);
        int type = ques_c.getInt(2);
        TextView question;
        final Button submit;
        Button next;

        switch(type)
        {
            case 1:
            {
                setContentView(R.layout.quiz_layout_mcq);
                question = (TextView) findViewById(R.id.question);
                String questionString = ques_c.getString(3);
                String quest = questionString.split(";")[0];
                String option1 = questionString.split(";")[1];
                String option2 = questionString.split(";")[2];
                String option3 = questionString.split(";")[3];
                String option4 = questionString.split(";")[4];
                question.setText(quest);
                RadioButton r1 = (RadioButton) findViewById(R.id.radio0);
                r1.setText(option1);
                RadioButton r2 = (RadioButton) findViewById(R.id.radio1);
                r2.setText(option2);
                RadioButton r3 = (RadioButton) findViewById(R.id.radio2);
                r3.setText(option3);
                RadioButton r4 = (RadioButton) findViewById(R.id.radio3);
                r4.setText(option4);
                break;
            }
            case 2:
            {
                setContentView(R.layout.quiz_layout_multiple);
                question = (TextView) findViewById(R.id.question);
                question.setText(ques_c.getString(3));
                CheckBox r1 = (CheckBox) findViewById(R.id.checkBox1);
                r1.setText(ques_c.getString(3));
                CheckBox r2 = (CheckBox) findViewById(R.id.checkBox2);
                r2.setText(ques_c.getString(3));
                CheckBox r3 = (CheckBox) findViewById(R.id.checkBox3);
                r3.setText(ques_c.getString(3));
                CheckBox r4 = (CheckBox) findViewById(R.id.checkBox4);
                r4.setText(ques_c.getString(3));
                break;
            }
            case 3:
            {
                setContentView(R.layout.quiz_layout_binary);
                question = (TextView) findViewById(R.id.question);
                question.setText(ques_c.getString(3));
                break;
            }
            case 4:
            {
                setContentView(R.layout.quiz_layout_text);
                question = (TextView) findViewById(R.id.question);
                question.setText(ques_c.getString(3));
                break;

            }

            default: break;
        }

        submit = (Button) findViewById(R.id.submit);
        next = (Button)  findViewById(R.id.next);
        next.setEnabled(false);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Button next = (Button)  findViewById(R.id.next);
                //next.setEnabled(true);
                SubmitAnswers(ques_id);
                //submit();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Boolean check=Boolean.FALSE;
                try {
                    check = ques_c.moveToNext();
                }catch (CursorIndexOutOfBoundsException e){
                    System.out.println("Finished questions");
                }

                Log.d("Loop","Running");
                if(check==Boolean.TRUE){
                    Questions();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Completed, Your score: "+Float.toString(runningAnswerWeight), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                //showQuestion(questionNum+1);
            }
        });

    }

    void ShowQuestion(AndroLoggerDatabase db) {

        TextView question;
        Button submit;
        Button next;

        String Query = "SELECT * FROM questions WHERE _id IN (SELECT _id FROM questions ORDER BY RANDOM() LIMIT 4)";
        ques_array = db.getData(Query);

        ques_c = ques_array.get(0);
        Questions();
        //ques_c.moveToFirst();

        //ques_c.close();

    }

    String submit(int questionType) {
        Button submit = (Button)findViewById(R.id.submit);
        boolean correct = false;
        String answer=null;
        try{
            switch(questionType){
                case 1:
                {
                    RadioGroup r = (RadioGroup)findViewById(R.id.radioGroup1);
                    View radioButton = r.findViewById(r.getCheckedRadioButtonId());
                    int idx = r.indexOfChild(radioButton);
                    answer = Integer.toString(idx);
                    /*
                    Question q = questions.get(questionNum);
                    q.response = new String[1];
                    q.response[0] = String.copyValueOf(q.options[idx].toCharArray());
                    questions.set(questionNum, q);
                    */
                    correct = true;
                    break;
                }

                case 3:
                {
                    RadioGroup r = (RadioGroup)findViewById(R.id.radioGroup1);
                    View radioButton = r.findViewById(r.getCheckedRadioButtonId());
                    int idx = r.indexOfChild(radioButton);
                    if(idx==0)
                        answer= "Y";
                    else
                        answer= "N";
                    correct = true;
                    break;
                }
                case 2:
                {
                    CheckBox[] chk = new CheckBox[4];
                    chk[0] = (CheckBox)findViewById(R.id.checkBox1);
                    chk[1] = (CheckBox)findViewById(R.id.checkBox2);
                    chk[2] = (CheckBox)findViewById(R.id.checkBox3);
                    chk[3] = (CheckBox)findViewById(R.id.checkBox4);
                    /*
                    Question q = questions.get(questionNum);
                    q.response = new String[4];
                    for(int i=0;i<4;i++)
                    {
                        if(chk[i].isChecked())
                            q.response[i] = String.copyValueOf(q.options[i].toCharArray());
                        else
                            q.response[i] = null;
                    }
                    questions.set(questionNum, q);
                    */
                    correct = true;
                    break;
                }

                case 4:
                {
                    EditText e = (EditText)findViewById(R.id.editText1);
                    /*
                    Question q = questions.get(questionNum);
                    q.response = new String[1];
                    q.response[0] = e.getText().toString();
                    questions.set(questionNum, q);
                    */
                    correct = true;
                    break;
                }

                default:
                {
                    break;
                }

            }
        }
        catch(Exception ex)
        {
            correct = false;
        }
        if(correct)
        {
            submit.setEnabled(false);
            Button next = (Button)findViewById(R.id.next);
            next.setEnabled(true);
        }
        else
        {
            Toast.makeText(this, "Select an option", Toast.LENGTH_SHORT).show();
        }
        return answer;
    }

    void SubmitAnswers(int ques_id){
        String Query = "select * from questions WHERE _id="+Integer.toString(ques_id);
        ArrayList<Cursor> ques_anwer = dbm.getData(Query);
        Cursor cur = ques_anwer.get(0);
        String answer = submit(cur.getInt(2));
        String database_ans = cur.getString(4);
        if (database_ans.equalsIgnoreCase(answer)){
            Log.d("Answer","Correct");
            runningAnswerWeight = (runningAnswerWeight + 1)/2;
        }
        else {
            runningAnswerWeight = (runningAnswerWeight) / 2;
        }
    }

}

