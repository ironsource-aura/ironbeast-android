package com.mobilecore.mctester.automation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class OfferwallAdapter extends ArrayAdapter<OfferwallItem> {
    private final String AD_SUFFIX = "ad";
    private final String ADS_SUFFIX = "ads";
    private List<OfferwallItem> objects;
    private LayoutInflater inflater;
    
    public OfferwallAdapter(Context context, int resource, List<OfferwallItem> objects) {
	super(context, resource, objects);
	this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	ViewHolder holder;
	if (convertView == null) {
	    convertView = inflater.inflate(R.layout.offerwall_list_item, null);
	    
	    holder = new ViewHolder();
	    holder.nameTV = (TextView) convertView.findViewById(R.id.tv_name);
	    holder.descriptionTV = (TextView) convertView.findViewById(R.id.tv_description);
	    holder.displaySizeTV = (TextView) convertView.findViewById(R.id.tv_display_size);
	    holder.iconIV = (ImageView) convertView.findViewById(R.id.iv_icon);
	    convertView.setTag(holder);
	} else {
	    holder = (ViewHolder) convertView.getTag();
	}

	OfferwallItem item = objects.get(position);

	holder.nameTV.setText(item.getName());
	holder.descriptionTV.setText(item.getDescription());

	holder.descriptionTV.setSelected(true); // required for marquee horizontal scroll animation

	String suffix = (item.getDisplaySize() == 1) ? AD_SUFFIX : ADS_SUFFIX;
	holder.displaySizeTV.setText("" + item.getDisplaySize() + " " + suffix);

	if (item.getIconId() != -1) {
	    holder.iconIV.setImageResource(item.getIconId());
	}

	return convertView;
    }

    /* private view holder class */
    private class ViewHolder {
	TextView nameTV;
	TextView descriptionTV;
	TextView displaySizeTV;
	ImageView iconIV;
    }

    @Override
    public boolean isEnabled(int position) {
	return super.isEnabled(position);
    }
}
