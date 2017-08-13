package pw.phylame.ted

import mala.core.App
import mala.ixin.*
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper
import java.util.concurrent.Executors

fun main(args: Array<String>) {
    App.run(Ted, args)
}

object Ted : IDelegate(), CommandListener {
    override val name = "ted"

    override val version = "1.0"

    val pool = lazy {
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    }

    override fun onStart() {
        App.translator = App.resourceManager.linguistFor("i18n/app")
    }

    override fun initUI() {
        BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.osLookAndFeelDecorated
        IxIn.isMnemonicEnable = UISettings.isMnemonicEnable
        IxIn.updateAntiAliasing(UISettings.isAntiAliasing)
        IxIn.updateSwingTheme(UISettings.swingTheme)
        IxIn.updateGlobalFont(UISettings.globalFont)
        IxIn.iconSet = UISettings.iconSet
        Form.isVisible = true
        pool.value.submit {
            proxy.addProxies(this, Form, Editor)
        }
        newFile()
    }

    override fun onStop() {
    }

    @Command
    fun newFile() {
        Editor.newTab()
    }

    @Command
    fun exitApp() {
        App.exit()
    }

    private val proxy = CommandDispatcher()

    override fun performed(command: String) {
        proxy.performed(command)
    }
}
