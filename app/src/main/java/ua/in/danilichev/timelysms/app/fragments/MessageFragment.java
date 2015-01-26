package ua.in.danilichev.timelysms.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import ua.in.danilichev.timelysms.app.R;

public class MessageFragment extends Fragment {

    EditText etMessage;

    private final String MESSAGE_KEY = "message";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        if (view == null) return null;
        etMessage = (EditText) view.findViewById(R.id.editTextMessage);

        if (savedInstanceState != null) {
            setMessage(savedInstanceState.getString(MESSAGE_KEY));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(MESSAGE_KEY, getMessage());
    }

    public String getMessage() {
        return etMessage.getText() == null ? "" : etMessage.getText().toString();
    }

    public void setMessage(String message) {
        etMessage.setText(message);
    }
}
