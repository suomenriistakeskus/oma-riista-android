package fi.riista.common.ui.dataField


interface DataFieldProducer<Model, FieldId : DataFieldId> {
    fun produceDataField(model: Model): DataField<FieldId>?
}

class DataFieldProducerProxy<Model, FieldId : DataFieldId>(
    private val delegateProducer: (model: Model) -> DataField<FieldId>?
): DataFieldProducer<Model, FieldId> {
    override fun produceDataField(model: Model): DataField<FieldId>? {
        return delegateProducer(model)
    }
}