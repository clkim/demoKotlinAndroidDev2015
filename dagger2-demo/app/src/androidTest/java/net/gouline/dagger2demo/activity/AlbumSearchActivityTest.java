package net.gouline.dagger2demo.activity;

import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import net.gouline.dagger2demo.R;
import net.gouline.dagger2demo.rest.ITunesService;

/**
 * Mostly following:
 *  http://developer.android.com/training/activity-testing/preparing-activity-testing.html
 *  http://developer.android.com/training/activity-testing/activity-basic-testing.html
 *
 * Created by clkim on 10/19/15
 */
public class AlbumSearchActivityTest extends ActivityInstrumentationTestCase2<AlbumSearchActivity> {
    private AlbumSearchActivity albumSearchActivity;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private ITunesService mITunesService;

    public AlbumSearchActivityTest() {
        super(AlbumSearchActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        albumSearchActivity = getActivity();
        recyclerView = (RecyclerView) albumSearchActivity.findViewById(R.id.recycler_view);
        emptyView = (TextView) albumSearchActivity.findViewById(R.id.empty_view);

        // accessing a Kotlin var in Activity class by the property name
        //  IDE also offers getter getmITunesService()
        mITunesService = albumSearchActivity.mITunesService;
    }

    public void testPreConditions() {
        assertNotNull(albumSearchActivity);
        assertNotNull(recyclerView);
        assertNotNull(emptyView);
    }

    public void testITuneServiceIsInjected() {
        // test for successful Dagger 2 injection in the Activity class
        assertNotNull(mITunesService);
    }

    public void testEmptyTextView_labelText() {
        String expected = albumSearchActivity.getString(R.string.search_empty);
        String actual = emptyView.getText().toString();
        assertEquals(expected, actual);
    }
}
