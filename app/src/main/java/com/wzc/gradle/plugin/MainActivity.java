package com.wzc.gradle.plugin;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wzc.test.Test1;
import com.wzc.test_library.Test2;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Test test = new Test();
        Test1 test1 = new Test1();
        Test2 test2 = new Test2();
        test.test();
        TextView textView = findViewById(R.id.text);
        textView.setText(" text=" +test + " text1=" + test1 + " test2=" + test2);
    }
}