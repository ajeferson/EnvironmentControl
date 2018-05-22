package br.com.environment.control.module.user

import br.com.environment.control.extension.clear
import br.com.environment.control.extension.coolAppend
import br.com.environment.control.module.deviceList.DeviceList
import br.com.environment.control.view.TableModel
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.*

class UserView : JFrame("User"), KeyListener {

    private val disposables = CompositeDisposable()

    private val container: Container by lazy {
        contentPane
    }

    private var logArea: JTextArea
    private var table: JTable
    private val tableModel: TableModel
    private val createEnvBtn: JButton
    private val removeEnvBtn: JButton
    private val enterBtn: JButton
    private val leaveBtn: JButton
    private val createDeviceBtn: JButton
    private val chatTextField: JTextField
    private val refreshButton: JButton
    private val sendBtn: JButton
    private val showDevicesBnt: JButton

    private val viewModel = UserViewModel()

    init {

        size = Dimension(600, 400)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        /**
         * Buttons
         * */
        createEnvBtn = JButton("Create Environment")
        removeEnvBtn = JButton("Remove Environment")
        enterBtn = JButton("Enter Environment")
        leaveBtn = JButton("Leave Environment")
        createDeviceBtn = JButton("Create Device")
        refreshButton = JButton("Refresh List")
        showDevicesBnt = JButton("Show Devices")

        createEnvBtn.addActionListener { didTouchCreateEnvBtn() }
        removeEnvBtn.addActionListener { didTouchRemoveEnvBtn() }
        enterBtn.addActionListener { didTouchEnterBtn() }
        leaveBtn.addActionListener { didTouchLeaveBtn() }
        createDeviceBtn.addActionListener { didTouchCreateDeviceBtn() }
        showDevicesBnt.addActionListener { didTouchShowDevicesBtn() }
        refreshButton.addActionListener { didTouchRefreshListBtn() }

        val bottomPanel = JPanel()
        bottomPanel.layout = GridLayout(4, 2)
        bottomPanel.add(createEnvBtn)
        bottomPanel.add(removeEnvBtn)
        bottomPanel.add(enterBtn)
        bottomPanel.add(leaveBtn)
        bottomPanel.add(createDeviceBtn)
        bottomPanel.add(showDevicesBnt)
        bottomPanel.add(refreshButton)

        container.add(bottomPanel, BorderLayout.SOUTH)


        /**
         * Table
         * */
        tableModel = TableModel(viewModel, viewModel)
        table = JTable(tableModel)
        val scroll = JScrollPane(table)

        table.fillsViewportHeight = true

        // Columns Widths
        val namesWidth = (0.6 * WIDTH).toInt()
        table.columnModel.getColumn(0).preferredWidth = namesWidth
        table.columnModel.getColumn(1).preferredWidth = WIDTH - namesWidth

        // Allow single selection
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        // Can select only an entire row
        table.rowSelectionAllowed = true
        table.columnSelectionAllowed = false

        container.add(scroll)


        /**
         * Log
         * */

        logArea = JTextArea()
        val scrollPane = JScrollPane(logArea)
        logArea.isEditable = false


        chatTextField = JTextField()
        chatTextField.addKeyListener(this)

        sendBtn = JButton("Send")
        sendBtn.addActionListener { didTouchSendBtn() }

        val chatPanel = JPanel(BorderLayout())
        chatPanel.add(chatTextField)
        chatPanel.add(sendBtn, BorderLayout.EAST)

        val sidePanel = JPanel(BorderLayout())
        sidePanel.add(scrollPane)
        sidePanel.add(chatPanel, BorderLayout.SOUTH)
        sidePanel.preferredSize = Dimension(300, 0)
        container.add(sidePanel, BorderLayout.EAST)


        // Finish
        isVisible = true
        container.repaint()
        requestFocus()

        subscribe()
        viewModel.setup()

    }

    private fun subscribe() {
        disposables.add(
                viewModel.error
                        .subscribe {
                            presentError(it)
                        }
        )

        disposables.add(
                viewModel.messages
                        .subscribe {
                            logArea.coolAppend(it)
                        }
        )

        disposables.add(
                viewModel.reload
                        .subscribe {
                            tableModel.reloadData()
                        }
        )

        disposables.add(
                viewModel.title
                        .subscribe {
                            title = it
                        }
        )

        disposables.add(
                viewModel.status
                        .subscribe {
                            updateView(it)
                        }
        )

        disposables.add(
                viewModel.selectedDevice
                        .subscribe {
                            DeviceList(viewModel.space, it)
                        }
        )

    }

    private fun didTouchCreateEnvBtn() {
        viewModel.createEnvironment()
    }

    private fun didTouchRemoveEnvBtn() {
        viewModel.removeEnvironment(table.selectedRow)
    }

    private fun didTouchEnterBtn() {
        viewModel.enterEnvironment(table.selectedRow)
    }

    private fun didTouchLeaveBtn() {
        viewModel.leaveEnvironment()
    }

    private fun didTouchCreateDeviceBtn() {
        viewModel.createDevice()
    }

    private fun didTouchRefreshListBtn() {
        viewModel.refresh()
    }

    private fun didTouchShowDevicesBtn() {
        if(viewModel.status.value == UserViewModel.UserStatus.OUTSIDE) {
            viewModel.showDevices(table.selectedRow)
        } else {
            viewModel.showDevices()
        }
    }

    private fun didTouchSendBtn() {
        sendChatMessage()
    }

    private fun sendChatMessage() {
        viewModel.sendMessage(chatTextField.text)
        chatTextField.clear()
    }

    private fun presentError(message: String) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
    }

    private fun updateView(status: UserViewModel.UserStatus) {
        enterBtn.isEnabled = status.isOutside
        leaveBtn.isEnabled = status.isInside
        sendBtn.isEnabled = status.isInside
        createDeviceBtn.isEnabled = status.isInside
        chatTextField.isEnabled = status.isInside
        chatTextField.clear()
    }


    /**
     * KeyListener
     * */

    override fun keyTyped(e: KeyEvent?) {
    }

    override fun keyPressed(e: KeyEvent?) {
    }

    override fun keyReleased(e: KeyEvent?) {
        if(e == null) {
            return
        }
        if(e.keyCode != ENTER_KEY_CODE) {
            return
        }
        sendChatMessage()
    }

    companion object {

        private const val ENTER_KEY_CODE = 10

    }


}

fun main(args: Array<String>) {
    UserView()
}