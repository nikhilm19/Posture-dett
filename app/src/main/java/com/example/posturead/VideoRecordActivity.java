package com.example.posturead;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.s3.transferutility.*;


import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;




import android.os.Build;


import java.io.File;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import androidx.annotation.RequiresApi;
import com.netcompss.loader.LoadJNI;


public class VideoRecordActivity extends AppCompatActivity {

    Uri videoUri;
    File outputFile;
    Button button,resultsBtn;
    VideoView videoView;
    private static final int VIDEO_CAPTURE = 101;
    String fileName;
    File f;
    ProgressBar uploadProgress;
    String videoId;
    DynamoDBMapper dynamoDBMapper;
    PostureDbDO currPoseItem;

    public void endExercise() {
        final PostureDbDO postureItem = new PostureDbDO();

        currPoseItem.setIsExerciseOn(Boolean.FALSE);




        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(currPoseItem);
                // Item saved
            }
        }).start();
    }

    public void readExercise(final AsyncTaskRunner a) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                currPoseItem = dynamoDBMapper.load(
                        PostureDbDO.class,
                        "amzn1.ask.account.AHGC4FDSZQGHDRU3UVOLGWQLVXHPY2CBMNMU4LYSG2AXREKYZZAZ3F5ADRCBGQQJV7BR6ZQQ3QOBL6SLOHFQ54NZ66GPJKA7IP7DVF3WEPAP4ZDH7KUVH2QXVMNFADK6I7J7N2NWJF77XRIKVZ4S5ZW7PRURBHNEF4PENNCRH7JDNROLIPLDCCU76LBIXEW33X26EJU3LY4FVCY");

                // Item read

                Log.d("pose Item:", currPoseItem.getExerciseName()+" "+currPoseItem.getUserId());

                if(currPoseItem.getIsExerciseOn()){

                    a.cancel(true);
                }
                else {
                    Looper.prepare();
                                    }

            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        button = findViewById(R.id.capture_video_btn);
        resultsBtn=findViewById(R.id.results_btn);
        videoView = findViewById(R.id.video_record_view);
        uploadProgress = findViewById(R.id.upload_progressbar);



        resultsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent  = new Intent(getApplicationContext(),Output.class);
                startActivity(intent);
            }
        });



        AWSMobileClient.getInstance().initialize(this).execute();
        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();


        // Add code to instantiate a AmazonDynamoDBClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);

        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskRunner runner = new AsyncTaskRunner();
                String sleepTime = "10";
                runner.execute(sleepTime);
            }
        });

        //startExercise();

    }

    static final int REQUEST_VIDEO_CAPTURE = 1;

    private void dispatchTakeVideoIntent() {


        //Soltution to --- exposed beyond app through ClipData.Item.getUri()

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());




        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        final String folderPath = Environment.getExternalStorageDirectory() + "/Project/Videos";


        File root = new File(Environment.getExternalStorageDirectory(),"Project");
        if(!root.exists()){

            Log.i("VIDEO","Root doesnt exist");
            root.mkdirs();

            root = new File(Environment.getExternalStorageDirectory()+"/Project/Videos");
            if(!root.exists()){
                root.mkdirs();
            }
        }


        root = new File(Environment.getExternalStorageDirectory(),"Project/Videos");

        String ts = String.valueOf(Instant.now().getEpochSecond());
        videoId =ts;



        outputFile = new File(folderPath+"/"+ts+".mp4");
        videoUri = Uri.fromFile(outputFile);
        Log.i("VIDEO",videoUri.toString());

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, videoUri);
        startActivityForResult(intent, VIDEO_CAPTURE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //using global uri here to solve null error
       super.onActivityResult(requestCode,resultCode,data);


        Log.i("VIDEO-RESULT",videoUri.toString());

        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video saved to:\n" +
                        videoUri, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_LONG).show();
            }
        }


        Uri video = Uri.parse(videoUri.toString());
        videoView.setVideoURI(video);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                videoView.start();
            }
        });

        LoadJNI vk = new LoadJNI();
        try {
            String workFolder = getApplicationContext().getFilesDir().getAbsolutePath();
            String[] complexCommand = {"ffmpeg","-i",outputFile.toString(),"-b","800k","/storage/emulated/0/Project/Videos/"+videoId+"_extracted.mp4" };
            vk.run(complexCommand , workFolder , getApplicationContext());
            Log.i("test", "ffmpeg4android finished successfully");
            //todo


            outputFile=new File("/storage/emulated/0/Project/Videos/"+videoId+"_extracted.mp4");
        } catch (Throwable e) {
            Log.e("test", "vk run exception.", e);
        }


        showProgress();


        uploadWithTransferUtility();

    }

    public void showProgress(){
        uploadProgress.setIndeterminate(true);

        uploadProgress.setVisibility(View.VISIBLE);

    }

    public  void  hideProgress(){

        uploadProgress.setVisibility(View.INVISIBLE);
    }


    public void setProgressBar(int progress){

        if(progress!=0){
            uploadProgress.setIndeterminate(false);
        }
        uploadProgress.setProgress(progress,true);

    }

    public void getServer(){


        new Thread(new Runnable() {
            @Override
            public void run() {

                PostureDbDO serverItem = dynamoDBMapper.load(
                        PostureDbDO.class,"server");

                Log.d("Server_ITEM",serverItem.getLocalServerUrl());

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());  // this = context





                final String videoUrl ="https://posturedetection-userfiles-mobilehub-1869887158.s3.amazonaws.com/uploads/"+videoId+".mp4";


                //TODO make a API CALL

                String API_URL = serverItem.getLocalServerUrl()+"/posture-output";

                StringRequest postRequest = new StringRequest(Request.Method.POST, API_URL,
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                // response
                                Log.d("Response", response);
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("Error.Response", error.toString());
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("videoId", videoId);
                        params.put("videoUrl",videoUrl);


                        return params;
                    }
                };
                queue.add(postRequest);



            }
        }).start();





    }


    public void uploadWithTransferUtility() {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))

                        .build();

        final AmazonS3Client s3 = new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider());
        Log.i("AWSS3",s3.toString());


        new Thread(new Runnable() {
            public void run() {
                ListObjectsV2Result result = s3.listObjectsV2("posturedetection-userfiles-mobilehub-1869887158");
                List<S3ObjectSummary> objects = result.getObjectSummaries();
                for (S3ObjectSummary os : objects) {
                    System.out.println("* " + os.getKey());
                }
            }
        }).start();






        TransferObserver uploadObserver =
                transferUtility.upload(
                        "uploads/"+videoId+"_extracted.mp4",
                        outputFile);

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.


                    //Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
                    //key.put("")

                    getServer();



                    hideProgress();
                    endExercise();


                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");


                Toast.makeText(getApplicationContext(),""+percentDone+"%",Toast.LENGTH_SHORT).show();
                setProgressBar(percentDone);
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d("YourActivity", "Bytes Transferrred: " + uploadObserver.getBytesTransferred());
        Log.d("YourActivity", "Bytes Total: " + uploadObserver.getBytesTotal());
    }



    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            readExercise(this);


            try {
                int time = Integer.parseInt(params[0])*1000;


                readExercise(this);


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

            dispatchTakeVideoIntent();
            super.onCancelled();
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),"Please ask Alexa for exercise first",Toast.LENGTH_LONG).show();



        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(VideoRecordActivity.this,
                    "Posture Video",
                    "Please wait");

        }


        @Override
        protected void onProgressUpdate(String... text) {
            //finalResult.setText(text[0]);

        }
    }
}









