package qzwx.app.feature_home.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * AppWidgetProvider: shows a modern voice input widget.
 * On click, launches WidgetVoiceInputActivity to handle voice note input.
 */
class QuickInputWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Intent to open WidgetVoiceInputActivity
        val intent = Intent(context, WidgetVoiceInputActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Resolve resources by name to avoid issues with generated R in multi-module setups
        val layoutId = context.resources.getIdentifier("quick_input_widget", "layout", context.packageName)
        val containerId = context.resources.getIdentifier("quick_input_container", "id", context.packageName)
        val views = if (layoutId != 0) RemoteViews(context.packageName, layoutId) else RemoteViews(context.packageName, 0)
        if (containerId != 0) {
            views.setOnClickPendingIntent(containerId, pending)
        }

        // Update all instances
        val component = ComponentName(context, QuickInputWidgetProvider::class.java)
        appWidgetManager.updateAppWidget(component, views)
    }
}

