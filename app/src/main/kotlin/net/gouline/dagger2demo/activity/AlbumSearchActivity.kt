package net.gouline.dagger2demo.activity

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * Activity for search iTunes albums by artist name.
 *
 *
 * Created by mgouline on 23/04/15.
 * Converted by clkim to Kotlin from original java,
 * then substantially refactored, e.g. RecyclerView,
 * hiding soft keyboard, hiding prompt in textview when
 * album list is not empty, handling orientation change
 * e.g. caching what has been fetched, using Retrofit 2,
 * RxJava/RxAndroid and life-cycle handling, Picasso...
 *
 * Added basic unit tests for Activity, written in Java
 *   AlbumSearchActivityTest
 * To run it, connect a device via USB to dev machine,
 * then in Android Studio > Run AlbumSearchActivityTest
 *
 * Acknowledgements:
 * Mike Gouline's blog on Dagger 2
 * Sittiphol Phanvilai’s blog on Retrofit 2
 * Dan Lew’s blogs “Grokking RxJava” and on RxAndroid 1.0
 * Edwin Jose and NILANCHALA for RecyclerView examples
 */
class AlbumSearchActivity : AppCompatActivity(),
        SearchView.OnQueryTextListener {

    // itunes api service (Retrofit2, injected in by Dagger2)
    @Inject
    lateinit var mITunesService: ITunesService

    // injected properties using Kotlin Android Extensions
    // from ids in the layout file activity_album_search.xml
    //  recycler_view: RecyclerView
    //  empty_view:    TextView

    // adapter for recycler view
    private var mAlbumViewAdapter: AlbumViewAdapter? = null

    // composite subscription used to un-subscribe rx subscriptions
    private var mCompositeSubscription: CompositeSubscription? = null

    // object with "static" member property used by Log.x
    companion object {
        private val TAG = AlbumSearchActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_search)

        // Actual injection, performed via the component
        DemoApplication.from(this).component.inject(this)

        mAlbumViewAdapter = AlbumViewAdapter()

        // id of RecyclerView in layout, using Kotlin Android Extensions
        this.recycler_view.adapter = mAlbumViewAdapter
        this.recycler_view.layoutManager = LinearLayoutManager(this)

        // Reference - http://blog.danlew.net/2014/10/08/grokking-rxjava-part-4/
        // we follow the pattern in above blog reference, although we have just one subscription in
        //  this demo app and un-subscribing from the subscription directly seemed to work ok too
        mCompositeSubscription = CompositeSubscription()

        // if there is observable cached, use it to display album items from prior api call;
        //  tried checking instead for empty sequence in cached observable using
        //   DemoApplication.albumItemObservableCache.count().toBlocking().single() != 0
        //  but it is too slow on orientation change right after starting a search, since it seems
        //  to block so as to count the items coming into the sequence from the new search; so we
        //  live with the edge case that displays an empty view if cache is present but empty
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
        if (term.length > 0) {
            fetchResults(term)

            // hide soft keyboard
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(this.recycler_view.applicationWindowToken, 0)
        }

        return true
    }

    override fun onQueryTextChange(s: String): Boolean {
        // show prompt-textview if search term is blanked out and no album items are displayed
        if (s.length > 0)
            setPromptVisibility(View.GONE)
        else if (s.length == 0 && mAlbumViewAdapter?.itemCount == 0)
            setPromptVisibility(View.VISIBLE)

        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeSubscription?.unsubscribe()
    }

    private fun fetchResults(term: String) {
        // clear the items in recyclerview adapter
        mAlbumViewAdapter?.clear()
        // cache newly fetched observable
        DemoApplication.albumItemObservableCache =
                // using the injected Retrofit service
                mITunesService.search(term, "album")
                        .flatMap { Observable.from(it.results) }
                        .map { AlbumItem(it.collectionName, it.artworkUrl100) }
                        .subscribeOn(Schedulers.io())
                        .cache()
        displayCachedResults(DemoApplication.albumItemObservableCache)
    }

    private fun displayCachedResults(cache: Observable<AlbumItem>) {
        // subscribe to the observable so as to display the album items
        val subscription: Subscription = cache
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { mAlbumViewAdapter?.addAlbumItem(it)
                          mAlbumViewAdapter?.notifyItemInserted(
                                  mAlbumViewAdapter?.itemCount?.minus(1) ?: 0)
                        },
                        { Log.w(TAG, "Retrieve albums failed\n" + it.message, it) })
        // add the subscription to the CompositeSubscription
        //  so we can do lifecycle un-subscribe
        mCompositeSubscription?.add(subscription)
    }

    private fun setPromptVisibility(visibility: Int) {
        // using id of TextView - Kotlin Android Extensions
        //  this contains the prompt to search for artists' albums
        this.empty_view.visibility = visibility
    }
}
