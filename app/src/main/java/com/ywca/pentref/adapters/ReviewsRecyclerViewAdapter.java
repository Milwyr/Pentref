package com.ywca.pentref.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.models.Review;

import java.util.List;

/**
 * An adapter that displays a list of reviews on a RecyclerView.
 */
public class ReviewsRecyclerViewAdapter extends
        RecyclerView.Adapter<ReviewsRecyclerViewAdapter.ViewHolder> {

    private List<Review> mReviews;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_row_layout, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Review review = mReviews.get(position);

        holder.userNameTextView.setText(review.getUserName());
        holder.ratingBar.setRating(review.getRating());
        holder.timeTextView.setText("11:59pm");
        holder.titleTextView.setText(review.getTitle());
        holder.descriptionTextView.setText(review.getDescription());
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    public void addReview(Review review) {
        mReviews.add(review);
        notifyItemInserted(mReviews.size() - 1);
    }

    public void setReviews(List<Review> reviews) {
        mReviews = reviews;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameTextView;
        private RatingBar ratingBar;
        private TextView timeTextView;
        private TextView titleTextView;
        private TextView descriptionTextView;

        ViewHolder(View view) {
            super(view);
            userNameTextView = (TextView) view.findViewById(R.id.user_name_text_view);
            ratingBar = (RatingBar) view.findViewById(R.id.review_rating_bar);
            timeTextView = (TextView) view.findViewById(R.id.review_time_text_view);
            titleTextView = (TextView) view.findViewById(R.id.review_title_text_view);
            descriptionTextView = (TextView) view.findViewById(R.id.review_description_text_view);
        }
    }
}