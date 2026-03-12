package Service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.CarritoServiceImpl;

public class CarritoGRPCserver {
    public static void main(String[] args) {
        try {

            Server server = ServerBuilder
                    .forPort(50051)
                    .addService(new CarritoServiceImpl())
                    .build();

            System.out.println("Servidor gRPC iniciado en puerto 50051");

            server.start();

            server.awaitTermination();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
