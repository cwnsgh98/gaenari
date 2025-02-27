package com.example.gaenari.activity.runandwalk.walk

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gaenari.R
import com.example.gaenari.CircularProgressButton
import kotlin.math.max
import kotlin.math.min

class WSecondFragment : Fragment() {
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var circularProgressButton: CircularProgressButton
    private var service: WService? = null
    private var isPaused = false
    private val pauseTargetTimeMillis = 500L
    private val stopTargetTimeMillis = 1500L

    companion object {
        fun newInstance(): WSecondFragment = WSecondFragment()
    }

    @SuppressLint("ClickableViewAccessibility", "MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_w_second, container, false)
        pauseButton = view.findViewById(R.id.pauseButton)
        stopButton = view.findViewById(R.id.stopButton)
        circularProgressButton = view.findViewById(R.id.circularProgressButton)

        pauseButton.setOnTouchListener { _, event ->
            val colorRes = if (isPaused) R.color.resultgreen else R.color.mainyellow
            handleProgress(
                event,
                pauseTargetTimeMillis,
                ::togglePause,
                colorRes
            )
        }
        stopButton.setOnTouchListener { _, event ->
            handleProgress(
                event,
                stopTargetTimeMillis,
                ::stopExercise,
                R.color.resultred
            )
        }

        bindService()
        return view
    }

    private fun handleProgress(event: MotionEvent, targetTimeMillis: Long, action: () -> Unit, colorRes: Int): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 눌린 버튼에 따라 다른 색상 설정
                circularProgressButton.setProgressColor(ContextCompat.getColor(requireContext(), colorRes))
                startProgress(targetTimeMillis, action)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> reduceProgressGradually()
        }
        return true
    }

    private var progressStartTime = 0L
    private var progressHandler: Handler = Handler(Looper.getMainLooper())
    private var action: (() -> Unit)? = null

    private val progressRunnable = object : Runnable {
        override fun run() {
            val elapsedTime = System.currentTimeMillis() - progressStartTime
            val targetTimeMillis = if (action == ::togglePause) pauseTargetTimeMillis else stopTargetTimeMillis
            val progressFraction = min(1f, elapsedTime.toFloat() / targetTimeMillis)
            circularProgressButton.updateProgress(progressFraction)
            if (progressFraction < 1f) {
                progressHandler.postDelayed(this, 16) // 60 FPS
            } else {
                action?.invoke() // 목표 달성 시 해당 기능 실행
            }
        }
    }

    private val reductionRunnable = object : Runnable {
        override fun run() {
            val currentProgress = circularProgressButton.getProgress() // CircularProgressButton에서 프로그레스 가져오기
            val newProgress = max(0f, currentProgress - 0.05f) // 일정량 감소
            circularProgressButton.updateProgress(newProgress)
            if (newProgress > 0f) {
                progressHandler.postDelayed(this, 16) // 60 FPS로 감소
            }
        }
    }

    private fun startProgress(targetTimeMillis: Long, action: () -> Unit) {
        progressStartTime = System.currentTimeMillis()
        this.action = action
        progressHandler.removeCallbacks(reductionRunnable) // 감소 애니메이션을 중단
        progressHandler.post(progressRunnable)
    }

    private fun reduceProgressGradually() {
        progressHandler.removeCallbacks(progressRunnable) // 진행 애니메이션을 중단
        progressHandler.post(reductionRunnable) // 감소 애니메이션 시작
    }

    private fun togglePause() {
        isPaused = !isPaused
        pauseButton.text = if (isPaused) "이어달리기" else "일시정지"
        pauseButton.setBackgroundResource(if (isPaused) R.drawable.pausebutton else R.drawable.resumebutton )
        if (isPaused) {
            service?.pauseService()
        } else {
            service?.resumeService()
        }
    }

    private fun stopExercise() {
        service?.onDestroy()
    }

    private fun bindService() {
        Intent(context, WService::class.java).also { intent ->
            context?.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as WService.LocalBinder
            this@WSecondFragment.service = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            service = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        context?.unbindService(serviceConnection)
    }
}
