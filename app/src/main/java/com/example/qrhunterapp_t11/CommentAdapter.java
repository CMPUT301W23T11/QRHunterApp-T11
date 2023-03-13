package com.example.qrhunterapp_t11;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * This class is a custom adapter to handle Comment objects
 *
 * @author Sarah Thomson
 * @reference <a href="https://www.youtube.com/watch?v=cGIAZAbJJPc&t=1753s">by Programming w/ Professor Sluiter for custom array adapter help</a>
 */

public class CommentAdapter extends BaseAdapter {
    private final ArrayList<Comment> commentList;
    private final Context context;

    /**
     * Constructor takes the context and the Array list of comments
     *
     * @param context     - Context
     * @param commentList - Arraylist of comments
     */
    public CommentAdapter(@NonNull Context context, @NonNull ArrayList<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    /**
     * Gets the size of the commentList
     *
     * @return commentList.size() - integer
     */
    @Override
    public int getCount() {
        return commentList.size();
    }

    /**
     * Gets an item in the commentList
     *
     * @param i - position of item
     * @return commentList.get(i) - Comment object
     */
    @Override
    public Comment getItem(int i) {
        return commentList.get(i);
    }

    /**
     * Gets the id of an item
     *
     * @param i - item position
     * @return 0
     */
    @Override
    public long getItemId(int i) {
        return 0;
    }

    /**
     * Gets the view that can be used in the individual comment ListView layout
     *
     * @param i         - position
     * @param view      - View
     * @param viewGroup - ViewGroup
     * @return commentView - View
     */
    @Override
    // send back view that can be used in list layout
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View commentView = inflater.inflate(R.layout.individual_comment, viewGroup, false);
        TextView profile_tv = commentView.findViewById(R.id.profile_tv);
        TextView comment_tv = commentView.findViewById(R.id.comment_tv);
        Comment c = this.getItem(i);
        profile_tv.setText(c.getProfile());
        comment_tv.setText(c.getComment());
        return commentView;
    }
}