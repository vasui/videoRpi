package crash.acn_project_v4;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<AppInfo> videos;
    private int rowLayout;
    private MainActivity mAct;

    public ApplicationAdapter(List<AppInfo> videos, int rowLayout, MainActivity act) {
        this.videos = videos;
        this.rowLayout = rowLayout;
        this.mAct = act;
    }

    @Override
    public int getItemCount() {
        return videos == null ? 0 : videos.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final AppInfo appInfo = videos.get(i);
        viewHolder.name.setText(appInfo.getName());
        viewHolder.size.setText(appInfo.getSize());
        //viewHolder.image.setImageDrawable(appInfo.getIcon());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAct.animateActivity(appInfo);
            }
        });
    }
    public void clearApplications() {
        int size = this.videos.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                videos.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public void addApplications(List<AppInfo> videos) {
        this.videos.addAll(videos);
        this.notifyItemRangeInserted(0, videos.size() - 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }





    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView size;
        //public ImageView image;


        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.videoName);
            size = (TextView) itemView.findViewById(R.id.videoSize);
            //image = (ImageView) itemView.findViewById(R.id.countryImage);
        }

    }
}
