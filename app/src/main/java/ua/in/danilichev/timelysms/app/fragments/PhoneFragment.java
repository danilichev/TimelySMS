package ua.in.danilichev.timelysms.app.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import ua.in.danilichev.timelysms.app.R;

import java.util.ArrayList;

public class PhoneFragment extends Fragment {

    EditText editTextPhoneNumber;
    Button buttonSelectPhoneNumber;
    TextView textViewNameOfContact;

    private ArrayList<DataOfContact> mDataOfContacts;

    private static final int PICK_CONTACT_REQUEST = 111;
    private final String NAME_OF_CONTACT_KEY = "name";
    private final String PHONE_NUMBER_KEY = "phoneNumber";
    private final String DATA_OF_CONTACTS_KEY = "dataOfContacts";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_phone, container, false);

        textViewNameOfContact = (TextView) view.findViewById(R.id.textViewNameOfContactInFragmentPhone);

        editTextPhoneNumber = (EditText) view.findViewById(R.id.editTextPhone);
        editTextPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ownerOfPhoneNumber = defineOwnerOfPhoneNumber(s.toString());
                textViewNameOfContact.setText(ownerOfPhoneNumber);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        buttonSelectPhoneNumber = (Button) view.findViewById(
                R.id.buttonSelectPhone);
        buttonSelectPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, Phone.CONTENT_URI);
                startActivityForResult(i, PICK_CONTACT_REQUEST);
            }
        });

        if (savedInstanceState != null) {
            mDataOfContacts = savedInstanceState
                    .getParcelableArrayList(DATA_OF_CONTACTS_KEY);
            setPhoneNumber(savedInstanceState.getString(PHONE_NUMBER_KEY));
            setNameOfContact(savedInstanceState.getString(NAME_OF_CONTACT_KEY));
        } else {
            mDataOfContacts = new ArrayList<DataOfContact>();
            retrieveAllPhoneNumbers();
        }

        return view;
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);

        if (reqCode != PICK_CONTACT_REQUEST || resCode != Activity.RESULT_OK) return;

        String contactId;

        Uri uri = data.getData();

        if (uri == null) return;

        contactId = uri.getLastPathSegment();

        Cursor cursor = getActivity().getContentResolver().query(
                Phone.CONTENT_URI,
                null,
                Phone._ID + "=?",
                new String[]{contactId},
                null);

        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }

        int phoneId = cursor.getColumnIndex(Phone.NUMBER);
        int nameOfContactId = cursor.getColumnIndex(Phone.DISPLAY_NAME);

        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                String phoneNumber = cursor.getString(phoneId);
                editTextPhoneNumber.setText(phoneNumber);
                String name = cursor.getString(nameOfContactId);
                textViewNameOfContact.setText(name);
            }
        }

        cursor.close();
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(DATA_OF_CONTACTS_KEY, mDataOfContacts);
        outState.putString(NAME_OF_CONTACT_KEY, getNameOfContact());
        outState.putString(PHONE_NUMBER_KEY, getPhoneNumber());
    }

    public String getNameOfContact() {
        CharSequence name = textViewNameOfContact.getText();
        return name == null ? "" : name.toString();
    }

    public void setNameOfContact(String nameOfContact) {
        textViewNameOfContact.setText(nameOfContact);
    }

    public String getPhoneNumber() {
        CharSequence phoneNumber = editTextPhoneNumber.getText();
        return phoneNumber == null ? "" : phoneNumber.toString();
    }

    public void setPhoneNumber(String phoneNumber) {
        editTextPhoneNumber.setText(phoneNumber);
    }

    private void retrieveAllPhoneNumbers() {
        ContentResolver resolver = getActivity().getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts.DISPLAY_NAME));

            int countOfPhoneNumbers = Integer.parseInt(cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

            if (countOfPhoneNumbers > 0) {
                Cursor cur = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[]{id}, null);

                while (cur.moveToNext()) {
                    String phoneNo = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    mDataOfContacts.add(new DataOfContact(name, phoneNo));
                }
                cur.close();
            }
        }

        cursor.close();
    }

    private String defineOwnerOfPhoneNumber(String phoneNumber) {
        for (DataOfContact dataOfContact : mDataOfContacts) {
            if (PhoneNumberUtils.compare(
                    dataOfContact.getPhoneNumber(), phoneNumber)) {
                return dataOfContact.getNameOfContact();
            }
        }
        return "";
    }


    
    private class DataOfContact implements Parcelable {
        private String nameOfContact;
        private String phoneNumber;

        public DataOfContact (String name, String phoneNumber) {
            this.nameOfContact = name;
            this.phoneNumber = phoneNumber;
        }
        
        public String getNameOfContact() { return nameOfContact; }
        
        public String getPhoneNumber() { return phoneNumber; }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(nameOfContact);
            parcel.writeString(phoneNumber);
        }

        public final Creator<DataOfContact> CREATOR =
                new Creator<DataOfContact>() {
                    @Override
                    public DataOfContact createFromParcel(Parcel source) {
                        return new DataOfContact(source);
                    }

                    @Override
                    public DataOfContact[] newArray(int size) {
                        return new DataOfContact[size];
                    }
                };

        private DataOfContact(Parcel parcel) {
            nameOfContact = parcel.readString();
            phoneNumber = parcel.readString();
        }
    }
}
