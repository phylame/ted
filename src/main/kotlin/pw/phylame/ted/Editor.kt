package pw.phylame.ted

import mala.core.App.tr
import mala.ixin.Command
import org.jdesktop.swingx.JXLabel
import org.jdesktop.swingx.JXPanel
import java.awt.Font
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File
import javax.swing.*

object Editor : ITabbedPane() {
    init {
        border = null
        isFocusable = false
        tabLayoutPolicy = SCROLL_TAB_LAYOUT
        addTabListener(object : TabListener {
            override fun tabCreated(e: TabEvent) {
                updateTabActions()
            }

            override fun tabActivated(e: TabEvent) {
                updateTitle(e.component as Tab)
            }

            override fun tabClosed(e: TabEvent) {
                updateTabActions()
            }
        })
    }

    private fun updateTitle(tab: Tab) {
        val title = tab.title + if (tab.isModified) "*" else ""
        Form.title = "$title - ${tr("app.name")} v${Ted.version}"
        tab.header.setTitle(title)
    }

    fun newTab(title: String = "Untitled") {
        val header = TabHeader()
        val tab = Tab(title, header)
        tab.addPropertyChangeListener("modified") {
            updateTitle(it.source as Tab)
        }
        addTab(title, tab)
        setTabComponentAt(tabCount - 1, header)
        selectedIndex = tabCount - 1
    }

    fun closeTab(index: Int) {
    }

    private fun updateTabActions() {
        val count = tabCount
        Form.setEnable("nextTab", count > 1)
        Form.setEnable("previousTab", count > 1)
        Form.setEnable("closeActiveTab", count != 0)
        Form.setEnable("closeOtherTabs", count > 1)
        Form.setEnable("closeAllTabs", count != 0)
        Form.setEnable("closeUnmodifiedTabs", count != 0)
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

    @Command
    fun closeActiveTab() {
        removeTabAt(selectedIndex)
    }
}

class TabHeader : JXPanel() {
    private val label = JXLabel()

    init {
        isOpaque = false
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        this += label
        this += Box.createHorizontalStrut(5)
        this += Form.actions["closeActiveTab"].toImageButton()
    }

    fun setTitle(title: String) {
        label.text = title
    }
}

class Tab(val title: String, val header: TabHeader) : JScrollPane() {
    private val textArea = JTextArea()
    private val undoHelper = UndoHelper()
    private var file: File? = null

    init {
        setViewportView(textArea)
        textArea.addPropertyChangeListener {
            when (it.propertyName) {
                "editable" -> {

                }
            }
        }
        textArea.font = Font.getFont("ted.editor.font")
        textArea.document.addUndoableEditListener(undoHelper)
        undoHelper.addChangeListener {
            firePropertyChange("modified", !undoHelper.isModified, undoHelper.isModified)
        }
    }

    internal val isModified get() = undoHelper.isModified

    internal fun undo() {
        undoHelper.undo()
    }

    internal fun redo() {
        undoHelper.redo()
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
        textArea.replaceSelection(null)
    }

    internal fun selectAll() {
        textArea.selectAll()
    }

    internal fun toggleReadonly() {
        textArea.isEditable = !textArea.isEditable
        updateActions()
    }

    internal fun onActivated() {
        Indicator.updateCursor(1, 1, 0)
        textArea.requestFocus()
        updateActions()
    }

    private fun updateActions() {
        val editable = textArea.isEditable
        Form.setEnable("saveFile", undoHelper.isModified || file == null)
//        Form.setEnable("refreshFile", editable && file != null)
//        Form.setEnable("fileDetails", editable && file != null)

        Form.actions["undo"]?.apply {
            isEnabled = editable && undoHelper.canUndo()
            putValue(Action.NAME, undoHelper.undoPresentationName)
            putValue(Action.SHORT_DESCRIPTION, undoHelper.undoPresentationName)
        }
        Form.actions["redo"]?.apply {
            isEnabled = editable && undoHelper.canRedo()
            putValue(Action.NAME, undoHelper.redoPresentationName)
            putValue(Action.SHORT_DESCRIPTION, undoHelper.redoPresentationName)
        }
        val hasSelection = textArea.selectionEnd != textArea.selectionStart
        Form.setEnable("cut", editable && hasSelection)
        Form.setEnable("copy", editable && hasSelection)
        val contents = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
        Form.setEnable("paste", editable && contents?.isDataFlavorSupported(DataFlavor.stringFlavor) ?: false)
        Form.setEnable("delete", editable && hasSelection)
    }
}
