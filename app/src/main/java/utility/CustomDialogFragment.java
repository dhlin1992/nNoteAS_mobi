package utility;

import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ntx.note.Global;

public class CustomDialogFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    public void dismiss() {
        getActivity().getFragmentManager().beginTransaction().remove(this).commit();

        //Dylan : Prevent after kill process, fragment will generate afterimage
        final TransparentDialog transparentDialog = new TransparentDialog(getActivity());
        transparentDialog.setCanceledOnTouchOutside(true);
        transparentDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                transparentDialog.dismiss();
            }
        }, 300);
        Global.refresh(getActivity());
    }
}
