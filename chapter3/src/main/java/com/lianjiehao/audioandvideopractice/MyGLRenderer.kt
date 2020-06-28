package com.lianjiehao.audioandvideopractice

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.lianjiehao.audioandvideopractice.drawer.GifDrawer
import com.lianjiehao.audioandvideopractice.utils.OpenglHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(val context: Context) : GLSurfaceView.Renderer {
    private lateinit var gifDrawer: GifDrawer
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        gifDrawer.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        gifDrawer.worldWidth = width
        gifDrawer.worldHeight = height
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1f, 1f, 1f, 0f)
        val textureId = OpenglHelper.createTextureIds(1)[0]
        gifDrawer = GifDrawer(textureId, context)
    }

    fun release() {
        gifDrawer.releaseGif()
    }

}
