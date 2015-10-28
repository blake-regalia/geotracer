package net.blurcast.android.input;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * Created by blake on 10/18/14.
 */
public class InputBinder {

    private Activity mActivity;

    public InputBinder(Activity activity) {
        mActivity = activity;
    }

    public View click(int viewId, View.OnClickListener onClickListener) {
        View view = mActivity.findViewById(viewId);
        view.setOnClickListener(onClickListener);
        return view;
    }

    public CompoundButton check(int viewId, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        CompoundButton compoundButton = (CompoundButton) mActivity.findViewById(viewId);
        compoundButton.setOnCheckedChangeListener(onCheckedChangeListener);
        return compoundButton;
    }

    public TextView asText(int viewId) {
        return (TextView) mActivity.findViewById(viewId);
    }


    public class Control {

        private View mView;

        public Control(Class viewType, View view) {

            mView = view;
        }

        public Control(View view) {
            mView = view;
        }

        public void click(int viewId, View.OnClickListener onClickListener) {
            mView.findViewById(viewId).setOnClickListener(onClickListener);
        }

        public void check(int viewId, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
            ((CompoundButton) mView.findViewById(viewId)).setOnCheckedChangeListener(onCheckedChangeListener);
        }

        public View asView() {
            return mView;
        }

    }
}
