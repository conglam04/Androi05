package com.example.todolist.Ui.setting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.R;

import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

    private final List<FaqItem> faqList;

    public FaqAdapter(List<FaqItem> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
        FaqItem faqItem = faqList.get(position);
        holder.questionTextView.setText(faqItem.getQuestion());
        holder.answerTextView.setText(faqItem.getAnswer());

        boolean isExpanded = faqItem.isExpanded();
        holder.answerLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            faqItem.setExpanded(!isExpanded);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    static class FaqViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView;
        TextView answerTextView;
        LinearLayout answerLayout;

        public FaqViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.faq_question);
            answerTextView = itemView.findViewById(R.id.faq_answer);
            answerLayout = itemView.findViewById(R.id.answer_layout);
        }
    }
}
