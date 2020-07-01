package com.lianjiehao.audioandvideopractice

import android.os.Bundle
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.lianjiehao.audioandvideopractice.drawer.VideoDrawer
import com.lianjiehao.audioandvideopractice.media.AudioDecoder
import com.lianjiehao.audioandvideopractice.media.VideoDecoder
import kotlinx.android.synthetic.main.activity_opengles20.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class OpenGLES20Activity : AppCompatActivity() {
    private lateinit var drawer: VideoDrawer
    private lateinit var audioDecoder: AudioDecoder
    private lateinit var videoDecoder: VideoDecoder
    private lateinit var threadPool: ExecutorService

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opengles20)
        render()
    }

    private fun render() {
        drawer = VideoDrawer {
            play(Surface(it))
        }
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(SimpleRender(drawer))
    }

    private fun play(sf: Surface) {
        val path =
            "http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4"

        threadPool = Executors.newFixedThreadPool(2)

        videoDecoder = VideoDecoder(path, sf) { videoWidth, videoHeight ->
            drawer.setVideoSize(videoWidth, videoHeight)
        }
        threadPool.execute(videoDecoder)

        audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)
    }

    override fun onDestroy() {
        drawer.release()
        audioDecoder.release()
        videoDecoder.release()
        threadPool.shutdownNow()
        super.onDestroy()
    }
}
