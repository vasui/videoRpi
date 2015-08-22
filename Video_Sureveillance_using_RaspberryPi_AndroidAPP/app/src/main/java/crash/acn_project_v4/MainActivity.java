package crash.acn_project_v4;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;


public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks {

    private List<AppInfo> videoList = new ArrayList<AppInfo>();

    private ApplicationAdapter mAdapter;
    
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private int PI_NUMBER = 1;
    private boolean firstrun = true;
    private String RPI1_IP = "<ipaddress>";
    private String RPI2_IP = "<ipaddress>";
    private boolean COMMAND_FOR_RPI1 = true;
    private boolean COMMAND_FOR_RPI2 = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);



        // Handle ProgressBar
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);


        mAdapter = new ApplicationAdapter(new ArrayList<AppInfo>(), R.layout.row_application, MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.myAccentColor));
        mSwipeRefreshLayout.setRefreshing(true);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new InitializeApplicationsTask().execute();
            }
        });

        new InitializeApplicationsTask().execute();

        //show progress
        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void animateActivity(AppInfo appInfo) {
        Intent i = new Intent(this, tempactivity.class);
        i.putExtra("videoName", appInfo.getActualFilename());

        ActivityOptionsCompat transitionActivityOptions =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivity(i, transitionActivityOptions.toBundle());
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if(!firstrun) {
            switch (position) {
                case 0:
                    new StartLiveStreamTask().execute();
                    break;
                case 1:
                    PI_NUMBER = 1;
                    COMMAND_FOR_RPI1 = true;
                    new StartStopRecordingTask().execute();
                    break;
                case 2:
                    PI_NUMBER = 1;
                    new InitializeApplicationsTask().execute();
                    break;
                case 3:
                    PI_NUMBER = 1;
                    COMMAND_FOR_RPI1 = false;
                    new StartStopRecordingTask().execute();
                    break;
                case 4:
                    PI_NUMBER = 2;
                    COMMAND_FOR_RPI2 = true;
                    new StartStopRecordingTask().execute();
                    break;
                case 5:
                    PI_NUMBER = 2;
                    new InitializeApplicationsTask().execute();
                    break;
                case 6:
                    PI_NUMBER = 2;
                    COMMAND_FOR_RPI2 = false;
                    new StartStopRecordingTask().execute();
                    break;
                default:
                    PI_NUMBER = 1;
                    break;
            }
        }
        firstrun = false;
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }

    private class InitializeApplicationsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mAdapter.clearApplications();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            videoList.clear();

            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            JSch jsch = new JSch();
            Session session = null;
            try {
                session = jsch.getSession("<username>", "<ipaddress>", 22);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword("<password>");
                session.setConfig("PreferredAuthentications",
                        "password");
                session.connect();

                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftpChannel = (ChannelSftp) channel;

                Vector<ChannelSftp.LsEntry> list = sftpChannel.ls("/root/livestream/r"+PI_NUMBER+"_*");



                for(ChannelSftp.LsEntry entry : list) {
                    if(entry.getFilename().contains(".mp4"))
                        videoList.add(new AppInfo(MainActivity.this, entry.getAttrs(), entry.getFilename()));
                }

                sftpChannel.exit();


                session.disconnect();
            } catch (JSchException e) {
                e.printStackTrace();
            }
            catch (SftpException e) {
                e.printStackTrace();
            }

//            videoList.add(new AppInfo(MainActivity.this, null, "0"));
//            videoList.add(new AppInfo(MainActivity.this, null, "0"));
            Collections.sort(videoList);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //handle visibility
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            //set data for list
            mAdapter.addApplications(videoList);
            mSwipeRefreshLayout.setRefreshing(false);

            super.onPostExecute(result);
        }
    }

    private class StartLiveStreamTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            JSch jsch = new JSch();
            Session session = null;
            try {
                session = jsch.getSession("pi", RPI2_IP, 22);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword("raspberry");
                session.setConfig("PreferredAuthentications",
                        "password");
                session.connect();


                //Code for running commands on PI directly, uncomment user credentials for pi above
                Channel channel =  session.openChannel("exec");

                ((ChannelExec) channel).setCommand("cd Desktop/xyz\npython app.py");

                channel.connect();
                //end Code for running commands on PI directly

                session.disconnect();
                // Navigate anywhere you want, but consider that this classes have only been tested on YouTube's mobile site
                Thread.sleep(2000);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Intent i = new Intent(MainActivity.this, Livestream.class);
                        startActivity(i);
                    }
                });


            } catch (JSchException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
        }
    }

    private class StartStopRecordingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            JSch jsch = new JSch();
            Session session = null;
            try {
                if(PI_NUMBER == 1)
                    session = jsch.getSession("pi", RPI1_IP, 22);
                else
                    session = jsch.getSession("pi", RPI2_IP, 22);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword("raspberry");
                session.setConfig("PreferredAuthentications",
                        "password");
                session.connect();


                //Code for running commands on PI directly, uncomment user credentials for pi above
                Channel channel =  session.openChannel("exec");
                if(PI_NUMBER == 1) {
                    if (COMMAND_FOR_RPI1 == true) {
                        ((ChannelExec) channel).setCommand("cd Desktop/\npython pic.py");
                    } else
                        ((ChannelExec) channel).setCommand("pkill -f pic.py");
                }
                else
                {
                    if (COMMAND_FOR_RPI2 == true) {
                        ((ChannelExec) channel).setCommand("cd Desktop/\npython pic.py");
                    } else
                        ((ChannelExec) channel).setCommand("pkill -f pic.py");
                }
                channel.connect();


                session.disconnect();
                // Navigate anywhere you want, but consider that this classes have only been tested on YouTube's mobile site

            } catch (JSchException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
        }
    }
}
