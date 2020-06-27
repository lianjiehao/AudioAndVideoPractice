package com.lianjiehao.audioandvideopractice

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.lianjiehao.audioandvideopractice.drawer.BitmapDrawer
import com.lianjiehao.audioandvideopractice.utils.OpenglHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(val bitmap: Bitmap) : GLSurfaceView.Renderer {
    private lateinit var bitmapDrawer: BitmapDrawer
    override fun onDrawFrame(gl: GL10?) {
        bitmapDrawer.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        bitmapDrawer.worldWidth = width
        bitmapDrawer.worldHeight = height
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val textureId = OpenglHelper.createTextureIds(1)[0]
        bitmapDrawer = BitmapDrawer(textureId, bitmap)
        bitmapDrawer.imageWidth = bitmap.width
        bitmapDrawer.imageHeight = bitmap.height
    }

}
