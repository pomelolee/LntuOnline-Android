package com.lntu.online.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lntu.online.R;
import com.lntu.online.adapter.CurriculumAdapter;
import com.lntu.online.http.HttpUtil;
import com.lntu.online.http.NormalAuthListener;
import com.lntu.online.http.RetryAuthListener;
import com.lntu.online.info.NetworkConfig;
import com.lntu.online.info.SecretKey;
import com.lntu.online.info.UserInfo;
import com.lntu.online.model.Curriculum;
import com.takwolf.util.crypto.DES3;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnPageChange;

public class CurriculumActivity extends ActionBarActivity {

    @InjectView(R.id.toolbar)
    protected Toolbar toolbar;

    @InjectView(R.id.curriculum_vp_root)
    protected ViewPager viewPager;

    private Time time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);
        ButterKnife.inject(this);

        //定义时间控件
        time = new Time();
        time.setToNow();
        String strTime = time.year + "-" + (time.month + 1) + "-" + time.monthDay + " （" + CurriculumAdapter.weekdayNames[time.weekDay] + "）";

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(strTime);

        //读取本地课表
        SharedPreferences sp = getSharedPreferences("curriculum_" + UserInfo.getSavedUserId(), Context.MODE_PRIVATE);
        try {
            Curriculum cc = Curriculum.dao.fromJson(DES3.decrypt(SecretKey.SP_KEY, sp.getString("json", "")));
            viewPager.setAdapter(new CurriculumAdapter(this, cc));
            viewPager.setCurrentItem(time.weekDay);
        } catch (Exception e) {
            startNetwork();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.curriculum, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.action_refresh:
            updateNetwork();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void startNetwork() {
        HttpUtil.get(this, NetworkConfig.serverUrl + "curriculum/info", new RetryAuthListener(this) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    Curriculum cc = Curriculum.dao.fromJson(responseString);
                    viewPager.setAdapter(new CurriculumAdapter(getContext(), cc));
                    viewPager.setCurrentItem(time.weekDay);
                    //保存在本地
                    Editor editer = getSharedPreferences("curriculum_" + UserInfo.getSavedUserId(), Context.MODE_PRIVATE).edit();
                    editer.putString("json", DES3.encrypt(SecretKey.SP_KEY, responseString));
                    editer.commit();
                } catch(Exception e) {
                    String[] msgs = responseString.split("\n");
                    showErrorDialog("提示", msgs[0], msgs[1]);
                }
            }

            @Override
            public void onBtnRetry() {
                startNetwork();
            }

        });
    }
    
    private void updateNetwork() {
        HttpUtil.get(this, NetworkConfig.serverUrl + "curriculum/info", new NormalAuthListener(this) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    Curriculum cc = Curriculum.dao.fromJson(responseString);
                    viewPager.setAdapter(new CurriculumAdapter(getContext(), cc));
                    viewPager.setCurrentItem(time.weekDay);
                    //保存在本地
                    Editor editer = getSharedPreferences("curriculum_" + UserInfo.getSavedUserId(), Context.MODE_PRIVATE).edit();
                    editer.putString("json", DES3.encrypt(SecretKey.SP_KEY, responseString));
                    editer.commit();
                    //Toast
                    Toast.makeText(getContext(), "数据更新成功", Toast.LENGTH_SHORT).show();
                } catch(Exception e) {
                    String[] msgs = responseString.split("\n");
                    showErrorDialog("提示", msgs[0], msgs[1]);
                }
            }

        });
    }

}
