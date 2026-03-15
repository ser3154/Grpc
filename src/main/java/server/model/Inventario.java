package server.model;

import org.example.Producto;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Inventario {
    private static Inventario instancia;
    private Map<String, Producto> productos;
    private List<InventarioObserver> observadores = new ArrayList<>();

    private Inventario() {
        productos = new ConcurrentHashMap<>();


        productos.put("1", Producto.newBuilder()
                .setId("1")
                .setNombre("Laptop")
                .setPrecio(15000)
                .setCantidad(10)
                .build());

        productos.put("2", Producto.newBuilder()
                .setId("2")
                .setNombre("Mouse")
                .setPrecio(300)
                .setCantidad(50)
                .build());

        productos.put("3", Producto.newBuilder()
                .setId("3")
                .setNombre("Teclado")
                .setPrecio(800)
                .setCantidad(30)
                .build());
    }

    public static synchronized Inventario getInstance() {
        if (instancia == null) {
            instancia = new Inventario();
        }
        return instancia;
    }


    public Collection<Producto> getProductos() {
        return productos.values();
    }


    public Producto getProducto(String id) {
        return productos.get(id);
    }


    public List<Producto> getCatalogo() {
        return new ArrayList<>(productos.values());
    }

    public synchronized boolean descontarStock(String id, int cantidad) {
        Producto actual = productos.get(id);
        if (actual != null && actual.getCantidad() >= cantidad) {

            Producto nuevo = Producto.newBuilder(actual)
                    .setCantidad(actual.getCantidad() - cantidad)
                    .build();

            productos.put(id, nuevo);
            notificarCambio();
            return true;
        }
        return false;
    }


    public interface InventarioObserver {
        void inventarioActualizado();
    }

    public void agregarObservador(InventarioObserver obs) {
        observadores.add(obs);
    }

    private void notificarCambio() {
        for (InventarioObserver obs : observadores) {
            obs.inventarioActualizado();
        }
    }
}