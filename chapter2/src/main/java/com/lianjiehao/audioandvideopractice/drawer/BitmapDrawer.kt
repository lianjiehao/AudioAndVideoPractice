package com.lianjiehao.audioandvideopractice.drawer

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.lianjiehao.audioandvideopractice.utils.OpenglHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class BitmapDrawer(val textureId: Int, val bitmap: Bitmap) {
    private val vertexShaderCode: String =
        //顶点坐标
        "attribute vec4 aPosition;" +
                //纹理坐标
                "attribute vec2 aCoordinate;" +
                //用于传递纹理坐标给片元着色器，命名和片元着色器中的一致
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  gl_Position = aPosition;" +
                "  vCoordinate = aCoordinate;" +
                "}"

    private val fragmentShaderCode: String =
        //配置float精度，使用了float数据一定要配置：lowp(低)/mediump(中)/highp(高)
        "precision mediump float;" +
                //从Java传递进入来的纹理单元
                "uniform sampler2D uTexture;" +
                //从顶点着色器传递进来的纹理坐标
                "varying vec2 vCoordinate;" +
                "void main() {" +
                //根据纹理坐标，从纹理单元中取色
                "  vec4 color = texture2D(uTexture, vCoordinate);" +
                "  gl_FragColor = color;" +
                "}"

    // 顶点坐标
    private val vertexCoors = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

    // 纹理坐标
    private val textureCoors = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureBuffer: FloatBuffer


    private var vertexShader = 0
    private var fragmentShader = 0
    private var program = 0

    private var positionHandle = 0
    private var coordinateHandle = 0
    private var textureHandle = 0


    init {
        initializeBuffers()
        initializeProgram()
    }

    private fun initializeBuffers() {
        var buff: ByteBuffer = ByteBuffer.allocateDirect(vertexCoors.size * 4)
        buff.order(ByteOrder.nativeOrder())
        vertexBuffer = buff.asFloatBuffer()
        vertexBuffer.put(vertexCoors)
        vertexBuffer.position(0)

        buff = ByteBuffer.allocateDirect(textureCoors.size * 4)
        buff.order(ByteOrder.nativeOrder())
        textureBuffer = buff.asFloatBuffer()
        textureBuffer.put(textureCoors)
        textureBuffer.position(0)
    }

    private fun initializeProgram() {
        vertexShader = OpenglHelper.compileVetexShader(vertexShaderCode)
        fragmentShader = OpenglHelper.compileFragmentShader(fragmentShaderCode)
        program = OpenglHelper.linkProgram(vertexShader, fragmentShader)
        OpenglHelper.validateProgram(program)
    }

    /**
     * 激活并绑定纹理
     */
    private fun enableAndBindTexture() {
        //激活指定纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //绑定纹理ID到纹理单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        //将激活的纹理单元传递到着色器里面
        GLES20.glUniform1i(textureHandle, 0)
        //配置边缘过渡参数
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        //绑定纹理
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
    }

    fun draw() {
        GLES20.glUseProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        //设置着色器参数， 第二个参数表示一个顶点包含的数据数量，这里为xy，所以为2
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        coordinateHandle = GLES20.glGetAttribLocation(program, "aCoordinate")
        GLES20.glEnableVertexAttribArray(coordinateHandle)
        GLES20.glVertexAttribPointer(coordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)

        textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        enableAndBindTexture()

        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        //释放资源
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(coordinateHandle)
        GLES20.glDisableVertexAttribArray(textureHandle)
        GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
        GLES20.glDeleteProgram(program)
    }

}