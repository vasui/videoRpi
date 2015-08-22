package crash.acn_project_v4;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

public class tempactivity extends ActionBarActivity {

    private VideoView videoView;
    private MediaController mediaCtrl;


    public final static int SOCKET_PORT = 5701;      // you may change this
    public final static String SERVER = "<ipaddress>";  // localhost
    public String FILE_TO_RECEIVE = "job.mp4\n";  // you may change this, I give a


//    private MjpegView mv;

//    ServerSocket serverSocket;
    private FileOutputStream fos=null;
    private BufferedOutputStream bos = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tempactivity);

        Intent intent = getIntent();
        //TO:DO change this later to get the raspbeery pi selected, will be filtered on the MainActivity itself
        FILE_TO_RECEIVE = intent.getStringExtra("videoName") + "\n";

        videoView = (VideoView) findViewById(R.id.videoa);
        mediaCtrl = new MediaController(this);
        mediaCtrl.setMediaPlayer(videoView);
        videoView.setMediaController(mediaCtrl);

        Thread socketClientThread = new Thread(new socketClientThread());
        socketClientThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private class socketClientThread extends Thread {

        int count = 0;

        @Override
        public void run() {
            try {
                    Socket socket = new Socket(SERVER, SOCKET_PORT);

                    SocketClientReplyThread socketClientReplyThread  = new SocketClientReplyThread (
                            socket);
                    socketClientReplyThread.run();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private class SocketClientReplyThread extends Thread {

        private Socket hostThreadSocket;


        SocketClientReplyThread (Socket socket) {
            hostThreadSocket = socket;

        }

        @Override
        public void run() {
            InputStream inputStream;
            OutputStream outputStream;

            try {
                inputStream = hostThreadSocket.getInputStream();
                outputStream = hostThreadSocket.getOutputStream();
                int bufferSize = hostThreadSocket.getReceiveBufferSize();

                OutputStreamWriter osw = new OutputStreamWriter(outputStream);
                BufferedWriter bw = new BufferedWriter(osw);

                bw.write(FILE_TO_RECEIVE);
                bw.flush();

                Log.d("CLient -- ", "Message sent about File to Receieve " + FILE_TO_RECEIVE);
                byte[] bytes = new byte[bufferSize];

                fos=new FileOutputStream(getFilesDir() + File.separator + "test.mp4");
                bos = new BufferedOutputStream(fos);
                Log.d("CLient -- ", "BOS and FOS created");

                int count;
                while ((count = inputStream.read(bytes)) > 0) {
                    bos.write(bytes, 0, count);
                    //Log.d("CLient -- ", count + " bytes read");
                }

                Log.d("CLient -- ", "File read Finished");

                osw.close();
                bw.close();
                bos.flush();
                bos.close();
                inputStream.close();
                hostThreadSocket.close();
                Log.d("CLient -- ", "Close all sockets");
                runOnUiThread(new Runnable() {
                    public void run() {

                        videoView.setVideoPath(getFilesDir() + File.separator + "test.mp4");
                        videoView.requestFocus();
                        videoView.start();

                    }
                });

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }

        }

    }

    public void onPause() {
        super.onPause();

    }

}
