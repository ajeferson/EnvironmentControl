package br.com.environment.control.module.manager

import br.com.environment.control.extension.coolAppend
import br.com.environment.control.module.userList.UserList
import br.com.environment.control.view.TableModel
import io.reactivex.disposables.CompositeDisposable
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class Manager : JFrame("Manager") {

    private val disposables = CompositeDisposable()

    private val container: Container by lazy {
        contentPane
    }

    private var logArea: JTextArea
    private var table: JTable
    private val tableModel: TableModel

    private val viewModel = ManagerViewModel()

    init {

        size = Dimension(600, 400)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        /**
         * Buttons
         * */
        val createBtn = JButton("Create Environment")
        val removeBtn = JButton("Remove Environment")
        val showUsersBtn = JButton("Show Users")
        val showDevicesBtn = JButton("Show Devices")

        createBtn.addActionListener { didTouchCreateBtn() }
        removeBtn.addActionListener { didTouchRemoveBtn() }
        showUsersBtn.addActionListener { didTouchShowUsersBtn() }


        val bottomPanel = JPanel()
        bottomPanel.layout = GridLayout(2, 2)
        bottomPanel.add(createBtn)
        bottomPanel.add(removeBtn)
        bottomPanel.add(showUsersBtn)
        bottomPanel.add(showDevicesBtn)

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
        scrollPane.preferredSize = Dimension(250, 400)
        logArea.isEditable = false
        container.add(scrollPane, BorderLayout.EAST)


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
                viewModel.selectedEnvUsers
                        .subscribe {
                            UserList(viewModel.space, it)
                        }
        )

    }

    private fun didTouchCreateBtn() {
        viewModel.createEnvironment()
    }

    private fun didTouchRemoveBtn() {
        viewModel.removeEnvironment(table.selectedRow)
    }

    private fun didTouchShowUsersBtn() {
        viewModel.showUsersOfEnvironment(table.selectedRow)
    }

    private fun presentError(message: String) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
    }


}

fun main(args: Array<String>) {
    Manager()
}