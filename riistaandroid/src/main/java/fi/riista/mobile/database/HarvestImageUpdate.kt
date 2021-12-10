package fi.riista.mobile.database

import fi.riista.mobile.models.GameLogImage

class HarvestImageUpdate(val addedImages: List<GameLogImage>, val deletedImages: List<GameLogImage>) {

    fun hasAddedImages(): Boolean = addedImages.isNotEmpty()

    fun hasDeletedImages(): Boolean = deletedImages.isNotEmpty()

    fun getNumberOfImageChanges(): Int = addedImages.size + deletedImages.size

    fun merge(other: HarvestImageUpdate): HarvestImageUpdate {
        val added: ArrayList<GameLogImage> = ArrayList(addedImages)
        added.addAll(other.addedImages)

        val deleted: ArrayList<GameLogImage> = ArrayList(deletedImages)
        deleted.addAll(other.deletedImages)

        return HarvestImageUpdate(added, deleted)
    }

    companion object {
        @JvmStatic
        fun empty(): HarvestImageUpdate = HarvestImageUpdate(emptyList(), emptyList())
    }
}
