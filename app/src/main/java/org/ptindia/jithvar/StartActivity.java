package org.ptindia.jithvar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.ptindia.jithvar.database.DataBaseApp;
import org.ptindia.jithvar.handler.DataBaseHandler;

import java.sql.SQLException;

/**
 * Created by Arvindo Mondal on 21/6/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */

public class StartActivity extends Activity {

    private DataBaseApp db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        db = new DataBaseApp(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            if(db.isTrackingTbEmpty()){
                db.insertTrackingED_TB(new DataBaseHandler("false"));
            }
            if(db.registrationOk()){
                Intent i = new Intent(this, HomeActivity.class);
//                i.putExtra("userID", db.getTrueUserId());
                startActivity(i);
                finish();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.register:
                startActivity(new Intent(this, RegistrationActivity.class));
                finish();
                break;

            case R.id.login:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }
    }
}
