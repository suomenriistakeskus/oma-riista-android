package fi.riista.mobile.pages

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.OperationResult
import fi.riista.mobile.R
import fi.riista.mobile.models.shootingTest.ShootingTestCalendarEvent
import fi.riista.mobile.models.shootingTest.ShootingTestOfficial
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.registerAlertDialogFragmentResultListener
import fi.riista.mobile.utils.DateTimeUtils
import fi.riista.mobile.utils.toVisibility
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class ShootingTestEventFragment : ShootingTestTabContentFragment() {
    @JvmField
    @Inject
    var viewModelFactory: ViewModelProvider.Factory? = null

    private lateinit var viewModel: ShootingTestMainViewModel
    private val shootingTestContext = RiistaSDK.shootingTestContext

    private var calendarEvent: ShootingTestCalendarEvent? = null
    private val selectedOfficials: MutableList<ShootingTestOfficial> = mutableListOf()
    private val availableOfficials: MutableList<ShootingTestOfficial> = mutableListOf()
    private var shootingTestResponsibleOccupationId: Long? = null
    private val selectedOfficialsMaster: MutableList<ShootingTestOfficial> = mutableListOf()
    private val availableOfficialsMaster: MutableList<ShootingTestOfficial> = mutableListOf()
    private var shootingTestResponsibleOccupationIdMaster: Long? = null
    private var isEditing = false
    private var hasSelectedOfficials = false
    private var hasAvailableOfficials = false

    private lateinit var eventTitleTextView: TextView
    private lateinit var eventDetailsTextView: TextView
    private lateinit var eventSumOfPaymentsTextView: TextView
    private lateinit var selectedOfficialsView: LinearLayout
    private lateinit var availableOfficialsView: LinearLayout
    private lateinit var startEventBtn: Button
    private lateinit var editEventBtn: Button
    private lateinit var closeEventBtn: Button
    private lateinit var reopenEventBtn: Button
    private lateinit var editButtonView: ViewGroup
    private lateinit var saveButton: AppCompatButton
    private lateinit var cancelButton: AppCompatButton

    private val onRemoveClickListener = View.OnClickListener { view: View ->
        val event = calendarEvent
        if (isEditing || event != null && event.isWaitingToStart) {
            val personId = view.tag as Long
            for (official in selectedOfficials) {
                if (official.personId == personId) {
                    availableOfficials.add(official)
                    selectedOfficials.remove(official)
                    break
                }
            }
            populateListView(selectedOfficialsView, selectedOfficials, true, isEditing)
            populateListView(availableOfficialsView, availableOfficials, false, isEditing)
        }
        refreshButtonStates()
    }
    private val mOnAddClickListener = View.OnClickListener { view: View ->
        val event = calendarEvent
        if (isEditing || event != null && event.isWaitingToStart) {
            val personId = view.tag as Long
            for (official in availableOfficials) {
                if (official.personId == personId) {
                    selectedOfficials.add(official)
                    availableOfficials.remove(official)
                    break
                }
            }
            populateListView(selectedOfficialsView, selectedOfficials, true, isEditing)
            populateListView(availableOfficialsView, availableOfficials, false, isEditing)
        }
        refreshButtonStates()
    }
    private val mOnMakeResponsibleClickListener = View.OnClickListener { view: View ->
        val event = calendarEvent
        if (isEditing || event != null && event.isWaitingToStart) {
            val occupationId = view.tag as Long
            if (shootingTestResponsibleOccupationId == occupationId) {
                return@OnClickListener
            }
            shootingTestResponsibleOccupationId = occupationId

            populateListView(selectedOfficialsView, selectedOfficials, true, isEditing)
        }
        refreshButtonStates()
    }

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private fun filterAvailableOfficials(availableOfficials: List<ShootingTestOfficial>) {
        if (hasSelectedOfficials && hasAvailableOfficials) {
            val availableAndNotSelected: MutableList<ShootingTestOfficial> = availableOfficials.toMutableList()
            this.availableOfficials.clear()
            for (selected in selectedOfficials) {
                for (available in availableOfficials) {
                    if (available.personId == selected.personId) {
                        availableAndNotSelected.remove(available)
                    }
                }
            }
            this.availableOfficials.addAll(availableAndNotSelected)
        }
    }

    private fun refreshButtonStates() {
        editEventBtn.isEnabled = hasAvailableOfficials && hasSelectedOfficials
        startEventBtn.isEnabled = selectedOfficials.size >= 2
        saveButton.isEnabled = selectedOfficials.size >= 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shooting_test_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory!!)[ShootingTestMainViewModel::class.java]
        viewModel.selectedOfficials.observe(viewLifecycleOwner) { selectedOfficials: List<ShootingTestOfficial> ->
            selectedOfficialsMaster.clear()
            shootingTestResponsibleOccupationIdMaster = null
            if (selectedOfficials.isNotEmpty()) {
                selectedOfficialsMaster.addAll(selectedOfficials)
                shootingTestResponsibleOccupationIdMaster = selectedOfficials
                    .firstOrNull { it.shootingTestResponsible }?.occupationId
            }

            this.selectedOfficials.clear()
            if (selectedOfficialsMaster.isNotEmpty()) {
                hasSelectedOfficials = true
                this.selectedOfficials.addAll(selectedOfficialsMaster)
                filterAvailableOfficials(availableOfficials)
                refreshButtonStates()
            }
            this.shootingTestResponsibleOccupationId = shootingTestResponsibleOccupationIdMaster
            selectedOfficialsView.removeAllViews()
            populateListView(selectedOfficialsView, this.selectedOfficials, true, isEditing)
        }
        viewModel.availableOfficials.observe(viewLifecycleOwner) { availableOfficials: List<ShootingTestOfficial> ->
            availableOfficialsMaster.clear()
            if (availableOfficials.isNotEmpty()) {
                availableOfficialsMaster.addAll(availableOfficials)
            }
            this.availableOfficials.clear()
            if (availableOfficialsMaster.isNotEmpty()) {
                hasAvailableOfficials = true
                filterAvailableOfficials(availableOfficialsMaster)
                refreshButtonStates()
            }
            availableOfficialsView.removeAllViews()
            populateListView(availableOfficialsView,
                this.availableOfficials, false, isEditing)
        }
        viewModel.calendarEvent.observe(viewLifecycleOwner) { calendarEvent: ShootingTestCalendarEvent? ->
            if (calendarEvent != null) {
                this.calendarEvent = calendarEvent
                setEditingOfficials(false)
                val waitingToStart = calendarEvent.isWaitingToStart
                populateListView(availableOfficialsView, availableOfficials, false, waitingToStart)
                populateListView(selectedOfficialsView, selectedOfficials, true, waitingToStart)
                updateTitle(calendarEvent)
                updateButtonVisibility(calendarEvent)
                availableOfficialsView.visibility = waitingToStart.toVisibility()
                if (waitingToStart) {
                    hasSelectedOfficials = true
                    filterAvailableOfficials(availableOfficialsMaster)
                }
                refreshOfficialsData()
            }
        }

        eventTitleTextView = view.findViewById(R.id.shooting_test_event_title)
        eventDetailsTextView = view.findViewById(R.id.shooting_test_event_details)
        eventSumOfPaymentsTextView = view.findViewById(R.id.shooting_test_event_sum_of_payments)
        selectedOfficialsView = view.findViewById(R.id.shooting_test_selected_officials)
        availableOfficialsView = view.findViewById(R.id.shooting_test_available_officials)
        startEventBtn = view.findViewById<Button?>(R.id.shooting_test_start_event_btn).also { button ->
            button.setOnClickListener { onStartClick() }
        }
        editEventBtn = view.findViewById<Button?>(R.id.shooting_test_edit_event_btn).also { button ->
            button.setOnClickListener { onEditClick() }
        }
        closeEventBtn = view.findViewById<Button?>(R.id.shooting_test_finish_event_btn).also { button ->
            button.setOnClickListener { onCloseClick() }
        }
        reopenEventBtn = view.findViewById<Button?>(R.id.shooting_test_reopen_event_btn).also { button ->
            button.setOnClickListener { onReopenClick() }
        }
        editButtonView = view.findViewById(R.id.edit_button_view)
        saveButton = view.findViewById<AppCompatButton?>(R.id.save_btn).also { button ->
            button.setOnClickListener { onSaveOfficialEditClick() }
        }
        cancelButton = view.findViewById<AppCompatButton?>(R.id.cancel_btn).also { button ->
            button.setOnClickListener { onCancelEditOfficialsClick() }
        }

        requireActivity().registerAlertDialogFragmentResultListener(
            dialogId = AlertDialogId.SHOOTING_TEST_EVENT_FRAGMENT_CLOSE_QUESTION,
            onPositive = ::onClose
        )
        requireActivity().registerAlertDialogFragmentResultListener(
            dialogId = AlertDialogId.SHOOTING_TEST_EVENT_FRAGMENT_REOPEN_QUESTION,
            onPositive = ::onReopen
        )
    }

    override fun onResume() {
        super.onResume()
        refreshEventData()
    }

    override fun onPause() {
        super.onPause()
        setEditingOfficials(false)
    }

    private fun onStartClick() {
        val officialIds: MutableList<Long> = mutableListOf()
        for (official in selectedOfficials) {
            officialIds.add(official.occupationId)
        }
        val testEventId = viewModel.testEventId
        val calendarEventId = viewModel.calendarEventId
        if (officialIds.size >= 2 && testEventId == null && calendarEventId != null) {
            startEvent(
                officialIds = officialIds,
                responsibleOfficialOccupationId = shootingTestResponsibleOccupationId,
                calendarEventId = calendarEventId,
                testEventId = testEventId
            )
        }
    }

    private fun onEditClick() {
        setEditingOfficials(true)
    }

    private fun onCloseClick() {
        AlertDialogFragment.Builder(requireContext(), AlertDialogId.SHOOTING_TEST_EVENT_FRAGMENT_CLOSE_QUESTION)
            .setMessage(requireContext().getString(R.string.confirm_operation_prompt))
            .setPositiveButton(R.string.yes)
            .setNegativeButton(R.string.no)
            .build()
            .show(requireActivity().supportFragmentManager)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onClose(s: String?) {
        viewModel.testEventId?.let { testEventId ->
            closeEvent(testEventId)
        }
    }

    private fun onReopenClick() {
        AlertDialogFragment.Builder(requireContext(), AlertDialogId.SHOOTING_TEST_EVENT_FRAGMENT_REOPEN_QUESTION)
            .setMessage(requireContext().getString(R.string.confirm_operation_prompt))
            .setPositiveButton(R.string.yes)
            .setNegativeButton(R.string.no)
            .build()
            .show(requireActivity().supportFragmentManager)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onReopen(s: String?) {
        viewModel.testEventId?.let { testEventId ->
            reopenEvent(testEventId)
        }
    }

    private fun onCancelEditOfficialsClick() {
        if (isEditing) {
            setEditingOfficials(false)
            selectedOfficials.clear()
            if (selectedOfficialsMaster.isNotEmpty()) {
                selectedOfficials.addAll(selectedOfficialsMaster)
            }
            populateListView(selectedOfficialsView, selectedOfficials, true, isEditing)
            availableOfficials.clear()
            filterAvailableOfficials(availableOfficialsMaster)
            populateListView(availableOfficialsView, availableOfficials, false, isEditing)
        }
    }

    private fun onSaveOfficialEditClick() {
        if (isEditing) {
            val officialIds: MutableList<Long> = mutableListOf()
            for (official in selectedOfficials) {
                officialIds.add(official.occupationId)
            }

            val testEventId = viewModel.testEventId
            val calendarEventId = viewModel.calendarEventId
            if (officialIds.size >= 2 && testEventId != null && calendarEventId != null) {
                updateEventOfficials(
                    officialIds = officialIds,
                    responsibleOfficialOccupationId = shootingTestResponsibleOccupationId,
                    calendarEventId = calendarEventId,
                    testEventId = testEventId,
                )
            }
        }
    }

    private fun startEvent(
        officialIds: List<Long>,
        responsibleOfficialOccupationId: Long?,
        calendarEventId: Long,
        testEventId: Long?
    ) {
        MainScope().launch {
            val response = shootingTestContext.openShootingTestEvent(
                calendarEventId = calendarEventId,
                shootingTestEventId = testEventId,
                occupationIds = officialIds,
                responsibleOccupationId = responsibleOfficialOccupationId,
            )

            if (!isResumed) {
                return@launch
            }

            when (response) {
                is OperationResult.Success -> refreshEventData()
                is OperationResult.Failure -> {
                    val errorMsg = if (response.statusCode != null) {
                        getString(R.string.error_operation_failed, response.statusCode)
                    } else {
                        getString(R.string.operation_failed)
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateEventOfficials(
        officialIds: List<Long>,
        responsibleOfficialOccupationId: Long?,
        calendarEventId: Long,
        testEventId: Long
    ) {
        MainScope().launch {
            val response = shootingTestContext.updateShootingTestOfficials(
                calendarEventId = calendarEventId,
                shootingTestEventId = testEventId,
                officialOccupationIds = officialIds,
                responsibleOccupationId = responsibleOfficialOccupationId,
            )

            if (!isResumed) {
                return@launch
            }

            when (response) {
                is OperationResult.Success -> {
                    setEditingOfficials(false)
                    refreshEventData()
                }
                is OperationResult.Failure -> {
                    val errorMsg = if (response.statusCode != null) {
                        getString(R.string.error_operation_failed, response.statusCode)
                    } else {
                        getString(R.string.operation_failed)
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun closeEvent(eventId: Long) {
        MainScope().launch {
            val response = shootingTestContext.closeShootingTestEvent(shootingTestEventId = eventId)

            if (!isResumed) {
                return@launch
            }

            when (response) {
                is OperationResult.Success -> {
                    refreshEventData()
                }
                is OperationResult.Failure -> {
                    val errorMsg = if (response.statusCode != null) {
                        getString(R.string.error_operation_failed, response.statusCode)
                    } else {
                        getString(R.string.operation_failed)
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun reopenEvent(eventId: Long) {
        MainScope().launch {
            val response = shootingTestContext.reopenShootingTestEvent(shootingTestEventId = eventId)

            if (!isResumed) {
                return@launch
            }

            when (response) {
                is OperationResult.Success -> {
                    refreshEventData()
                }
                is OperationResult.Failure -> {
                    val errorMsg = if (response.statusCode != null) {
                        getString(R.string.error_operation_failed, response.statusCode)
                    } else {
                        getString(R.string.operation_failed)
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateTitle(event: ShootingTestCalendarEvent) {
        val localisedDateText = DateTimeUtils.convertDateStringToFinnishFormat(event.date)
        eventTitleTextView.text = String.format(
            "%s %s\n%s %s %s",
            ShootingTestCalendarEvent.localisedEventTypeText(context, event.calendarEventType)
                .uppercase(Locale.getDefault()),
            if (TextUtils.isEmpty(event.name)) "" else event.name,
            localisedDateText,
            event.beginTime,
            if (TextUtils.isEmpty(event.endTime)) "" else "- " + event.endTime
        )
        eventDetailsTextView.text = String.format(
            "%s\n%s\n%s",
            if (event.venue.name != null) event.venue.name else "",
            if (event.venue.address.streetAddress != null) event.venue.address.streetAddress else "",
            if (event.venue.address.city != null) event.venue.address.city else ""
        )
        eventSumOfPaymentsTextView.text =
            getString(R.string.shooting_test_event_sum_of_payments, event.totalPaidAmount)
    }

    private fun updateButtonVisibility(event: ShootingTestCalendarEvent) {
        val isUserSelectedAsOfficial = viewModel.isUserSelectedOfficial.value ?: false
        val isUserCoordinator = viewModel.isUserCoordinator.value ?: false
        startEventBtn.visibility = if (event.isWaitingToStart) View.VISIBLE else View.GONE
        closeEventBtn.visibility = (!isEditing && event.isReadyToClose && (isUserSelectedAsOfficial || isUserCoordinator)).toVisibility()
        reopenEventBtn.visibility = (!isEditing && event.isClosed).toVisibility()
        editEventBtn.visibility = (!isEditing && event.isOngoing).toVisibility()
    }

    private fun setEditingOfficials(enabled: Boolean) {
        isEditing = enabled
        editButtonView.visibility = isEditing.toVisibility()
        populateListView(selectedOfficialsView, selectedOfficials, true, enabled)
        populateListView(availableOfficialsView, availableOfficials, false, enabled)
        availableOfficialsView.visibility = isEditing.toVisibility()
        calendarEvent?.let { event -> updateButtonVisibility(event) }
    }

    private fun refreshEventData() {
        viewModel.refreshCalendarEvent()
        viewModel.refreshParticipants()
    }

    private fun refreshOfficialsData() {
        viewModel.refreshSelectedOfficials()
        viewModel.refreshAvailableOfficials()
    }

    private fun populateListView(
        parent: ViewGroup,
        data: List<ShootingTestOfficial>,
        isSelected: Boolean,
        isEdit: Boolean
    ) {
        parent.removeAllViews()
        val event = calendarEvent
        for (item in data) {
            parent.addView(
                createOfficialView(
                    item = item,
                    parent = parent,
                    isSelected = isSelected,
                    isShootingTestResponsible = isSelected && shootingTestResponsibleOccupationId == item.occupationId,
                    isEdit = isEdit || event != null && event.isWaitingToStart,
                )
            )
        }
    }

    private fun createOfficialView(
        item: ShootingTestOfficial,
        parent: ViewGroup?,
        isSelected: Boolean,
        isShootingTestResponsible: Boolean,
        isEdit: Boolean
    ): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.view_shooting_official_item, parent, false)
        val nameLabel = view.findViewById<TextView>(R.id.official_name)
        nameLabel.text = String.format("%s %s", item.lastName, item.firstName)

        val makeResponsibleButton = view.findViewById<AppCompatImageButton>(R.id.make_responsible_btn)
        makeResponsibleButton.setImageResource(
            when (isShootingTestResponsible) {
                true -> R.drawable.star_filled
                false -> R.drawable.star
            }
        )
        makeResponsibleButton.visibility = isSelected.toVisibility()
        makeResponsibleButton.tag = item.occupationId

        if (isEdit) {
            makeResponsibleButton.setOnClickListener(mOnMakeResponsibleClickListener)
            if (isSelected) {
                val removeButton = view.findViewById<Button>(R.id.remove_official_btn)
                removeButton.setOnClickListener(onRemoveClickListener)
                removeButton.visibility = View.VISIBLE
                removeButton.tag = item.personId
            } else {
                val addButton = view.findViewById<Button>(R.id.add_official_btn)
                addButton.setOnClickListener(mOnAddClickListener)
                addButton.visibility = View.VISIBLE
                addButton.tag = item.personId
            }
        }
        return view
    }
}
