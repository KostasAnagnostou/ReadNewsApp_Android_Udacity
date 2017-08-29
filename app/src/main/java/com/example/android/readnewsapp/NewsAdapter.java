package com.example.android.readnewsapp;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kostas on 14/7/2017.
 */

public class NewsAdapter extends ArrayAdapter<News> {

    /**
     * Constructs a new {@link NewsAdapter} object.
     *
     * @param context is the current context (i.e. Activity) that the adapter is being created in.
     * @param news    is the list of {@link News} Articles to be displayed.
     */
    public NewsAdapter(Activity context, ArrayList<News> news) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        super(context, 0, news);
    }

    /*
    * Provides a view for an AdapterView (ListView, GridView, etc.)
     *  Returns a list item view that displays information about a News Article
     *  at the given position in the list of News Articles
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        ViewHolder viewHolder;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);

            viewHolder = new ViewHolder(listItemView);
            listItemView.setTag(viewHolder);
        } // For those situations when the listItemView(convertView) is already inflated
        else {
            viewHolder = (ViewHolder) listItemView.getTag();
        }

        // Find the News Article at the given position in the list of News Articles
        News currentNews = getItem(position);

        // Get the title string from the News Article object
        String title = currentNews.getTitle();
        // Set the title of the current News Article in that TextView
        viewHolder.titleTextView.setText(title);

        // Get the Author string from the News Article object
        String description = currentNews.getDescription();
        // Set the Author Name of the current News Article in that TextView
        viewHolder.descriptionTextView.setText(Html.fromHtml(description));

        // Get the Section Name string from the News Article object
        String section = currentNews.getSection();
        // Set the Section Name of the current News Article in that TextView
        viewHolder.sectionTextView.setText(section);

        // Get the Date from the News Article object
        String dateTime = currentNews.getDateTime();
        // Set the Date of the current News Article in that TextView
        viewHolder.dateTimeTextView.setText(dateTime);

        return listItemView;
    }

    // We use inner Class ViewHolder to find & handle the views in list_item.xml
    // This Article (from Vlad) helped --> http://spreys.com/view-holder-design-pattern-for-android/

    private class ViewHolder {
        private TextView titleTextView;
        private TextView descriptionTextView;
        private TextView sectionTextView;
        private TextView dateTimeTextView;

        private ViewHolder(@NonNull View view) {
            this.titleTextView = (TextView) view
                    .findViewById(R.id.title_text_view);
            this.descriptionTextView = (TextView) view
                    .findViewById(R.id.description_text_view);
            this.sectionTextView = (TextView) view
                    .findViewById(R.id.section_text_view);
            this.dateTimeTextView = (TextView) view
                    .findViewById(R.id.date_text_view);
        }
    }
}