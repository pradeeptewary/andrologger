package com.andrologger;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import static weka.core.Instances.mergeInstances;

public class Classification extends IntentService {

    private int count=0,savedcount=0;
    public Classification() {
        super("Classification");
    }

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
