package pw.phylame.ted

import mala.core.App
import mala.core.AppSettings
import mala.core.map
import mala.ixin.Command
import mala.ixin.isSelected
import pw.phylame.ted.Editor.addChangeListener
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File
import javax.swing.Action
import javax.swing.JScrollPane
import javax.swing.JTabbedPane
import javax.swing.JTextArea

object Editor : JTabbedPane() {
    init {
        border = null
        isFocusable = false
        tabLayoutPolicy = SCROLL_TAB_LAYOUT
        addChangeListener {
            updateActions()
            ((it.source as Editor).selectedComponent as? Tab)?.apply {
                onActivated()
                Form.title = "$title - ${App.tr("app.name")} v${Ted.version}"
            }
        }
    }

    fun newTab(title: String = "Untitled") {
        addTab(title, Tab(title))
        selectedIndex = tabCount - 1
    }

    fun closeTab(index: Int) {
    }

    private fun updateActions() {
        val count = tabCount
        Form.actions["nextTab"]?.isEnabled = count > 1
        Form.actions["previousTab"]?.isEnabled = count > 1
        Form.actions["closeActiveTab"]?.isEnabled = count != 0
        Form.actions["closeOtherTabs"]?.isEnabled = count > 1
        Form.actions["closeAllTabs"]?.isEnabled = count != 0
        Form.actions["closeUnmodifiedTabs"]?.isEnabled = count != 0
    }

    @Command
    fun undo() {
        (selectedComponent as? Tab)?.undo()
    }

    @Command
    fun redo() {
        (selectedComponent as? Tab)?.redo()
    }

    @Command
    fun cut() {
        (selectedComponent as? Tab)?.cut()
    }

    @Command
    fun copy() {
        (selectedComponent as? Tab)?.copy()
    }

    @Command
    fun paste() {
        (selectedComponent as? Tab)?.paste()
    }

    @Command
    fun delete() {
        (selectedComponent as? Tab)?.delete()
    }

    @Command
    fun selectAll() {
        (selectedComponent as? Tab)?.selectAll()
    }

    @Command
    fun toggleReadonly() {
        (selectedComponent as? Tab)?.toggleReadonly()
    }
}

class Tab(val title: String) : JScrollPane() {
    private val textArea = JTextArea()
    private val support: TextSupport
    private var file: File? = null

    init {
        setViewportView(textArea)
        support = TextSupport(textArea)
        textArea.font = textArea.font.deriveFont(EditorSettings.fontSize.toFloat())

    }

    internal fun undo() {
        undoManager.undo()
        if (!undoManager.canUndo()) {
            isModified = false
        }
    }

    internal fun redo() {
        undoManager.redo()
    }

    internal fun cut() {
        textArea.cut()
    }

    internal fun copy() {
        textArea.copy()
    }

    internal fun paste() {
        textArea.paste()
    }

    internal fun delete() {
        textArea.document.remove(textArea.selectionStart, textArea.selectionEnd - textArea.selectionStart)
    }

    internal fun selectAll() {
        textArea.selectAll()
    }

    internal fun toggleReadonly() {
        textArea.isEditable = !textArea.isEditable
        updateActions()
    }

    internal fun onActivated() {
        textArea.requestFocus()
        updateActions()
        updateCursor()
    }

    private fun updateActions() {
        val editable = textArea.isEditable
        Form.actions["saveFile"]?.isEnabled = isModified || file == null
        Form.actions["refreshFile"]?.isEnabled = editable && file != null
        Form.actions["fileDetails"]?.isEnabled = editable && file != null
        Form.actions["toggleReadonly"]?.isSelected = !editable

        Form.actions["undo"]?.apply {
            isEnabled = editable && undoManager.canUndo()
            putValue(Action.NAME, undoManager.undoPresentationName)
            putValue(Action.SHORT_DESCRIPTION, undoManager.undoPresentationName)
        }
        Form.actions["redo"]?.apply {
            isEnabled = editable && undoManager.canRedo()
            putValue(Action.NAME, undoManager.redoPresentationName)
            putValue(Action.SHORT_DESCRIPTION, undoManager.redoPresentationName)
        }
        val hasSelection = textArea.selectionEnd != textArea.selectionStart
        Form.actions["cut"]?.isEnabled = editable && hasSelection
        Form.actions["copy"]?.isEnabled = editable && hasSelection
        val contents = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
        Form.actions["paste"]?.isEnabled = editable && contents?.isDataFlavorSupported(DataFlavor.stringFlavor) ?: false
        Form.actions["delete"]?.isEnabled = editable && hasSelection
    }

    private fun updateCursor() {
        val position = textArea.caretPosition
        val line = textArea.getLineOfOffset(position)
        val column = position - textArea.getLineStartOffset(line)
        val selection = textArea.selectionEnd - textArea.selectionStart
        Indicator.updateCursor(line + 1, column + 1, selection)
    }
}


object EditorSettings : AppSettings("editor.cfg") {
    var fontSize by map(14)
}

