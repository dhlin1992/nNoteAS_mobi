package utility;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ntx.note.Global;
import ntx.note.NoteWriterActivity;
import ntx.note.bookshelf.NtxLauncherActivity;
import ntx.note2.R;

import static ntx.note.ActivityAsyncBase.IsInitialize;

public class StarTagAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NtxLauncherActivity.ListItem> listItemArrayList;
    private Handler mHandler = new Handler();

    public StarTagAdapter(Context context, ArrayList<NtxLauncherActivity.ListItem> listItemArrayList) {
        this.context = context;
        this.listItemArrayList = listItemArrayList;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public int getCount() {
        return listItemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return listItemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (listItemArrayList.get(position).isHeader()) {
                convertView = inflater.inflate(R.layout.lv_header, null, true);
                holder.tvLabel = (TextView) convertView.findViewById(R.id.tvVehicle);
                holder.clickLayout = (LinearLayout) convertView.findViewById(R.id.header_layout);
                holder.clickLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Do Nothing
                    }
                });
            } else {
                convertView = inflater.inflate(R.layout.lv_child, null, true);
                holder.tvLabel = (TextView) convertView.findViewById(R.id.tvChild);
                holder.clickLayout = (LinearLayout) convertView.findViewById(R.id.child_layout);
                holder.clickLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        waitBookshelfInitCompleted(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                openNote(position);
                                return null;
                            }
                        });
                    }
                });
            }

            convertView.setTag(holder);
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvLabel.setText(listItemArrayList.get(position).getName());

        return convertView;
    }

    private void openNote(int position) {
        Global.openWaitDialog(context);
        ComponentName componentName = new ComponentName("ntx.note2", "ntx.note.NoteWriterActivity");
        Intent mIntent = new Intent();
        mIntent.putExtra("uuid", ((StarTagChildModel) listItemArrayList.get(position)).getBookUUID().toString());
        //mIntent.putExtra("pageIndex", ((StarTagChildModel) listItemArrayList.get(position)).getPageNumber());
        mIntent.putExtra("pageUuid", ((StarTagChildModel) listItemArrayList.get(position)).getPageUuid().toString());
        mIntent.putExtra("CreateNote", false);
        mIntent.setComponent(componentName);
        context.startActivity(mIntent);
    }

    private void waitBookshelfInitCompleted(final Callable<Void> callableMethod) {

        Global.openAlwaysWaitDialog(context);
        Runnable waitBookshelfInitRunnable = new Runnable() {
            @Override
            public void run() {
                if (IsInitialize) {
                    Global.closeWaitDialog(context);
                    try {
                        callableMethod.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    mHandler.postDelayed(this, 300);
                }
            }
        };
        mHandler.post(waitBookshelfInitRunnable);
    }

    private class ViewHolder {

        protected TextView tvLabel;

        protected LinearLayout clickLayout;

    }

}
