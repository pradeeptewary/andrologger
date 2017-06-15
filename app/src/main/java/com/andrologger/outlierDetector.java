package com.andrologger;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.CSVLoader;
import weka.core.Attribute;
import weka.filters.unsupervised.attribute.InterquartileRange;


import static weka.core.Instances.mergeInstances;

public class outlierDetector extends IntentService {

    private int count=0,savedcount=0;
    public outlierDetector() {
        super("Classification");
    }


    AndroLoggerDatabase dbm;

    @Override
    protected void onHandleIntent(Intent intent) {
        try {

            System.out.println("[BuildModelService]: BuildModelService is running");




            buildModel(getString(R.string.data_file_path)+getString(R.string.train_file_path));

            System.out.println("[BuildModelService]: Model Saved");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Instances makeARFF(){
        dbm = new AndroLoggerDatabase(outlierDetector.this);
        ArrayList<Attribute> attrib = new ArrayList<Attribute>();
        Attribute a1 = new Attribute("Latitude");
        Attribute a2 = new Attribute("Longitude");
        attrib.add(a1);
        attrib.add(a2);

        Instances  data;
        Instances  dataRel;
        double[]   vals;
        double[]   valsRel;
        int i;




        String Query = "select * from events where detector='CallWatcher'";
        ArrayList<Cursor> alc = dbm.getData(Query);
        data = new Instances("Dataset",attrib,0);
        Cursor c;
        c = alc.get(0);
        if((c!=null) && (c.getCount()>0)) {
            c.moveToFirst();
            int count = c.getCount();
            while (count > 0) {
                count--;
                vals = new double[data.numAttributes()];
                vals[0]=c.getLong(1);
                vals[1]=c.getLong(2);
                //vals[1]=data.attribute(1).addStringValue(c.getString(1));
                //vals[3] = data.attribute(3).parseDate("2001-11-09");
                //vals[1] = attVals.indexOf("val3");
                //dataRel = new Instances(data.attribute(4).relation(), 0);
                // -- first instance
                //valsRel = new double[2];
                //valsRel[0] = Math.PI + 1;
                //valsRel[1] = attValsRel.indexOf("val5.3");
                //dataRel.add(new Instance(1.0, valsRel));
                // -- second instance
                //valsRel = new double[2];
                //valsRel[0] = Math.PI + 2;
                //valsRel[1] = attValsRel.indexOf("val5.2");
                //dataRel.add(new Instance(1.0, valsRel));
                //vals[4] = data.attribute(4).addRelation(dataRel);
                data.add(new SparseInstance(1.0, vals));


            }
        }
        return data;
    }


    private void buildModel(String filePath) throws IOException {


        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        System.out.println("---->>>>>> "+sdCardRoot.getAbsolutePath());

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        final SharedPreferences counter = getSharedPreferences("counter", Context.MODE_PRIVATE);
        final SharedPreferences.Editor countedit = counter.edit();

        count = counter.getInt("counter",savedcount) + 1;
        countedit.putInt("counter",count).commit();

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String currentDateandTime = sdf.format(new Date());

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null,iFilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        float batteryPct = level*100/(float)scale;

        String outtext = count + ","+currentDateandTime+","+batteryPct+",";

        File filefolder = new File(Environment.getExternalStorageDirectory() + filePath);
        final File[] files = filefolder.listFiles();
        if (files != null) {
            Instances data = null;
            int flag = 0;
            for (File file : files) {
                if (file != null) {
                    CSVLoader loader = new CSVLoader();
                    try {
                        loader.setSource(file);
                        if(flag == 0) {
                            data = loader.getDataSet();
                            flag = 1;
                        }
                        else
                            data = mergeInstances(data,loader.getDataSet());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(data.numInstances()==0) {
                String error_log = getString(R.string.log_file_path) + getString(R.string.error_log);
                String errout = currentDateandTime+" Build Model Failed (Train Data is Null)\n";
                byte[] error_data = errout.getBytes();

                File error_file = new File(dataDir, error_log);
                try {
                    FileOutputStream fos;
                    fos = new FileOutputStream(error_file,true);
                    fos.write(error_data);
                    fos.close();
                }
                catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }

                System.out.println("Build Model Failed...Train Data is Null");
                return;
            }
            for(int i=data.numAttributes()-2;i>=2;i--) {
                data.deleteAttributeAt(i);
            }
            data.setClassIndex(data.numAttributes() - 1);
            RandomForest rf = new RandomForest();
            try {
                System.out.println("[BuildModelService]: Model Training Start");
                rf.buildClassifier(data);
                System.out.println("[BuildModelService]: Model Build Completed");
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory() + getString(R.string.model_path)));
                out.writeObject(rf);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            outtext = outtext+data.numInstances()+",";
        }

        String currentDateandTime2 = sdf.format(new Date());

        level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        float batteryPct2 = level*100/(float)scale;

        long diff = 0;
        try {
            Date starttime = sdf.parse(currentDateandTime);
            Date endtime = sdf.parse(currentDateandTime2);
            diff = (endtime.getTime() - starttime.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        float batteryDiff = batteryPct - batteryPct2;

        outtext = outtext+currentDateandTime2+","+batteryPct2+","+diff+","+batteryDiff+"\n";

        String buildModel_log = getString(R.string.log_file_path) + getString(R.string.buildModel_log);
        byte[] buildModel_data = outtext.getBytes();

        File buildModel_file = new File(dataDir, buildModel_log);
        try {
            FileOutputStream fos;
            fos = new FileOutputStream(buildModel_file,true);
            fos.write(buildModel_data);
            fos.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
