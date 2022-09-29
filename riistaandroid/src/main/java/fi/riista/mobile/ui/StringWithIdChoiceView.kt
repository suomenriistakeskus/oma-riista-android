package fi.riista.mobile.ui

import android.content.Context
import android.util.AttributeSet
import fi.riista.common.model.StringWithId

class StringWithIdChoiceView(context: Context, attributeSet: AttributeSet?): ChoiceView<StringWithIdWrapper>(context, attributeSet)

class StringWithIdWrapper(val stringWithId: StringWithId) {
    val name: String = stringWithId.string

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is StringWithIdWrapper) {
            return false
        }
        return other.stringWithId == stringWithId
    }

    override fun hashCode(): Int {
        return stringWithId.hashCode() ?: 0
    }
}
