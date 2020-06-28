package com.lianjiehao.audioandvideopractice.utils

import android.opengl.GLES20


object OpenglHelper {
    fun compileVetexShader(shaderCode: String): Int =
        compileShader(
            GLES20.GL_VERTEX_SHADER,
            shaderCode
        )

    fun compileFragmentShader(shaderCode: String): Int =
        compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            shaderCode
        )

    private fun compileShader(type: Int, shaderCode: String): Int {
        val shaderId = GLES20.glCreateShader(type)
        if (shaderId == 0) {
            logError("Could not create shader.")
            return 0
        }
        GLES20.glShaderSource(shaderId, shaderCode)
        GLES20.glCompileShader(shaderId)
        //check code.
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        logDebug(
            "Results of compiling source:\n${shaderCode}\n" +
                    "InfoLog:${GLES20.glGetShaderInfoLog(shaderId)}"
        )
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shaderId)
            logError("Compilation of shader failed.")
            return 0
        }
        return shaderId
    }


    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
        val programId = GLES20.glCreateProgram()
        if (programId == 0) {
            logError("Could not create new program.")
            return 0
        }
        GLES20.glAttachShader(programId, vertexShaderId)
        GLES20.glAttachShader(programId, fragmentShaderId)
        GLES20.glLinkProgram(programId)
        //check code.
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0)
        logDebug(
            "Results of linking program:${linkStatus[0]}\n" +
                    "InfoLog:${GLES20.glGetProgramInfoLog(programId)}"
        )
        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(programId)
            logError("Linking of program failed.")
            return 0
        }

        return programId
    }


    fun validateProgram(programId: Int) {
        GLES20.glValidateProgram(programId)
        val validateStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0)
        logDebug(
            "Results of validate program:${validateStatus[0]}\n" +
                    "InfoLog:${GLES20.glGetProgramInfoLog(programId)}"
        )
    }

    fun createTextureIds(count: Int): IntArray {
        val texture = IntArray(count)
        GLES20.glGenTextures(count, texture, 0) //生成纹理
        return texture
    }

}