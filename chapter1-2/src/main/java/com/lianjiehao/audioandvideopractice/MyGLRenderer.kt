package com.lianjiehao.audioandvideopractice

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.lianjiehao.audioandvideopractice.shape.Square
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {
    private lateinit var square: Square
    override fun onDrawFrame(gl: GL10?) {
        square.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        square = Square()
    }

}
