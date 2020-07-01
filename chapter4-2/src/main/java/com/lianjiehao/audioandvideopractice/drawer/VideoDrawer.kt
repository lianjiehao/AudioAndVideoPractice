package com.lianjiehao.audioandvideopractice.drawer

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import com.lianjiehao.audioandvideopractice.utils.OpenglHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class VideoDrawer(val callback: (surfaceTexture: SurfaceTexture) -> Unit) {
    private val vertexShaderCode: String =
        "attribute vec4 aPosition;" +//顶点坐标
                "uniform mat4 uMatrix;" +
                "attribute vec2 aCoordinate;" +//纹理坐标
                "varying vec2 vCoordinate;" +//用于传递纹理坐标给片元着色器，命名和片元着色器中的一致
                "void main() {" +
                "  gl_Position = aPosition*uMatrix;" +
                "  vCoordinate = aCoordinate;" +
                "}"

    private val fragmentShaderCode: String =
        "#extension GL_OES_EGL_image_external : require\n" +//一定要加换行"\n"，否则会和下一行的precision混在一起，导致编译出错
                "precision mediump float;" + //配置float精度，使用了float数据一定要配置：lowp(低)/mediump(中)/highp(高)
                "varying vec2 vCoordinate;" +
                "uniform samplerExternalOES uTexture;" +
                "void main() {" +
                "  gl_FragColor=texture2D(uTexture, vCoordinate);" +
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
    private var vertexMatrixHandler: Int = -1

    private var worldWidth: Int = -1
    private var worldHeight: Int = -1
    private var videoWidth: Int = -1
    private var videoHeight: Int = -1

    private lateinit var surfaceTexture: SurfaceTexture
    private var textureId: Int = -1

    //计算坐标变换矩阵，防止图片拉伸。
    private var matrix: FloatArray = FloatArray(16)

    init {
        initializeVideo()
        initializeBuffers()
    }

    private fun initializeVideo() {
        textureId = OpenglHelper.createTextureIds(1)[0]
        surfaceTexture = SurfaceTexture(textureId)
        callback.invoke(surfaceTexture)
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


    fun createProgram() {
        vertexShader = OpenglHelper.compileVetexShader(vertexShaderCode)
        fragmentShader = OpenglHelper.compileFragmentShader(fragmentShaderCode)
        program = OpenglHelper.linkProgram(vertexShader, fragmentShader)
        OpenglHelper.validateProgram(program)
    }

    /**
     * 渲染纹理
     */
    private fun renderTexture() {
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        //激活指定纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //绑定纹理ID到纹理单元
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        //将激活的纹理单元传递到着色器里面
        GLES20.glUniform1i(textureHandle, 0)
        //配置边缘过渡参数
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        //更新纹理
        surfaceTexture?.updateTexImage()
    }

    /**
     * 绘制
     */
    fun draw() {
        GLES20.glUseProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        //设置着色器参数， 第二个参数表示一个顶点包含的数据数量，这里为xy，所以为2
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        coordinateHandle = GLES20.glGetAttribLocation(program, "aCoordinate")
        GLES20.glEnableVertexAttribArray(coordinateHandle)
        GLES20.glVertexAttribPointer(coordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)

        vertexMatrixHandler = GLES20.glGetUniformLocation(program, "uMatrix")
        GLES20.glEnableVertexAttribArray(vertexMatrixHandler)
        GLES20.glUniformMatrix4fv(vertexMatrixHandler, 1, false, matrix, 0)

        //渲染纹理
        renderTexture()

        //设置图片透明通道
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        //释放资源
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(coordinateHandle)
        GLES20.glDisableVertexAttribArray(textureHandle)
        GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
        GLES20.glDeleteProgram(program)
    }


    fun release() {
        surfaceTexture.release()
    }

    fun setWordSize(width: Int, height: Int) {
        worldWidth = width
        worldHeight = height
        matrix = getMatrix()
    }

    fun setVideoSize(width: Int, height: Int) {
        videoWidth = width
        videoHeight = height
        matrix = getMatrix()
    }

    private fun getMatrix(): FloatArray {
        val orthoM = FloatArray(16)
        if (videoWidth != -1 && videoHeight != -1 &&
            worldWidth != -1 && worldHeight != -1
        ) {
            val originRatio = videoWidth / videoHeight.toFloat()
            val worldRatio = worldWidth / worldHeight.toFloat()
            if (videoWidth > worldWidth || videoHeight > worldHeight) {
                if (worldWidth > worldHeight) {
                    if (originRatio > worldRatio) {
                        val actualRatio = originRatio / worldRatio
                        Matrix.orthoM(
                            orthoM, 0,
                            -1f, 1f,
                            -actualRatio, actualRatio,
                            -1f, 3f
                        )
                    } else {// 原始比例小于窗口比例，缩放高度度会导致高度超出，因此，高度以窗口为准，缩放宽度
                        val actualRatio = worldRatio / originRatio
                        Matrix.orthoM(
                            orthoM, 0,
                            -actualRatio, actualRatio,
                            -1f, 1f,
                            -1f, 3f
                        )
                    }
                } else {
                    if (originRatio > worldRatio) {
                        val actualRatio = originRatio / worldRatio
                        Matrix.orthoM(
                            orthoM, 0,
                            -1f, 1f,
                            -actualRatio, actualRatio,
                            -1f, 3f
                        )
                    } else {// 原始比例小于窗口比例，缩放高度会导致高度超出，因此，高度以窗口为准，缩放宽度
                        val actualRatio = worldRatio / originRatio
                        Matrix.orthoM(
                            orthoM, 0,
                            -actualRatio, actualRatio,
                            -1f, 1f,
                            -1f, 3f
                        )
                    }
                }
            } else {
                val wRatio = worldWidth / videoWidth.toFloat()
                val hRatio = worldHeight / videoHeight.toFloat()
                Matrix.orthoM(
                    orthoM, 0,
                    -wRatio, wRatio,
                    -hRatio, hRatio,
                    -1f, 3f
                )
            }
        }
        return orthoM
    }

}