package server;


import org.example.CarritoServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class servidorA {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50051)
                .addService(new CarritoServiceImpl())
                .build();

        server.start();
        System.out.println(" Servidor corriendo en puerto 50051");
        server.awaitTermination();
    }
}