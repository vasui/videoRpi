package crash.acn_project_v4.Dump;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.util.Vector;

/**
 * Created by harshadeep on 4/5/15.
 */
public class RetrieveTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession("axd142130", "10.176.67.111", 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword("Aashish123$");
            session.setConfig("PreferredAuthentications",
                    "password");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
//            sftpChannel.get("remotefile.txt", "localfile.txt");

            Vector<ChannelSftp.LsEntry> list = sftpChannel.ls("/home/axd142130/Desktop/project/");
            String file = "";
            for(ChannelSftp.LsEntry entry : list) {
                file = entry.getFilename();

                SftpATTRS temp = entry.getAttrs();

                if(file.contains(".mp4"))
                    Log.d("file: ", String.valueOf(temp.getSize()));
            }

            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }

        return "success";
    }

    protected void onPostExecute(String feed) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }

}
