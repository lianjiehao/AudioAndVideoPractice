package com.lianjiehao.chapter4

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import java.nio.ByteBuffer

/**
 * @author :created by tangxianming
 * @date: 2020/6/29
 * @desc: XXX
 */
class VideoDecoder(
    path: String,
    private val textureView: TextureView?,
    private var surface: Surface?
) : BaseDecoder(path) {

    override fun check(): Boolean {
        if (textureView == null && surface == null) {
            logError("TextureView和Surface都为空，至少需要一个不为空")
            return false
        }
        return true
    }

    override fun initExtractor(path: String): IExtractor {
        return VideoExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {
    }

    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        if (surface != null) {
            codec.configure(format, surface, null, 0)
            notifyDecode()
        } else {
            textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture?,
                    width: Int,
                    height: Int
                ) {
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                    return false
                }

                override fun onSurfaceTextureAvailable(
                    sf: SurfaceTexture?,
                    width: Int,
                    height: Int
                ) {
                    surface = Surface(sf)
                    configCodec(codec, format)
                }

            }
            return false
        }
        return true
    }

    override fun initRender(): Boolean {
        return true
    }

    override fun render(
        outputBuffers: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ) {
        logDebug("video render.")
    }

    override fun doneDecode() {
    }
}