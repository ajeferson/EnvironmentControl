package br.com.environment.control.module.deviceList

import br.com.environment.control.view.TableModel
import io.reactivex.disposables.CompositeDisposable
import net.jini.space.JavaSpace
import java.awt.Container
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.ListSelectionModel

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

        subscribe()
        viewModel.setup()

    }

    private fun subscribe() {
        disposables.add(
                viewModel.reload
                        .subscribe {
                            tableModel.reloadData()
                        }
        )
    }

}