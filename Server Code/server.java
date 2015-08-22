import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class server {

    public final static int SOCKET_PORT = 5701;  // you may change this
    public static String FILE_TO_SEND = "job.mp4";  // you may change this

    public static void main (String [] args ) throws IOException {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        InputStream is = null;
        ServerSocket servsock = null;
        Socket sock = null;
        try {
            servsock = new ServerSocket(SOCKET_PORT);
            while (true) {
                System.out.println("Waiting...");
                try {
                    sock = servsock.accept();
                    System.out.println("Accepted connection : " + sock);

                    is = sock.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuffer sb = new StringBuffer();

                    // while (br.ready()) {
                    //     FILE_TO_SEND = br.readLine();
                    // }

                    while (true) {
                        String temp = br.readLine();
                        if (temp != null)
                        {
                            FILE_TO_SEND = temp;
                            break;
                        }
                    }

                    System.out.println("Server got String " + FILE_TO_SEND);
                    // send file
                    File myFile = new File (FILE_TO_SEND);
                    byte [] bufferSize  = new byte [sock.getSendBufferSize()];
                    fis = new FileInputStream(myFile);
                    bis = new BufferedInputStream(fis);
                    os = sock.getOutputStream();
                    System.out.println("Sending " + FILE_TO_SEND + "(" + bufferSize.length + " bytes)");
                    int count = 0;

                    while((count = bis.read(bufferSize)) > 0)
                        os.write(bufferSize,0,bufferSize.length);
                    os.flush();
                    br.close();
                    isr.close();
                    System.out.println("Done.");
                }
                finally {
                    if (bis != null) bis.close();
                    if (os != null) os.close();
                    if (is != null) is.close();
                    if (sock!=null) sock.close();
                }
            }
        }
        finally {
            if (servsock != null) servsock.close();
        }
    }
}
