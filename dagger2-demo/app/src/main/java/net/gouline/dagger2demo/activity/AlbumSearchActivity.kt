package net.gouline.dagger2demo.activity

import android.app.ProgressDialog
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.activity_album_search.empty_view
import net.gouline.dagger2demo.DemoApplication
import net.gouline.dagger2demo.R
import net.gouline.dagger2demo.model.ITunesResult
import net.gouline.dagger2demo.model.ITunesResultSet
import net.gouline.dagger2demo.rest.ITunesService
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.functions.Action1
import rx.schedulers.Schedulers
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

    // Kotlin Android Extensions seems NOT to work with android-domain id e.g. "@android:id/list"
    //  so need to declare here and initialize later with findViewById() the old fashion way
    var mListView: ListView? = null

    private var mProgressDialog: ProgressDialog? = null

    private var mListAdapter: ArrayAdapter<ITunesResult>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_search)
        mListView = findViewById(android.R.id.list) as ListView

        // Actual injection, now performed via the component
        DemoApplication.from(this).component.inject(this)

        mListAdapter = ArrayAdapter<ITunesResult>(this, android.R.layout.simple_list_item_1)
        mListView?.emptyView = empty_view // id of TextView, using Kotlin Android Extensions
        mListView?.adapter = mListAdapter
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
        return true
    }

    override fun onQueryTextChange(s: String): Boolean {
        return false
    }

    private fun fetchResults(term: String) {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.search_progress))

        // Properly injected Retrofit service
        mITunesService.search(term, "album")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        object : Action1<ITunesResultSet> {
                            override fun call(iTunesResultSet: ITunesResultSet) {
                                mListAdapter!!.addAll(iTunesResultSet.results)
                                mListAdapter!!.notifyDataSetChanged()
                            }
                        },
                        object : Action1<Throwable> {
                            override fun call(throwable: Throwable) {
                                Log.w(TAG, "Failed to retrieve albums", throwable)
                                mProgressDialog!!.dismiss()
                            }
                        },
                        object : Action0 {
                            override fun call() {
                                mProgressDialog!!.dismiss()
                            }
                        }
                )
    }

    companion object {
        private val TAG = AlbumSearchActivity::class.java.simpleName
    }
}
