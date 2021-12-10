package fi.riista.mobile.ui

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

/**
 * A [RecyclerView.ItemAnimator] that acts just like [DefaultItemAnimator] with the exception
 * that change animations are not performed.
 *
 * By preventing change animations, the [RecyclerView] can reuse same ViewHolders which
 * is extra nice when item contains e.g. an EditText. By not recycling the ViewHolder
 * the EditText can keep the focus and cursor position.
 */
class NoChangeAnimationsItemAnimator : DefaultItemAnimator() {
    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder?,
        newHolder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        // don't animate changes. Instead call dispatchChangeFinished according to
        // SimpleItemAnimator documentation (base class of DefaultItemAnimator)
        if (oldHolder != newHolder) {
            dispatchChangeFinished(oldHolder, true)
        }

        if (newHolder != null) {
            dispatchChangeFinished(newHolder, false)
        }
        return false
    }

    override fun getSupportsChangeAnimations(): Boolean {
        return false
    }

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun canReuseUpdatedViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ): Boolean {
        return true
    }
}