package org.example;

import io.grpc.stub.StreamObserver;
import java.util.UUID;

public class CarritoServiceImpl extends CarritoServiceGrpc.CarritoServiceImplBase {

    @Override
    public void procesarCarrito(CarritoRequest request,
                                StreamObserver<CarritoResponse> responseObserver) {

        System.out.println("Procesando carrito para el usuario: " + request.getUsuarioId());

        double subtotal = 0;

        // Iteramos sobre la lista repetida de productos definida en el archivo .proto
        for (Producto p : request.getItemsList()) {
            subtotal += p.getPrecio() * p.getCantidad();
        }

        double impuestos = subtotal * 0.16; // IVA del 16%
        double total     = subtotal + impuestos;

        // Construimos la respuesta usando el Builder generado por Protobuf
        CarritoResponse response = CarritoResponse.newBuilder()
                .setTransaccionId(UUID.randomUUID().toString())
                .setTotalNeto(subtotal)
                .setImpuestos(impuestos)
                .setTotalPagar(total)
                .setEstado("EXITOSO")
                .build();

        responseObserver.onNext(response);   // Enviamos al cliente
        responseObserver.onCompleted();
    }
}
