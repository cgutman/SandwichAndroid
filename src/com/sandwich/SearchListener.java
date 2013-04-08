package com.sandwich;

import java.util.ArrayList;

import com.sandwich.client.Client;
import com.sandwich.client.ResultListener;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SearchListener implements ResultListener,OnItemClickListener,Runnable  {
	private Client sandwichClient;
	private Activity activity;
	
	private ListView resultsView;
	
	private ArrayList<ResultListener.Result> results;
		
	public SearchListener(Activity activity, Client client)
	{
		this.sandwichClient = client;
		this.activity = activity;

		// Fetch these here so we don't have to do it later
		this.resultsView = (ListView)activity.findViewById(R.id.resultsListView);
		
		// Initialize the results list
		results = new ArrayList<ResultListener.Result>();
	}
	
	@SuppressWarnings("unchecked")
	// Called when the search button is clicked
	public boolean onQueryTextSubmit(String query)
	{
		ResultAdapter<ResultListener.Result> listAdapter;
		
		// Clear the results list
		listAdapter = (ResultAdapter<ResultListener.Result>)resultsView.getAdapter();
		listAdapter.clear();
		
		// Initialize the results list
		results.clear();
		
		// Execute the asynchronous search with the client
		try {
			sandwichClient.beginSearch(query, this);
		} catch (Exception e) {
			e.printStackTrace();
			Dialog.displayDialog(activity, "Search Error", e.getMessage(), false);
		}
		
		// We handled the event so return true
		return true;
	}

	@Override
	// Called for each result found during the search
	public void foundResult(String query, ResultListener.Result result) {
		synchronized (results) {
			results.add(result);
		}
		
		activity.runOnUiThread(this);
	}
	
	@Override
	// Called in UI thread to add search result
	public void run() {
		@SuppressWarnings("unchecked")
		ResultAdapter<ResultListener.Result> listAdapter = (ResultAdapter<ResultListener.Result>)resultsView.getAdapter();
		synchronized (results) {
			for (ResultListener.Result result : results)
			{
				listAdapter.add(result);
			}
			
			// Remove the results we just added
			results.clear();
		}
	}
	
	@Override
	public void searchFailed(String query, String peer, Exception e) {
		Dialog.displayDialog(activity, "Search Error", e.getMessage(), false);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		activity.openContextMenu(view);
	}
}
