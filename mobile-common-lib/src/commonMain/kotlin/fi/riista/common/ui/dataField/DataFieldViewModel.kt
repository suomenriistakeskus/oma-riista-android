package fi.riista.common.ui.dataField

abstract class DataFieldViewModel<FieldId : DataFieldId> {
    abstract val fields: DataFields<FieldId>
}
