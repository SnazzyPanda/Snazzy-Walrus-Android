package com.snazzy.android.snazzywalrus.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.snazzy.android.snazzywalrus.R;
import com.snazzy.android.snazzywalrus.util.SettingsManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BottomMenuFragment.OnBottomMenuFragmentClickListener} interface
 * to handle interaction events.
 */
public class BottomMenuFragment extends Fragment {

	private Fragment callingActivity;

	private OnBottomMenuFragmentClickListener mListener;
	private TextView bottomBarTextView;
	private SettingsManager sm = SettingsManager.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_bottom_menu, container, false);

		callingActivity = getParentFragment();

		bottomBarTextView = (TextView) v.findViewById(R.id.bottomBarText);
		if(callingActivity.getClass().equals(ImageListFragment.class)){
			// if image list fragment
			updateRegisteredImagesText();
		}
		if(callingActivity.getClass().equals(SetManagementFragment.class)){
			// if set management fragment
			updateRegisteredSetsText();
		}

		Button addItemButton = (Button) v.findViewById(R.id.bottomAddButton);
		addItemButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onAddItemButtonClicked(view);
			}
		});

		Button deleteListButton = (Button) v.findViewById(R.id.bottomDeleteButton);
		deleteListButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onDeleteItemsButtonClicked(view);
			}
		});

		// Inflate the layout for this fragment
		return v;
	}

	private void onAddItemButtonClicked(View view){
		if(callingActivity.getClass().equals(ImageListFragment.class)){
			// if image list fragment
			((ImageListFragment) callingActivity).onBottomMenuFragmentClickListener(view);
			updateRegisteredImagesText();
			return;
		}
		if(callingActivity.getClass().equals(SetManagementFragment.class)){
			// set manager fragment
			((SetManagementFragment) callingActivity).onBottomMenuFragmentClickListener(view);
			updateRegisteredSetsText();
			return;
		}
	}

	private void onDeleteItemsButtonClicked(View view){
		if(callingActivity.getClass().equals(ImageListFragment.class)){
			// if image list fragment
			((ImageListFragment) callingActivity).onBottomMenuFragmentClickListener(view);
			updateRegisteredImagesText();
			return;
		}
		if(callingActivity.getClass().equals(SetManagementFragment.class)){
			// set manager fragment
			((SetManagementFragment) callingActivity).onBottomMenuFragmentClickListener(view);
			updateRegisteredSetsText();
			return;
		}
	}

	/**
	 * Update the text for number of registered images/wallpapers (assumes image list fragment)
	 */
	public void updateRegisteredImagesText(){
		int num = sm.getWallpaperList().size();
		String base = String.format(getString(R.string.registered_images_text), num);
		bottomBarTextView.setText(base);
	}

	/**
	 * Update the text for the number of registered sets (assumes set management screen)
	 */
	public void updateRegisteredSetsText(){
		int num = sm.getOverviewListOfSets().size();
		String base = String.format(getString(R.string.registered_sets_text), num);
		bottomBarTextView.setText(base);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		callingActivity = getParentFragment();
		if (callingActivity instanceof OnBottomMenuFragmentClickListener) {
			mListener = (OnBottomMenuFragmentClickListener) callingActivity;
		} else {
			throw new RuntimeException(callingActivity.toString()
					+ " must implement OnBottomMenuFragmentClickListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnBottomMenuFragmentClickListener {
		void onBottomMenuFragmentClickListener(View view);
	}
}
