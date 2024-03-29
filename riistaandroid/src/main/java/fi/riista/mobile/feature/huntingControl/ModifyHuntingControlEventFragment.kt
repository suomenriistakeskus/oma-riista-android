package fi.riista.mobile.feature.huntingControl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import fi.riista.common.domain.huntingControl.model.HuntingControlAttachment
import fi.riista.common.domain.huntingControl.model.encodeToBase64
import fi.riista.common.domain.huntingControl.ui.HuntingControlEventField
import fi.riista.common.domain.huntingControl.ui.modify.ModifyHuntingControlEventController
import fi.riista.common.io.CommonFileStorage
import fi.riista.common.io.FileSaveResult
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.LocalTime
import fi.riista.common.model.StringId
import fi.riista.common.model.StringWithId
import fi.riista.common.model.toBackendEnum
import fi.riista.common.ui.dataField.AttachmentField
import fi.riista.common.ui.dataField.BooleanField
import fi.riista.common.ui.dataField.ButtonField
import fi.riista.common.ui.dataField.ChipField
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.DateField
import fi.riista.common.ui.dataField.IntField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.LocationField
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.ui.dataField.StringListField
import fi.riista.common.ui.dataField.TimespanField
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import fi.riista.mobile.AppConfig
import fi.riista.mobile.R
import fi.riista.mobile.activity.MapViewerActivity
import fi.riista.mobile.activity.SelectStringWithIdActivity
import fi.riista.mobile.feature.groupHunting.DataFieldPageFragment
import fi.riista.mobile.network.AppDownloadManager
import fi.riista.mobile.riistaSdkHelpers.determineViewHolderType
import fi.riista.mobile.riistaSdkHelpers.fromJodaLocalDate
import fi.riista.mobile.riistaSdkHelpers.fromJodaLocalTime
import fi.riista.mobile.riistaSdkHelpers.registerLabelFieldViewHolderFactories
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.DateTimePickerFragment
import fi.riista.mobile.ui.DelegatingAlertDialogListener
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.AttachmentViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ButtonViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ChipsViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ChoiceViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.ChoiceViewLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderTypeResolver
import fi.riista.mobile.ui.dataFields.viewHolder.DatePickerFragmentLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.DateViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.EditableBooleanAsRadioToggleViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.EditableTextViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.IntFieldViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.LocationOnMapViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.MapOpener
import fi.riista.mobile.ui.dataFields.viewHolder.ReadOnlySingleLineTextViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.TimePickerFragmentLauncher
import fi.riista.mobile.ui.dataFields.viewHolder.TimespanViewHolder
import fi.riista.mobile.ui.registerDatePickerFragmentResultListener
import fi.riista.mobile.ui.showDatePickerFragment
import fi.riista.mobile.utils.DiaryImageUtil
import fi.riista.mobile.utils.FileUtils
import fi.riista.mobile.utils.MapUtils
import fi.riista.mobile.utils.PermissionHelper
import org.joda.time.DateTime
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*
import javax.inject.Inject

abstract class ModifyHuntingControlEventFragment<Controller : ModifyHuntingControlEventController>
    : DataFieldPageFragment<HuntingControlEventField>()
    , DataFieldViewHolderTypeResolver<HuntingControlEventField>
    , ChoiceViewLauncher<HuntingControlEventField>
    , MapOpener
    , DatePickerFragmentLauncher<HuntingControlEventField>
    , TimePickerFragmentLauncher<HuntingControlEventField>
    , DateTimePickerFragment.Listener
{
    @Inject
    lateinit var appDownloadManager: AppDownloadManager
    private var pictureUri: Uri? = null

    private lateinit var dialogListener: AlertDialogFragment.Listener

    private val selectFileActivityResultLaunch = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            copySelectedFileToInternalStorage(uri)
        }
    }

    private val captureImageActivityResultLaunch = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val capturedImageUri = pictureUri
        if (result.resultCode == Activity.RESULT_OK && capturedImageUri != null) {
            copySelectedFileToInternalStorage(capturedImageUri)
        }
        pictureUri = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getString(PICTURE_URI_KEY)?.let { uriString ->
            pictureUri = uriString.toUri()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val pictureUriString = pictureUri?.toString()
        pictureUriString?.let { uri ->
            outState.putString(PICTURE_URI_KEY, uri)
        }
    }

    private fun copySelectedFileToInternalStorage(uri: Uri) {
        val uriString = uri.toString()

        if (uriString.startsWith("content://")) {
            requireActivity().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(index)
                    copyFile(uri, fileName)
                }
            }
        } else if (uriString.startsWith("file://")) {
            val file = File(uriString)
            copyFile(uri, file.name)
        }
    }

    private fun copyFile(uri: Uri, fileName: String) {
        val uuid = UUID.randomUUID().toString()
        CommonFileStorage.saveFileToTemporaryFiles(
            sourceUri = uri,
            targetFileUuid = uuid,
        ) { fileSaveResult ->
            when (fileSaveResult) {
                is FileSaveResult.SaveFailed ->
                    fileSaved(
                        result = false,
                        fileName = fileName,
                        uuid = uuid,
                        path = null
                    )
                is FileSaveResult.Saved ->
                    fileSaved(
                        result = true,
                        fileName = fileName,
                        uuid = uuid,
                        path = fileSaveResult.targetFile.path
                    )
            }
        }
    }

    private fun fileSaved(result: Boolean, fileName: String, uuid: String, path: String?) {
        if (result && path != null) {
            val mimeType = FileUtils.getMimeType(fileName)
            val thumbnailBitmap = if (mimeType != null && mimeType.startsWith("image/")) {
                createThumbnail(path)
            } else {
                null
            }

            getController().eventDispatchers.attachmentEventDispatcher.addAttachment(
                HuntingControlAttachment(
                    fileName = fileName,
                    isImage = thumbnailBitmap != null,
                    thumbnailBase64 = thumbnailBitmap?.toByteArray()?.encodeToBase64(),
                    uuid = uuid,
                    mimeType = mimeType,
                )
            )
        }
    }

    private fun createThumbnail(path: String): Bitmap {
        val  bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inJustDecodeBounds = true // obtain the size of the image, without loading it in memory
        BitmapFactory.decodeFile(path, bitmapOptions)

        val desiredWidth = 48f
        val desiredHeight = 48f
        val widthScale = bitmapOptions.outWidth / desiredWidth
        val heightScale = bitmapOptions.outHeight / desiredHeight
        val scale = widthScale.coerceAtMost(heightScale)

        var sampleSize = 1
        while (sampleSize < scale) {
            sampleSize *= 2
        }
        bitmapOptions.inSampleSize = sampleSize // this value must be a power of 2,
        bitmapOptions.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(path, bitmapOptions)
    }

    private val locationRequestActivityResultLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val location = result.data?.getParcelableExtra<Location>(MapViewerActivity.RESULT_LOCATION)
            val source = result.data?.getStringExtra(MapViewerActivity.RESULT_LOCATION_SOURCE)

            if (location != null && source != null) {
                setLocation(location, source)
            }
        }
    }

    private val selectStringWithIdActivityResultLaunch = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            handleSelectStringWithIdResult(data)
        }
    }

    protected abstract fun getController(): Controller
    protected abstract fun showAttachment(attachment: HuntingControlAttachment)

    override fun openMap(location: Location) {
        val intent = Intent(context, MapViewerActivity::class.java)
        intent.putExtra(MapViewerActivity.EXTRA_EDIT_MODE, true)
        intent.putExtra(MapViewerActivity.EXTRA_START_LOCATION, location)
        intent.putExtra(MapViewerActivity.EXTRA_NEW, false)
        intent.putExtra(MapViewerActivity.EXTRA_SHOW_ITEMS, false)
        locationRequestActivityResultLaunch.launch(intent)
    }

    override fun resolveViewHolderType(dataField: DataField<HuntingControlEventField>): DataFieldViewHolderType {
        return when (dataField) {
            is StringField -> when (dataField.settings.readOnly) {
                true -> {
                    if (dataField.settings.singleLine) {
                        DataFieldViewHolderType.READONLY_TEXT_SINGLE_LINE
                    } else {
                        throw IllegalStateException("Non-singleline StringField not supported: ${dataField.id}")
                    }
                }
                false -> DataFieldViewHolderType.EDITABLE_TEXT
            }
            is LocationField -> DataFieldViewHolderType.LOCATION_ON_MAP
            is StringListField -> DataFieldViewHolderType.SELECTABLE_STRING
            is IntField -> DataFieldViewHolderType.INT
            is DateField -> DataFieldViewHolderType.DATE
            is TimespanField -> DataFieldViewHolderType.TIMESPAN
            is BooleanField -> DataFieldViewHolderType.EDITABLE_BOOLEAN_AS_RADIO_TOGGLE
            is LabelField -> dataField.determineViewHolderType()
            is AttachmentField -> DataFieldViewHolderType.ATTACHMENT
            is ButtonField -> DataFieldViewHolderType.BUTTON
            is ChipField -> DataFieldViewHolderType.CHIPS
            else -> {
                throw IllegalArgumentException("Unexpected DataField type: ${dataField::class.simpleName}")
            }
        }
    }

    protected fun registerViewHolderFactories(adapter: DataFieldRecyclerViewAdapter<HuntingControlEventField>) {
        adapter.apply {
            registerLabelFieldViewHolderFactories(linkActionEventDispatcher = null)
            registerViewHolderFactory(
                LocationOnMapViewHolder.Factory(
                    mapOpener = this@ModifyHuntingControlEventFragment,
                ),
            )
            registerViewHolderFactory(ReadOnlySingleLineTextViewHolder.Factory())
            registerViewHolderFactory(
                EditableTextViewHolder.Factory(
                    eventDispatcher = getController().eventDispatchers.stringEventDispatcher
                )
            )
            registerViewHolderFactory(
                ChoiceViewHolder.Factory(
                    eventDispatcher = getController().eventDispatchers.stringWithIdEventDispatcher,
                    choiceViewLauncher = this@ModifyHuntingControlEventFragment,
                )
            )
            registerViewHolderFactory(IntFieldViewHolder.Factory(getController().eventDispatchers.intEventDispatcher))
            registerViewHolderFactory(DateViewHolder.Factory(this@ModifyHuntingControlEventFragment))
            registerViewHolderFactory(TimespanViewHolder.Factory(this@ModifyHuntingControlEventFragment))
            registerViewHolderFactory(
                EditableBooleanAsRadioToggleViewHolder.Factory(getController().eventDispatchers.booleanEventDispatcher)
            )
            registerViewHolderFactory(AttachmentViewHolder.Factory(::attachmentClicked, ::deleteAttachmentClicked))
            registerViewHolderFactory(ButtonViewHolder.Factory(::addAttachmentClicked))
            registerViewHolderFactory(
                ChipsViewHolder.Factory(getController().eventDispatchers.stringWithIdClickEventDispatcher)
            )
        }
    }

    protected fun registerFragmentResultListeners() {
        registerDatePickerFragmentResultListener(DATE_PICKER_REQUEST_CODE)
        dialogListener = DelegatingAlertDialogListener(requireActivity()).apply {
            registerPositiveCallback(AlertDialogId.MODIFY_HUNTING_CONTROL_EVENT_FRAGMENT_DELETE_ATTACHMENT, ::deleteAttachment)
            registerPositiveCallback(
                AlertDialogId.MODIFY_HUNTING_CONTROL_EVENT_FRAGMENT_ATTACHMENT_DOWNLOAD_CONFIRMATION,
                ::startAttachmentDownload
            )
            registerItemCallback(AlertDialogId.MODIFY_HUNTING_CONTROL_EVENT_FRAGMENT_ADD_ATTACHMENT, ::addAttachment)
        }
    }

    override fun displayChoicesInSeparateView(
        fieldId: HuntingControlEventField,
        mode: StringListField.Mode,
        choices: List<StringWithId>,
        selectedChoices: List<StringId>?,
        viewConfiguration: StringListField.ExternalViewConfiguration,
    ) {
        val intent = SelectStringWithIdActivity.getLaunchIntent(
            packageContext = requireContext(),
            fieldId = fieldId,
            mode = mode,
            possibleValues = choices,
            selectedValueIds = selectedChoices,
            configuration = viewConfiguration,
        )

        selectStringWithIdActivityResultLaunch.launch(intent)
    }

    override fun pickDate(
        fieldId: HuntingControlEventField,
        currentDate: LocalDate,
        minDate: LocalDate?,
        maxDate: LocalDate?
    ) {
        val datePickerFragment = DateTimePickerFragment.create(
            requestCode = DATE_PICKER_REQUEST_CODE,
            fieldId = fieldId.toInt(),
            pickMode = DateTimePickerFragment.PickMode.DATE,
            selectedDateTime = currentDate.toLocalDateTime(12, 0, 0).toJodaDateTime(),
            minDateTime = minDate?.toLocalDateTime(0, 0, 0)?.toJodaDateTime(),
            maxDateTime = maxDate?.toLocalDateTime(23, 59, 59)?.toJodaDateTime(),
        )

        showDatePickerFragment(datePickerFragment)
    }

    override fun pickTime(fieldId: HuntingControlEventField, currentTime: LocalTime?) {
        val date = LocalDate(2022, 1, 1)
        val datePickerFragment = DateTimePickerFragment.create(
            requestCode = DATE_PICKER_REQUEST_CODE,
            fieldId = fieldId.toInt(),
            pickMode = DateTimePickerFragment.PickMode.TIME,
            selectedDateTime = date.toLocalDateTime(
                currentTime?.hour ?: 12,
                currentTime?.minute ?: 0,
                currentTime?.second ?: 0
            ).toJodaDateTime(),
            minDateTime = null,
            maxDateTime = null,
        )

        showDatePickerFragment(datePickerFragment)
    }

    override fun onDateTimeSelected(fieldId: Int, dateTime: DateTime) {
        HuntingControlEventField.fromInt(fieldId)?.let { eventField ->
            when (eventField.type) {
                HuntingControlEventField.Type.DATE -> {
                    getController().eventDispatchers.localDateEventDispatcher.dispatchLocalDateChanged(
                        fieldId = eventField,
                        value = LocalDate.fromJodaLocalDate(dateTime.toLocalDate())
                    )
                }
                HuntingControlEventField.Type.START_TIME,
                HuntingControlEventField.Type.END_TIME -> {
                    getController().eventDispatchers.localTimeEventDispatcher.dispatchLocalTimeChanged(
                        fieldId = eventField,
                        value = LocalTime.fromJodaLocalTime(dateTime.toLocalTime())
                    )
                }
                else -> {
                    Log.w("MHCEF", "Invalid fieldId $eventField onDateTimeSelected")
                }
            }
        }
    }

    private fun handleSelectStringWithIdResult(data: Intent) {
        val fieldId = HuntingControlEventField.fromInt(SelectStringWithIdActivity.getFieldIdFromIntent(data))
        val selectedValue = SelectStringWithIdActivity.getStringWithIdResulListFromIntent(data)

        if (fieldId != null && selectedValue != null) {
            getController().eventDispatchers.stringWithIdEventDispatcher.dispatchStringWithIdChanged(
                fieldId = fieldId,
                value = selectedValue,
            )
        }
    }

    private fun setLocation(location: Location, source: String) {
        val coordinates = MapUtils.WGS84toETRSTM35FIN(location.latitude, location.longitude)
        if (coordinates.first != null && coordinates.second != null) {
            val geoLocation = ETRMSGeoLocation(
                coordinates.first.toInt(),
                coordinates.second.toInt(),
                source.toBackendEnum(),
                // todo: consider passing accuracy and other values as well
                null, null, null)
            getController().eventDispatchers.locationEventDispatcher.dispatchLocationChanged(
                fieldId = HuntingControlEventField.Type.LOCATION.toField(),
                value = geoLocation,
            )
        }
    }

    private fun attachmentClicked(field: HuntingControlEventField, fileName: String) {
        val attachment = getController().getAttachment(field)
        if (attachment != null) {
            if (attachment.uuid != null) {
                showAttachment(attachment)
            } else {
                val confirmation = AttachmentConfirmation(attachment.remoteId, fileName)
                val jsonConfirmation = confirmation.serializeToJson()
                AlertDialogFragment.Builder(
                    requireContext(),
                    AlertDialogId.MODIFY_HUNTING_CONTROL_EVENT_FRAGMENT_ATTACHMENT_DOWNLOAD_CONFIRMATION
                )
                    .setMessage(R.string.hunting_control_download_attachment_question)
                    .setPositiveButton(R.string.yes, jsonConfirmation)
                    .setNegativeButton(R.string.no)
                    .build()
                    .show(requireActivity().supportFragmentManager)
            }
        }
    }

    private fun startAttachmentDownload(value: String?) {
        value?.let { json ->
            val attachmentConfirmation = json.deserializeFromJson<AttachmentConfirmation>()
            attachmentConfirmation?.let { confirmation ->
                val remoteId = confirmation.remoteId
                if (remoteId != null) {
                    val url = URL(AppConfig.getBaseUrl() + "/huntingcontrol/attachment/$remoteId/download")
                    appDownloadManager.startDownload(url, confirmation.fileName)
                }
            }
        }
    }

    private fun deleteAttachmentClicked(fieldId: HuntingControlEventField, filename: String) {
        AlertDialogFragment.Builder(requireContext(), AlertDialogId.MODIFY_HUNTING_CONTROL_EVENT_FRAGMENT_DELETE_ATTACHMENT)
            .setTitle(getString(R.string.group_hunting_are_you_sure))
            .setMessage(getString(R.string.hunting_control_delete_attachment_question, filename))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.yes, fieldId.toInt().toString())
            .setNegativeButton(R.string.no, null)
            .build()
            .show(requireActivity().supportFragmentManager)
    }

    private fun deleteAttachment(value: String?) {
        val intValue = value?.toInt()
        intValue?.let {
            val fieldId = HuntingControlEventField.fromInt(intValue)
            fieldId?.let {
                getController().eventDispatchers.attachmentActionEventDispatcher.dispatchEvent(fieldId)
            }
        }
    }

    private fun addAttachmentClicked(field: HuntingControlEventField) {
        if (field.type == HuntingControlEventField.Type.ADD_ATTACHMENT) {
            val context = requireContext()
            val items = arrayOf<CharSequence>(
                context.getString(R.string.take_picture),
                context.getString(R.string.hunting_control_select_attachment)
            )
            AlertDialogFragment.Builder(context, AlertDialogId.MODIFY_HUNTING_CONTROL_EVENT_FRAGMENT_ADD_ATTACHMENT)
                .setTitle(context.getString(R.string.hunting_control_add_attachment))
                .setItems(items)
                .build()
                .show(requireActivity().supportFragmentManager)
        }
    }

    private fun addAttachment(which: Int) {
        if (which == 0) {
            if (PermissionHelper.hasPhotoPermissions()) {
                takePicture()
            } else {
                PermissionHelper.requestPhotoPermissions(requireActivity(), 111) // result is ignored for now
            }
        } else {
            chooseFile()
        }
    }

    private fun chooseFile() {
        val chooseFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooseFileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
        chooseFileIntent.type = "*/*"
        selectFileActivityResultLaunch.launch(
            Intent.createChooser(
                chooseFileIntent,
                getString(R.string.hunting_control_select_attachment)
            )
        )
    }

    private fun takePicture() {
        val context = requireContext()
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            var imageFile: File? = null
            try {
                imageFile = DiaryImageUtil.createImageFile(context)
            } catch (ignored: IOException) {
            }
            if (imageFile != null) {
                pictureUri = Uri.fromFile(imageFile)
                val appContext: Context = context.applicationContext
                val cameraUri = FileProvider.getUriForFile(
                    appContext, appContext.packageName + ".provider", imageFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
                captureImageActivityResultLaunch.launch(takePictureIntent)
            }
        }
    }

    companion object {
        private const val PICTURE_URI_KEY = "MHCEF_picture_uri_key"
        private const val DATE_PICKER_REQUEST_CODE = "MHCEF_date_picker_request_code"
    }
}

fun LocalDate.toLocalDateTime(hour: Int, minute: Int, second: Int): LocalDateTime {
    return LocalDateTime(
        year = year,
        monthNumber = monthNumber,
        dayOfMonth = dayOfMonth,
        hour = hour,
        minute = minute,
        second = second,
    )
}

fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}
