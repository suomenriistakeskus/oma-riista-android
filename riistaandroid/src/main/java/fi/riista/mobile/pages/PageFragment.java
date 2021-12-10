package fi.riista.mobile.pages;

import android.app.Activity;
import android.content.Context;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.context.WorkContextProvider;

public abstract class PageFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    /**
     * Is the PageFragment using custom actionBar i.e. has {@see #setupActionBar}
     * been called with a non-null layout?
     */
    private boolean mActionBarHasBeenSetupped = false;
    private boolean mHasOptionsMenu = false;
    private Integer mLayoutId = null;

    @Override
    public void onAttach(@NonNull Context context) {
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

    @Override
    public void onStart() {
        super.onStart();
        setupActionBar();
    }

    @Override
    public void onStop() {
        super.onStop();
        clearActionBar();
    }

    @Override
    public void setHasOptionsMenu(final boolean hasOptionsMenu) {
        this.mHasOptionsMenu = hasOptionsMenu;
        super.setHasOptionsMenu(hasOptionsMenu);
    }

    public void setViewTitle(@StringRes int titleResId) {
        setViewTitle(getString(titleResId));
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

    void setupActionBar(@Nullable Integer layoutId, boolean hasOptionsMenu) {
        mLayoutId = layoutId;
        mHasOptionsMenu = hasOptionsMenu;
    }

    private void setupActionBar() {
        if (mLayoutId != null) {
            AppCompatActivity activity = (AppCompatActivity) requireActivity();
            ActionBar actionBar = Objects.requireNonNull(activity.getSupportActionBar());

            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(mLayoutId);
            mActionBarHasBeenSetupped = true;
        }
        setHasOptionsMenu(mHasOptionsMenu);
    }

    private void clearActionBar() {
        // only clear actionbar if set by this fragment
        if (!mActionBarHasBeenSetupped) {
            return;
        }

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        Objects.requireNonNull(actionBar);

        actionBar.setDisplayOptions(0);
        actionBar.setDisplayShowCustomEnabled(false);
    }

    public interface OnFragmentInteractionListener {
        void setCustomTitle(String title);
    }
}
