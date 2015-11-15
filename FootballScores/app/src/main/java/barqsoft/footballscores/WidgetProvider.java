package barqsoft.footballscores;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Calendar;

/**
 * @author Veronika Rodionova nika.blaine@gmail.com
 */
public class WidgetProvider extends AppWidgetProvider {

    private static final long UPDATE_INTERVAL = 60 * 1000L;
    private PendingIntent service = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        final Calendar TIME = Calendar.getInstance();
        TIME.set(Calendar.MINUTE, 0);
        TIME.set(Calendar.SECOND, 0);
        TIME.set(Calendar.MILLISECOND, 0);

        final Intent intent = new Intent(context, WidgetUpdaterService.class);
        if (service == null) {
            service = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        alarmManager.setRepeating(AlarmManager.RTC, TIME.getTime().getTime(), UPDATE_INTERVAL, service);

        // open application on click on the widget
        for (int appWidgetId : appWidgetIds) {
            Intent applicationIntent = new Intent("android.intent.action.MAIN");
            applicationIntent.addCategory("android.intent.category.LAUNCHER");
            applicationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            applicationIntent.setComponent(new ComponentName(context.getPackageName(), MainActivity.class.getName()));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, applicationIntent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.empty_widget_view, pendingIntent);
            views.setOnClickPendingIntent(R.id.normal_widget_view, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onDisabled(Context context) {
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(service);
    }
}
