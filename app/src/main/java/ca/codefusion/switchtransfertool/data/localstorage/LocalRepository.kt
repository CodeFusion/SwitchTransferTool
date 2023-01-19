package ca.codefusion.switchtransfertool.data.localstorage

interface LocalRepository {

    fun getGallerySortDirection(): Boolean

    fun setGallerySortDirection(desc: Boolean)

    fun getShowQRTutorial(): Boolean

    fun setShowQRTutorial(shouldShow: Boolean)

    fun getShowManualTutorial(): Boolean

    fun setShowManualTutorial(shouldShow: Boolean)

    fun getLastViewedChangelogVersion(): Int

    fun setLastViewedChangelogVersion(versionCode: Int)

    fun isFirstRun(): Boolean

    fun setIsFirstRun(firstRun: Boolean)

    fun isDefaultManualMode(): Boolean

    fun setDefaultManualMode(useManualMode: Boolean)
}