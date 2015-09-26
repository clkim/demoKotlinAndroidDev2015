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

    private var mGridAdapter: GridAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_search)

        // Actual injection, now performed via the component
        DemoApplication.from(this).component.inject(this)

        mGridAdapter = GridAdapter()

        // id of RecyclerView in layout, using Kotlin Android Extensions
        recycler_view.adapter = mGridAdapter
        recycler_view.layoutManager = LinearLayoutManager(this)

        if (DemoApplication.albumItemObservableCache != null)
            fetchResults("")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_album_search, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.menu_item_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(this)

        return true
    }

    override fun onQueryTextSubmit(s: String): Boolean {
        fetchResults(s)

        // hide soft keyboard
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(recycler_view.applicationWindowToken, 0)
        return true
    }

    override fun onQueryTextChange(s: String): Boolean {
        // hide textview displaying the prompt // TODO put into a method to be more DRY
        if (s.length() > 0 || mGridAdapter!!.itemCount > 0)
            empty_view.visibility = View.GONE   // id of TextView - Kotlin Android Extensions
        else
            empty_view.visibility = View.VISIBLE
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
        DemoApplication.albumItemObservableCache
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { albumItem ->
                            mGridAdapter!!.addAlbumItem(albumItem)
                            mGridAdapter!!.notifyDataSetChanged() },
                        { throwable -> Log.w(TAG, "Failed to retrieve albums", throwable) },
                        { }
//                        { ->  mProgressDialog!!.dismiss() }
                )
    }

    companion object {
        private val TAG = AlbumSearchActivity::class.java.simpleName
    }
}
