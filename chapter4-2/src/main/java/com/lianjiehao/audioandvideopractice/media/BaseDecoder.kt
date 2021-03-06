package com.lianjiehao.audioandvideopractice.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.SystemClock
import com.lianjiehao.audioandvideopractice.utils.logError
import java.nio.ByteBuffer

/**
 * @author :created by tangxianming
 * @date: 2020/6/29
 * @desc: XXX
 */
abstract class BaseDecoder(private val mFilePath: String) : IDecoder {
    //-------------线程相关------------------------
    /**
     * 解码器是否在运行
     */
    @Volatile
    private var mIsRunning = true

    /**
     * 线程等待锁
     */
    private val mLock = Object()


    //---------------解码相关-----------------------
    /**
     * 音视频解码器
     */
    private var mCodec: MediaCodec? = null

    /**
     * 音视频数据读取器
     */
    private var mExtractor: IExtractor? = null

    /**
     * 解码输入缓存区
     */
    private var mInputBuffers: Array<ByteBuffer>? = null

    /**
     * 解码输出缓存区
     */
    private var mOutputBuffers: Array<ByteBuffer>? = null

    /**
     * 解码数据信息
     */
    private var mBufferInfo = MediaCodec.BufferInfo()

    @Volatile
    private var mState = DecodeState.STOP

    private var mDuration: Long = 0

    private var mEndPos: Long = 0

    /**
     * 开始解码时间，用于音视频同步
     */
    private var mStartTimeForSync = -1L


    final override fun run() {
        try {
            mState == DecodeState.START
            //【解码步骤：1. 初始化，并启动解码器】
            if (!init()) return
            while (mIsRunning) {
                if (mState == DecodeState.PAUSE || mState == DecodeState.SEEKING) {
                    waitDecode()
                    // ---------【同步时间矫正】-------------
                    //恢复同步的起始时间，即去除等待流失的时间
                    mStartTimeForSync = SystemClock.elapsedRealtime() - getCurTimeStamp()
                }
                if (mStartTimeForSync == -1L) {
                    mStartTimeForSync = SystemClock.elapsedRealtime()
                }
                //【解码步骤：2. 将数据压入解码器输入缓冲】
                pushBufferToDecoder()
                //【解码步骤：3. 将解码好的数据从缓冲区拉取出来】
                val index = pullBufferFromDecoder()
                if (index >= 0) {
                    sleepRender()
                    //【解码步骤：4. 渲染】
                    render(mOutputBuffers!![index], mBufferInfo)
                    //【解码步骤：5. 释放输出缓冲】
                    mCodec!!.releaseOutputBuffer(index, true)
                }
                //【解码步骤：6. 判断解码是否完成】
                if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    mState = DecodeState.FINISH
                    mIsRunning = false
                    logError("errormessage=finish-${Thread.currentThread().name}")
                }
                logError("decodeing-${Thread.currentThread().name}")
            }
        } catch (e: Exception) {
            logError("errormessage=${e.message}-${Thread.currentThread().name}")
        } finally {
            doneDecode()
            //【解码步骤：7. 释放解码器】
            release()
        }

    }


    /**
     * 解码线程进入等待
     */
    private fun waitDecode() {
        try {
            synchronized(mLock) {
                mLock.wait()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 通知解码线程继续运行
     */
    protected fun notifyDecode() {
        synchronized(mLock) {
            mLock.notifyAll()
        }
    }

    /**
     * 渲染
     */
    abstract fun render(
        outputBuffers: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    )

    /**
     * 结束解码
     */
    abstract fun doneDecode()


    private fun init(): Boolean {
        //1.检查参数是否完整
        if (mFilePath == null || mFilePath.isEmpty()) {
            return false
        }
        //调用虚函数，检查子类参数是否完整
        if (!check()) return false

        //2.初始化数据提取器
        mExtractor = initExtractor(mFilePath)
        if (mExtractor == null ||
            mExtractor!!.getFormat() == null
        ) return false

        //3.初始化参数
        if (!initParams()) return false

        //4.初始化渲染器
        if (!initRender()) return false

        //5.初始化解码器
        if (!initCodec()) return false
        return true
    }

    private fun initParams(): Boolean {
        try {
            val format = mExtractor!!.getFormat()!!
            mDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000
            if (mEndPos == 0L) mEndPos = mDuration
            logError("time--mDuration=${mEndPos}")
            initSpecParams(mExtractor!!.getFormat()!!)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun initCodec(): Boolean {
        try {
            //1.根据音视频编码格式初始化解码器
            val type = mExtractor!!.getFormat()!!.getString(MediaFormat.KEY_MIME)
            mCodec = MediaCodec.createDecoderByType(type)
            //2.配置解码器
            configCodec(mCodec!!, mExtractor!!.getFormat()!!)
            //3.启动解码器
            mCodec!!.start()

            //4.获取解码器缓冲区
            mInputBuffers = mCodec?.inputBuffers
            mOutputBuffers = mCodec?.outputBuffers
        } catch (e: Exception) {
            return false
        }
        return true
    }


    private fun pushBufferToDecoder(): Boolean {
        var inputBufferIndex = mCodec!!.dequeueInputBuffer(2000)
        var isEndOfStream = false

        if (inputBufferIndex >= 0) {
            val inputBuffer = mInputBuffers!![inputBufferIndex]
            val sampleSize = mExtractor!!.readBuffer(inputBuffer)
            if (sampleSize < 0) {
                //如果数据已经取完，压入数据结束标志：BUFFER_FLAG_END_OF_STREAM
                mCodec!!.queueInputBuffer(
                    inputBufferIndex, 0, 0,
                    0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                isEndOfStream = true
            } else {
                mCodec!!.queueInputBuffer(
                    inputBufferIndex, 0,
                    sampleSize, mExtractor!!.getCurrentTimestamp(), 0
                )
            }
        }
        return isEndOfStream
    }

    private fun pullBufferFromDecoder(): Int {
        // 查询是否有解码完成的数据，index >=0 时，表示数据有效，并且index为缓冲区索引
        var index = mCodec!!.dequeueOutputBuffer(mBufferInfo, 2000)
        when (index) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
            }
            MediaCodec.INFO_TRY_AGAIN_LATER -> {
            }
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                mOutputBuffers = mCodec!!.outputBuffers
            }
            else -> {
                return index
            }
        }
        return -1
    }

    fun release() {
        try {
            mIsRunning = false
            mState = DecodeState.STOP
            mExtractor?.stop()
            mCodec?.stop()
            mCodec?.release()
        } catch (e: Exception) {
        }
    }

    /**
     * 检查子类参数
     */
    abstract fun check(): Boolean

    /**
     * 初始化数据提取器
     */
    abstract fun initExtractor(path: String): IExtractor

    /**
     * 初始化子类自己特有的参数
     */
    abstract fun initSpecParams(format: MediaFormat)

    /**
     * 初始化渲染器
     */
    abstract fun initRender(): Boolean

    /**
     * 配置解码器
     */
    abstract fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean

    override fun goOn() {
        mState = DecodeState.DECODING
        notifyDecode()
    }

    override fun getCurTimeStamp(): Long {
        return mBufferInfo.presentationTimeUs / 1000
    }

    private fun sleepRender() {
        val passTime = SystemClock.elapsedRealtime() - mStartTimeForSync
        val curTime = getCurTimeStamp()
        if (curTime > passTime) {
            Thread.sleep(curTime - passTime)
        }
    }
}