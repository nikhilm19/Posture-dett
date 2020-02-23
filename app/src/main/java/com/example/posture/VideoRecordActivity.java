package com.example.posture;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import java.util.List;

import com.amazonaws.mobileconnectors.s3.transferutility.*;


import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class VideoRecordActivity extends AppCompatActivity {

    Uri videoUri;
    File outputFile;


    Button button;
    VideoView videoView;
    private static final int VIDEO_CAPTURE = 101;
    String fileName;
    File f;
    ProgressBar uploadProgress;
    String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        button = findViewById(R.id.capture_video_btn);
        videoView = findViewById(R.id.video_record_view);
        uploadProgress = findViewById(R.id.upload_progressbar);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakeVideoIntent();
            }
        });
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


        Log.i("VIDEO",videoUri.toString());

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

        showProgress();

        AWSMobileClient.getInstance().initialize(this).execute();
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
                ListObjectsV2Result result = s3.listObjectsV2(BuildConfig.BUCKET_NAME);
                List<S3ObjectSummary> objects = result.getObjectSummaries();
                for (S3ObjectSummary os : objects) {
                    System.out.println("* " + os.getKey());
                }
            }
        }).start();






        TransferObserver uploadObserver =
                transferUtility.upload(
                        "uploads/"+videoId+".mp4",
                        outputFile);

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    hideProgress();


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
}
