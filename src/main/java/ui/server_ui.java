package ui;

import org.example.Producto;
import server.model.Inventario;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class server_ui {


    public class ServerFrame extends JFrame implements Inventario.InventarioObserver {
        private JTable tabla;
        private DefaultTableModel modelo;
        private Inventario inventario;

        public ServerFrame() {
            inventario = Inventario.getInstance();
            inventario.agregarObservador(this);

            setTitle(" SERVIDOR - Inventario");
            setSize(700, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            modelo = new DefaultTableModel(new String[]{"ID", "Producto", "Precio", "Stock"}, 0);
            tabla = new JTable(modelo);
            add(new JScrollPane(tabla), BorderLayout.CENTER);

            actualizarTabla();
            setVisible(true);
        }

        private void actualizarTabla() {
            modelo.setRowCount(0);
            for (Producto p : inventario.getCatalogo()) {
                modelo.addRow(new Object[]{
                        p.getId(), p.getNombre(),
                        String.format("$%.2f", p.getPrecio()), p.getCantidad()
                });
            }
        }

        @Override
        public void inventarioActualizado() {
            SwingUtilities.invokeLater(this::actualizarTabla);
        }
    }
}
