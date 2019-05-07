package com.liyu.fakeweather.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.liyu.fakeweather.R;
import com.liyu.fakeweather.event.ModuleChangedEvent;
import com.liyu.fakeweather.event.ThemeChangedEvent;
import com.liyu.fakeweather.model.Module;
import com.liyu.fakeweather.ui.base.BaseActivity;
import com.liyu.fakeweather.ui.weather.WeatherFragment;
import com.liyu.fakeweather.utils.DoubleClickExit;
import com.liyu.fakeweather.utils.RxDrawer;
import com.liyu.fakeweather.utils.SimpleSubscriber;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends BaseActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;
    private String currentFragmentTag;

    private static final String FRAGMENT_TAG_WEATHER = "天气";
    private static final String FRAGMENT_TAG_EMPTY = "四大皆空";

    private static final String CURRENT_INDEX = "currentIndex";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getMenuId() {
        return 0;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_INDEX);
        }
        fragmentManager = getSupportFragmentManager();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        initNavigationViewHeader();
        initFragment();
    }

    private void initFragment() {
        DataSupport.order("index").findAsync(Module.class).listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                List<Module> modules = (List<Module>) t;
                List<Module> enabledModule = new ArrayList<>();
                if (t == null || t.size() == 0) {
                    modules.add(new Module("天气", R.drawable.ic_weather, R.id.navigation_item_2, 0, true));
                    DataSupport.saveAll(modules);
                }
                for (Module module : modules) {
                    if (module.isEnable()) {
                        enabledModule.add(module);
                        navigationView.getMenu().add(R.id.module_group, getMenuId(module.getName()), module.getIndex(), module.getName()).setIcon(getDrawbleId(module.getName())).setCheckable(true);
                    }
                }
                if (enabledModule.size() > 0) {
                    navigationView.getMenu().getItem(0).setChecked(true);
                    switchContent(enabledModule.get(0).getName());
                } else {
                    switchContent(FRAGMENT_TAG_EMPTY);
                }

            }
        });

    }

    private int getDrawbleId(String name) {
        switch (name) {
            case "天气":
                return getResId(this, "ic_weather", "drawable");
            default:
                return getResId(this, "ic_weather", "drawable");
        }
    }

    private int getMenuId(String name) {
        switch (name) {
            case "天气":
                return getResId(this, "navigation_item_2", "id");
            default:
                return getResId(this, "navigation_item_2", "id");
        }
    }

    private int getResId(Context context, String resName, String defType) {
        return context.getResources().getIdentifier(resName, defType, context.getPackageName());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_INDEX, currentFragmentTag);
    }

    @Override
    protected void loadData() {
    }

    public void initDrawer(Toolbar toolbar) {
        if (toolbar != null) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                }
            };
            mDrawerToggle.syncState();
            mDrawerLayout.addDrawerListener(mDrawerToggle);
        }
    }

    private void initNavigationViewHeader() {
        navigationView = findViewById(R.id.navigation);
        navigationView.inflateHeaderView(R.layout.drawer_header);
        navigationView.setNavigationItemSelectedListener(new NavigationItemSelected());
    }

    class NavigationItemSelected implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(final MenuItem menuItem) {
            RxDrawer.close(mDrawerLayout).observeOn(AndroidSchedulers.mainThread()).subscribe(
                    new SimpleSubscriber<Void>() {
                        @Override
                        public void onNext(Void aVoid) {
                            switch (menuItem.getItemId()) {
                                case R.id.navigation_item_2:
                                    menuItem.setChecked(true);
                                    switchContent(FRAGMENT_TAG_WEATHER);
                                    break;
                            }
                        }
                    });
            return false;
        }
    }

    public void switchContent(String name) {
        if (currentFragmentTag != null && currentFragmentTag.equals(name))
            return;

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        Fragment currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag);
        if (currentFragment != null) {
            ft.hide(currentFragment);
        }

        Fragment foundFragment = fragmentManager.findFragmentByTag(name);

        if (foundFragment == null) {
            switch (name) {
                case FRAGMENT_TAG_WEATHER:
                    foundFragment = new WeatherFragment();
                    break;

            }
        }

        if (foundFragment == null) {

        } else if (foundFragment.isAdded()) {
            ft.show(foundFragment);
        } else {
            ft.add(R.id.contentLayout, foundFragment, name);
        }
        ft.commit();
        currentFragmentTag = name;
        invalidateOptionsMenu();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onThemeChanged(ThemeChangedEvent event) {
        this.recreate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onModuleChanged(ModuleChangedEvent event) {
        this.recreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (!DoubleClickExit.check()) {
                Snackbar.make(MainActivity.this.getWindow().getDecorView().findViewById(android.R.id.content), "再按一次退出 App!", Snackbar.LENGTH_SHORT).show();
            } else {
                super.onBackPressed();
                System.exit(0);
            }
        }
    }
}
