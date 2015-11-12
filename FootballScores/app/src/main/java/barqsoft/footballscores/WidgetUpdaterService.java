package barqsoft.footballscores;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import barqsoft.footballscores.data.FootballScore;
import barqsoft.footballscores.data.ScoresAdapter;

/**
 * @author Veronika Rodionova nika.blaine@gmail.com
 */
public class WidgetUpdaterService extends Service {

    public static final String LOG_TAG = WidgetUpdaterService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        update();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void update() {
        Log.d(LOG_TAG, "Updating the widget");
        new FetchScoresTask(getApplicationContext()).execute();
    }

    public class FetchScoresTask extends AsyncTask<Void, Void, Void> {

        private Context context;
        private List<FootballScore> scores;

        public FetchScoresTask(Context context) {
            this.context = context;
        }

        private void getFootballScoresFromCursor(Cursor cursor) {
            if (cursor != null && cursor.moveToFirst()) {
                scores = new ArrayList<>();
                while (cursor.moveToNext()) {
                    scores.add(
                            new FootballScore(
                                    cursor.getString(ScoresAdapter.COL_HOME),
                                    cursor.getString(ScoresAdapter.COL_AWAY),
                                    cursor.getInt(ScoresAdapter.COL_HOME_GOALS),
                                    cursor.getInt(ScoresAdapter.COL_AWAY_GOALS),
                                    cursor.getString(ScoresAdapter.COL_DATE),
                                    cursor.getString(ScoresAdapter.COL_MATCHTIME)
                            )
                    );
                }
                cursor.close();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String[] selectionArgs = new String[]{format.format(new Date())};
            Cursor cursor = context.getContentResolver().query(
                    DatabaseContract.scores_table.buildScoreWithDate(),
                    DatabaseContract.DEFAULT_PROJECTION,
                    null,
                    selectionArgs,
                    null
            );
            getFootballScoresFromCursor(cursor);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            RemoteViews view = new RemoteViews(getPackageName(), R.layout.widget);
            Log.d(LOG_TAG, "scores = " + scores);
            if (scores != null) {
                for (FootballScore score : scores) {
                    view.setTextViewText(R.id.home_textview, score.getHome());
                    view.setTextViewText(R.id.away_textview, score.getAway());
                    view.setTextViewText(R.id.score_textview, Utilies.getScores(score.getHomeGoals(), score.getAwayGoals()));
                    view.setTextViewText(R.id.date_textview, score.getDate());
                }
                // Push update for this widget to the home screen
                ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                manager.updateAppWidget(thisWidget, view);
            } else {
                view.setTextViewText(R.id.score_textview, getString(R.string.no_matches));
            }
        }
    }
}