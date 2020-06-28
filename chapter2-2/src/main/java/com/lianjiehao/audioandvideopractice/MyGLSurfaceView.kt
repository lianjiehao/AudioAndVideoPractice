package com.lianjiehao.audioandvideopractice

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer

    init {
        setEGLContextClientVersion(2)
        val bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.image)
        renderer = MyGLRenderer(bitmap)
        setRenderer(renderer)
        // 渲染模式设置为："仅当绘制数据发生变化时绘制视图"。
        renderMode = RENDERMODE_WHEN_DIRTY
    }

}
