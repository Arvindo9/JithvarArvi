package org.ptindia.jithvar;

import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by Arvindo on 23-02-2017.
 * Company KinG
 * email at support@towardtheinfinity.com
 */

public class DrawerActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout fullLayout;
//    private FrameLayout frame_layout;
    private Toolbar toolbar;


    @Override
    public void setContentView(@LayoutRes int layoutResID) {
//        fullLayout = (DrawerLayout)getLayoutInflater().inflate(R.layout.drawer_activity,null);
//        frame_layout = (FrameLayout)fullLayout.findViewById(R.id.conetnt_frame);
//        getLayoutInflater().inflate(layoutResID,frame_layout,true);
        super.setContentView(R.layout.drawer_activity);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction().replace(R.id.container,new ProfileFragment()).commit();

        final DrawerLayout drawer= (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setCheckable(true);
        int id = item.getItemId();
        switch (id){
            case R.id.profile:
                getFragmentManager().beginTransaction().replace(R.id.container,
                        new ProfileFragment()).addToBackStack(null).commit();
                break;

            case R.id.attendance:
//                getFragmentManager().beginTransaction().replace(R.id.container,
//                        new AttendanceFragment()).addToBackStack(null).commit();
                break;

            case R.id.update:
//                getFragmentManager().beginTransaction().replace(R.id.container,
//                        new UpdateFragment()).addToBackStack(null).commit();
                break;

            case R.id.track:
//                getFragmentManager().beginTransaction().replace(R.id.container,
//                        new SendMsgFragment()).addToBackStack(null).commit();
                break;

            case R.id.inbox:
//                getFragmentManager().beginTransaction().replace(R.id.container,
//                        new InboxFragment()).addToBackStack(null).commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
