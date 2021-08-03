package com.example.froject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryHolder> {
    private ArrayList<CategoryData> list;

    CategoryAdapter(ArrayList<CategoryData> list) { this.list = list; }

    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_category, parent, false);
        return new CategoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}

class CategoryHolder extends RecyclerView.ViewHolder {
    TextView categoryname;
    CircleImageView categorysrc;

    public CategoryHolder(@NonNull View itemView) {
        super(itemView);
        categoryname = itemView.findViewById(R.id.categoryname);
        categorysrc = itemView.findViewById(R.id.categorysrc);
    }

    void onBind(CategoryData categoryData) {
        categoryname.setText(categoryData.getName());
        categorysrc.setImageResource(categoryData.getSrc());
    }
}