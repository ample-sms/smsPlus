package ghareeb.smsplus.fragments;

import ghareeb.smsplus.R;
import ghareeb.smsplus.asynctasks.FilterAsyncTask;
import ghareeb.smsplus.asynctasks.helper.TaskListener;
import ghareeb.smsplus.common.ContactListSection;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.Contact;
import ghareeb.smsplus.fragments.parents.Fragment_LoaderList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.QuickContactBadge;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * This fragment manages the list of registered contacts, and provides the facility to refresh it
 * manually.
 */
public class Fragment_RegisteredNumbersList extends Fragment_LoaderList<Contact> {
    class ContactsAdapter extends ArrayAdapter<Contact> implements SectionIndexer {
        private Context context;
        private int layoutResourceId;
        private ArrayList<Contact> data = null;

        private static final int TYPE_SECTION_HEADER = 0;
        private static final int TYPE_LIST_ITEM = 1;

        ContactsAdapter(Context context, int layoutResourceId, ArrayList<Contact> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (data != null) {
                Contact item = data.get(position);

                if (item instanceof ContactListSection) {
                    return TYPE_SECTION_HEADER;
                } else {
                    return TYPE_LIST_ITEM;
                }
            }

            return 0;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            ContactViewsHolder holder;
            Contact contact = data.get(position);

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                holder = new ContactViewsHolder();

                if (contact instanceof ContactListSection) {
                    row = inflater.inflate(R.layout.list_item_section_header, parent, false);
                    holder.nameNumber = row.findViewById(R.id.list_header_title);
                    row.setFocusable(true);
                } else {
                    row = inflater.inflate(layoutResourceId, parent, false);

                    holder.quickContactBadge = row.findViewById(R.id.quickcontact);
                    holder.nameNumber = row.findViewById(R.id.nameNumberTV);
                    holder.number = row.findViewById(R.id.numberTV);
                    row.setFocusable(false);
                }

                row.setTag(holder);
            } else {
                holder = (ContactViewsHolder) row.getTag();
            }

            if (!(contact instanceof ContactListSection)) {
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

                holder.number.setText(contact.getNumber().toString());
            } else {
                holder.nameNumber.setText(contact.getName());
            }

            return row;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            ContactListSection section = (ContactListSection) sectionHeaders[sectionIndex];
            return hashMap.get(section);
        }

        @Override
        public int getSectionForPosition(int position) {
            for (int i = sectionHeaders.length - 1; i >= 0; i--) {
                if (position >= hashMap.get(sectionHeaders[i])) {
                    return i;
                }
            }
            return 0;
        }

        @Override
        public Object[] getSections() {
            return sectionHeaders;
        }
    }

    static class ContactViewsHolder {
        QuickContactBadge quickContactBadge;
        TextView nameNumber;
        TextView number;
    }

    public static final int EVENT_REFRESHING_STARTED = 3;
    public static final int EVENT_REFRESHING_FINISHED = 4;

    private HashMap<Contact, Integer> hashMap = new HashMap<>();
    private Contact[] sectionHeaders;

    public void startFiltering(String selfNumber) {
        int permission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_CONTACTS);

        if(permission == PackageManager.PERMISSION_GRANTED) {
            TaskListener<ArrayList<Contact>> refreshTaskListener = new TaskListener<ArrayList<Contact>>() {
                @Override
                public void onTaskStarted() {
                    setListShown(false);
                    listener.eventOccurred(EVENT_REFRESHING_STARTED, null);
                }

                @Override
                public void onTaskFinished(ArrayList<Contact> result) {
                    try {
                        setListShown(true);

                        if (result != null) {
                            items = result;
                            addSectionHeadersToList(result);
                            adapter = new ContactsAdapter(getActivity(), R.layout.list_item_pick_contact, items);
                            setListAdapter(adapter);
                            listener.eventOccurred(EVENT_REFRESHING_FINISHED, true);
                        } else {
                            listener.eventOccurred(EVENT_REFRESHING_FINISHED, false);
                        }
                    } catch (Exception e) {
                        Log.e(Fragment_RegisteredNumbersList.class.getName(), e.getMessage());
                    }
                }
            };
            FilterAsyncTask refresherTask = new FilterAsyncTask(refreshTaskListener, selfNumber, true);
            refresherTask.execute(getActivity());
        } else {
            Log.e(Fragment_RegisteredNumbersList.class.getName(), "No permission to read contacts");
        }
    }

    @Override
    protected ArrayList<Contact> onLoaderTaskDoInBackground() {
        int permission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_CONTACTS);

        if(permission == PackageManager.PERMISSION_GRANTED) {

            ArrayList<Contact> result;
            SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(getActivity());
            result = helper.Contact_getAllRegisteredContacts();

            for (Contact c : result)
                c.loadContactBasicContractInformation(getActivity());

            addSectionHeadersToList(result);
            helper.close();

            return result;
        } else {
            Log.e(Fragment_RegisteredNumbersList.class.getName(), "No permission to read contacts");
            return new ArrayList<>();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getListView() != null)
            getListView().setFastScrollEnabled(true);
    }

    @Override
    protected void onItemClicked(int index) {
        try {
            Contact sel = items.get(index);

            if (!(sel instanceof ContactListSection))
                listener.eventOccurred(EVENT_ITEM_CLICKED, sel.getNumber().toString());
        } catch (Exception e) {
            Log.e(Fragment_RegisteredNumbersList.class.getName(), e.getMessage());
        }
    }

    @Override
    protected CharSequence getEmptyText() {
        return getActivity().getResources().getText(R.string.recepient_no_contacts);
    }

    @Override
    protected ArrayAdapter<Contact> instantiateArrayAdapter() {
        if (items == null)
            return null;

        return new ContactsAdapter(getActivity(), R.layout.list_item_pick_contact, items);
    }

    private void addSectionHeadersToList(ArrayList<Contact> list) {
        final char SYMBOL_HEADER = '#';
        ArrayList<Character> headers = new ArrayList<>();
        char current;
        ContactListSection currentSection;

        for (Contact c : list) {
            current = Character.toUpperCase(c.toString().charAt(0));

            if ((current >= '0' && current <= '9') || current == '+') {
                current = SYMBOL_HEADER;
            }

            if (!headers.contains(current))
                headers.add(current);
        }

//		 for (int i = 0; i < 100; i++)
//		 {
//		 Contact temp = new Contact();
//		 temp.setName("Ahmag");
//		 list.add(temp);
//		 }

        for (Character c : headers) {
            currentSection = new ContactListSection();
            currentSection.setName(String.valueOf(c));
            list.add(currentSection);
        }

        Collections.sort(headers);
        Collections.sort(list);
        sectionHeaders = new Contact[headers.size()];
        int sectionsCounter = 0;

        for (int i = 0; i < list.size(); i++)
            if (list.get(i) instanceof ContactListSection) {
                hashMap.put(list.get(i), i);
                sectionHeaders[sectionsCounter++] = list.get(i);
            }

    }
}
