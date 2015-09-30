package net.gouline.dagger2demo.activity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.gouline.dagger2demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by clkim on 9/22/15
 */
public class AlbumViewAdapter extends RecyclerView.Adapter<AlbumViewAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imgThumbnail;
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView)itemView.findViewById(R.id.thumbnail);
            textView = (TextView)itemView.findViewById(R.id.title);
        }
    }

    private List<AlbumItem> albumItems;

    public AlbumViewAdapter() {
        super();
        albumItems = new ArrayList<>();
    }

    public void clear() {
        albumItems.clear();
    }

    public void addAlbumItem(AlbumItem item) {
        albumItems.add(item);
    }

    @Override
    public AlbumViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AlbumViewAdapter.ViewHolder holder, int position) {
        AlbumItem albumItem = albumItems.get(position);
        holder.textView.setText(albumItem.getName());
        Picasso.with(holder.imgThumbnail.getContext())
                .load(albumItem.getUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgThumbnail);
    }

    @Override
    public int getItemCount() {
        return albumItems.size();
    }
}
