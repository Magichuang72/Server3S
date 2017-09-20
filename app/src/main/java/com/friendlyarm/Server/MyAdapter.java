package com.friendlyarm.Server;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.magichuang.server.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by magichuang on 17-9-12.
 */
public class MyAdapter extends BaseAdapter {
    private ArrayList<String> list;
    private HashMap<Integer, Boolean> isSelected;
    private Context context;
    private LayoutInflater inflater;

    class ViewHolder {
        TextView title;
        CheckBox checkBox;
        LinearLayout Ll;
    }

    public MyAdapter(ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        isSelected = new HashMap<>();
        initDate();
    }

    private void initDate() {
        for (int i = 0; i < list.size(); i++) {
            getIsSelected().put(i, false);
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_list, null);
            holder.title = (TextView) convertView.findViewById(R.id.itemTitle);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(list.get(position));
        holder.checkBox.setChecked(false);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    getIsSelected().put(position, true);
                    Log.i("selected",position+"true");
                } else {
                    getIsSelected().put(position, false);
                    Log.i("selected",position+"false");
                }
            }
        });
        return convertView;
    }

    public HashMap<Integer, Boolean> getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(HashMap<Integer, Boolean> isSelected) {
        this.isSelected = isSelected;
    }
}
