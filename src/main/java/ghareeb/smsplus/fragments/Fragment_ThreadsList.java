package ghareeb.smsplus.fragments;

import ghareeb.smsplus.R;
import ghareeb.smsplus.common.Smiley;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.Contact;
import ghareeb.smsplus.database.entities.Message;
import ghareeb.smsplus.database.entities.ReceivedMessage;
import ghareeb.smsplus.database.entities.SentMessage;
import ghareeb.smsplus.database.entities.ThreadEntity;
import ghareeb.smsplus.fragments.parents.Fragment_LoaderListFromDatabase;
import ghareeb.smsplus.helper.SmiliesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;
//This fragment is loaded when:
//It is created
//It is returned to after entering chat fragment

public class Fragment_ThreadsList extends Fragment_LoaderListFromDatabase<ThreadEntity> {
    class ThreadsAdapter extends ArrayAdapter<ThreadEntity> {
        private Context context;
        private int layoutResourceId;
        private ArrayList<ThreadEntity> data;

        ThreadsAdapter(Context context, int layoutResourceId, ArrayList<ThreadEntity> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        private String getDateString(Date date) {
            SimpleDateFormat formatter;

            Date now = new Date(System.currentTimeMillis());
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(date);
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTime(now);

            if (dateCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)) {
                if (dateCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR))
                    formatter = (SimpleDateFormat) SimpleDateFormat.getTimeInstance();
                else
                    formatter = new SimpleDateFormat("MMM dd", Locale.getDefault());
            } else {
                formatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
            }

            return formatter.format(date);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (data != null) {
                View row = convertView;
                ContactViewsHolder holder;

                if (row == null) {
                    LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                    row = inflater.inflate(layoutResourceId, parent, false);

                    holder = new ContactViewsHolder();
                    holder.quickContactBadge = row.findViewById(R.id.quickcontact);
                    holder.nameNumber = row.findViewById(R.id.nameNumberTV);
                    holder.dateStatus = row.findViewById(R.id.dateStatusTV);
                    holder.lastMessage = row.findViewById(R.id.lastMessageTV);
                    holder.unseenTicker = row.findViewById(R.id.unseenMessagesTickerTV);

                    row.setTag(holder);
                } else {
                    holder = (ContactViewsHolder) row.getTag();
                }

                ThreadEntity thread = data.get(position);
                Contact contact = thread.getContact();
                String temp = contact.getName();

                if (temp != null && !temp.equals(""))
                    holder.nameNumber.setText(temp);
                else
                    holder.nameNumber.setText(contact.getNumber().toString());

                if (contact.getPhoto() != null) {
                    holder.quickContactBadge.setImageBitmap(contact.getPhoto());
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
                        holder.quickContactBadge.setImageToDefault();
                    else
                        holder.quickContactBadge.setImageResource(R.drawable.ic_person);
                }

                if (contact.getLookupKey() != null && contact.getLookupKey().length() > 0) {
                    holder.quickContactBadge.assignContactUri(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                            contact.getLookupKey()));
                }

                if (thread.getDraft() != null && thread.getDraft().length() > 0) {
                    holder.lastMessage.setText(SmiliesHelper.replaceAllPatternsWithImages(thread.getDraft(), holder.lastMessage.getTextSize(), getActivity()));
                    holder.dateStatus.setText(getActivity().getResources().getText(R.string.main_draft));
                } else {
                    Message recent = thread.getMostRecentMessage();

                    if (recent != null) {
                        holder.lastMessage.setText(SmiliesHelper.replaceAllPatternsWithImages(recent.getBody(), holder.lastMessage.getTextSize(), getActivity()));

                        if (recent instanceof ReceivedMessage)
                            holder.dateStatus.setText(getDateString(((ReceivedMessage) recent).getReceiveDateTime()));
                        else
                            holder.dateStatus.setText(getDateString(recent.getSendDateTime()));
                    }
                }

                int unseenCount = thread.getUnSeenMessagesCount();

                if (unseenCount > 0) {
                    holder.unseenTicker.setText("  " + String.valueOf(unseenCount) + " ");
                    holder.unseenTicker.setVisibility(View.VISIBLE);
                } else {
                    holder.unseenTicker.setText("");
                    holder.unseenTicker.setVisibility(View.GONE);
                }

                return row;
            }
            return null;
        }

        @Override
        public int getCount() {
            if (data != null)
                return data.size();

            return 0;
        }
    }

    static class ContactViewsHolder {
        QuickContactBadge quickContactBadge;
        TextView nameNumber;
        TextView dateStatus;
        TextView lastMessage;
        TextView unseenTicker;
    }

    public static final int EVENT_DELETE_CLICKED = 3;
    public static final int EVENT_CALL_CLICKED = 4;

    private int lastSelectedPosition = -1;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu_thread, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        lastSelectedPosition = info.position;

        switch (item.getItemId()) {
            case R.id.delete:
                listener.eventOccurred(EVENT_DELETE_CLICKED, null);
                return true;
            case R.id.call:
                listener.eventOccurred(EVENT_CALL_CLICKED, info.position);
                break;
            case R.id.view:
                ThreadEntity th1 = items.get(info.position);
                Contact c1 = th1.getContact();

                if (!c1.viewDetails(getActivity())) {
                    Toast.makeText(getActivity(), R.string.contact_not_found, Toast.LENGTH_SHORT).show();
                }
                break;

        }

        return super.onContextItemSelected(item);
    }

    public void performCall(int position) {
        ThreadEntity th = items.get(position);
        Contact c = th.getContact();
        c.call(getActivity());
    }

    @Override
    protected ArrayList<ThreadEntity> onLoaderTaskDoInBackground() {
        SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(getActivity());
        ArrayList<ThreadEntity> result = helper.Thread_getAllThreadsFilled(true);

        for (ThreadEntity t : result) {
            t.getContact().loadContactBasicContractInformation(getActivity());
            helper.Thread_loadBasicThreadMessagesInformation(t);
        }

        Collections.sort(result);

        return result;
    }

    @Override
    protected void onItemClicked(int index) {
        ThreadEntity selected = items.get(index);

        if (selected != null) {
            listener.eventOccurred(EVENT_ITEM_CLICKED, selected);
        }
    }

    @Override
    protected CharSequence getEmptyText() {
        CharSequence resource = getActivity().getResources().getText(R.string.main_click_plus_to_start);
        Smiley s = new Smiley(R.drawable.ic_action_new, '\ucccc', "'+'");

        return s.replacePatternsWithImage(resource, -1.0f, getActivity());
    }

    @Override
    protected ArrayAdapter<ThreadEntity> instantiateArrayAdapter() {
        if (items == null)
            return null;

        return new ThreadsAdapter(getActivity(), R.layout.list_item_contact, items);
    }

    public void handleReceivedMessage(ReceivedMessage message) {
        ThreadEntity t = new ThreadEntity();
        t.setId(message.getThreadId());

        if (items != null) {
            int index = items.indexOf(t);

            if (index >= 0) {
                ThreadEntity selected = items.get(index);
                selected.setUnSeenMessagesCount(selected.getUnSeenMessagesCount() + 1);
                selected.setMostRecentMessage(message);
            } else {
                SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(getActivity());
                Contact c = helper.Contact_getContactById(t.getContactId());

                if (c != null)
                    c.loadContactBasicContractInformation(getActivity());

                t.setUnSeenMessagesCount(t.getUnSeenMessagesCount() + 1);
                t.setMostRecentMessage(message);

                if (adapter != null) {
                    adapter.insert(t, 0);
                }
            }

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

        }
    }

    public void confirmDeleteThread() {
        if (lastSelectedPosition >= 0) {
            ThreadEntity selected = items.get(lastSelectedPosition);

            if (selected != null) {
                // TODO: Make thread deletion be done by a thread
                SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(getActivity());
                helper.Thread_delete(selected);

                if (adapter != null) {
                    adapter.remove(selected);
                    adapter.notifyDataSetChanged();
                }

            }

            lastSelectedPosition = -1;
        }
    }
}
