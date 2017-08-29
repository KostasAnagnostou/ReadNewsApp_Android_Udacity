package com.example.android.readnewsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<News>> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = MainActivity.class.getName();

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int NEWS_LOADER_ID = 1;

    /**
     * URL for News Articles data from the Guardian API
     */
    private static final String GUARDIAN_API_SEARCH = "https://content.guardianapis.com/search?";
    private static final String API_KEY = "test";

    /**
     * Global Variables for Adapter, SearchView, EmptyStateTextView,
     * LoaderManager, searchQuery & ProgressBar
     */
    private NewsAdapter mAdapter;
    private SearchView mSearchView;
    private TextView mEmptyStateTextView;
    private LoaderManager loaderManager;
    private String searchQuery;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = (ListView) findViewById(R.id.list);

        // Create a new adapter that takes an empty list of News Articles as input
        mAdapter = new NewsAdapter(this, new ArrayList<News>());

        //  Find and set Empty View (TextView) in activity_main.xml
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateTextView);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(mAdapter);

        // Prepare the loader
        loaderManager = getLoaderManager();

        // Find the Progress Bar in the activity_main.xml
        loadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);

        // If the user has Internet connection
        if (isConnected()) {
            // Initialize the loader to fetch news articles from sports section
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            // If there is no Internet connection, display error
            // First, hide loading indicator
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet);
        }

        // Find the SearchView with id search_view in activity_main.xml
        mSearchView = (SearchView) findViewById(R.id.search_view);
        // SetOnQueryTextListener to handle the search values
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // Is triggered when the user submits the query
            @Override
            public boolean onQueryTextSubmit(String query) {
                // if the user is connected on the internet
                if (isConnected()) {
                    // Get the value from the SearchView and hide the virtual keyboard
                    searchQuery = mSearchView.getQuery().toString().replace(" ", "");
                    mSearchView.clearFocus();

                    // Show a Toast Message with the query of the user
                    Toast toast = Toast.makeText(getApplicationContext(), "Searching for: " + searchQuery, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();

                    // restart the loader with the new data
                    loaderManager.restartLoader(1, null, MainActivity.this);
                } else {
                    // If there is no Internet, display error
                    // First, hide loading indicator
                    loadingIndicator.setVisibility(View.GONE);

                    // Update empty state with no connection error message
                    mEmptyStateTextView.setText(R.string.no_internet);

                    // Hide the virtual keyboard
                    mSearchView.clearFocus();
                    // Clear out our existing data
                    mAdapter.clear();
                }
                return false;
            }

            // Called when the query text is changed by the user
            @Override
            public boolean onQueryTextChange(String newText) {
                // If the new Text is Empty
                if (newText.equals("")) {
                    // Show a Toast Message to inform the user
                    // that has to type something
                    Toast toast = makeText(getApplicationContext(), "Type something to Search", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
                return false;
            }
        });

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected News.
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current News Article that was clicked on
                News currentNews = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsUri = Uri.parse(currentNews.getUrl());

                // Create a new intent to view the News Article URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
    }

    /*
     * Read the user’s latest preferences for the maximum results and order by settings
     * construct a proper URI with their preference,
     * and then create a new Loader for that URI.
     * Else, returns the Default URI for the Query
   */
    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {

        // Hide Empty View in activity_main.xml when loader starts
        mEmptyStateTextView.setVisibility(View.GONE);

        // Displays the Loading Indicator in activity_main.xml
        loadingIndicator.setVisibility(View.VISIBLE);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String maxNewsFeed = sharedPrefs.getString(
                getString(R.string.settings_max_news_results_key),
                getString(R.string.settings_max_news_results_default));

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        // Constructs the Uri
        Uri baseUri = Uri.parse(GUARDIAN_API_SEARCH);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // If the user submitted a search query
        // Add it to the Uri
        if (searchQuery != null && !searchQuery.isEmpty()) {
            uriBuilder.appendQueryParameter("q", searchQuery);
        }

        // Then add the Default Query Parameters
        uriBuilder.appendQueryParameter("format", "json");
        uriBuilder.appendQueryParameter("section", "sport");
        uriBuilder.appendQueryParameter("show-fields", "trailText");
        uriBuilder.appendQueryParameter("page-size", maxNewsFeed);
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("api-key", API_KEY);

        // Create a new loader for the given URI
        return new NewsLoader(this, uriBuilder.toString());
    }

    // Called when a loader has finished loading data
    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {

        // Clear the adapter of previous news data
        mAdapter.clear();

        // Hide the loading indicator in activity_main.xml
        loadingIndicator.setVisibility(View.GONE);

        // If there is a valid list of {@link News} Articles, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        } else {
            // Update empty state with no results found
            mEmptyStateTextView.setText(R.string.no_results);
        }
    }

    // Called when a previously created loader is being reset
    @Override
    public void onLoaderReset(Loader<List<News>> loader) {

        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();

        // Displays loading indicator in activity_main.xml
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    // This method checks if the user has Internet connection
    private boolean isConnected() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        // boolean to check if there is a network connection
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}