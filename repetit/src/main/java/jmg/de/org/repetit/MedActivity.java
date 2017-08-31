package jmg.de.org.repetit;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import me.texy.treeview.TreeNode;
import me.texy.treeview.base.BaseNodeViewBinder;

/**
 * Created by hmnatalie on 29.08.17.
 */

public class MedActivity extends Fragment {
    public final static int fragID = 1;

    public MainActivity _main;
}
public class FirstLevelNodeViewBinder extends BaseNodeViewBinder {
    TextView textView;
    public FirstLevelNodeViewBinder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.node_name_view)
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_first_level;
    }

    @Override
    public void bindView(TreeNode treeNode) {
        textView.setText(treeNode.getValue().toString());
    }
}

