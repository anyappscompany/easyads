package ua.com.anyapps.easyads.easyads;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

// автоматическая высота GridView
public class FullHeightGridView extends GridView {

    public FullHeightGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullHeightGridView(Context context) {
        super(context);
    }

    public FullHeightGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
