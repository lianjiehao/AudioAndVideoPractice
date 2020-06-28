package com.lianjiehao.audioandvideopractice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OpenGLES20Activity : AppCompatActivity() {
    lateinit var glSurfaceView: MyGLSurfaceView
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = MyGLSurfaceView(this)
        setContentView(glSurfaceView)
    }

    override fun onDestroy() {
        super.onDestroy()
        glSurfaceView.release()
    }
}
