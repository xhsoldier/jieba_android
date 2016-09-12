package com.analysis.jieba_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.analysis.jieba.JiebaSegmenter;
import com.analysis.jieba.WordDictionary;


public class MainActivity extends AppCompatActivity {

    //todo 启动需要很长时间,大概1分钟.基于高通820
    static JiebaSegmenter segmenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        segmenter = new JiebaSegmenter();
        testDemo();
    }

    public void testDemo() {

        String[] sentences =
                new String[] {"天山会议室在哪里?","徐海的座位在哪?"};


        for (String sentence : sentences) {
            WordDictionary dic = WordDictionary.getInstance();
            double fre = dic.getFreq("天山");

            String temp = segmenter.process(sentence, JiebaSegmenter.SegMode.INDEX).toString();
            Log.e("Segment:", temp);

            String se  = segmenter.sentenceProcess(sentence).toString();
            Log.e("se:", se);


        }
    }
}
