package com.lianjiehao.audioandvideopractice

import android.content.Context
import android.opengl.GLSurfaceView

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer()
        setRenderer(renderer)
        // 渲染模式设置为："仅当绘制数据发生变化时绘制视图"。
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}
