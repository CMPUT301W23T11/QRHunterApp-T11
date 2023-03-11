package com.example.qrhunterapp_t11;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Comment Adapter is a custom adapter class to handle Comment objects
 * References: https://www.youtube.com/watch?v=cGIAZAbJJPc&t=1753s
 * by Programming w/ Professor Sluiter for custom array adapter help, video posted Sep 26, 2019, CC BY
 */

public class CommentAdapter extends BaseAdapter {

    private ArrayList<Comment> commentList;
    private View commentView;

    private Context context;

    public CommentAdapter(Context context, ArrayList<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @Override
    public int getCount() {
        return commentList.size();
    }

    @Override
    public Comment getItem(int i) {
        return commentList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    // send back view that can be used in list layout
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        commentView = inflater.inflate(R.layout.individual_comment, viewGroup, false);

        TextView profile_tv = commentView.findViewById(R.id.profile_tv);
        TextView comment_tv = commentView.findViewById(R.id.comment_tv);
        Comment c = this.getItem(i);
        profile_tv.setText(c.getProfile());
        comment_tv.setText(c.getComment());
        return commentView;
    }
}