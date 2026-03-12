package org.example;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.*;

public class CarritoServiceImpl extends CarritoServiceGrpc.CarritoServiceImplBase {
    
    private Map<String, Integer> inventario = new HashMap<>();
    private Map<String, Producto> catalogo = new HashMap<>();

    //INVENTARIO
    public CarritoServiceImpl() {

        Producto p1 = Producto.newBuilder().setId("1").setNombre("Laptop").setPrecio(15000).build();
        Producto p2 = Producto.newBuilder().setId("2").setNombre("Mouse").setPrecio(300).build();
        Producto p3 = Producto.newBuilder().setId("3").setNombre("Teclado").setPrecio(800).build();
        Producto p4 = Producto.newBuilder().setId("4").setNombre("Audifonos").setPrecio(1200).build();


        catalogo.put("1", p1);
        catalogo.put("2", p2);
        catalogo.put("3", p3);
        catalogo.put("4", p4);

        inventario.put("1", 5);
        inventario.put("2", 10);
        inventario.put("3", 7);
        inventario.put("4", 14);
    }
    

    // METODO PARA OBTENER CATALOGO
    @Override
    public void obtenerCatalogo(Empty request,
                                StreamObserver<CatalogoResponse> responseObserver) {

        CatalogoResponse.Builder response = CatalogoResponse.newBuilder();

        for (Producto p : catalogo.values()) {
            response.addProductos(p);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    // PROCESAR CARRITO
    @Override
    public void procesarCarrito(CarritoRequest request,
                                StreamObserver<CarritoResponse> responseObserver) {

        if(request.getItemsCount() == 0){
            responseObserver.onNext(
                    CarritoResponse.newBuilder()
                            .setEstado("ERROR: Carrito vacio")
                            .build()
            );
            responseObserver.onCompleted();
            return;
        }

        // Validar inventario
        for(Producto p : request.getItemsList()){

            if(p.getCantidad() <= 0 || p.getPrecio() <= 0){
                responseObserver.onNext(
                        CarritoResponse.newBuilder()
                                .setEstado("ERROR: cantidad o precio invalidos")
                                .build()
                );
                responseObserver.onCompleted();
                return;
            }

            int stock = inventario.getOrDefault(p.getId(),0);

            if(p.getCantidad() > stock){
                responseObserver.onNext(
                        CarritoResponse.newBuilder()
                                .setEstado("ERROR: Inventario insuficiente para " + p.getNombre())
                                .build()
                );
                responseObserver.onCompleted();
                return;
            }
        }

        double subtotal = 0;

        // descontar inventario
        for(Producto p : request.getItemsList()){

            subtotal += p.getPrecio() * p.getCantidad();

            int stock = inventario.get(p.getId());
            inventario.put(p.getId(), stock - p.getCantidad());
        }

        double impuestos = subtotal * 0.16;
        double total = subtotal + impuestos;

        CarritoResponse response = CarritoResponse.newBuilder()
                .setTransaccionId(UUID.randomUUID().toString())
                .setTotalNeto(subtotal)
                .setImpuestos(impuestos)
                .setTotalPagar(total)
                .setEstado("EXITOSO")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
//    @Override
//    public void procesarCarrito(CarritoRequest request,
//                                StreamObserver<CarritoResponse> responseObserver) {
//
//        System.out.println("Procesando carrito para el usuario: " + request.getUsuarioId());
//
//        double subtotal = 0;
//
//        // Iteramos sobre la lista repetida de productos definida en el archivo .proto
//        for (Producto p : request.getItemsList()) {
//            subtotal += p.getPrecio() * p.getCantidad();
//        }
//
//        double impuestos = subtotal * 0.16; // IVA del 16%
//        double total     = subtotal + impuestos;
//
//        // Construimos la respuesta usando el Builder generado por Protobuf
//        CarritoResponse response = CarritoResponse.newBuilder()
//                .setTransaccionId(UUID.randomUUID().toString())
//                .setTotalNeto(subtotal)
//                .setImpuestos(impuestos)
//                .setTotalPagar(total)
//                .setEstado("EXITOSO")
//                .build();
//
//        responseObserver.onNext(response);   // Enviamos al cliente
//        responseObserver.onCompleted();
//    }
}
