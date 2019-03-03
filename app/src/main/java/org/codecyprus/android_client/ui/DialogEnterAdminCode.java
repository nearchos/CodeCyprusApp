package org.codecyprus.android_client.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.view.View;

import org.codecyprus.android_client.R;

/**
 * @author Nearchos
 * Created: 03-Mar-19
 */
public class DialogEnterAdminCode extends AlertDialog {

    public static final String TAG = "codecyprus";

    private Listener listener;

    public DialogEnterAdminCode(final Context context, final Listener listener) {
        super(context);

        assert listener != null;

        this.listener = listener;

        final View rootView = View.inflate(context, R.layout.fragment_dialog_enter_admin_code, null);
        setView(rootView);

        final TextInputEditText editText = rootView.findViewById(R.id.dialog_enter_admin_code_edit_text);
        editText.requestFocus();

        setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.OK), (dialog, which) -> {
            final String code = editText.getText() == null ? "" : editText.getText().toString();
            this.listener.enteredCode(code);
            dismiss();
        });

        setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.Cancel), (dialog, which) -> {
            dismiss();
        });
    }

    interface Listener {
        void enteredCode(String code);
    }
}