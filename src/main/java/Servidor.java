
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;

public class Servidor {
    public static void main(String[] args) throws Exception{
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8000) , 0);

        server.createContext("/", exchange -> {
            byte[] resposta = Files.readAllBytes(Path.of("public/index.html"));

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

            exchange.sendResponseHeaders(200, resposta.length);

            exchange.getResponseBody().write(resposta);

            exchange.getResponseBody().close();
        });


        server.createContext("/pessoas", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405,-1);
                return;
            }

            String corpo = new String(exchange.getRequestBody().readAllBytes());

            JsonObject json = JsonParser.parseString(corpo).getAsJsonObject();
            System.out.println(json);
            String nome = json.get("nome").getAsString();
            String cidade = json.get("cidade").getAsString();
            System.out.println("Nome recebido: " + nome);
            System.out.println("Cidade recebida: " + cidade);

            OkHttpClient client = new OkHttpClient();

            String json_supabase = "{\"name\": \"" + nome + "\", \"cidade\": \"" + cidade + "\"}";

            RequestBody body = RequestBody.create(
                json_supabase,
                MediaType.get("application/json")
            );

            Request request = new Request.Builder()
                .url(System.getenv("SUPABASE_URL_PROD"))
                .post(body)
                .addHeader("apikey", System.getenv("SUPABASE_KEY_PROD"))
                .addHeader("Authorization", "Bearer " + System.getenv("SUPABASE_KEY_PROD"))
                .addHeader("Content-type", "application/json")
                .build();

            okhttp3.Response response = client.newCall(request).execute();
            System.out.println("Status Supabase: " + response.code());
            System.out.println("Resposta Supabase: " + response.body().string());
            
            String resposta = "{\"status\": \"salvo com sucesso\"}";
            byte[] bytes = resposta.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(response.code(), bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();

            });

        server.start();
        System.out.println("Servidor iniciado na porta: 8000");
    }
    
}
