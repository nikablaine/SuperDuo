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

import com.google.common.base.Throwables;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
                parseCursor(cursor);
                while (cursor.moveToNext()) {
                    parseCursor(cursor);
                }
                cursor.close();
            }
        }

        private void parseCursor(Cursor cursor) {
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

        @Override
        protected Void doInBackground(Void... params) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String[] selectionArgs = new String[]{format.format(new Date())};
            // String[] selectionArgs = new String[] {"2015-11-09"};
            String sortOrder = DatabaseContract.scores_table.TIME_COL + " DESC";
            Cursor cursor = context.getContentResolver().query(
                    DatabaseContract.scores_table.buildScoreWithDate(),
                    DatabaseContract.DEFAULT_PROJECTION,
                    null,
                    selectionArgs,
                    sortOrder
            );
            getFootballScoresFromCursor(cursor);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            RemoteViews view = new RemoteViews(getPackageName(), R.layout.widget);
            Log.d(LOG_TAG, "scores = " + scores);
            Calendar calendar = Calendar.getInstance();
            Date currentTime = new Date();
            Date matchTime = new Date();
            calendar.setTime(currentTime);
            if (scores != null) {
                for (FootballScore score : scores) {
                    try {
                        Date parsedTime = new SimpleDateFormat("hh:mm", Locale.getDefault()).parse(score.getTime());
                        Calendar matchCalendar = Calendar.getInstance();
                        matchCalendar.setTime(parsedTime);
                        Log.d(LOG_TAG, "parsed time is " + parsedTime);
                        calendar.set(Calendar.HOUR_OF_DAY, matchCalendar.get(Calendar.HOUR_OF_DAY));
                        calendar.set(Calendar.MINUTE, matchCalendar.get(Calendar.MINUTE));
                        matchTime = calendar.getTime();
                    } catch (ParseException e) {
                        Log.e(LOG_TAG, Throwables.getStackTraceAsString(e), e);
                    }

                    if (currentTime.after(matchTime)) {
                        view.setTextViewText(R.id.home_textview, score.getHome());
                        view.setTextViewText(R.id.away_textview, score.getAway());
                        view.setTextViewText(R.id.score_textview, Utilies.getScores(score.getHomeGoals(), score.getAwayGoals()));
                        view.setTextViewText(R.id.date_textview, score.getTime());
                        break;
                    }
                }
                // Push update for this widget to the home screen
                ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                manager.updateAppWidget(thisWidget, view);
            } else {
                Log.d(LOG_TAG, "Before setting the text view..");
                view.setTextViewText(R.id.score_textview, "XXX");
                // getString(R.string.no_matches)
            }
        }
    }
}
