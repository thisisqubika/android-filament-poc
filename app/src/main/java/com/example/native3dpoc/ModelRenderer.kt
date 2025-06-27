package com.example.native3dpoc

import android.content.res.AssetManager
import android.view.Choreographer
import android.view.SurfaceView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.filament.Camera
import com.google.android.filament.ColorGrading
import com.google.android.filament.EntityManager
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.View
import com.google.android.filament.android.UiHelper
import com.google.android.filament.utils.ModelViewer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ModelRenderer {
    private lateinit var surfaceView: SurfaceView
    private lateinit var lifecycle: Lifecycle

    private lateinit var choreographer: Choreographer
    private lateinit var uiHelper: UiHelper
    private lateinit var modelViewer: ModelViewer
    private lateinit var assets: AssetManager

    private var currentFov = 45.0 // degrees
    private val minFov = 1.0
    private val maxFov = 90.0

    val lights = listOf(
        Triple(0.0f, -1.0f, -0.5f)
    )

    private val frameScheduler = FrameCallback()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            choreographer.postFrameCallback(frameScheduler)
        }

        override fun onPause(owner: LifecycleOwner) {
            choreographer.removeFrameCallback(frameScheduler)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            choreographer.removeFrameCallback(frameScheduler)
            lifecycle.removeObserver(this)
        }
    }

    fun zoomByFov(zoomIn: Boolean) {
        currentFov = if (zoomIn) {
            (currentFov - 1.0).coerceAtLeast(minFov)
        } else {
            (currentFov + 1.0).coerceAtMost(maxFov)
        }
        updateProjectionMatrix()
    }

    private fun updateProjectionMatrix() {
        val aspect = surfaceView.width.toDouble() / surfaceView.height.toDouble()
        modelViewer.camera.setProjection(
            currentFov,
            aspect,
            0.1,
            1000.0,
            Camera.Fov.VERTICAL
        )
    }

    fun onSurfaceAvailable(surfaceView: SurfaceView, lifecycle: Lifecycle) {
        this.surfaceView = surfaceView
        this.lifecycle = lifecycle
        assets = surfaceView.context.assets
        lifecycle.addObserver(lifecycleObserver)
        choreographer = Choreographer.getInstance()
        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
            // This is needed to make the background transparent
            isOpaque = false
        }
        modelViewer = ModelViewer(surfaceView = surfaceView, uiHelper = uiHelper)

        // This is needed so we can move the camera in the rendering
        surfaceView.setOnTouchListener { _, event ->
            modelViewer.onTouchEvent(event)
            true
        }

        addSimpleBrightLight()

        // This is the other code needed to make the background transparent
        modelViewer.scene.skybox = null
        modelViewer.view.blendMode = View.BlendMode.TRANSLUCENT
        modelViewer.renderer.clearOptions = modelViewer.renderer.clearOptions.apply {
            clear = true
        }
        // This part defines the quality of your model, feel free to change it or
        // add other options
        modelViewer.view.apply {
            renderQuality = renderQuality.apply {
                hdrColorBuffer = View.QualityLevel.MEDIUM
            }
        }

        createRenderables()
    }

    private fun addSimpleBrightLight() {
        lights.forEach { (x, y, z) ->
            val light = EntityManager.get().create()
            LightManager.Builder(LightManager.Type.DIRECTIONAL)
                .color(1.0f, 1.0f, 1.0f)
                .intensity(500000.0f)
                .direction(x, y, z)
                .castShadows(false) // Critical: no shadow casting
                .build(modelViewer.engine, light)

            modelViewer.scene.addEntity(light)
        }
    }

    private fun createRenderables() {
        val buffer = assets.open("models/model.glb").use { input ->
            val bytes = ByteArray(input.available())
            input.read(bytes)
            ByteBuffer.allocateDirect(bytes.size).apply {
                order(ByteOrder.nativeOrder())
                put(bytes)
                rewind()
            }
        }
        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube()
    }

    inner class FrameCallback : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            modelViewer.render(frameTimeNanos)
        }
    }
}

