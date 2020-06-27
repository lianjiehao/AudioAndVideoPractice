package com.lianjiehao.audioandvideopractice.drawer

import android.opengl.GLES20
import com.lianjiehao.audioandvideopractice.utils.ShaderHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class Square {
    private val vertexShaderCode: String =
        "attribute vec4 aPosition;" +
                "void main() {" +
                "  gl_Position = aPosition;" +
                "}"

    private val fragmentShaderCode: String =
        "precision mediump float;" +
                "void main() {" +
                "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" +
                "}"

    // 顶点坐标
    private val vertexCoors = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )
    private lateinit var vertexBuffer: FloatBuffer

    private var vertexShader = 0
    private var fragmentShader = 0
    private var program = 0

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
    }

    private fun initializeProgram() {
        vertexShader = ShaderHelper.compileVetexShader(vertexShaderCode)
        fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderCode)
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader)
        ShaderHelper.validateProgram(program)
    }

    fun draw() {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        //设置着色器参数， 第二个参数表示一个顶点包含的数据数量，这里为xy，所以为2
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        //释放资源
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDeleteProgram(program)
    }


}