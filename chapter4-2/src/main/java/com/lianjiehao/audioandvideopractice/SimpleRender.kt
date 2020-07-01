package com.lianjiehao.audioandvideopractice

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.lianjiehao.audioandvideopractice.drawer.VideoDrawer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SimpleRender(private val drawer: VideoDrawer) : GLSurfaceView.Renderer {
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        drawer.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        drawer.setWordSize(width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1f, 1f, 1f, 0f)
        drawer.createProgram()
    }
}
