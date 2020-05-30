package com.example.posturead;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.List;

public class Output extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);

        AWSMobileClient.getInstance().initialize(this).execute();
        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();

        uploadWithTransferUtility();












    }



    public void uploadWithTransferUtility() {

        final ArrayList urls= new ArrayList<String>();

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))

                        .build();

        final AmazonS3Client s3 = new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider());
        Log.i("AWSS3",s3.toString());


        Thread imagesThread  = new Thread(new Runnable() {
            public void run() {
                ListObjectsV2Result result = s3.listObjectsV2("posturedetection-userfiles-mobilehub-1869887158");
                List<S3ObjectSummary> objects = result.getObjectSummaries();

                for (S3ObjectSummary os : objects) {


                    if(os.getKey().startsWith("feedback-images-left/") || os.getKey().startsWith("feedback-images-right/")){
                        String url = "https://posturedetection-userfiles-mobilehub-1869887158.s3.amazonaws.com/"+os.getKey();
                        urls.add(url);

                        Log.i("PUBLIC-URL",url);

                    }





                    //System.out.println("* " + os.getKey());


                }
            }
        });

        try{
            imagesThread.start();
            imagesThread.join();

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            ImagesListData[] myListData = new ImagesListData[urls.size()];

            for(int i =0;i<urls.size();i++){

                Log.i("urls",urls.get(i).toString());
                myListData[i]=new ImagesListData("Email", urls.get(i).toString());


            }

            ImagesListAdapter adapter = new ImagesListAdapter(myListData,getApplicationContext());
            // recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(adapter);

        }
        catch (Exception e){
            Log.e("Exception in thread",e.getMessage());
        }









        // Attach a listener to the observer to get state update and progress notifications

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.


    }


    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
           // readExercise(this);


            try {
                int time = Integer.parseInt(params[0])*1000;


               // readExercise(this);


                Thread.sleep(time);
                resp = "Slept for " + params[0] + " seconds";
            } catch (InterruptedException e) {
                e.printStackTrace();
                resp = e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }

        protected void onCancelled() {
            Toast.makeText(getApplicationContext(), "Record your video..", Toast.LENGTH_LONG).show();
            progressDialog.hide(); /*hide the progressbar dialog here...*/

           // dispatchTakeVideoIntent();
            super.onCancelled();
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();

        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(Output.this,
                    "Posture Video",
                    "Please wait");

        }


        @Override
        protected void onProgressUpdate(String... text) {
            //finalResult.setText(text[0]);

        }
    }
}  