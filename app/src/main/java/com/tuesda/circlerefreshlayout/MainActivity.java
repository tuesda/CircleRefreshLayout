package com.tuesda.circlerefreshlayout;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.tuesda.walker.circlerefresh.CircleRefreshLayout;


public class MainActivity extends Activity {

    private CircleRefreshLayout mRefreshLayout;
    private ListView mList;
    private Button mStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRefreshLayout = (CircleRefreshLayout) findViewById(R.id.refresh_layout);
        mList = (ListView) findViewById(R.id.list);
        mStop = (Button) findViewById(R.id.stop_refresh);

        String[] strs = {
            "The",
            "Canvas",
            "class",
            "holds",
            "the",
            "draw",
            "calls",
            ".",
            "To",
            "draw",
            "something,",
            "you",
            "need",
            "4 basic",
            "components",
            "Bitmap",
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strs);
        mList.setAdapter(adapter);

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRefreshLayout.finishRefreshing();
            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
