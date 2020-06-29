package com.lianjiehao.chapter4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPlayer()
    }


    private fun initPlayer() {
        val path = Environment.getExternalStorageDirectory().absolutePath + "/hero.mp4"

        //创建线程池
        val threadPool = Executors.newFixedThreadPool(2)

        //创建视频解码器
        val videoDecoder = VideoDecoder(path, textureView, null)
        threadPool.execute(videoDecoder)

        //创建音频解码器
        val audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        //开启播放
        videoDecoder.goOn()
        audioDecoder.goOn()
    }
}
