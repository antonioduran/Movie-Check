package com.tassioauad.moviecheck.view.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tassioauad.moviecheck.MovieCheckApplication;
import com.tassioauad.moviecheck.R;
import com.tassioauad.moviecheck.dagger.DiscoveryViewModule;
import com.tassioauad.moviecheck.model.entity.Movie;
import com.tassioauad.moviecheck.presenter.DiscoveryPresenter;
import com.tassioauad.moviecheck.view.DiscoveryView;
import com.tassioauad.moviecheck.view.fragment.CastCrewFragment;
import com.tassioauad.moviecheck.view.fragment.ListMovieMediaFragment;
import com.tassioauad.moviecheck.view.fragment.MovieDetailFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiscoveryActivity extends AppCompatActivity implements DiscoveryView {

    private static final String KEY_MOVIELIST = "MOVIELIST";
    private static final String KEY_INDEX = "INDEX";
    private static final String KEY_PAGE = "PAGE";
    private List<Movie> movieList;
    private int page = 1;
    private int index = 0;

    @Inject
    DiscoveryPresenter presenter;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.progressbar)
    ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MovieCheckApplication) getApplication()).getObjectGraph().plus(new DiscoveryViewModule(this)).inject(this);
        setContentView(R.layout.activity_discovery);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(getString(R.string.discoveryactivity_title));

        if(savedInstanceState == null) {
            presenter.loadAllMoviesFromPage(page);
        } else {
            movieList = savedInstanceState.getParcelableArrayList(KEY_MOVIELIST);
            index = savedInstanceState.getInt(KEY_INDEX);
            page = savedInstanceState.getInt(KEY_PAGE);
            presenter.init(movieList, page, index);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(movieList != null) {
            outState.putParcelableArrayList(KEY_MOVIELIST, new ArrayList<>(movieList));
        }
        outState.putInt(KEY_INDEX, index);
        outState.putInt(KEY_PAGE, page);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.discovery, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.foward:
                presenter.loadMovie(++index);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        presenter.stop();
        super.onStop();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, DiscoveryActivity.class);
    }

    @Override
    public void showMovie(Movie movie, int index) {
        this.index = index;
        getSupportActionBar().setSubtitle(movie.getTitle());
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout_detail, MovieDetailFragment.newInstance(movie)).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout_castcrew, CastCrewFragment.newInstance(movie)).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout_media, ListMovieMediaFragment.newInstance(movie)).commit();
    }

    @Override
    public void movieLoaded(List<Movie> movieList, int page) {
        this.movieList = movieList;
        this.page = page;
        index = 0;
        presenter.loadMovie(index);
    }

    @Override
    public void warnWasNotPossibleToLoadMoreMovies() {
        Toast.makeText(this, R.string.discoveryactivity_tryagain, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void warnFailedToLoadMoreMovies() {
        Toast.makeText(this, R.string.discoveryactivity_tryagain, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
        progressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressbar.setVisibility(View.GONE);
    }
}