package hamouanis.stormy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by NGSi on 9/23/2017.
 */

public class AlertDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        // cause we're on a different class

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.error_title).setMessage(R.string.error_message).setPositiveButton(R.string.error_ok_buttom_text,null);

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
