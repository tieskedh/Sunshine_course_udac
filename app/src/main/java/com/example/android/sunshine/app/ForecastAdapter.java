/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_DATE;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder>{

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;

    private Cursor mCursor;
    private Context mContext;
    private String LOG_TAG = ForecastAdapter.class.getSimpleName();
    private View emptyView;
    private OnClickListener mOnClickListener;

    /**
     * Cache of the children views for a forecast list item.
     */


    public ForecastAdapter(Context context, Cursor cursor, View emptyView) {
        mContext = context;
        mCursor = cursor;
        this.emptyView = emptyView;
        updateEmtpView();
    }
    private void updateEmtpView() {
        if (getItemCount()== 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void swapCursor(@Nullable Cursor cursor) {
        mCursor = cursor;
        Log.d(LOG_TAG, "swapped cursor");
        notifyDataSetChanged();
        updateEmtpView();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView iconView;
        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;

        public ViewHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onClick(view, mCursor, getAdapterPosition());
        }
    }


    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_forecast;
                break;
            }
        }

        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    public interface OnClickListener{
        void onClick(View view, Cursor cursor, int pos);
    }
    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Log.d(LOG_TAG, "onBindViewHolder: " + ForecastFragment.COL_WEATHER_CONDITION_ID);
        mCursor.moveToPosition(position);
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int fallbackIconId;
        int viewType = getItemViewType(mCursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                fallbackIconId = Utility.getArtResourceForWeatherCondition(
                    weatherId);
                break;
            }
            default: {
                // Get weather icon
                fallbackIconId = Utility.getIconResourceForWeatherCondition(
                    weatherId);
                break;
            }
        }

        Glide.with(this.mContext)
            .load(Utility.getArtUrlForWeatherCondition(this.mContext, weatherId))
            .error(fallbackIconId)
            .crossFade()
            .into(viewHolder.iconView);

        // Read date from cursor
        long dateInMillis = mCursor.getLong(COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(mContext, dateInMillis));
        // Get description from weather condition ID
        String description = Utility.getStringForWeatherCondition(mContext, weatherId);
        // Find TextView and set weather forecast on it
        viewHolder.descriptionView.setText(description);
        viewHolder.descriptionView.setContentDescription(mContext.getString(R.string.a11y_forecast, description));

        // For accessibility, we don't want a content description for the icon field
        // because the information is repeated in the description view and the icon
        // is not individually selectable

        // Read high temperature from cursor
        String high = Utility.formatTemperature(
            mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP));
        viewHolder.highTempView.setText(high);
        viewHolder.highTempView.setContentDescription(mContext.getString(R.string.a11y_high_temp, high));

        // Read low temperature from cursor
        String low = Utility.formatTemperature(
            mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));
        viewHolder.lowTempView.setText(low);
        viewHolder.lowTempView.setContentDescription(mContext.getString(R.string.a11y_low_temp, low));

    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        return (mCursor == null)? 0 : mCursor.getCount();
    }
}