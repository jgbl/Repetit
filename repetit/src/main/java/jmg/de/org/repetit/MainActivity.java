package jmg.de.org.repetit;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jmg.de.org.repetit.lib.HackyViewPager;
import jmg.de.org.repetit.lib.lib;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private boolean isTV;
    private boolean isWatch;
    private ViewGroup Layout;
    private HackyViewPager mPager;
    private MyFragmentPagerAdapter fPA;

    public MainActivity()
{



};

@Override
protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        //lib.main = this;

        //getting the kind of userinterface: television or watch or else
        int UIMode = lib.getUIMode(this);
        switch (UIMode)
        {
        case Configuration.UI_MODE_TYPE_TELEVISION:
        isTV = true;
        break;
        case Configuration.UI_MODE_TYPE_WATCH:
        isWatch = true;
        break;
        }

        if (savedInstanceState != null)
        {
        //JMGDataDirectory = savedInstanceState.getString("JMGDataDirectory");
        }

        setContentView(R.layout.activity_main_viewpager);

        /** Getting a reference to ViewPager from the layout */
        View pager = this.findViewById(R.id.pager);
        Layout = (ViewGroup) pager;
        mPager = (HackyViewPager) pager;

        /** Getting a reference to FragmentManager */
        FragmentManager fm = getSupportFragmentManager();

        setPageChangedListener();

        /** Creating an instance of FragmentPagerAdapter */
        if (fPA == null)
        {
        fPA = new MyFragmentPagerAdapter(fm, this, savedInstanceState != null);
        }

        /** Setting the FragmentPagerAdapter object to the viewPager object */
        mPager.setAdapter(fPA);
        //mPager.setCurrentItem(0);

        lib.gStatus = "onCreate getEink";


        }
    private void setPageChangedListener()
    {
        /** Defining a listener for pageChange */
        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener()
        {
            int LastPosition = -1;

            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);

                if (LastPosition == MedActivity.fragID)
                {
                    try
                    {
                        if (fPA != null && fPA.fragSettings != null)
                        {
                            try
                            {
                                //fPA.fragSettings.saveResultsAndFinish(true);
                            }
                            catch (Throwable ex)
                            {
                                Log.e(".saveResultsAndFinish", ex.getMessage(), ex);
                            }
                    					/*
                    					if (lib.NookSimpleTouch())
                    					{
                    						RemoveFragSettings();
                    					}
                    					*/
                        }

                    }
                    catch (Throwable e)
                    {

                        lib.ShowException(MainActivity.this, e);
                    }
                    //mnuUploadToQuizlet.setEnabled(true);
                }
                else if (LastPosition == SymptomsActivity.fragID)
                {
                    if (fPA != null && fPA.fragMed != null)
                    {
                        //fPA.fragMed.removeCallbacks();
                    }
                }

                if (position == MedActivity.fragID)
                {
                    //mnuAddNew.setEnabled(false);
                        	/*
							try {
								if (!checkLoadFile())
								{
									mPager.setCurrentItem(_MainActivity.fragID);
								}
							} catch (Throwable e) {

								lib.ShowException(MainActivity.this, e);
							}
							*/
                }
                else if (position == MedActivity.fragID)
                {
                    //mnuAddNew.setEnabled(true);
                    //mnuUploadToQuizlet.setEnabled(true);
                    if (fPA != null && fPA.fragMed != null)
                    {
                        /*
                        fPA.fragMed._txtMeaning1.setOnFocusChangeListener(new View.OnFocusChangeListener()
                        {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus)
                            {
                                fPA.fragMed._txtMeaning1.setOnFocusChangeListener(fPA.fragMed.FocusListenerMeaning1);
                                if (hasFocus)
                                {
                                    fPA.fragMed._scrollView.fullScroll(View.FOCUS_UP);
                                }

                            }
                        });
                        */
                    }
                }
                else if (position == SettingsActivity.fragID)
                {
                    if (fPA != null && fPA.fragSettings != null)
                    {
                        try
                        {
                            /*
                            int Language = fPA.fragSettings.getIntent().getIntExtra(
                                    "Language", org.de.jmg.learn.vok.Vokabel.EnumSprachen.undefiniert.ordinal());
                            fPA.fragSettings.spnLanguages.setSelection(Language);
                            fPA.fragSettings.setSpnMeaningPosition();
                            fPA.fragSettings.setSpnWordPosition();
                            fPA.fragSettings.setChkTSS();
                            */
                        }
                        catch (Throwable ex)
                        {
                            Log.e(".saveResultsAndFinish", ex.getMessage(), ex);
                        }
                    }
                }
                else if (position == SymptomsActivity.fragID)
                {
                    if (fPA != null && fPA.fragSymptoms != null)
                    {
                        //searchQuizlet();
                    }

                }
                else
                {
                    //mnuAddNew.setEnabled(false);
                }

                LastPosition = position;


            }

        };

        /** Setting the pageChange listener to the viewPager */
        mPager.addOnPageChangeListener(pageChangeListener);

    }


}
