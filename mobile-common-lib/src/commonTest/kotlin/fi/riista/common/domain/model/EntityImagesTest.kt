package fi.riista.common.domain.model

import kotlin.test.*

class EntityImagesTest {

    @Test
    fun testPrimaryDoesntExist() {
        val images = EntityImages(
            remoteImageIds = listOf(),
            localImages = listOf()
        )

        assertNull(images.primaryImage)
    }

    @Test
    fun testPrimaryExistsWhenRemoteImages() {
        val images = EntityImages(
            remoteImageIds = listOf("1"),
            localImages = listOf()
        )

        assertNotNull(images.primaryImage)
        assertEquals("1", images.primaryImage!!.serverId)
    }

    @Test
    fun testPrimaryExistsWhenLocalImages() {
        val images = EntityImages(
            remoteImageIds = listOf(),
            localImages = listOf(
                EntityImage(
                    serverId = "1",
                    localIdentifier = null,
                    localUrl = null,
                    status = EntityImage.Status.LOCAL
                ))
        )

        assertNotNull(images.primaryImage)
        assertEquals("1", images.primaryImage!!.serverId)
    }

    @Test
    fun testPrimaryImageLocalOnly() {
        val images = EntityImages(
            remoteImageIds = listOf("1", "2"),
            localImages = listOf(
                EntityImage(
                    serverId = "3",
                    localIdentifier = null,
                    localUrl = null,
                    status = EntityImage.Status.LOCAL
                )
            )
        )

        assertEquals("3", images.primaryImage!!.serverId)
    }



    @Test
    fun testPrimaryImageMultipleLocalImages() {
        val images = EntityImages(
            remoteImageIds = listOf("1", "2"),
            localImages = listOf(
                EntityImage(
                    serverId = "3",
                    localIdentifier = null,
                    localUrl = null,
                    status = EntityImage.Status.LOCAL
                ),
                EntityImage(
                    serverId = "4",
                    localIdentifier = null,
                    localUrl = null,
                    status = EntityImage.Status.LOCAL
                )
            )
        )

        assertEquals("4", images.primaryImage!!.serverId)
    }

    @Test
    fun testPrimaryNonLastLocal() {
        val images = EntityImages(
            remoteImageIds = listOf("1", "2"),
            localImages = listOf(
                EntityImage(
                    serverId = "1",
                    localIdentifier = null,
                    localUrl = null,
                    status = EntityImage.Status.UPLOADED
                )
            )
        )

        assertEquals("2", images.primaryImage!!.serverId)
    }

    @Test
    fun testPrimaryLastLocal() {
        val images = EntityImages(
            remoteImageIds = listOf("1", "2"),
            localImages = listOf(
                EntityImage(
                    serverId = "2",
                    localIdentifier = null,
                    localUrl = "2-url",
                    status = EntityImage.Status.UPLOADED
                )
            )
        )

        assertEquals("2", images.primaryImage!!.serverId)
        assertEquals("2-url", images.primaryImage!!.localUrl)
    }
}
