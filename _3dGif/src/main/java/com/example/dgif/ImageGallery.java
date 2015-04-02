package com.example.dgif;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.dgif.utils.Constants;
import com.example.dgif.utils.MemoryManager;

public class ImageGallery extends Activity {

    private GridView gridview;
	
	private static final int SIZE = 200;
	
	private Button testGifButton;

    private static final int NONE_SELECTED = -1;
	
	private Bitmap[] mPics;
	private boolean[] mPicSelected;

    private String[] mFilenames;
	private ImageAdapter mImageAdapter;

    private int selected;
    private CheckBox selectedCheckBox = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_gallery);
		
		MemoryManager m = new MemoryManager(this);
		testGifButton = (Button) findViewById(R.id.viewTestGifButton);
		
		testGifButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(ImageGallery.this, Preview3DObject.class);
                i.putExtra(Constants.SERIALIZABLE_GIF, mFilenames[selected]);
				startActivity(i);
				
			}
			
		});
		
		
		mImageAdapter = new ImageAdapter(this);
        int size = getResources().getDimensionPixelSize(R.dimen.grid_item_size);
		mPics = m.getAllAvatars();
		mPicSelected = new boolean[mPics.length];
		
		gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(mImageAdapter);

        mFilenames = fileList();
		

	}
	
	private class ImageAdapter extends BaseAdapter {

		
		LayoutInflater mInflater;

		public ImageAdapter(Context c) {
			
			//Inflater instantiates a layout XML to its corresponding View object
			mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
		}
		
		@Override
		public int getCount() {
			return mPics.length;
		}

		@Override
		public Object getItem(int position) {
			return mPics[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		
		class ViewHolder {
			ImageView imageview;
			CheckBox checkbox;
			int id;
			
			public ViewHolder(ImageView iv, CheckBox cb) {
				imageview = iv;
				checkbox = cb;
				
			}
		}
		
		
		/*GET VIEW
		 * create new imageView for each item referenced by the adapter
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder;
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.image_view, null);
				ImageView iv = (ImageView) convertView.findViewById(R.id.gridImage);
				CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox);
				holder = new ViewHolder(iv, cb);
				
				
				//pass imageview/cb object to this view
				convertView.setTag(holder);
				
			} else { //get recycled view
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.checkbox.setId(position);
			holder.imageview.setId(position);
			holder.checkbox.setOnClickListener(new OnClickListener() {

				//toggle check box
				@Override
				public void onClick(View v) {
					CheckBox cb = (CheckBox)v;
					int id = cb.getId();
                    if (selected == id) {
                        cb.setChecked(false);
                        selected = NONE_SELECTED;
                    } else {
                        cb.setChecked(true);
                        if (selectedCheckBox != null) selectedCheckBox.setChecked(false);
                        selectedCheckBox = cb;
                        selected = id;
                    }
//					if (mPicSelected[id]) {
//						cb.setChecked(false);
//						mPicSelected[id] = false;
//					} else {
//						cb.setChecked(true);
//						mPicSelected[id] = true;
//					}
//
				}
				
			});


			holder.imageview.setImageBitmap(mPics[position]);
            holder.imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
			holder.checkbox.setChecked(mPicSelected[position]);
			holder.id = position;
			
			
			return convertView;

		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_gallery, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
