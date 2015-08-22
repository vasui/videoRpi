package crash.acn_project_v4.Dump;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.File;

import crash.acn_project_v4.R;


public class DetailActivity_sftp extends ActionBarActivity {

    private VideoView videoView;
    private MediaController mediaCtrl;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        videoView = (VideoView) findViewById(R.id.video);
        mediaCtrl = new MediaController(this);
        mediaCtrl.setMediaPlayer(videoView);
        videoView.setMediaController(mediaCtrl);

        new InitializeApplicationsTask().execute();


        videoView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }


    private class InitializeApplicationsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {


            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            JSch jsch = new JSch();
            Session session = null;
            try {
                File tempfile = getFileStreamPath("test.mp4");
                if(tempfile.exists()) {
                session = jsch.getSession("axd142130", "10.176.67.111", 22);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword("Aashish123$");
                session.setConfig("PreferredAuthentications",
                        "password");
                session.connect();

                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftpChannel = (ChannelSftp) channel;


                sftpChannel.cd("/home/axd142130/Desktop/project/");


//                byte[] buffer = new byte[1024];

                File file = new File(getDir("data", MODE_PRIVATE), "test.mp4");

                sftpChannel.get("job.mp4", getFilesDir() + File.separator + "test.mp4");

//                BufferedInputStream bis = new BufferedInputStream(sftpChannel.get("job.mp4"));
////
////                File file = new File(getDir("data", MODE_PRIVATE), "test.mp4");
//
//                    FileOutputStream ous = openFileOutput("test.mp4", Context.MODE_PRIVATE);
//                    BufferedOutputStream bos = new BufferedOutputStream(ous);
//                    int readCount;
//
//                    while ((readCount = bis.read(buffer)) > 0) {
//                        bos.write(buffer, 0, readCount);
//
//                    }
//                    bis.close();
//                    bos.close();
//                    ous.close();
                sftpChannel.exit();
                session.disconnect();
                }
                runOnUiThread(new Runnable() {
                    public void run() {

                        videoView.setVideoPath(getFilesDir() + File.separator + "test.mp4");
                        videoView.requestFocus();
                        videoView.start();
                    }
                });


            } catch (JSchException e) {
                e.printStackTrace();
            } catch (SftpException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            videoView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            super.onPostExecute(result);

        }
    }

}
