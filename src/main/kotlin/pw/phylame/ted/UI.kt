package pw.phylame.ted

import jclp.value.Values
import mala.core.App
import mala.core.App.tr
import mala.core.AppSettings
import mala.core.map
import mala.ixin.*
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

object Form : IForm(tr("app.name")) {
    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                Ted.performed("exitApp")
            }
        })
        val resourceManager = App.resourceManager
        iconImage = resourceManager.imageFor("icon")
        init(resourceManager.designerFor("ui/app")!!, Ted, App, resourceManager)
        contentPane.add(Editor, BorderLayout.CENTER)
        statusBar?.add(JSeparator(), BorderLayout.PAGE_START)
        statusBar?.add(Indicator, BorderLayout.LINE_END)
        statusBar?.border = BorderFactory.createEmptyBorder(0, 2, 0, 2)
        size = 1244 x 700
        setLocationRelativeTo(null)
        statusText = tr("ready")
    }

    override fun initActions() {
        actions.updateKeys(App.resourceManager.keymapFor("ui/keys"))

        var visible = UISettings.isToolBarVisible
        actions["showToolBar"].isSelected = visible
        toolBar?.isVisible = visible

        visible = UISettings.isStatusBarVisible
        actions["showStatusBar"].isSelected = visible
        statusBar?.isVisible = visible
    }

    @Command
    fun showToolBar() {
        UISettings.isToolBarVisible = toolBar?.toggleVisible() ?: true
    }

    @Command
    fun showStatusBar() {
        UISettings.isStatusBarVisible = statusBar?.toggleVisible() ?: true
    }
}

object Indicator : JPanel() {
    val cursor: JLabel = JLabel()

    init {
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        cursor.toolTipText = App.tr("cursor.tip")
        cursor.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 1 && e.isLeft) {
                    Ted.performed("gotoPosition")
                }
            }
        })
        addItem(cursor)
        addItem(selectionLabel("LF"))
        addItem(selectionLabel("UTF-8"))
        val button = JToggleButton(Form.actions["toggleReadonly"])
        button.icon = App.resourceManager.iconFor("misc/unlocked")
        button.selectedIcon = App.resourceManager.iconFor("misc/locked")
        addItem(button)
        add(JSeparator(JSeparator.VERTICAL))
    }

    fun updateCursor(row: Int, column: Int, selection: Int) {
        cursor.text = "$row:$column${if (selection != 0) "/$selection" else ""}"
    }

    fun addItem(comp: Component) {
        if (comp is AbstractButton) {
            comp.border = null
            comp.isFocusable = false
            comp.hideActionText = true
            comp.isRolloverEnabled = false
            comp.isContentAreaFilled = false
        }
        add(JSeparator(JSeparator.VERTICAL))
        add(Box.createRigidArea(4 x 0))
        add(comp)
        add(Box.createRigidArea(4 x 0))
    }

    private fun selectionLabel(text: String, command: String = ""): JLabel {
        val label = JLabel(text, App.resourceManager.iconFor("misc/arrow"), JLabel.LEADING)
        label.horizontalTextPosition = JLabel.LEADING
        label.isFocusable = false
        label.iconTextGap = 1
        label.border = null
        if (command.isNotEmpty()) {
            label.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 1 && e.isLeft) {
                        Ted.performed(command)
                    }
                }
            })
        }
        return label
    }
}

object UISettings : AppSettings("ui.cfg") {
    var iconSet by map(IxIn.iconSet)

    var swingTheme by map(IxIn.swingTheme)

    var isAntiAliasing by map(IxIn.isAntiAliasing)

    var isMnemonicEnable by map(IxIn.isMnemonicEnable)

    var globalFont by map(Values.wrap(IxIn.globalFont))

    var isToolBarVisible by map(true)

    var isStatusBarVisible by map(true)
}
