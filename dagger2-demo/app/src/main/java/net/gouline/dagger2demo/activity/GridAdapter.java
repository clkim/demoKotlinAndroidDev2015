package net.gouline.dagger2demo.activity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.gouline.dagger2demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by clkim on 9/22/15.
 */
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imgThumbnail;
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView)itemView.findViewById(R.id.img_thumbnail);
            textView = (TextView)itemView.findViewById(R.id.tv_species);
        }
    }

    private List<AlbumItem> albumItems;

    public GridAdapter() {
        super();
        albumItems = new ArrayList<AlbumItem>();
    }

    public void addAll(List<AlbumItem> items) {
        albumItems.addAll(items);
    }

    public void addAlbumItem(AlbumItem item) {
        albumItems.add(item);
    }

    @Override
    public GridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(GridAdapter.ViewHolder holder, int position) {
        AlbumItem albumItem = albumItems.get(position);
        holder.textView.setText(albumItem.getName());
        holder.imgThumbnail.setImageBitmap(albumItem.getBitMap());
    }

    @Override
    public int getItemCount() {
        return albumItems.size();
    }
}
