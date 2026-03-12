package org.example;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CarritoGRPCcliente {
    
    private DefaultTableModel modeloCatalogo;
    
    private JFrame ventana;
    private JTextField txtId, txtNombre, txtPrecio, txtCantidad;
    private DefaultTableModel modeloCarrito;
    private JLabel lblEstado;

    private ManagedChannel channel;
    private CarritoServiceGrpc.CarritoServiceBlockingStub stub;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CarritoGRPCcliente().iniciar());
    }
git
    public void iniciar(){
        channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
        stub = CarritoServiceGrpc.newBlockingStub(channel);

        construirGUI();
        cargarCatalogo();
    }

    private void construirGUI(){
        ventana = new JFrame("Cliente - Carrito de compras gRPC");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(650, 1000);
        ventana.setLocationRelativeTo(null);
        ventana.setLayout(new BorderLayout(8, 8));

        ventana.add(crearPanelFormulario(), BorderLayout.NORTH);
        ventana.add(crearPanelCarrito(), BorderLayout.CENTER);
        ventana.add(crearPanelAcciones(), BorderLayout.SOUTH);
        ventana.add(crearPanelCatalogo(), BorderLayout.NORTH);

        ventana.setVisible(true);
    }

    private JPanel crearPanelFormulario(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Agregar producto al carrito"));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        gbc.gridx = 0; panel.add(new JLabel("ID;"), gbc);
        gbc.gridx = 1; txtId = new JTextField(10); panel.add(txtId, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 3; txtNombre = new JTextField(12); panel.add(txtNombre, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0; panel.add(new JLabel("Precio;"), gbc);
        gbc.gridx = 1; txtPrecio = new JTextField(10); panel.add(txtPrecio, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 3; txtCantidad = new JTextField(10); panel.add(txtCantidad, gbc);

        gbc.gridy = 0; gbc.gridx = 4;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.setBackground(new Color(0x2980B9));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAgregar.addActionListener(e -> agregarProducto());
        panel.add(btnAgregar, gbc);

        return panel;
    }

    private JPanel crearPanelCarrito(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Carrito actual"));

        modeloCarrito = new DefaultTableModel(new String[]{"ID", "Nombre", "Precio", "Cantidad", "Subtotal"}, 0){
            @Override public boolean isCellEditable(int r, int c){return false;}
        };

        JTable tabla = new JTable(modeloCarrito);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setBackground(new Color(0x2C3E50));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(new Color(0x6EAF8));

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelAcciones(){
        JPanel panel = new JPanel(new BorderLayout(8, 4));
        panel.setBorder(new EmptyBorder(4, 8, 8, 8));

        lblEstado = new JLabel(" ");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblEstado, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton btnLimpiar = new JButton("Limpiar carrito");
        btnLimpiar.addActionListener(e -> limpiarCarrito());

        JButton btnEnviar = new JButton("Enviar carrito");
        btnEnviar.setBackground(new Color(0x27AE60));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEnviar.addActionListener(e -> enviarCarrito());

        botones.add(btnLimpiar);
        botones.add(btnEnviar);
        panel.add(botones, BorderLayout.EAST);

        return panel;
    }

    private void agregarProducto() {
        String id = txtId.getText().trim();
        String nombre = txtNombre.getText().trim();
        String sPrecio = txtPrecio.getText().trim();
        String sCant = txtCantidad.getText().trim();

        if (id.isEmpty() || nombre.isEmpty() || sPrecio.isEmpty() || sCant.isEmpty()) {
            mostrarError("Por favor completa todos los campos");
            return;
        }

        double precio;
        int cantidad;
        try {
            precio = Double.parseDouble(sPrecio);
            cantidad = Integer.parseInt(sCant);
        }catch (NumberFormatException ex){
            mostrarError("El precio y cantidad deben ser valores numericos");
            return;
        }

        if (precio <= 0 || cantidad <= 0){
            mostrarError("El precio y la cantidad deben ser mayores que 0");
            return;
        }

        modeloCarrito.addRow(new Object[]{
                id, nombre,
                String.format("$%.2f", precio),
                cantidad,
                String.format("$%.2f", precio * cantidad)
        });
        txtId.setText(""); txtNombre.setText("");
        txtPrecio.setText(""); txtCantidad.setText("");
        lblEstado.setText("");
    }

    private void enviarCarrito(){
        if (modeloCarrito.getRowCount() == 0){
            mostrarError("El carrito esta vacio. Agrega al menos un producto");
            return;
        }

        CarritoRequest.Builder reqBuilder = CarritoRequest.newBuilder().setUsuarioId("USER-" + (System.currentTimeMillis() % 1000));

        for (int i = 0; i < modeloCarrito.getRowCount(); i++){
            String id = modeloCarrito.getValueAt(i, 0).toString();
            String nombre = modeloCarrito.getValueAt(i, 1).toString();
            double precio = Double.parseDouble(modeloCarrito.getValueAt(i, 2).toString().replace("$", ""));
            int cantidad = Integer.parseInt(modeloCarrito.getValueAt(i, 3).toString());

            reqBuilder.addItems(Producto.newBuilder()
                    .setId(id)
                    .setNombre(nombre)
                    .setPrecio(precio)
                    .setCantidad(cantidad)
                    .build());
        }

        try {
            CarritoResponse response = stub.procesarCarrito(reqBuilder.build());

            if (response.getEstado().equals("EXITOSO")) {
                mostrarExito(response);
            } else {
                mostrarError(response.getEstado());
            }
        } catch (Exception ex) {
            mostrarError("No se pudo conectar al servidor: " + ex.getMessage());
            }

        }
        private void limpiarCarrito() {
            modeloCarrito.setRowCount(0);
            lblEstado.setText(" ");
        }

        private void mostrarExito(CarritoResponse r){
        lblEstado.setText("Orden procesada | ID: " + r.getTransaccionId());
        lblEstado.setForeground(new Color(0x27AE60));
        JOptionPane.showMessageDialog(
                ventana,
                String.format(
                        "Compra realizada con exito\n\n" +
                        "ID Transaccion: %s\n" +
                        "Subtotal:   $%.2f\n" +
                        "IVA (16%%):     $%.2f\n" +
                        "TOTAL A PAGAR:     $%.2f",
                        r.getTransaccionId(),
                        r.getTotalNeto(),
                        r.getImpuestos(),
                        r.getTotalPagar()
                ),
                "Compra exitosa",
                JOptionPane.INFORMATION_MESSAGE
        );
        limpiarCarrito();
    }

    private void mostrarError(String mesaje){
        lblEstado.setText(mesaje);
        lblEstado.setForeground(new Color(0xC0392B));

        JOptionPane.showMessageDialog(ventana, mesaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void cargarCatalogo() {

        CatalogoResponse response = stub.obtenerCatalogo(Empty.newBuilder().build());

        for (Producto p : response.getProductosList()) {
            modeloCatalogo.addRow(new Object[]{
                    p.getId(),
                    p.getNombre(),
                    p.getPrecio()
            });
        }
    }
    
    private JPanel crearPanelCatalogo(){

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Catalogo de productos"));

        modeloCatalogo = new DefaultTableModel(
                new String[]{"ID","Nombre","Precio"},0);

        JTable tabla = new JTable(modeloCatalogo);

        JButton btnAgregar = new JButton("Agregar al carrito");

        btnAgregar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();

            if(fila == -1){
                mostrarError("Selecciona un producto");
                return;
            }

            String id = modeloCatalogo.getValueAt(fila,0).toString();
            String nombre = modeloCatalogo.getValueAt(fila,1).toString();
            double precio = Double.parseDouble(modeloCatalogo.getValueAt(fila,2).toString());

            String cantidadStr = JOptionPane.showInputDialog("Cantidad:");

            int cantidad = Integer.parseInt(cantidadStr);

            modeloCarrito.addRow(new Object[]{
                    id,
                    nombre,
                    String.format("$%.2f",precio),
                    cantidad,
                    String.format("$%.2f",precio*cantidad)
            });
        });

        panel.add(new JScrollPane(tabla),BorderLayout.CENTER);
        panel.add(btnAgregar,BorderLayout.SOUTH);

        return panel;
    }

}
