/*
 * Copyright (c) 2015 GPL by J.M.Goebel. Distributed under the GNU GPL v3.
 * 
 * 08.06.2015
 * 
 * This file is part of learnforandroid.
 *
 * learnforandroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  learnforandroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package jmg.de.org.repetit;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import jmg.de.org.repetit.lib.lib;


public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

	int PAGE_COUNT = 3;
	public SQLiteDataActivity fragData;
	public SymptomsActivity fragSymptoms;
	public MedActivity fragMed;
	public SettingsActivity fragSettings;
	//public org.liberty.android.fantastischmemo.downloader.quizlet.QuizletOAuth2AccessCodeRetrievalFragment fragAuthQuizlet;
	public MainActivity main;
	/** Constructor of the class */
	public MyFragmentPagerAdapter(FragmentManager fm, MainActivity main, boolean restart) {
		super(fm);
		this.main = main;
		try
		{
			if (restart) 
			{
				LayoutInflater Li = LayoutInflater.from(main);
				for (Fragment f: fm.getFragments())
				{
					if (f instanceof MedActivity)
					{
						fragMed = (MedActivity) f;
						fragMed._main = main;
					}
					else if (f instanceof SettingsActivity)
					{
						fragSettings = (SettingsActivity) f;
						fragSettings._main = main;
						
					}
					else if (f instanceof SymptomsActivity)
					{
						fragSymptoms = (SymptomsActivity) f;
						fragSymptoms._main = main;
						//fragChooser.onCreateView(Li, main.Layout, null);
					}
					else if (f instanceof SQLiteDataActivity)
					{
						fragData = (SQLiteDataActivity) f;
						fragData._main = main;
						//fragChooser.onCreateView(Li, main.Layout, null);
					}
					/*
					else if (f instanceof QuizletOAuth2AccessCodeRetrievalFragment)
					{
						fragAuthQuizlet = (QuizletOAuth2AccessCodeRetrievalFragment) f;
					}
					*/
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/** This method will be invoked when a page is requested to create */
	public Fragment LastItem;
	
	@Override
	public Fragment getItem(int arg0) {
		switch(arg0)
		{
		
			/** tab1 is selected */
			case MedActivity.fragID:
				if (fragMed == null)
				{
					fragMed = new MedActivity();
					if (main.treeView == null) main.treeView = this.fragMed.treeView;
				}
				LastItem = fragMed;
				return fragMed;
				
			case SymptomsActivity.fragID:
			try 
			{
				if (fragSymptoms==null)
				{
					fragSymptoms=new SymptomsActivity();
					//if (main!=null) fragChooser.init(main.getFileChooserIntent(true),main);
				}
				LastItem = fragSymptoms;
				return fragSymptoms;
				
			} catch (Exception e) {

				lib.ShowException(main, e);
			}
				
			case SettingsActivity.fragID:
				if (fragSettings==null)
				{
					fragSettings = new SettingsActivity();
					//fragSettings.init(main.getSettingsIntent(),main);
				}
				LastItem = fragSettings;
				return fragSettings;
			case SQLiteDataActivity.fragID:
				if (fragData==null)
				{
					fragData = new SQLiteDataActivity();
					//fragSettings.init(main.getSettingsIntent(),main);
				}
				LastItem = fragData;
				try {
					fragData.init(main.db);
				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}
				return fragData;


		}
		
		return null;
	}

	/** Returns the number of pages */
	@Override
	public int getCount() {		
		if (Build.VERSION.SDK_INT < 11) PAGE_COUNT = 3;
		return PAGE_COUNT;
	}
	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object)
	{		
		super.setPrimaryItem(container, position, object);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{		
		super.destroyItem(container, position, object);
		if (lib.NookSimpleTouch() && object == fragSettings) 
		{
			//main.RemoveFragSettings();
		}
	}

	
}
