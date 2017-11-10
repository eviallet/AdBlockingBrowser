package com.gueg.browser.web.bookmarks;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.gueg.browser.R;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogEngine;


public class BookmarkDialog extends DialogFragment {

    private BlurDialogEngine mBlurEngine;

    private BookmarkInterface _listener;
    View rootView;

    EditText title;
    EditText url;

    Bookmark b;
    boolean modif;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Light_Dialog_Alert);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_bookmark, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        mBlurEngine = new BlurDialogEngine(getActivity());
        mBlurEngine.setBlurRadius(8);
        mBlurEngine.setDownScaleFactor(8f);
        mBlurEngine.debug(false);
        mBlurEngine.setBlurActionBar(true);
        mBlurEngine.setUseRenderScript(true);


        title = rootView.findViewById(R.id.editTextTitle);
        title.setText(b.getName());
        url = rootView.findViewById(R.id.editTextUrl);
        url.setText(b.getUrl());

        if(modif) {
            Button remove = rootView.findViewById(R.id.btn_bookmarkActivity_remove);
            remove.setVisibility(View.VISIBLE);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _listener.onBookmarkRemoved();
                    dismiss();
                }
            });
        }

        rootView.findViewById(R.id.btn_bookmarkActivity_annuler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        rootView.findViewById(R.id.btn_bookmarkActivity_valider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!title.getText().toString().isEmpty()&&!url.getText().toString().isEmpty()) {
                    _listener.onBookmarkAdded(new Bookmark(title.getText().toString(),url.getText().toString(),b.getPic()));
                    dismiss();
                } else
                    Toast.makeText(getActivity(), "Entrer un titre et une url", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    public void setListener(BookmarkInterface listener) {
        _listener = listener;
    }

    public void setBookmark(Bookmark b, boolean modif) {
        this.b = b;
        this.modif = modif;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBlurEngine.onResume(getRetainInstance());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        _listener.onCancel();
        mBlurEngine.onDismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBlurEngine.onDetach();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
