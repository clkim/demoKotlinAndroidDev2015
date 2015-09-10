package net.gouline.dagger2demo.di.module;

import net.gouline.dagger2demo.DemoApplication;
import net.gouline.dagger2demo.rest.ITunesService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Application-wide dependencies.
 * <p/>
 * Now it only contains the injectable objects and has no direct pairing
 * to the injection targets - that's what components are now for.
 * <p/>
 * Created by mgouline on 23/04/15.
 */
@Module
public class ApplicationModule {
    //private final DemoApplication mApplication; // seems not needed

    private ITunesService mITunesService;

    public ApplicationModule() {
    //public ApplicationModule(DemoApplication application) {
    //    mApplication = application;
        mITunesService = new Retrofit.Builder()
                .baseUrl("https://itunes.apple.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ITunesService.class);
    }

    /* seems not needed
    @Provides
    @Singleton
    Context provideApplicationContext() {
        return mApplication;
    } */

    @Provides
    @Singleton
    ITunesService provideITunesService() {
        return mITunesService;
    }
}
