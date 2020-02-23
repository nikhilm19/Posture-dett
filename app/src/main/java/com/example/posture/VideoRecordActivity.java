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
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.time.Instant;



public class VideoRecordActivity extends AppCompatActivity {

    Uri videoUri;


    Button button;
    VideoView videoView;
    private static final int VIDEO_CAPTURE = 101;
    String fileName;
    File f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        button = findViewById(R.id.capture_video_btn);
        videoView = findViewById(R.id.video_record_view);

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


        File outputFile = new File(folderPath+"/"+ts+".mp4");
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
    }
}
