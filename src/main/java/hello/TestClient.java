package hello;

import java.io.IOException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;

public class TestClient {
	
	public static void main(String[] args) throws JSONException, IOException 
    {
		TestClient my_client = new TestClient();
        File file_upload = new File("C:\\Users\\mkmay\\Desktop\\Desktop.zip");
        my_client.sendFileJSON(file_upload);
    }

 
    private void sendFileJSON(File file_upload) throws JSONException, IOException{

//        ClientConfig config = new DefaultClientConfig();
//        Client client = Client.create(config);
//        client.addFilter(new LoggingFilter());
//        WebResource service = client.resource("localhost:8080/save");
        
        
        JSONObject data_file = new JSONObject();
        data_file.put("name", file_upload.getName());
        data_file.put("path", "D:\\classdocs\\295B\\testapi");
        data_file.put("content", convertFileToString(file_upload));
        
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://localhost:8080/save");
		StringEntity input = new StringEntity(data_file.toString());
        input.setContentType("application/json");
        post.setEntity(input);
        HttpResponse response = client.execute(post);
        
        


        //ClientResponse client_response = service.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, data_file);

        //System.out.println("Status: "+client_response.getStatus());

        //client.destroy();

    }


    //Convert my file to a Base64 String
    private String convertFileToString(File file) throws IOException{
        byte[] bytes = Files.readAllBytes(file.toPath());   
        return new String(Base64.encode(bytes));
    }



}
