package com.lianjiehao.audioandvideopractice

import android.content.Context
import android.opengl.GLSurfaceView

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer(context)
        setRenderer(renderer)
    }

    fun release() {
        renderer.release()
    }
}
