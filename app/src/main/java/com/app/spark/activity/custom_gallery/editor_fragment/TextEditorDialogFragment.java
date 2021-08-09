package com.app.spark.activity.custom_gallery.editor_fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.spark.R;
import com.app.spark.activity.custom_gallery.adapter.ColorPickerAdapter;
import com.app.spark.activity.custom_gallery.adapter.FontPickerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TextEditorDialogFragment extends DialogFragment {

    public static final String TAG = TextEditorDialogFragment.class.getSimpleName();
    public static final String EXTRA_INPUT_TEXT = "extra_input_text";
    public static final String EXTRA_COLOR_CODE = "extra_color_code";
    public static final String SELECTED_POSITION = "extra_selected_position";
    private EditText mAddTextEditText;
    private ImageView mAddTextDoneTextView;
    private InputMethodManager mInputMethodManager;
    private static List<String> fontNames;
    private static List<Integer> fontIds;
    private int mColorCode;
    private static int position = 0;
    private TextEditor mTextEditor;

    public interface TextEditor {
        void onDone(String inputText, int colorCode, int position);
    }


    //Show dialog with provide text and text color
    public static TextEditorDialogFragment show(@NonNull AppCompatActivity appCompatActivity,
                                                @NonNull String inputText,
                                                @ColorInt int colorCode, int position) {
        Bundle args = new Bundle();
        args.putString(EXTRA_INPUT_TEXT, inputText);
        args.putInt(EXTRA_COLOR_CODE, colorCode);
        args.putInt(SELECTED_POSITION, position);
        TextEditorDialogFragment fragment = new TextEditorDialogFragment();
        fragment.setArguments(args);
        fragment.show(appCompatActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    //Show dialog with default text input as empty and text color white
    public static TextEditorDialogFragment show(@NonNull AppCompatActivity appCompatActivity,int position) {
        return show(appCompatActivity,
                "", ContextCompat.getColor(appCompatActivity, R.color.white), position);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        //Make dialog full screen with transparent background
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_text_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAddTextEditText = view.findViewById(R.id.add_text_edit_text);
        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mAddTextDoneTextView = view.findViewById(R.id.add_text_done_tv);

        //Setup the color picker for text color
        RecyclerView addTextColorPickerRecyclerView = view.findViewById(R.id.add_text_color_picker_recycler_view);
        RecyclerView reyFonts = view.findViewById(R.id.reyFonts);
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 10);
        LinearLayoutManager layoutManagerFonts = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        addTextColorPickerRecyclerView.setLayoutManager(layoutManager);
        reyFonts.setLayoutManager(layoutManagerFonts);
        addTextColorPickerRecyclerView.setHasFixedSize(true);
        reyFonts.setHasFixedSize(true);
        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(getActivity());
        //This listener will change the text color when clicked on any color from picker
        colorPickerAdapter.setOnColorPickerClickListener(new ColorPickerAdapter.OnColorPickerClickListener() {
            @Override
            public void onColorPickerClickListener(int colorCode) {
                mColorCode = colorCode;
                mAddTextEditText.setTextColor(colorCode);
            }
        });
        addTextColorPickerRecyclerView.setAdapter(colorPickerAdapter);
        position = getArguments().getInt(SELECTED_POSITION);
        final FontPickerAdapter fontPickerAdapter = new FontPickerAdapter(getActivity(), position, getDefaultFonts(getActivity()), getDefaultFontIds(getActivity()));
        fontPickerAdapter.setOnFontSelectListener(new FontPickerAdapter.OnFontSelectListner() {
            @Override
            public void onFontSelcetion(int position) {
                TextEditorDialogFragment.this.position = position;
                Typeface typeface = ResourcesCompat.getFont(getActivity(), fontIds.get(position));
                mAddTextEditText.setTypeface(typeface);
            }
        });

        reyFonts.setAdapter(fontPickerAdapter);

        mAddTextEditText.setText(getArguments().getString(EXTRA_INPUT_TEXT));
        mColorCode = getArguments().getInt(EXTRA_COLOR_CODE);

        mAddTextEditText.setTextColor(mColorCode);
        Typeface typeface = ResourcesCompat.getFont(getActivity(), fontIds.get(position));
        mAddTextEditText.setTypeface(typeface);

        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        //Make a callback on activity when user is done with text editing
        mAddTextDoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                dismiss();
                String inputText = mAddTextEditText.getText().toString();
                if (!TextUtils.isEmpty(inputText) && mTextEditor != null) {
                    mTextEditor.onDone(inputText, mColorCode, fontPickerAdapter.getSelecetedPosition());
                }
            }
        });
        view.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                dismiss();
            }
        });
        reyFonts.setVisibility(View.GONE);
        addTextColorPickerRecyclerView.setVisibility(View.GONE);
        view.findViewById(R.id.ivColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAddTextEditText.setFocusable(false);
                reyFonts.setVisibility(View.GONE);
                addTextColorPickerRecyclerView.setVisibility(View.VISIBLE);
                InputMethodManager imm =(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
        view.findViewById(R.id.ivFont).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAddTextEditText.setFocusable(false);
                reyFonts.setVisibility(View.VISIBLE);
                addTextColorPickerRecyclerView.setVisibility(View.GONE);
                InputMethodManager imm =(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
        view.findViewById(R.id.ivKeypad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reyFonts.setVisibility(View.GONE);
                addTextColorPickerRecyclerView.setVisibility(View.GONE);
                mAddTextEditText.setEnabled(true);
                mAddTextEditText.setFocusable(true);
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mAddTextEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        mAddTextEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange:Cntd:- "+hasFocus);
                if (!hasFocus) {
                    reyFonts.setVisibility(View.GONE);
                    addTextColorPickerRecyclerView.setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mAddTextEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    public static List<Integer> getDefaultFontIds(Context context) {
        fontIds = new ArrayList<>();
        fontIds.add(R.font.constantia);
        fontIds.add(R.font.pristina_regular);
        fontIds.add(R.font.sourcesansproblack);
        fontIds.add(R.font.sourcesansproextralight);
        fontIds.add(R.font.sourcesansprosegular);
        fontIds.add(R.font.roboto_medium_numbers);
        fontIds.add(R.font.sourcesansprobold);
        return fontIds;
    }

    public static List<String> getDefaultFonts(Context context) {
        fontNames = new ArrayList<>();
        fontNames.add("Wonderland");
        fontNames.add("Cinzel");
        fontNames.add("Emojione");
        fontNames.add("Josefinsans");
        fontNames.add("Merriweather");
        fontNames.add("Raleway");
        fontNames.add("Roboto");
        return fontNames;
    }

    //Callback to listener if user is done with text editing
    public void setOnTextEditorListener(TextEditor textEditor) {
        mTextEditor = textEditor;
    }
}
