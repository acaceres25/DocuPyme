package com.example

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.platform.DocumentNotifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DocumentNotifierTest {
    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val manager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Test
    fun deniedPermissionDoesNotPostNotification() {
        shadowOf(application).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertFalse(DocumentNotifier(application).show("Contrato", "Vence mañana"))
        assertTrue(shadowOf(manager).allNotifications.isEmpty())
    }

    @Test
    fun grantedPermissionPostsNotificationAndCreatesChannel() {
        shadowOf(application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertTrue(DocumentNotifier(application).show("Contrato", "Vence mañana"))
        assertEquals(1, shadowOf(manager).allNotifications.size)
        assertEquals("docupyme_alerts_channel", shadowOf(manager).allNotifications.single().channelId)
    }
}
