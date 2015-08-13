package eleven.insert;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


public class gameActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamelayout);

        Intent intent = getIntent();
        double rotateDegree = intent.getIntExtra("rotate", 40);
        int noNumBalls = intent.getIntExtra("noNum", 4);
        int withNumBalls = intent.getIntExtra("withNum", 18);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.framelayout);
        MyView gameView = new MyView(this, rotateDegree, noNumBalls, withNumBalls);
        gameView.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        frameLayout.addView(gameView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.restart) {
            Intent intent = getIntent();
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            return true;
        }

        if (id == R.id.over) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
