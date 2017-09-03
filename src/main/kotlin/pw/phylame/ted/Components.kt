package pw.phylame.ted

import mala.ixin.IForm
import mala.ixin.get
import org.jdesktop.swingx.JXButton
import java.awt.Component
import java.awt.Container
import java.util.*
import javax.swing.*
import javax.swing.event.*
import javax.swing.undo.UndoManager
import javax.swing.undo.UndoableEdit

fun JComponent.scrolled(): JScrollPane = JScrollPane(this)

operator fun Container.plusAssign(comp: Component) {
    add(comp)
}

val JTextArea.column get() = caretPosition - getLineStartOffset(row)

val JTextArea.row get() = getLineOfOffset(caretPosition)

abstract class TextSupport(text: JTextArea) : CaretListener, UndoableEditListener {
    val canUndo get() = undo.canUndo()

    val undoName: String get() = undo.undoPresentationName

    val canRedo get() = undo.canRedo()

    val redoName: String get() = undo.redoPresentationName

    var isModified = false
        set(value) {
            field = value
            if (!value) { // not modified
                edit = undo.nextUndo
            }
            textUpdated(value)
        }

    init {
        text.addCaretListener(this)
        text.document.addUndoableEditListener(this)
    }

    abstract fun textUpdated(modified: Boolean)

    abstract fun cursorUpdated(row: Int, column: Int, selection: Int)

    fun undo() {
        undo.undo()
        if (!isModified) {
            textUpdated(true)
        } else if (edit === undo.nextUndo) {
            isModified = false
        }
    }

    fun redo() {
        undo.redo()
        if (!isModified) {
            textUpdated(true)
        } else if (edit === undo.nextUndo) {
            isModified = false
        }
    }

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
        val nextUndo: UndoableEdit? get() = editToBeUndone()
    }
}

fun <L : EventListener, E : EventObject> EventListenerList.fireEvent(type: Class<L>, event: E, action: L.(E) -> Unit) {
    val listeners = listenerList
    for (i in listeners.size - 2 downTo 0 step 2) {
        if (listeners[i] === type) {
            action(type.cast(listeners[i + 1]), event)
        }
    }
}

class CaretIndicator : JLabel(), CaretListener {
    override fun caretUpdate(e: CaretEvent) {

    }
}

class UndoHelper : UndoManager() {
    var isModified = false
        set(value) {
            field = value
            if (!isModified) {
                edit = editToBeUndone()
            }
        }

    private var edit: UndoableEdit? = null

    private val listenerList = EventListenerList()

    private val changeEvent by lazy { ChangeEvent(this) }

    fun addChangeListener(l: ChangeListener) {
        listenerList.add(ChangeListener::class.java, l)
    }

    fun addChangeListener(action: (ChangeEvent) -> Unit) {
        listenerList.add(ChangeListener::class.java, ChangeListener {
            action(it)
        })
    }

    fun removeChangeListener(l: ChangeListener) {
        listenerList.remove(ChangeListener::class.java, l)
    }

    val changeListeners get() = listenerList.getListeners(ChangeListener::class.java)

    override fun undo() {
        super.undo()
        if (!isModified) {
            fireStateChanged(true)
        } else if (edit === editToBeUndone()) {
            fireStateChanged(false)
        }
    }

    override fun redo() {
        super.redo()
        if (!isModified) {
            fireStateChanged(true)
        } else if (edit === editToBeUndone()) {
            fireStateChanged(false)
        }
    }

    override fun undoableEditHappened(e: UndoableEditEvent?) {
        super.undoableEditHappened(e)
        fireStateChanged(true)
    }

    private fun fireStateChanged(modified: Boolean) {
        if (modified != isModified) {
            isModified = modified
            listenerList.fireEvent(ChangeListener::class.java, changeEvent, ChangeListener::stateChanged)
        }
    }
}

fun Action.toImageButton(): JXButton {
    val button = JXButton()
    button.addActionListener(this)
    button.actionCommand = this[Action.ACTION_COMMAND_KEY]
    button.icon = this[Action.SMALL_ICON]
    button.hideActionText = true
    button.isFocusable = false
    button.border = null
    button.isRolloverEnabled = true
    return button
}

fun IForm.setEnable(id: String, enable: Boolean) {
    actions[id]?.isEnabled = enable
}

class TabEvent(source: Any, val component: Component) : EventObject(source)

interface TabListener : EventListener {
    fun tabCreated(e: TabEvent) {}

    fun tabActivated(e: TabEvent) {}

    fun tabInactivated(e: TabEvent) {}

    fun tabClosed(e: TabEvent) {}
}

open class ITabbedPane : JTabbedPane() {
    fun addTabListener(l: TabListener) {
        listenerList.add(TabListener::class.java, l)
    }

    fun removeTabListener(l: TabListener) {
        listenerList.remove(TabListener::class.java, l)
    }

    val tabListeners get() = listenerList.getListeners(TabListener::class.java)

    override fun insertTab(title: String?, icon: Icon?, component: Component, tip: String?, index: Int) {
        super.insertTab(title, icon, component, tip, index)
        fireTabEvent(component, TabListener::tabCreated)
    }

    override fun setSelectedIndex(index: Int) {
        val currentIndex = selectedIndex
        val currentComponent = selectedComponent
        super.setSelectedIndex(index)
        if (currentIndex == -1) {
            return
        }
        if (currentIndex != index) {
            fireTabEvent(currentComponent, TabListener::tabInactivated)
        }
        fireTabEvent(selectedComponent, TabListener::tabActivated)
    }

    override fun removeTabAt(index: Int) {
        val component = selectedComponent
        super.removeTabAt(index)
        fireTabEvent(component, TabListener::tabClosed)
    }

    private fun fireTabEvent(component: Component, action: TabListener.(TabEvent) -> Unit) {
        listenerList.fireEvent(TabListener::class.java, TabEvent(this, component), action)
    }
}
