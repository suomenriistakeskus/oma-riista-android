package fi.riista.common.ui.dataField

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.reactive.Observable

@Suppress("MemberVisibilityCanBePrivate")
abstract class DataFieldController<Model, FieldId : DataFieldId, ViewModel : DataFieldViewModel<FieldId>>(
    val viewModel: Observable<ViewModel>,
) {
    protected var dataFieldProducers: List<DataFieldProducer<Model, FieldId>> = listOf()

    constructor(viewModel: ViewModel): this(Observable(viewModel))

    init {
        ensureNeverFrozen()
    }

    protected fun produceDataFields(model: Model): DataFields<FieldId> {
        return dataFieldProducers.mapNotNull { it.produceDataField(model) }
    }
}