package com.jack.newsobserver.activity;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;

import com.jack.newsobserver.R;
import com.jack.newsobserver.fragments.DrawerExpListFragment;
import com.jack.newsobserver.fragments.RecyclerViewFragment;
import com.jack.newsobserver.fragments.WebViewFragment;
import com.jack.newsobserver.manager.HtmlPageManager;
import com.jack.newsobserver.util.Constants;


public class MainActivity extends ActionBarActivity implements
        DrawerExpListFragment.onSelectedExpListListener,RecyclerViewFragment.onMinimizingFinishedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private static String subTitleString;
    private static String newsListUrl;
    private static long newsLinkId;
    private static final String SUBTITLE_KEY = "SUBTITLE";
    private static final String RECENT_LINK_URL_KEY = "LINKURLKEY";
    private static final String RECENT_LINK_ID_KEY = "LINKIDKEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (null != savedInstanceState){
            subTitleString =savedInstanceState.getString(SUBTITLE_KEY);
            newsListUrl =savedInstanceState.getString(RECENT_LINK_URL_KEY);
            newsLinkId = savedInstanceState.getLong(RECENT_LINK_ID_KEY);
        }else {
            if (null == newsListUrl){
                newsListUrl = Constants.DEFAULT_URL;
                newsLinkId = 1;
            }
        }
        final FragmentManager manager = getFragmentManager();
        if (null == manager.findFragmentByTag(RecyclerViewFragment.TAG)){
            FragmentTransaction transaction = manager.beginTransaction();
            RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
            transaction.add(R.id.list_view_fragment, recyclerViewFragment, RecyclerViewFragment.TAG);
            recyclerViewFragment.setListUrl(newsListUrl, newsLinkId);
            transaction.commit();
        }
        mDrawerLayout=(DrawerLayout) findViewById(R.id.main_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (null == subTitleString){
            subTitleString = Constants.DEFAULT_SUBTITLE;
        }
        getSupportActionBar().setSubtitle(subTitleString);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SUBTITLE_KEY, subTitleString);
        outState.putString(RECENT_LINK_URL_KEY, newsListUrl);
        outState.putLong(RECENT_LINK_ID_KEY, newsLinkId);
    }



    @Override
    public void onBackPressed() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager manager = getFragmentManager();
            if (manager.getBackStackEntryCount()>0){
                WebViewFragment webViewFragment = (WebViewFragment) manager.findFragmentByTag(WebViewFragment.TAG);
                if (null != webViewFragment){
                    if(webViewFragment.canGoPreviousPage()) {
                        webViewFragment.goPreviousPage();
                    }else{
                        getSupportActionBar().setSubtitle(manager.getBackStackEntryAt(
                                manager.getBackStackEntryCount() - 1).getName());
                        HtmlPageManager.clearStoredHtmlPages(this);
                        manager.popBackStack();
                        enableDrawer();
                    }
                }
            }else {
                this.finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        switch (item.getItemId()){
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            case R.id.action_search:
                return true;
            case R.id.action_clear_history:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onExpandableChildItemClick(String title, String url, long id) {
        subTitleString = title;
        newsListUrl = url;
        newsLinkId = id;
        getSupportActionBar().setSubtitle(subTitleString);
        FragmentManager manager = getFragmentManager();
        RecyclerViewFragment recyclerViewFragment = (RecyclerViewFragment) manager
                .findFragmentByTag(RecyclerViewFragment.TAG);
        WebViewFragment webViewFragment = (WebViewFragment) manager
                .findFragmentByTag(WebViewFragment.TAG);
        if (webViewFragment !=null && webViewFragment.isVisible()){
            recyclerViewFragment = new RecyclerViewFragment();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.list_view_fragment, recyclerViewFragment, RecyclerViewFragment.TAG);
            transaction.addToBackStack(subTitleString);
            recyclerViewFragment.setListUrl(newsListUrl, newsLinkId);
            transaction.commit();
        }else {
            recyclerViewFragment.setListUrl(newsListUrl, newsLinkId);
            recyclerViewFragment.onRefresh();
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void minimizingHtmlPageCallback(String htmlPageString, String primaryUrl) {

        disableDrawer();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
        WebViewFragment webViewFragment = (WebViewFragment) manager.findFragmentByTag(WebViewFragment.TAG);
        if (null == webViewFragment  ) {
            webViewFragment = new WebViewFragment();
        }
        transaction.replace(R.id.list_view_fragment, webViewFragment, WebViewFragment.TAG);
        transaction.addToBackStack(subTitleString);
        webViewFragment.setWebViewUrl(htmlPageString, primaryUrl);
        transaction.commit();
    }
    private void enableDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getSupportActionBar().setLogo(R.drawable.ic_actionbar_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void disableDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        getSupportActionBar().setLogo(R.drawable.ic_actionbar_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

}