package ua.in.danilichev.timelysms.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.*;
import android.widget.AdapterView;
import android.widget.TextView;
import ua.in.danilichev.timelysms.app.R;
import ua.in.danilichev.timelysms.app.SmsActivity;
import ua.in.danilichev.timelysms.app.data.SmsContentProvider;
import ua.in.danilichev.timelysms.app.data.SmsTable;
import ua.in.danilichev.timelysms.app.sms.SmsAlarmReceiver;

public class SmsHistoryFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String INTENT_KEY = "MessagesListActivity";

    private static final int EDIT_ID = Menu.FIRST + 1;
    private static final int DELETED_ID = Menu.FIRST + 2;

    private SimpleCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] from = new String[] {
                SmsTable.COLUMN_PHONE_NO,
                SmsTable.COLUMN_NAME_OF_CONTACT,
                SmsTable.COLUMN_MESSAGE,
                SmsTable.COLUMN_STATUS,
                SmsTable.COLUMN_STATUS_UPDATE_DATE,
                SmsTable.COLUMN_STATUS_UPDATE_TIME };

        int[] to = new int[] {
                R.id.textViewPhoneNumber,
                R.id.textViewNameOfContactInItemSms,
                R.id.textViewMessage,
                R.id.textViewStatus,
                R.id.textViewStatusUpdateDate,
                R.id.textViewStatusUpdateTime };

        adapter = new SimpleCursorAdapter(getActivity(), R.layout.item_sms, null, from, to, 0);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sms_history, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu,
            View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        TextView smsSendingStatusTextView =
                (TextView) info.targetView.findViewById(R.id.textViewStatus);
        String sendingStatus = smsSendingStatusTextView.getText().toString();
        String statusSmsDelivered = getActivity()
                .getResources().getString(R.string.sms_delivered);

        if (!sendingStatus.equals(statusSmsDelivered)) {
            menu.add(0, EDIT_ID, 0, R.string.menu_edit);
        }
        menu.add(0, DELETED_ID, 0, R.string.menu_deleted);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Uri smsUri = Uri.parse(SmsContentProvider.CONTENT_URI + "/" + info.id);

        switch (item.getItemId()) {
            case EDIT_ID:
                String uriToString = smsUri.toString();
                Intent i = new Intent(getActivity(), SmsActivity.class);
                i.putExtra(INTENT_KEY, uriToString);
                getActivity().setResult(Activity.RESULT_OK, i);
                getActivity().finish();
                return true;
            case DELETED_ID:
                //cancel intent which must send sms
                TextView smsSendingStatusTextView =
                        (TextView) info.targetView.findViewById(R.id.textViewStatus);
                String sendingStatus = smsSendingStatusTextView.getText().toString();
                String statusWillBeSent = getActivity()
                        .getResources().getString(R.string.sms_will_be_sent);
                if (sendingStatus.equals(statusWillBeSent)) {
                    SmsAlarmReceiver receiver = new SmsAlarmReceiver();
                    receiver.cancelAlarm(getActivity(), smsUri);
                }

                //delete sms from db
                getActivity().getContentResolver().delete(smsUri, null, null);
                //restart loader without deleted sms
                getLoaderManager().restartLoader(0, null, this);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                SmsTable.COLUMN_ID,
                SmsTable.COLUMN_NAME_OF_CONTACT,
                SmsTable.COLUMN_PHONE_NO,
                SmsTable.COLUMN_MESSAGE,
                SmsTable.COLUMN_STATUS,
                SmsTable.COLUMN_STATUS_UPDATE_DATE,
                SmsTable.COLUMN_STATUS_UPDATE_TIME };

        String sortOrder = SmsTable.COLUMN_ID + " DESC";

        return new CursorLoader(getActivity(),
                SmsContentProvider.CONTENT_URI, projection, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}
