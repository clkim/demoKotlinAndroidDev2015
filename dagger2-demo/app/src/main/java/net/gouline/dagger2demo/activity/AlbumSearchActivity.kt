package net.gouline.dagger2demo.activity

import android.app.ProgressDialog
import android.app.SearchManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.activity_album_search.empty_view
import kotlinx.android.synthetic.activity_album_search.recycler_view
import net.gouline.dagger2demo.DemoApplication
import net.gouline.dagger2demo.R
import net.gouline.dagger2demo.rest.ITunesService
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.InputStream
import java.net.URL
import javax.inject.Inject

/**
 * Activity for search iTunes albums by artist name.
 *
 *
 * Created by mgouline on 23/04/15.
 */
public class AlbumSearchActivity : ActionBarActivity(), SearchView.OnQueryTextListener {

    @Inject
    lateinit var mITunesService: ITunesService

    private var mProgressDialog: ProgressDialog? = null

    private var mAlbumViewAdapter: AlbumViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_search)

        // Actual injection, now performed via the component
        DemoApplication.from(this).component.inject(this)

        mAlbumViewAdapter = AlbumViewAdapter()

        // id of RecyclerView in layout, using Kotlin Android Extensions
        recycler_view.adapter = mAlbumViewAdapter
        recycler_view.layoutManager = LinearLayoutManager(this)

        // if cached observable, use it to display album items from prior api call
        // tried checking for empty sequence in cached observable using
        //   DemoApplication.albumItemObservableCache.count().toBlocking().single() != 0
        //  but it is too slow on orientation change right after starting a search, since it seems
        //  to block so as to count the items coming into the sequence from the new search; so we
        //  accept the edge case that displays an empty view if cache is present but empty
        if (DemoApplication.albumItemObservableCache != null) {
            displayCachedResults(DemoApplication.albumItemObservableCache)
            // hide prompt-textview
            setPromptVisibility(View.GONE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_album_search, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.menu_item_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(this)

        return true
    }

    override fun onQueryTextSubmit(term: String): Boolean {
        if (term.length() > 0) {
            // clear the cached observable from last api call
            DemoApplication.albumItemObservableCache = null
            // clear the items in recyclerview adapter
            mAlbumViewAdapter?.clear()

            fetchResults(term)

            // hide soft keyboard
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(recycler_view.applicationWindowToken, 0)
        }

        return true
    }

    override fun onQueryTextChange(s: String): Boolean {
        // show prompt-textview if search term is blanked out and no album items are displayed
        if (s.length() > 0)
            setPromptVisibility(View.GONE)
        else if (s.length() == 0 && mAlbumViewAdapter?.itemCount == 0)
            setPromptVisibility(View.VISIBLE)

        return false
    }

    private fun fetchResults(term: String) {
//        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
//            mProgressDialog!!.dismiss()
//        }
//        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.search_progress))

        if (DemoApplication.albumItemObservableCache == null) {
            DemoApplication.albumItemObservableCache =
                    // Properly injected Retrofit service
                    mITunesService.search(term, "album")
                            .flatMap({ iTunesResultSet -> Observable.from(iTunesResultSet.results) })
                            .map({ iTunesResult ->
                                val url: URL = URL(iTunesResult.artworkUrl100)
                                val instream: InputStream = url.openConnection().inputStream
                                val bitmap: Bitmap = BitmapFactory.decodeStream(instream)
                                AlbumItem(bitmap, iTunesResult.collectionName)
                            })
                            .subscribeOn(Schedulers.newThread())
                            .cache()
        }
//        mProgressDialog!!.dismiss()
        displayCachedResults(DemoApplication.albumItemObservableCache)
    }

    private fun displayCachedResults(cache: Observable<AlbumItem>) {
        cache.observeOn(AndroidSchedulers.mainThread())
             .subscribe(
                     { albumItem ->
                         mAlbumViewAdapter!!.addAlbumItem(albumItem)
                         mAlbumViewAdapter!!.notifyDataSetChanged()
                     },
                     { throwable -> Log.w(TAG, "Failed to retrieve albums", throwable) }
             )
    }

    companion object {
        private val TAG = AlbumSearchActivity::class.java.simpleName
    }

    private fun setPromptVisibility(visibility: Int) {
        // using id of TextView - Kotlin Android Extensions
        //  this contains the prompt to search for artists' albums
        empty_view.visibility = visibility
    }
}
