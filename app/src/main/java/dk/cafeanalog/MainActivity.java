/*
 * Copyright 2016 Analog IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.cafeanalog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jsoup.nodes.Document;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IsOpenFragment.ShowOpening {
    private static final String IS_OPEN_FRAGMENT = "dk.cafeanalog.MainActivity.IS_OPEN_FRAGMENT",
                                OPENING_FRAGMENT = "dk.cafeanalog.MainActivity.OPENING_FRAGMENT";

    private boolean mVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mVisible = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isDualPane = findViewById(R.id.opening_layout) != null;

        Log.d("MainActivity", "DualPane: " + isDualPane);

        if (savedInstanceState != null) {
            getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_layout, new IsOpenFragment(), IS_OPEN_FRAGMENT)
                .commit();

        if (isDualPane) {
            getOpenings(
                    new Action<ArrayList<Opening>>() {
                        @Override
                        public void run(ArrayList<Opening> openings) {
                            if (mVisible) {
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.opening_layout, OpeningFragment.newInstance(openings), OPENING_FRAGMENT)
                                        .commit();
                            }
                        }
                    }
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVisible = true;
    }

    @Override
    protected void onPause() {
        mVisible = false;
        super.onPause();
    }

    private void getOpenings(final Action<ArrayList<Opening>> resultFunction) {
        new AsyncTask<Void, Void, ArrayList<Opening>>() {
            @Override
            protected ArrayList<Opening> doInBackground(Void... params) {
                try {
                    AnalogDownloader downloader = new AnalogDownloader(getApplicationContext());
                    Document page = downloader.downloadPage();

                    return downloader.getOpenings(page);
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
                return new ArrayList<>();
            }

            @Override
            protected void onPostExecute(ArrayList<Opening> openings) {
                resultFunction.run(openings);
            }
        }.execute();
    }

    @Override
    public void showOpening() {
        getOpenings(
                new Action<ArrayList<Opening>>() {
                    @Override
                    public void run(ArrayList<Opening> openings) {
                        if (mVisible) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.main_layout, OpeningFragment.newInstance(openings), OPENING_FRAGMENT)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                }
        );
    }
}
