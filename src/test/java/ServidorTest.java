import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Tag;

import io.github.cdimascio.dotenv.Dotenv;

public class ServidorTest {

    final Dotenv dotenv = Dotenv.load();

    @Test
    @Tag("unitario")
    void deveExtrairNomeDoJSON() {
        // 1. Prepara o dado de entrada
        String corpo = "{\"nome\": \"Eric\" , \"cidade\": \"BH\"}";

        // 2. Executa a lógica
        JsonObject json = JsonParser.parseString(corpo).getAsJsonObject();
        String nome = json.get("nome").getAsString();
        String cidade = json.get("cidade").getAsString();
        

        // 3. Verifica se o resultado é o esperado
        assertEquals("Eric", nome);
        assertEquals("BH", cidade);

        System.out.println("Resposta do teste - Nome: " + nome+" Cidade: " +cidade);

    }

    @Test
    @Tag("integracao")
    void deveReceberCodigo200() throws Exception {
    // 1. Prepara o dado de entrada
    
        Request request = new Request.Builder()
            .url("http://localhost:8000")
            .build();

        OkHttpClient client = new OkHttpClient();    

        okhttp3.Response response = client.newCall(request).execute();

        assertEquals(200, response.code());

        System.out.println("Status Aplicação HTTP: " + response.code());
    }

    @Test
    @Tag("integracao")
    void conexaoBanco() throws Exception {
    // 1. Prepara o dado de entrada
    
        Request request = new Request.Builder()
            .url(dotenv.get("SUPABASE_URL_PROD"))
            .addHeader("apikey", dotenv.get("SUPABASE_KEY_PROD"))
            .addHeader("Authorization", "Bearer " + dotenv.get("SUPABASE_KEY_PROD"))
            .addHeader("Content-type", "application/json")
            .build();

        OkHttpClient client = new OkHttpClient();    
        okhttp3.Response response = client.newCall(request).execute();

        assertEquals(200, response.code());

        System.out.println("Status Banco HTTP: " + response.code());
    }

    @Test
    @Tag("integracao")
    void formularioServidor() throws Exception {

        String corpo = "{\"nome\": \"Eric\" , \"cidade\": \"BH\"}";

        RequestBody body = RequestBody.create(
            corpo,
            MediaType.get("application/json")
        );

        Request request = new Request.Builder()
            .url("http://localhost:8000/pessoas")
            .post(body)
            .build();

        OkHttpClient client = new OkHttpClient();    

        okhttp3.Response response = client.newCall(request).execute();

        assertEquals(409, response.code());

        System.out.println("Status Formualrio -> Servidor HTTP: " + response.code());

    }

    @Test
    @Tag("integracao")
    void servidorBanco() throws Exception {

        String corpo = "{\"nome\": \"Eric\" , \"cidade\": \"BH\"}";

        RequestBody body = RequestBody.create(
            corpo,
            MediaType.get("application/json")
        );

        Request requestpost = new Request.Builder()
            .url("http://localhost:8000/pessoas")
            .post(body)
            .build();

        Request requestget = new Request.Builder()
            .url(dotenv.get("SUPABASE_URL_TEST"))
            .addHeader("apikey", dotenv.get("SUPABASE_KEY_TEST"))
            .addHeader("Authorization", "Bearer " + dotenv.get("SUPABASE_KEY_TEST"))
            .addHeader("Content-type", "application/json")
            .build();

        OkHttpClient client = new OkHttpClient();    

        okhttp3.Response responsepost = client.newCall(requestpost).execute();
        okhttp3.Response responseget = client.newCall(requestget).execute();

        assertEquals(200, responseget.code());

        System.out.println("Status Servidor -> Banco HTTP: " + responseget.code());

    }

    @Test
    @Tag("integracao")
    void dadoDuplicado() throws Exception {

        String corpo = "{\"nome\": \"Eric\" , \"cidade\": \"BH\"}";

        RequestBody body = RequestBody.create(
            corpo,
            MediaType.get("application/json")
        );

        Request request = new Request.Builder()
            .url("http://localhost:8000/pessoas")
            .post(body)
            .build();

        OkHttpClient client = new OkHttpClient();    

        okhttp3.Response response = client.newCall(request).execute();

        assertEquals(409, response.code());

        System.out.println("Status Duplicação HTTP: " + response.code());
        

    }
}