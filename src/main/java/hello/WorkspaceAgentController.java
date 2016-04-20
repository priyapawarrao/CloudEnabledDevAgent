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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.io.*;

import com.sun.jersey.core.util.Base64;


@RestController
public class WorkspaceAgentController {
	
	@Consumes({"application/xml", "application/json","text/html"})
	@Produces({"text/html", "application/json"})
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/create", method = RequestMethod.POST)
    public  @ResponseBody String createProject(@RequestBody JSONObject o) {
    	
		String projectType = o.get("projectType").toString();
		String buildType = o.get("buildType").toString();
				
		
		String groupId = o.get("groupId").toString();
		String artifactId = o.get("projectName").toString();
		String version = o.get("version").toString();
		String packageName = o.get("packageName").toString();
		
		String script = "";
		
		//script = "/agentScripts/spring-boot_create.sh"; //getClass().getResource("/scripts/spring-boot_create.sh").getPath();
		
		if(buildType.equalsIgnoreCase("maven"))
		{
			switch(projectType){
			
			case "spring-boot": script = "/agentScripts/spring-boot_create.sh";
				break;
			case "j2ee": script = "/agentScripts/j2ee_create.sh";
			    break;
			case "android": script = "/agentScripts/android_create.sh";
				break;
			case "simple": script = "/agentScripts/simple_create.sh";
				break;
			case "webapp": script = "/agentScripts/webapp_create.sh";
				break;
			default: script = "/agentScripts/simple_create.sh";
            	break;
			}
		}
		
		String command1 = "mkdir -p /agent/workspace";
		String output1 = executeCommand(command1);
			
		
	    String[] command2 = {script,groupId,artifactId,version,packageName};
		String output = executeCommand(command2);
		System.out.println(" Output is: " + output);
		
		String[] command3 = {"/agentScripts/create_json.sh",artifactId};
		String json = executeCommand(command3);
		
		System.out.println("JSON string format: " + json);
		
//		JSONParser parser = new JSONParser();
//		JSONObject json = null;
//		try {
//			json = (JSONObject) parser.parse(output3);
//		} catch (ParseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
//		String absoluteFilePath = "/agent/workspace" + File.separator + artifactId + ".json";
//		File srcFile = new File(absoluteFilePath);
//    	
//    	
//		String content = null;
//		try {
//			content = convertFileToString(srcFile);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//    	JSONObject data_file = new JSONObject();
//        //data_file.put("create_folder_struc", output);
////        data_file.put("zip_file_content", content);
////        return data_file;
        
//		JSONObject json = new JSONObject();
//		json.put("JSON", output3);
		
		return json;
   }

   
    @Consumes({"application/xml", "application/json","text/html"})
	@Produces({"text/html", "application/json"})
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/saveFile", method = RequestMethod.POST)
    public @ResponseBody JSONObject saveFile(@RequestBody JSONObject o) {
    
    	String status;
    	String name = o.get("name").toString();
    	String content = o.get("content").toString();
    	String path = o.get("path").toString();
    	String type = o.get("fileType").toString();
    	
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
			status = "SUCCESS";
		} catch (IOException e) {
			status = "FAIL " + e.getMessage();
			e.printStackTrace();
		}
    	
    	if(type.equalsIgnoreCase("folder"))
    	{
    		String command = "unzip -o " + path + File.separator + name;
    		String output = executeCommand(command);
    	}
    	
    	    	
    	JSONObject obj = new JSONObject();
        obj.put("result", status);
        return obj;
    }
    
    // Loads a file from disk, returns a encoded string.
    @Consumes({"application/xml", "application/json","text/html"})
	@Produces({"text/html", "application/json"})
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/loadFile", method = RequestMethod.POST)
    public @ResponseBody JSONObject loadFile(@RequestBody JSONObject o) {
    	
    	String name = o.get("name").toString();
    	String path = o.get("path").toString();
    	
    	
    	String absoluteFilePath = path + File.separator ;
    	absoluteFilePath = path + File.separator + name;
    	
 	   	File srcFile = new File(absoluteFilePath);
    	
    	
		String content = null;
		try {
			content = convertFileToString(srcFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        JSONObject data_file = new JSONObject();
        data_file.put("name", name);
        data_file.put("path", path);
        data_file.put("content", content);
    	
        return data_file;
    }
    
    
    // Loads project, Input: project Name, Output: JSON of the project in string format
    @Consumes({"application/xml", "application/json","text/html"})
	@Produces({"text/html", "application/json"})
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/loadProject", method = RequestMethod.POST)
    public @ResponseBody String loadProject(@RequestBody JSONObject o) {

       	String projectName = o.get("projectName").toString();
    	
    	String[] command3 = {"/agentScripts/create_json.sh",projectName};
		String json = executeCommand(command3);
	
    	return json;
    }
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/createFile", method = RequestMethod.POST)
       public @ResponseBody Boolean createFile(@RequestBody JSONObject o) {

    	Boolean status = null;
    	String name = o.get("name").toString();
    	String path = o.get("path").toString();
    	
    	String absoluteFilePath = path + File.separator + name;
    	
    	File srcFile = new File(absoluteFilePath);
    	
    	
    	if (srcFile.exists()) {
            System.out.println("File already exists");
        } else {
        	try {
				status = srcFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    	
    	return status;
    	
   }
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/createFolder", method = RequestMethod.POST)
       public @ResponseBody Boolean createFolder(@RequestBody JSONObject o) {

    	Boolean status = null;
    	String name = o.get("name").toString();
    	String path = o.get("path").toString();
    	
    	String absoluteFilePath = path + File.separator + name;
    	File directory = new File(absoluteFilePath);
    	
    	if (directory.exists()) {
            System.out.println("Folder already exists");
        } else {
        	status = directory.mkdir();
        }

    return status;
    	
   }
    
    
    
    
    @Consumes({"application/xml", "application/json","text/html"})
	@Produces({"text/html", "application/json"})
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/compile", method = RequestMethod.POST)
    public  @ResponseBody JSONObject compileProject(@RequestBody JSONObject o) {
    	
       	String dir = "/agent/workspace/" + o.get("projectName").toString();
    	
    	String[] command = {"/agentScripts/mvn_compile.sh",dir};
		
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
    
    private String executeCommand(String command) {

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
  			
  			 BufferedReader readErrorProc=new BufferedReader(new InputStreamReader(p.getErrorStream()));
  		      while(readErrorProc.ready()) {
  		        String output1 = readErrorProc.readLine();
  		        System.out.println(output1);
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
    
  //Convert my file to a Base64 String
    private String convertFileToString(File file) throws IOException{
        byte[] bytes = Files.readAllBytes(file.toPath());   
        return new String(Base64.encode(bytes));
    }
}
