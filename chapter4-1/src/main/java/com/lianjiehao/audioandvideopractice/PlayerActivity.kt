package com.lianjiehao.audioandvideopractice

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_player.*
import java.util.concurrent.Executors


class PlayerActivity : AppCompatActivity() {
    private var videoDecoder: BaseDecoder? = null
    private var audioDecoder: BaseDecoder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                logError("thread=onSurfaceTextureSizeChanged")
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                logError("thread=onSurfaceTextureUpdated")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                logError("thread=onSurfaceTextureDestroyed")
                return true
            }

            override fun onSurfaceTextureAvailable(
                sf: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                logError("thread=onSurfaceTextureAvailable")
                play(Surface(sf))
            }
        }
    }

    private fun play(surface: Surface) {
        val path =
            "http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4"

        //创建线程池
        val threadPool = Executors.newFixedThreadPool(2)

        //创建视频解码器
        videoDecoder = VideoDecoder(path, surface)
        threadPool.execute(videoDecoder)

        //创建音频解码器
        audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        //开启播放
        videoDecoder!!.goOn()
        audioDecoder!!.goOn()
    }

    override fun onDestroy() {
        audioDecoder?.release()
        videoDecoder?.release()
        super.onDestroy()
    }

}
