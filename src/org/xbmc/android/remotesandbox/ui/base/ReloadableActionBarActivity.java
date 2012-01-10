/*
 *      Copyright (C) 2005-2015 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.remotesandbox.ui.base;

import org.xbmc.android.remotesandbox.R;
import org.xbmc.android.util.google.DetachableResultReceiver;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Action bar activity that contains a sync action.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public abstract class ReloadableActionBarActivity extends ActionBarActivity implements DetachableResultReceiver.Receiver {

	protected DetachableResultReceiver mReceiver;
	private boolean mSyncing = false;
	
	/**
	 * Excecuted when the sync button is pressed.
	 */
	protected abstract void onSyncPressed();
	
	/**
	 * Executed when the sync process has terminated.
	 *  
	 * @param resultCode Result code of the sync process
	 * @param resultData Result data of the sync process
	 * @return Boolean indicating if still syncing or not.
	 */
	protected abstract boolean onSyncResult(int resultCode, Bundle resultData);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);
		getActionBarHelper().setRefreshActionItemState(mSyncing);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.refresh_menu_items, menu);

		// Calling super after populating the menu is necessary here to ensure
		// that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				onSyncPressed();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		mSyncing = onSyncResult(resultCode, resultData);
		getActionBarHelper().setRefreshActionItemState(mSyncing);
	}
	
}
