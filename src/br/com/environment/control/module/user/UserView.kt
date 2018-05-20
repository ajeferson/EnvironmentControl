package br.com.environment.control.module.user

import br.com.environment.control.extension.clear
import br.com.environment.control.extension.coolAppend
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
    private val enterBtn: JButton
    private val leaveBtn: JButton
    private val chatTextField: JTextField
    private val sendBtn: JButton

    private val viewModel = UserViewModel()

    init {

        size = Dimension(600, 400)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        /**
         * Buttons
         * */
        enterBtn = JButton("Enter Environment")
        leaveBtn = JButton("Leave Environment")

        enterBtn.addActionListener { didTouchEnterBtn() }
        leaveBtn.addActionListener { didTouchLeaveBtn() }


        val bottomPanel = JPanel()
        bottomPanel.layout = GridLayout(1, 2)
        bottomPanel.add(enterBtn)
        bottomPanel.add(leaveBtn)

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

    }

    private fun didTouchEnterBtn() {
        viewModel.enterEnvironment(table.selectedRow)
    }

    private fun didTouchLeaveBtn() {
        viewModel.leaveEnvironment()
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