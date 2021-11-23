package com.mampod.routerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.mampod.apt_annotation.BindView;
import com.mampod.apt_library.BindViewTools;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.tv)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindViewTools.bind(this);
        textView.setText("apt实现butterknife成功");
    }
}