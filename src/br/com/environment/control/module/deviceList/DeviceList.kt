package br.com.environment.control.module.deviceList

import br.com.environment.control.view.TableModel
import io.reactivex.disposables.CompositeDisposable
import net.jini.space.JavaSpace
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import javax.swing.*

class DeviceList(space: JavaSpace, envId: Int): JFrame("Devices") {

    private val disposables = CompositeDisposable()

    private val container: Container by lazy {
        contentPane
    }

    private var table: JTable
    private val tableModel: TableModel

    private val viewModel = DeviceListViewModel(space, envId)

    init {

        size = Dimension(400, 200)


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

        // Allow single selection
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        // Can select only an entire row
        table.rowSelectionAllowed = true
        table.columnSelectionAllowed = false

        container.add(scroll)

        // Finish
        isVisible = true
        container.repaint()
        requestFocus()

        val moveBtn = JButton("Move Device")
        moveBtn.addActionListener { didTouchMoveDeviceBtn() }
        container.add(moveBtn, BorderLayout.SOUTH)


        subscribe()
        viewModel.setup()

    }

    private fun didTouchMoveDeviceBtn() {
        viewModel.touchedMoveDevice(table.selectedRow)
    }

    private fun showMovePane(devices: Array<Int>) {
        val index = table.selectedRow
        val option = JOptionPane.showInputDialog(null,
                "Choose an environment",
                "Move device", JOptionPane.PLAIN_MESSAGE,
                null, devices.map { "env$it" }.toTypedArray(), "") as? String ?: return
        val id = option.substring(3).toInt()
        viewModel.moveDevice(index, id)
    }

    private fun subscribe() {
        disposables.add(
                viewModel.reload
                        .subscribe {
                            tableModel.reloadData()
                        }
        )
        disposables.add(
                viewModel.envs
                        .subscribe {
                            showMovePane(it)
                        }
        )
        disposables.add(
                viewModel.error
                        .subscribe {
                            presentError(it)
                        }
        )
    }

    private fun presentError(message: String) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
    }

}