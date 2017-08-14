package pw.phylame.ted

import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener
import javax.swing.event.UndoableEditEvent
import javax.swing.event.UndoableEditListener
import javax.swing.undo.UndoManager
import javax.swing.undo.UndoableEdit

fun JComponent.scrolled(): JScrollPane = JScrollPane(this)

val JTextArea.column get() = caretPosition - getLineStartOffset(row)

val JTextArea.row get() = getLineOfOffset(caretPosition)

abstract class TextSupport(text: JTextArea) : CaretListener, UndoableEditListener {
    var isModified = false
        set(value) {
            if (value != field) {
                field = value
                if (!value) { // not modified
                    edit = undo.nextUndo
                }
                textUpdated(false)
            }
        }

    val undoAction = {
        if (undo.canUndo()) {
            undo.undo()
            if (!isModified) {
                textUpdated(true)
            } else if (edit === undo.nextUndo) {
                isModified = false
            }
        }
    }

    val redoAction = {
        if (undo.canRedo()) {
            undo.redo()
            if (!isModified) {
                textUpdated(true)
            } else if (edit === undo.nextUndo) {
                isModified = false
            }
        }
    }

    init {
        text.addCaretListener(this)
        text.document.addUndoableEditListener(this)
    }

    abstract fun textUpdated(modified: Boolean)

    abstract fun cursorUpdated(row: Int, column: Int, selection: Int)

    private var dot = 0
    private var mark = 0
    private val undo = UndoSupport()
    private var edit: UndoableEdit? = null

    override fun caretUpdate(e: CaretEvent) {
        if (e.dot != dot && e.mark != mark) {
            dot = e.dot
            mark = e.mark
            val text = e.source as JTextArea
            cursorUpdated(text.row, text.column, Math.abs(e.mark - e.dot))
        }
    }

    override fun undoableEditHappened(e: UndoableEditEvent) {
        undo.addEdit(e.edit)
        isModified = true
    }

    class UndoSupport : UndoManager() {
        val nextUndo get() = this.editToBeUndone()
    }
}

class CaretIndicator : JLabel(), CaretListener {
    override fun caretUpdate(e: CaretEvent) {

    }
}