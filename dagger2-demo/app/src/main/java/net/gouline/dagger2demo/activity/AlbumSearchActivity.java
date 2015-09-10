package net.gouline.dagger2demo.activity;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.gouline.dagger2demo.DemoApplication;
import net.gouline.dagger2demo.R;
import net.gouline.dagger2demo.model.ITunesResult;
import net.gouline.dagger2demo.model.ITunesResultSet;
import net.gouline.dagger2demo.rest.ITunesService;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

/**
 * Activity for search iTunes albums by artist name.
 * <p/>
 * Created by mgouline on 23/04/15.
 */
public class AlbumSearchActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = AlbumSearchActivity.class.getSimpleName();

    @Inject
    ITunesService mITunesService;

    @InjectView(android.R.id.list)
    ListView mListView;
    @InjectView(R.id.empty_view)
    View mEmptyView;

    private ProgressDialog mProgressDialog;

    private ArrayAdapter<ITunesResult> mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_search);
        ButterKnife.inject(this);

        // Actual injection, now performed via the component
        DemoApplication.from(this).getComponent().inject(this);

        mListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mListView.setEmptyView(mEmptyView);
        mListView.setAdapter(mListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_album_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_item_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        fetchResults(s);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    private void fetchResults(String term) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.search_progress));

        // Properly injected Retrofit service
        Call<ITunesResultSet> call = mITunesService.search(term, "album");
        call.enqueue(new Callback<ITunesResultSet>() {
            @Override
            public void onResponse(Response<ITunesResultSet> response) {
                // handle problems per http://inthecheesefactory.com/blog/retrofit-2.0/en
                if (response.errorBody() != null) {
                    try {
                        Log.w(TAG, "Snap! Error in itunes api call response: "
                                + response.errorBody().string());
                    } catch (IOException e) {
                        Log.w(TAG, "Snap! Failed to get error in itunes api call response", e);
                    }
                } else if (response.body() == null) {
                    Log.w(TAG, "Snap! Failed to parse itunes api call response");
                } else {
                    mListAdapter.addAll(response.body().getResults());
                    mListAdapter.notifyDataSetChanged();
                }
                mProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.w(TAG, "Failed to retrieve albums", t);
                mProgressDialog.dismiss();
            }
        });
    }
}
