package crash.acn_project_v4;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DetailActivity extends ActionBarActivity {

    private VideoView videoView;
    private MediaController mediaCtrl;
    private ProgressBar mProgressBar;

    private FileOutputStream fos=null;


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

                session = jsch.getSession("<username>", "<ipaddress>", 22);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword("<password>");
                session.setConfig("PreferredAuthentications",
                        "password");
                session.connect();

                String command = "scp -f /home/<username>/Desktop/project/job.mp4";
                Channel channel = session.openChannel("exec");
                ((ChannelExec)channel).setCommand(command);

                OutputStream out = channel.getOutputStream();
                InputStream in = channel.getInputStream();

                channel.connect();

                byte[] buf = new byte[4096];


                    buf[0]=0; out.write(buf, 0, 1); out.flush();

                    while(true) {
                        int c = checkAck(in);
                        if (c != 'C') {
                            break;
                        }

                        // read '0644 '
                        in.read(buf, 0, 5);

                        long filesize = 0L;
                        while (true) {
                            if (in.read(buf, 0, 1) < 0) {
                                // error
                                break;
                            }
                            if (buf[0] == ' ') break;
                            filesize = filesize * 10L + (long) (buf[0] - '0');
                        }

                        String file=null;
                        for(int i=0;;i++){
                            in.read(buf, i, 1);
                            if(buf[i]==(byte)0x0a){
                                file=new String(buf, 0, i);
                                break;
                            }
                        }

                        // send '\0'
                        buf[0]=0; out.write(buf, 0, 1); out.flush();

                        // read a content of lfile
                        fos=new FileOutputStream(getFilesDir() + File.separator + "test.mp4");

                        int foo;
                        while(true){
                            if(buf.length<filesize) foo=buf.length;
                            else foo=(int)filesize;
                            foo=in.read(buf, 0, foo);
                            if(foo<0){
                                // error
                                break;
                            }
                            fos.write(buf, 0, foo);
                            filesize-=foo;
                            if(filesize==0L) break;
                        }
                        fos.close();
                        fos=null;

                        if(checkAck(in)!=0){
                            return null;
                        }

                        // send '\0'
                        buf[0]=0; out.write(buf, 0, 1); out.flush();
                    }

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
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        int checkAck(InputStream in) throws IOException{
            int b=in.read();
            // b may be 0 for success,
            //          1 for error,
            //          2 for fatal error,
            //          -1
            if(b==0) return b;
            if(b==-1) return b;

            if(b==1 || b==2){
                StringBuffer sb=new StringBuffer();
                int c;
                do {
                    c=in.read();
                    sb.append((char)c);
                }
                while(c!='\n');
                if(b==1){ // error
                    Log.d("SCP Error:", sb.toString());
                }
                if(b==2){ // fatal error
                    Log.d("Fatal SCP Error:", sb.toString());
                }
            }
            return b;
        }

        @Override
        protected void onPostExecute(Void result) {

            videoView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            super.onPostExecute(result);

        }
    }

}
