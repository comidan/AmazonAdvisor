package com.dev.amazonadvisor;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.authorization.User;
import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.orm.SugarContext;

public class MainActivity extends AppCompatActivity {

    private static RequestContext requestContext;
    private static boolean loggedInState = false;
    private static final long LOGOUT_ID = 333;

    private ImageView amazonLoginButton;
    private AccountHeader headerResult;
    private ViewPager viewPager;
    private SmartTabLayout viewPagerTab;
    private Drawer navigationDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        ViewGroup tab = (ViewGroup) findViewById(R.id.tab);
        tab.addView(LayoutInflater.from(this).inflate(R.layout.tab_indicator, tab, false));
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_item_home);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.drawer_item_settings);
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();
        navigationDrawer = new DrawerBuilder()
                        .withActivity(this)
                        .withToolbar(toolbar)
                        .withAccountHeader(headerResult)
                        .addDrawerItems(item1, new DividerDrawerItem(), item2)
                        .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                return false;
                            }
                        })
                        .build();
        SugarContext.init(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        navigationDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        if(loggedInState)
            authorizeInAppFeatures();
        else
        {
            final RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);
            amazonLoginButton = (ImageView) findViewById(R.id.login_with_amazon);
            requestContext = RequestContext.create(this);
            requestContext.registerListener(new AuthorizeListener() {

                @Override
                public void onSuccess(AuthorizeResult authorizeResult) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loggedInState = true;
                            Snackbar.make(mainLayout,
                                    "Logged in successful",
                                    Snackbar.LENGTH_LONG).show();
                        }
                    });
                    fetchUserProfile();
                }

                @Override
                public void onError(AuthError authError) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(mainLayout,
                                          "Error during authorization.  Please try again.",
                                          Snackbar.LENGTH_LONG).show();
                            updateAccountHeaderOnLogout();
                            loggedInState = false;
                        }
                    });
                }

                @Override
                public void onCancel(AuthCancellation authCancellation) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(mainLayout,
                                    "Authorization cancelled",
                                    Snackbar.LENGTH_LONG).show();
                            updateAccountHeaderOnLogout();
                            loggedInState = false;
                        }
                    });
                }
            });
            amazonLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AuthorizationManager.authorize(
                            new AuthorizeRequest.Builder(requestContext)
                                    .addScopes(ProfileScope.profile(), ProfileScope.postalCode())
                                    .build()
                    );
                }
            });
        }
    }

    void updateAccountHeaderOnLogin(String name, String email)
    {
        if(navigationDrawer.getDrawerItem(LOGOUT_ID) == null) {
            headerResult.addProfile(new ProfileDrawerItem()
                    .withName(name)
                    .withEmail(email)
                    .withIcon(getResources().getDrawable(R.drawable.profile)), 0);
            final SecondaryDrawerItem logout = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.drawer_item_logout);
            logout.withIdentifier(LOGOUT_ID);
            navigationDrawer.addItem(logout);
            navigationDrawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    if (drawerItem.getIdentifier() == LOGOUT_ID) {
                        AuthorizationManager.signOut(getApplicationContext(), new Listener<Void, AuthError>() {
                            @Override
                            public void onSuccess(Void response) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateAccountHeaderOnLogout();
                                        resetDrawer();
                                        cancelAuthorizeInAppFeatures();
                                        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);
                                        Snackbar.make(mainLayout,
                                                "Logged in successful",
                                                Snackbar.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(AuthError authError) {

                            }
                        });
                    }
                    return false;
                }
            });
        }
    }

    void updateAccountHeaderOnLogout()
    {
        headerResult.removeProfile(0);  //should not delete profile?
    }

    void resetDrawer()
    {
        navigationDrawer.removeItem(LOGOUT_ID);
    }

    private void authorizeInAppFeatures()
    {
        amazonLoginButton.setVisibility(View.GONE);
        FragmentPagerItems pages = new FragmentPagerItems(this);
        pages.add(FragmentPagerItem.of(getString(R.string.following), FollowingFragment.class));
        pages.add(FragmentPagerItem.of(getString(R.string.lists), GenericFragment.class));
        pages.add(FragmentPagerItem.of(getString(R.string.advices), GenericFragment.class));
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(getSupportFragmentManager(), pages);
        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);
    }

    private void cancelAuthorizeInAppFeatures()
    {
        amazonLoginButton.setVisibility(View.VISIBLE);
        FragmentPagerItems pages = new FragmentPagerItems(this);
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(getSupportFragmentManager(), pages);
        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);
    }

    private void fetchUserProfile() {
        User.fetch(this, new Listener<User, AuthError>() {

            @Override
            public void onSuccess(User user) {
                final String name = user.getUserName();
                final String email = user.getUserEmail();
                final String account = user.getUserId();
                final String zipCode = user.getUserPostalCode();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateAccountHeaderOnLogin(name, email);
                        getSharedPreferences("ACCOUNT_INFO", MODE_PRIVATE).edit().putString("EMAIL", email).apply();
                        authorizeInAppFeatures();
                    }
                });
            }

            @Override
            public void onError(AuthError ae) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_relative_layout);
                        Snackbar.make(mainLayout,
                                "Error retrieving profile information.\nPlease log in again",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestContext.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Scope[] scopes = {ProfileScope.profile(), ProfileScope.postalCode()};
        AuthorizationManager.getToken(this, scopes, new Listener<AuthorizeResult, AuthError>() {
            @Override
            public void onSuccess(AuthorizeResult result) {
                if (result.getAccessToken() != null) {
                    loggedInState = true;
                    fetchUserProfile();
                }
                else
                {
                    loggedInState = false;
                }
            }

            @Override
            public void onError(AuthError ae) {
                loggedInState = false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SugarContext.terminate();
    }
}
