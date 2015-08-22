package crash.acn_project_v4;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jcraft.jsch.SftpATTRS;

public class AppInfo implements Comparable<Object> {

    private Context ctx;

    private String name;
    private String size;
    private Drawable icon;



    private String actualFilename;

    public AppInfo(Context ctx, SftpATTRS sftpattrs,String actualFilename) {
        this.ctx = ctx;
        this.name = sftpattrs.getMtimeString();
        this.actualFilename = actualFilename;
        this.size = String.valueOf(sftpattrs.getSize() / (1024*1024)) + " MB";
//        this.name = "Sat Apr 04 16:16:22 CDT 2015";
//        this.actualFilename = actualFilename;
//        this.size = String.valueOf( 5522118 / (1024*1024)) + " MB";
        this.icon = ctx.getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp);
    }

    public String getName() {
        return this.name;
    }

    public String getSize() {
        return this.size;
    }

    public String getActualFilename() {
        return actualFilename;
    }

    public Drawable getIcon() {
        return this.icon;
    }
    @Override
    public int compareTo(Object o) {
        AppInfo f = (AppInfo) o;
        return getName().compareTo(f.getName());
    }

    @Override
    public String toString() {
        return getName();
    }
}
