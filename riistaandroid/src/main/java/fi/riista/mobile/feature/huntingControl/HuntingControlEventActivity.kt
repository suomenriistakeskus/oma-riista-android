package fi.riista.mobile.feature.huntingControl

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.FileProvider
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.huntingControl.model.*
import fi.riista.common.domain.huntingControl.ui.modify.CreateHuntingControlEventController
import fi.riista.common.domain.huntingControl.ui.modify.EditHuntingControlEventController
import fi.riista.common.domain.huntingControl.ui.view.ViewHuntingControlEventController
import fi.riista.common.extensions.loadHuntingControlEventTarget
import fi.riista.common.extensions.loadHuntingControlRhyTarget
import fi.riista.common.extensions.saveToBundle
import fi.riista.common.logging.getLogger
import fi.riista.common.domain.model.EntityImage
import fi.riista.common.io.CommonFileProvider
import fi.riista.common.io.CommonFileStorage
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.riistaSdkHelpers.ContextStringProviderFactory
import fi.riista.mobile.ui.FullScreenEntityImageDialog
import java.io.File

class HuntingControlEventActivity
    : BaseActivity()
    , ViewHuntingControlEventFragment.InteractionManager
    , CreateHuntingControlEventFragment.InteractionManager
    , EditHuntingControlEventFragment.InteractionManager
{

    private enum class Mode {
        VIEW,
        CREATE
    }

    private var _huntingControlEventTarget: HuntingControlEventTarget? = null
    override val huntingControlEventTarget: HuntingControlEventTarget
        get() {
            return requireNotNull(_huntingControlEventTarget) {
                "hunting control event target not set!"
            }
        }

    private var _huntingControlRhyTarget: HuntingControlRhyTarget? = null
    override val huntingControlRhyTarget: HuntingControlRhyTarget
        get() {
            return requireNotNull(_huntingControlRhyTarget) {
                "RHY target not set!"
            }
        }

    override val viewHuntingControlEventController: ViewHuntingControlEventController
        get() {
            return ViewHuntingControlEventController(
                huntingControlContext = RiistaSDK.huntingControlContext,
                huntingControlEventTarget = huntingControlEventTarget,
                stringProvider = ContextStringProviderFactory.createForContext(this),
            )
        }
    override val createHuntingControlEventController: CreateHuntingControlEventController
        get() {
            return CreateHuntingControlEventController(
                huntingControlContext = RiistaSDK.huntingControlContext,
                huntingControlRhyTarget = huntingControlRhyTarget,
                stringProvider = ContextStringProviderFactory.createForContext(this),
                commonFileProvider = RiistaSDK.commonFileProvider,
            )
        }

    override val editHuntingControlEventController: EditHuntingControlEventController
        get() {
            return EditHuntingControlEventController(
                huntingControlContext = RiistaSDK.huntingControlContext,
                huntingControlEventTarget = huntingControlEventTarget,
                stringProvider = ContextStringProviderFactory.createForContext(this),
                commonFileProvider = RiistaSDK.commonFileProvider,
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hunting_control_event)

        _huntingControlEventTarget = getHuntingControlEventTargetFromIntent(intent)
        _huntingControlRhyTarget = getHuntingControlRhyTargetFromIntent(intent)

        if (savedInstanceState == null) {
            val mode = getModeFromIntent(intent)
            val fragment = when (mode) {
                Mode.VIEW -> ViewHuntingControlEventFragment()
                Mode.CREATE -> CreateHuntingControlEventFragment()
            }

            supportFragmentManager.beginTransaction()
                .add(
                    R.id.fragment_container,
                    fragment,
                    "${mode}HuntingControlEventFragment"
                )
                .commit()
        }
    }

    override fun startEditingHuntingControlEvent() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                EditHuntingControlEventFragment(),
                "EditHuntingControlEventFragment"
            )
            .addToBackStack(null)
            .commit()
    }

    override fun showAttachment(attachment: HuntingControlAttachment) {
        val path = getAttachmentPath(attachment) ?: return
        if (attachment.isImage) {
            // Show local image
            val asEntityImage = EntityImage(
                localIdentifier = null,
                localUrl = path,
                serverId = null,
                status = EntityImage.Status.LOCAL,
            )
            FullScreenEntityImageDialog.newInstance(entityImage = asEntityImage)
                .show(supportFragmentManager, FullScreenEntityImageDialog.TAG)
        } else {
            val file = File(path)
            val uri = FileProvider.getUriForFile(
                this,
                applicationContext.packageName.toString() + ".provider",
                file
            )

            val openIntent = Intent(Intent.ACTION_VIEW)
            openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            openIntent.setDataAndType(uri, attachment.mimeType)
            try {
                startActivity(openIntent)
            }
            catch (e: ActivityNotFoundException) {
                logger.w { "Unable to open file ${e.message}" }
            }
        }
    }

    private fun getAttachmentPath(attachment: HuntingControlAttachment): String? {
        val uuid = attachment.uuid ?: return null

        // If attachment doesn't have localId then it is not saved yet and it can be found from temporary files
        val commonFile = if (attachment.localId == null) {
            CommonFileStorage.getFile(CommonFileProvider.Directory.TEMPORARY_FILES, uuid)
        } else {
            CommonFileStorage.getFile(CommonFileProvider.Directory.ATTACHMENTS, uuid)
        }
        if (commonFile?.exists() == true) {
            return commonFile.path
        }
        return null
    }

    override fun onCreatingNewHuntingControlEvent() {
        // No op
    }

    override fun onNewHuntingControlEventCreateCompleted(
        success: Boolean,
        eventId: HuntingControlEventId?,
        indicatorsDismissed: () -> Unit
    ) {
        if (eventId != null) {
            _huntingControlEventTarget = huntingControlRhyTarget.createTargetForEvent(eventId)

            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    ViewHuntingControlEventFragment(),
                    "VIEWHuntingControlEventFragment"
                )
                .commit()
        } else {
            indicatorsDismissed()
            supportFragmentManager.popBackStack()
        }
    }

    override fun cancelCreateNewHuntingControlEvent() {
        onBackPressed()
    }

    override fun onSavingHuntingControlEvent() {
        // No op
    }

    override fun onHuntingControlEventSaveCompleted(success: Boolean, indicatorsDismissed: () -> Unit) {
        if (isFinishing) {
            return
        }
        if (success) {
            finish()
        } else {
            indicatorsDismissed()
        }
    }

    override fun cancelEditHuntingControlEvent() {
        onBackPressed()
    }

    companion object {
        private const val EXTRAS_PREFIX = "HuntingControlEventActivity"
        private const val KEY_MODE = "${EXTRAS_PREFIX}_mode"

        fun getLaunchIntentForViewing(
            context: Context,
            huntingControlEventTarget: HuntingControlEventTarget,
        ): Intent {
            return Intent(context, HuntingControlEventActivity::class.java)
                .apply {
                    putExtras(Bundle().also {
                        huntingControlEventTarget.saveToBundle(it, EXTRAS_PREFIX)
                        it.putMode(Mode.VIEW)
                    })
                }
        }

        fun getLaunchIntentForCreating(
            packageContext: Context,
            huntingControlRhyTarget: HuntingControlRhyTarget,
            huntingControlEventTarget: HuntingControlEventTarget?,
        ): Intent {
            return Intent(packageContext, HuntingControlEventActivity::class.java)
                .apply {
                    putExtras(Bundle().also {
                        huntingControlRhyTarget.saveToBundle(it, EXTRAS_PREFIX)
                        huntingControlEventTarget?.saveToBundle(it, EXTRAS_PREFIX)
                        it.putMode(Mode.CREATE)
                    })
                }
        }

        private fun Bundle.putMode(mode: Mode) {
            putString(KEY_MODE, mode.toString())
        }

        private fun getModeFromIntent(intent: Intent): Mode {
            val mode = intent.getStringExtra(KEY_MODE)
                ?.let {
                    Mode.valueOf(it)
                }

            return requireNotNull(mode) { "Mode must exist in intent!" }
        }

        private fun getHuntingControlEventTargetFromIntent(intent: Intent): HuntingControlEventTarget? {
            return intent.extras?.loadHuntingControlEventTarget(EXTRAS_PREFIX)
        }

        private fun getHuntingControlRhyTargetFromIntent(intent: Intent): HuntingControlRhyTarget? {
            return intent.extras?.loadHuntingControlRhyTarget(EXTRAS_PREFIX)
        }

        private val logger by getLogger(HuntingControlEventActivity::class)
    }
}
