package com.example.android.virtualpot;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link PlantWidgetConfigureActivity PlantWidgetConfigureActivity}
 */
public class PlantWidget extends AppWidgetProvider {

    private static String URI_SCHEME = "widget_uri";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            SharedPrefUtils.deleteAll(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    /**
     * Updates a specific widget instance given the corresponding widget Id
     *
     * @param context          The calling context
     * @param appWidgetManager The widget manager
     * @param appWidgetId      The Id of the widget to be updated
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);

        // Set the click handler to open the configuration activity
        Intent configIntent = new Intent(context, PlantWidgetConfigureActivity.class);
        configIntent.setData(Uri.parse(URI_SCHEME + "://widget/id/" + appWidgetId));
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.plant_image, configPendingIntent);

        // Set the click handler to water the plant
        Intent waterIntent = new Intent(context, PlantWateringService.class);
        waterIntent.setData(Uri.parse(URI_SCHEME + "://widget/id/" + appWidgetId));
        waterIntent.setAction(PlantWateringService.ACTION_WATER_PLANT);
        waterIntent.putExtra(PlantWateringService.EXTRA_WIDGET_ID, appWidgetId);
        PendingIntent wateringPendingIntent = PendingIntent.getService(context, 0, waterIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.cloud_image, wateringPendingIntent);

        //update image if plant is old enough
        long createdAt = SharedPrefUtils.loadStartTime(context, appWidgetId);
        long wateredAt = SharedPrefUtils.loadWaterTime(context, appWidgetId);
        long now = System.currentTimeMillis();
        long plantAge = now - createdAt;
        long waterAge = now - wateredAt;
        views.setImageViewResource(R.id.plant_image, PlantUtils.getPlantImageRes(context,plantAge, waterAge,0));
        views.setImageViewResource(R.id.cloud_image, PlantUtils.getCloudImageRes(waterAge));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

