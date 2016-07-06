package org.buildmlearn.dictation.fragment;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.buildmlearn.dictation.Constants;
import org.buildmlearn.dictation.R;
import org.buildmlearn.dictation.adapter.DictArrayAdapter;
import org.buildmlearn.dictation.data.DataUtils;
import org.buildmlearn.dictation.data.DictContract;
import org.buildmlearn.dictation.data.DictDb;
import org.buildmlearn.dictation.data.DictModel;

import java.util.ArrayList;

/**
 * Created by Anupam (opticod) on 4/7/16.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String SELECTED_KEY = "selected_position";
    private static final int DICT_LOADER = 0;

    private DictArrayAdapter dictListAdapter;
    private int mPosition = ListView.INVALID_POSITION;
    private ListView listView;
    private ArrayList<DictModel> dictList;
    private View rootView;
    private DictDb db;

    public MainActivityFragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("dictList", dictList);
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("dictList")) {
            dictList = new ArrayList<>();
        } else {
            dictList = savedInstanceState.getParcelableArrayList("dictList");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        dictListAdapter =
                new DictArrayAdapter(
                        getActivity());

        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.card_toolbar);
        toolbar.setTitle("List of Passages :");

        Toolbar maintoolbar = (Toolbar) rootView.findViewById(R.id.toolbar_main);
        final String result[] = DataUtils.readTitleAuthor(getContext());
        maintoolbar.setTitle(result[0]);
        maintoolbar.inflateMenu(R.menu.menu);
        maintoolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_about:
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(getActivity());
                        builder.setTitle(String.format("%1$s", getString(R.string.about_us)));
                        builder.setMessage(getResources().getText(R.string.about_text));
                        builder.setPositiveButton("OK", null);
                        AlertDialog welcomeAlert = builder.create();
                        welcomeAlert.show();
                        assert ((TextView) welcomeAlert.findViewById(android.R.id.message)) != null;
                        ((TextView) welcomeAlert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                        break;
                    default: //do nothing
                        break;
                }
                return true;
            }
        });

        listView = (ListView) rootView.findViewById(R.id.list_view_dict);
        listView.setAdapter(dictListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(cursor.getString(Constants.COL_ID));
                }
                mPosition = position;
            }
        });

        db = new DictDb(getContext());
        db.open();

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        SharedPreferences prefs = getActivity().getSharedPreferences("Radio", getContext().MODE_PRIVATE);
        int pos = prefs.getInt("radio_b", 1);
        RadioGroup rg = (RadioGroup) rootView.findViewById(R.id.radio_group);
        rg.check(rg.getChildAt(pos).getId());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DICT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String sortOrder = DictContract.Dict._ID + " ASC";

        return new CursorLoader(getActivity(), null, Constants.DICT_COLUMNS, null, null, sortOrder) {
            @Override
            public Cursor loadInBackground() {
                return db.getDictsCursor();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        dictListAdapter.swapCursor(cursor);

        rootView.findViewById(R.id.radioButton1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("Radio", getContext().MODE_PRIVATE).edit();
                editor.putInt("radio_b", 0);
                editor.apply();
            }
        });

        rootView.findViewById(R.id.radioButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("Radio", getContext().MODE_PRIVATE).edit();
                editor.putInt("radio_b", 1);
                editor.apply();
            }
        });

        rootView.findViewById(R.id.radioButton3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("Radio", getContext().MODE_PRIVATE).edit();
                editor.putInt("radio_b", 2);
                editor.apply();
            }
        });

        if (mPosition != ListView.INVALID_POSITION) {
            listView.smoothScrollToPosition(mPosition);
        }
        try {
            TextView info = (TextView) rootView.findViewById(R.id.empty);
            if (dictListAdapter.getCount() == 0) {
                info.setText(R.string.list_empty);
                info.setVisibility(View.VISIBLE);
            } else {
                info.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        dictListAdapter.swapCursor(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        db.close();
    }

    public interface Callback {
        void onItemSelected(String dictUri);
    }
}
