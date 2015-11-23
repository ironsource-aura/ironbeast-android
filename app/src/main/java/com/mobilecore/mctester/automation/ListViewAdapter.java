package com.mobilecore.mctester.automation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

	private static final boolean USE_TWO_TYPES = true;

	private ArrayList<BaseDataItem> mDataArray;

	private Context mContext;
	private LayoutInflater mInflater;

	private enum ETypes {
		TYPE_A, TYPE_B
	}

	public ListViewAdapter(Context context) {

		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//
		mDataArray = new ArrayList<BaseDataItem>();

		doUpdateData();

	}

	public void updateData() {
		doUpdateData();
		notifyDataSetChanged();
	}

	private void doUpdateData() {
		//set 20 items for automation testing, please don't change it.
		int numItems = 20;

		Toast.makeText(mContext, "Refreshing list! num items:" + numItems, Toast.LENGTH_SHORT).show();

		mDataArray.clear();

		String[] titles = new String[] { "Dogs", "Cats", "Penguins", "Dragons" };
		String[] contents = new String[] { "Loyal friends", "Love to play", "Are simply awesome", "Shoot flaming fire" };

		for (int i = 0; i < numItems; i++) {

			boolean addTypeAItem = true;

			if (USE_TWO_TYPES) {

				int rand = (int) Math.floor(Math.random() * 2);

				if (rand == 0) {
					addTypeAItem = false;
				}
			}

			if (addTypeAItem) {
				mDataArray.add(new TypeADataItem("Main text " + i, "Secondary text " + i));
			} else {
				mDataArray.add(new TypeBDataItem("Main text " + i, "Secondary text " + i));
			}

			/*
			 * int index = i % titles.length;
			 * mDataArray.add(new TypeADataItem(titles[index] , contents[index]));
			 */
		}
	}

	@Override
	public int getCount() {
		return mDataArray.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		return mDataArray.get(position).getType();
	}

	@Override
	public int getViewTypeCount() {
		return ETypes.values().length;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		BaseDataItem data = mDataArray.get(position);

		BaseUIHolder holder = (convertView == null) ? null : (BaseUIHolder) convertView.getTag();

		if (holder == null) {

			if (data.getType() == ETypes.TYPE_A.ordinal()) {

				holder = new TypeAUIHolder();
				convertView = mInflater.inflate(R.layout.list_item_type_a, null);

			}
			else if (data.getType() == ETypes.TYPE_B.ordinal()) {

				holder = new TypeBUIHolder();
				convertView = mInflater.inflate(R.layout.list_item_type_b, null);

			}

			holder.initHolder(convertView);
			convertView.setTag(holder);

			// Log.d("aaa", "Creating new");

		} else {
			// Log.d("aaa", "REUSING!!!");
		}

		holder.updateView(data);

		return convertView;
	}

	private abstract class BaseDataItem implements IAdapterDataItem {

		private final String mainText;
		private final String secondaryText;

		public BaseDataItem(String mainText, String secondaryText) {
			this.mainText = mainText;
			this.secondaryText = secondaryText;
		}

		@Override
		public int getType() {
			return ETypes.TYPE_A.ordinal();
		}

		public String getMainText() {
			return mainText;
		}

		public String getSecondaryText() {
			return secondaryText;
		}

	}

	private class TypeADataItem extends BaseDataItem {

		public TypeADataItem(String mainText, String secondaryText) {
			super(mainText, secondaryText);
		}

		@Override
		public int getType() {
			return ETypes.TYPE_A.ordinal();
		}
	}

	private class TypeBDataItem extends BaseDataItem {

		public TypeBDataItem(String mainText, String secondaryText) {
			super(mainText, secondaryText);
		}

		@Override
		public int getType() {
			return ETypes.TYPE_B.ordinal();
		}
	}

	private abstract class BaseUIHolder implements IAdapterUIHolder {

		private TextView mMainTV;
		private TextView mSecondaryTV;

		@Override
		public void initHolder(View view) {
			mMainTV = (TextView) view.findViewById(R.id.listItem_a_mainTV);
			mSecondaryTV = (TextView) view.findViewById(R.id.listItem_a_secondaryTV);
		}

		@Override
		public void updateView(IAdapterDataItem data) {

			BaseDataItem typeADataItem = (BaseDataItem) data;

			mMainTV.setText(typeADataItem.getMainText());
			mSecondaryTV.setText(typeADataItem.getSecondaryText());

		}

	}

	private class TypeAUIHolder extends BaseUIHolder {

	}

	private class TypeBUIHolder extends BaseUIHolder {

	}
}
