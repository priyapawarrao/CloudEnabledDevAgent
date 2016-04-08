package hello;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import javax.ws.rs.*;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.io.*;

import com.sun.jersey.core.util.Base64;


@RestController
public class WorkspaceAgentController {

   
    @Consumes({"application/xml", "application/json","text/html"})
	@Produces({"text/html", "application/json"})
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/save", method = RequestMethod.POST)
    public @ResponseBody JSONObject saveProject(@RequestBody JSONObject o) {
    	
    	//FileObject f  = new FileObject(o.get("name").toString(),o.get("content").toString(),o.get("path").toString());
    	//f.setName(o.get("name").toString());
    	//f.setPath(o.get("path").toString());
    	//f.setContent((File) o.get("content"));
    	
    	
    	String status;
    	
    	String name = o.get("name").toString();
    	String content = o.get("content").toString();
    	String path = o.get("path").toString();
    	
    	
    	
    	byte[] bytes = Base64.decode(content);
        File srcFile = new File(name);
        FileOutputStream fop;
		try {
			fop = new FileOutputStream(srcFile);
			fop.write(bytes);
			fop.flush();
	        fop.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
    	
    	String absoluteFilePath = path + File.separator + name;
 	   	File destFile = new File(absoluteFilePath);
    	
    	try {
			FileUtils.copyFile(srcFile, destFile);
			status = "success";
		} catch (IOException e) {
			status = "File copy failed: " + e.getMessage();
			e.printStackTrace();
		}
    	    	
    	JSONObject obj = new JSONObject();
        obj.put("result", status);
        return obj;
    }
    
    // Loads a file from disk, returns a encoded string.
    
    @Consumes({"application/xml", "application/json","text/html"})
	@Produces({"text/html", "application/json"})
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/load", method = RequestMethod.POST)
    public @ResponseBody JSONObject loadProject(@RequestBody JSONObject o) {
    	
 	  	String status;
    	
    	String name = o.get("name").toString();
    	String path = o.get("path").toString();
    	
    	String absoluteFilePath = path + File.separator + name;
 	   	File srcFile = new File(absoluteFilePath);
    	
    	byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(srcFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}   
        String content = Base64.encode(bytes).toString();
    	
    	
        JSONObject data_file = new JSONObject();
        data_file.put("name", name);
        data_file.put("path", path);
        data_file.put("content", content);

        return data_file;
    }
    
    @Consumes({"application/xml", "application/json","text/html"})
	@Produces({"text/html", "application/json"})
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/compile", method = RequestMethod.POST)
    public  @ResponseBody JSONObject compileProject(@RequestBody JSONObject o) {
    	
    	// the dir has to be changed to generic folder in docker container like /home/user/workspace
    	
    	String dir = "/home/pripawar/git/CloudEnabledDevAgent" + o.get("projectName").toString();
    	
    	String[] command = {"/home/pripawar/git/CloudEnabledDevAgent/script.sh",dir};
		
		String output = executeCommand(command);

		System.out.println(output);
		System.out.println("command exec completed");
		
		//return output;

    	JSONObject data_file = new JSONObject();
        data_file.put("output", output);
        return data_file;
   }
    
//    @RequestMapping("/compile")
//    public FileObject compileProject(@RequestParam(value="name", defaultValue="World") String name) {
//        return new FileObject(counter.incrementAndGet(),String.format(template, name));
//    }
//    @RequestMapping("/execute")
//    public FileObject executeProject(@RequestParam(value="name", defaultValue="World") String name) {
//        return new FileObject(counter.incrementAndGet(),
//                            String.format(template, name));
//    }
    
    private String executeCommand(String[] command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}
    
    private String executeMultipleCommands(String[] commands) {
    	
    	StringBuilder sb = new StringBuilder();
    	
    	try {
            Process proc = new ProcessBuilder(commands).start();
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            int waitFor = proc.waitFor();
            
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }

            while ((s = stdError.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
        } catch (IOException | InterruptedException e) {
            return e.getMessage();
        }
        return sb.toString();
    	
    	
    }
}
