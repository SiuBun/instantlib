package com.baselib.room.user;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baselib.use.R;

import java.util.Collections;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {


    private final LayoutInflater mLayoutInflater;

    public UserAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    private List<UserEntity> mUserList = Collections.emptyList();

    public void setUsers(List<UserEntity> list) {
        mUserList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.recyclerview_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if (!mUserList.isEmpty()){
            UserEntity userEntity = mUserList.get(i);
            viewHolder.tvName.setText(
                    userEntity.getUserId()+"_"+userEntity.getUserName()+"_"+userEntity.getPassword()+"_"+userEntity.getUpdateTime()
            );
        }
    }

    @Override
    public int getItemCount() {
        if (mUserList != null) {
            return mUserList.size();
        } else {
            return 0;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.textView);
        }
    }
}
