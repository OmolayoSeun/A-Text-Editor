package com.omolayoseun.atexteditor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.Fragment;

@SuppressWarnings("deprecation")
public class EditTextFragment extends Fragment {

    View view;
    EditText etV;
    private StringBuilder str;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.edit_text_fragment, container, false);
         etV = view.findViewById(R.id.etV);
        etV.setText(str);
        return view;
    }
    public void setString(StringBuilder str){
        this.str = str;
    }
    public String getString(){
        return etV.getText().toString();
    }

    public  void refresh(StringBuilder builder){
        str = builder;
        etV.setText(builder);
    }
}
