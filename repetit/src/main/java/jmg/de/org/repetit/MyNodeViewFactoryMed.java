package jmg.de.org.repetit;

import android.view.View;

import me.texy.treeview.base.BaseNodeViewBinder;
import me.texy.treeview.base.BaseNodeViewFactory;


/**
 * Created by zxy on 17/4/23.
 */

public class MyNodeViewFactoryMed extends BaseNodeViewFactory {

    @Override
    public BaseNodeViewBinder getNodeViewBinder(View view, int level) {
        switch (level) {
            case 0:
                return new FirstLevelNodeViewBinderMed(view);
            case 1:
                return new SecondLevelNodeViewBinder(view);
            case 2:
                return new SecondLevelNodeViewBinder(view);
            default:
                return new SecondLevelNodeViewBinder(view);
        }
    }
}
