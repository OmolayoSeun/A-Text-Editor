package com.omolayoseun.atexteditor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.Fragment;

@SuppressWarnings("deprecation")
public class TextViewFragment extends Fragment {
    View view;
    TextView txtV;
    private StringBuilder str;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.text_view_framelayout, container, false);
        txtV = view.findViewById(R.id.txtV);
        txtV.setText(str);
        return view;
    }
    public void setString(StringBuilder str){
        this.str = str;
    }


    public void refresh(StringBuilder builder) {
        str = builder;
        txtV.setText(builder);
    }
}
