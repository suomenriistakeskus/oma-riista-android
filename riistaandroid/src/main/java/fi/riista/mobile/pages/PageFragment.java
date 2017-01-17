package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.context.WorkContextProvider;

public abstract class PageFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public PageFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    public void setViewTitle(String title) {
        if (mListener != null) {
            mListener.setCustomTitle(title);
        }
    }

    public WorkContext getWorkContext() {
        WorkContext workContext = null;
        Activity activity = getActivity();
        if (activity instanceof WorkContextProvider) {
            workContext = ((WorkContextProvider) activity).getWorkContext();
        }
        return workContext;
    }

    public interface OnFragmentInteractionListener {
        void setCustomTitle(String title);
    }
}
