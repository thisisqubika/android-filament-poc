package com.example.native3dpoc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.Volume
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.scale
import androidx.xr.compose.subspace.layout.width
import androidx.xr.runtime.Session
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_3D_CONTENT
import androidx.xr.scenecore.scene
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

@Composable
fun VolumeContainer() {
    Subspace {
        SpatialPanel(
            SubspaceModifier.height(1500.dp).width(1500.dp)
                .resizable().movable()
        ) {
            ObjectInAVolume(true)
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Welcome",
                    fontSize = 50.sp,
                )
            }
        }
    }
}

@Composable
fun ObjectInAVolume(show3DObject: Boolean) {
    // [START_EXCLUDE silent]
    val volumeXOffset = 0.dp
    val volumeYOffset = 0.dp
    val volumeZOffset = 0.dp
    // [END_EXCLUDE silent]
    val session = checkNotNull(LocalSession.current)
    val scope = rememberCoroutineScope()
    if (show3DObject) {
        Subspace {
            Volume(
                modifier = SubspaceModifier
                    .offset(volumeXOffset, volumeYOffset, volumeZOffset) // Relative position
                    .scale(1.2f) // Scale to 120% of the size

            ) { parent ->
                scope.launch {
                    val gltfModel = GltfModel.create(
                        session,
                        "models/model.glb"
                    ).await()
                    createModelEntity(session, gltfModel)?.let {
                        parent.addChild(it)
                    }
                }
            }
        }
    }
}

private fun createModelEntity(session: Session, gltfModel: GltfModel): GltfModelEntity? {
    return if (session.scene.spatialCapabilities
            .hasCapability(SPATIAL_CAPABILITY_3D_CONTENT)
    ) {
        GltfModelEntity.create(session, gltfModel)
    } else {
        null
    }
}