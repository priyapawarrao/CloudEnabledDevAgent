package hello;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

//import javax.ws.rs.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.io.*;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sun.jersey.core.util.Base64;


@RestController
public class WorkspaceAgentController {
	
	private String workspaceDir = "/agent/workspace/";
	private String logsDir = "/agent/logs/";
	
	@Consumes({"application/xml", "application/json","text/html"})
	@Produces({"text/html", "application/json"})
	@RequestMapping(value="/check", method = RequestMethod.GET)
    public  @ResponseBody String checkREST() {
		return "UP";
	}
	
	
	@Consumes({"application/xml", "application/json","text/html"})
	@Produces({"text/html", "application/json"})
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/create", method = RequestMethod.POST)
    public  @ResponseBody String createProject(@RequestBody JSONObject o) {
    	
		String projectType = o.get("projectType").toString();
		String buildType = o.get("buildType").toString();
				
		
		String groupId = "com.cloud.core";
		String artifactId = o.get("projectName").toString();
		String version = "1.0";
		String packageName = "com.cloud.core.dev";
		
		String script = "";
		
		//script = "/agentScripts/spring-boot_create.sh"; //getClass().getResource("/scripts/spring-boot_create.sh").getPath();
		
		Boolean empty = false;
		
		if(buildType.equalsIgnoreCase("maven"))
		{
			switch(projectType){
			
			case "spring-boot": script = "/agentScripts/spring-boot_create.sh";
				break;
			case "j2ee-project": script = "/agentScripts/j2ee_create.sh";
			    break;
			case "android-project": script = "/agentScripts/android_create.sh";
				break;
			case "java-project": script = "/agentScripts/simple_create.sh";
				break;
			case "webapp-project": script = "/agentScripts/webapp_create.sh";
				break;
			case "empty-project": script = "/agentScripts/empty_create.sh";
								empty =true;
				break;
			default: script = "/agentScripts/simple_create.sh";
            	break;
			}
		}
		
		String command1 = "mkdir -p /agent/workspace";
		String output1 = executeCommand(command1);
		
		String createLogDir = "mkdir /agent/logs";
		String output_createLogDir = executeCommand(createLogDir);
		
		String output;
			
		if(empty){
			String[]  command2 = {script,artifactId};
			output = executeCommand(command2);
			
		}else {
			String[]  command2 = {script,groupId,artifactId,version,packageName};
			output = executeCommand(command2);
		}
		
		System.out.println(" Output is: " + output);
		
		String[] command3 = {"/agentScripts/create_json.sh",artifactId};
		String json = executeCommand(command3);
		
		System.out.println("JSON string format: " + json);
		
	
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
        
        String absoluteFilePath = workspaceDir + path + File.separator + name;
    	File destFile = new File(absoluteFilePath);
    	
    	try {
			FileUtils.copyFile(srcFile, destFile);
			status = "SUCCESS";
		} catch (IOException e) {
			status = "FAIL " + e.getMessage();
			e.printStackTrace();
		}
    	
    	/*if(type.equalsIgnoreCase("folder"))
    	{
    		String command = "unzip -o " + path + File.separator + name;
    		String output = executeCommand(command);
    	}*/
    	
    	    	
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
    	
    	
    	String absoluteFilePath = workspaceDir + path + File.separator + name;
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
    public @ResponseBody JSONArray loadProject(@RequestBody JSONObject o) {

       	String projectName = o.get("projectName").toString();
    	
    	String[] command3 = {"/agentScripts/create_json.sh",projectName};
		String json = executeCommand(command3);
	
		 JSONParser parser = new JSONParser();
	        Object obj = null;
			try {
				obj = parser.parse(json);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        JSONArray jsonResult = (JSONArray)obj;
		
		
    	return jsonResult;
    }
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/createFile", method = RequestMethod.POST)
       public @ResponseBody JSONObject createFile(@RequestBody JSONObject o) {

    	Boolean status = null;
    	String name = o.get("name").toString();
    	String path = o.get("path").toString();
    	
    	String absoluteFilePath = workspaceDir + path + File.separator + name;
    	
    	File srcFile = new File(absoluteFilePath);
    	
    	
    	if (srcFile.exists()) {
            System.out.println("File already exists");
            status = false;
        } else {
        	try {
				status = srcFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    	JSONObject json = new JSONObject();
    	json.put("status", status);
        return json;
    	
   }
    
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/deleteFile", method = RequestMethod.POST)
       public @ResponseBody JSONObject deleteFile(@RequestBody JSONObject o) {

    	Boolean status = null;
    	String name = o.get("name").toString();
    	String path = o.get("path").toString();
    	
    	String absoluteFilePath = workspaceDir + path + File.separator + name;
    	
    	File srcFile = new File(absoluteFilePath);
    	
    	
    	if (srcFile.exists()) {
            System.out.println("File exists: deleting file" + srcFile);
            status = srcFile.delete();
        } else {
        	System.out.println("File doesnt exist");
        	status = false;
        }
    	
    	   	
    	// status True --> File deleted, False --> File doesn't exist , null --> unsuccessful deletion
    	JSONObject json = new JSONObject();
    	json.put("status", status);
        return json;
    	
   }
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/deleteFolder", method = RequestMethod.POST)
       public @ResponseBody JSONObject deleteFolder(@RequestBody JSONObject o) {

    	Boolean status = null;
    	String name = o.get("name").toString();
    	String path = o.get("path").toString();
    	
    	String absoluteFilePath = workspaceDir + path + File.separator + name;
    	File directory = new File(absoluteFilePath);
    	
    	if (directory.exists()) {
            System.out.println("Folder already exists");
            try {
				FileUtils.deleteDirectory(directory);
				status = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
        	status = false;
        }
    	
    	// status True --> successful deletion, false --> doesn't exist, null --> unsuccessful deletion
    	JSONObject json = new JSONObject();
    	json.put("status", status);
        return json;
   }
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/createFolder", method = RequestMethod.POST)
       public @ResponseBody JSONObject createFolder(@RequestBody JSONObject o) {

    	Boolean status = null;
    	String name = o.get("name").toString();
    	String path = o.get("path").toString();
    	
    	String absoluteFilePath = workspaceDir + path + File.separator + name;
    	File directory = new File(absoluteFilePath);
    	
    	if (directory.exists()) {
            System.out.println("Folder exists, deleting dir: " + directory);
            status = false;
        } else {
        	status = directory.mkdir();
        }
    	

    	JSONObject json = new JSONObject();
    	json.put("status", status);
        return json;
   }
    
    
    
    
    @Consumes({"application/xml", "application/json","text/html"})
	@Produces({"text/html", "application/json"})
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value="/compile", method = RequestMethod.POST)
    public  @ResponseBody JSONObject compileProject(@RequestBody JSONObject o) {
    	
    	String projectName = o.get("projectName").toString();
    	
       	String dir = workspaceDir + projectName;
    	
    	String[] command = {"/agentScripts/mvn_compile.sh",dir,projectName};
		
		String output = executeCommand(command);
		
		String contents = null;
		
		String logFile = "/agent/logs/" + projectName + "_compile.log";
		
		try {
			contents = FileUtils.readFileToString(new File(logFile), "UTF-8");
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		System.out.println(output);
		System.out.println("command exec completed");
		
		JSONObject data_file = new JSONObject();
        data_file.put("status", contents);
        return data_file;
   }
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/execute", method = RequestMethod.POST)
    public  @ResponseBody JSONObject executeProject(@RequestBody JSONObject o) throws Exception {
    	
    	String projectName = o.get("projectName").toString();
       	
        String dir = workspaceDir + projectName;
       	System.out.println("Param1: Dir:--->"+dir);
    	System.out.println("Param2: projectName:--->"+projectName);
       //	String[] command = {"/agentScripts/mvn_execute.sh",dir,projectName};
   		
    	
    	String command = "/agentScripts/mvn_execute.sh "+dir+" "+projectName;
    	System.out.println("Command:"+command);
       	//Executing user application on port 8080	
       	Thread t = new Thread(){
       		
       		
       		public void run()
       		{
       			String output = executeBashCommand(command);

       	   		System.out.println(output);
       	   		System.out.println("command exec completed");
       	 
   	        
       	   }
       	};
       	
       	t.start();
       	
       	//Checking if the user application is up
       	
       	@SuppressWarnings("deprecation")
		HttpClient client = new DefaultHttpClient();
		
		String url = "http://localhost:8080/";
		
		System.out.println("Url:"+url);
		
        HttpGet get = new HttpGet(url);
		StringEntity input;
		HttpResponse response = null;
       	
		String responseString="";
       	
       	
       	int count = 0;
        int maxTries = 10;
        while(true) {
            try {
                Thread.sleep(8000);
                System.out.println("Connecting to user application on port 8080");
                response = client.execute(get);
                System.out.println("Response Code : " 
                        + response.getStatusLine().getStatusCode());
                break;
            } catch (Exception e) {
            	
                if (++count == maxTries) throw e;
                System.out.println("Retry:"+count);
            }
        }
       	
       	
       	
       	
        String contents = null;
        
		String logFile = "/agent/logs/" + projectName + "_execute.log";
		
		try {
			contents = FileUtils.readFileToString(new File(logFile), "UTF-8");
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		
		JSONObject data_file = new JSONObject();
        data_file.put("status", contents);
        return data_file;
       	
       	
 
   		
      }
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/renameFile", method = RequestMethod.POST)
    public  @ResponseBody JSONObject renameFile(@RequestBody JSONObject o) {
       	
    	String oldName = o.get("oldName").toString();
    	String newName = o.get("newName").toString();
    	String path = o.get("path").toString();
    	
    	
    	File oldFile = new File(workspaceDir + path + File.separator + oldName);
    	File newFile = new File(workspaceDir + path + File.separator + newName);
    	
    	boolean status = false;

    	if (!newFile.exists())
    	{
    		status = oldFile.renameTo(newFile);
    	}
    	
    	JSONObject json = new JSONObject();
    	json.put("status", status);
        return json;
      }
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/moveFile", method = RequestMethod.POST)
    public  @ResponseBody JSONObject moveFile(@RequestBody JSONObject o) {
       	
    	String oldPath = o.get("oldPath").toString();
    	String newPath = o.get("newPath").toString();
    	String fileName = o.get("fileName").toString();
    	
    	
    	File sourceFile = new File(workspaceDir + oldPath + File.separator + fileName);
		File destinationFile = new File(workspaceDir + newPath + File.separator + fileName);

		  	
    	Boolean status = null;

    	if (sourceFile.exists())
    	{
    		try {
				FileUtils.moveFile(sourceFile, destinationFile);
				status = true;
			} catch (IOException e) {
				status = false;
				e.printStackTrace();
			}
    		
    	}
    	
    	JSONObject json = new JSONObject();
    	json.put("status", status);
        return json;
      }
    
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/moveFolder", method = RequestMethod.POST)
    public  @ResponseBody JSONObject moveFolder(@RequestBody JSONObject o) {
       	
    	String oldPath = o.get("oldPath").toString();
    	String newPath = o.get("newPath").toString();
    	String dirName = o.get("dirName").toString();
    	
    	
    	File sourceDir = new File(workspaceDir + oldPath + File.separator + dirName);
		File destinationDir = new File(workspaceDir + newPath + File.separator + dirName);

		  	
    	Boolean status = null;

    	if (sourceDir.exists())
    	{
    		try {
				FileUtils.moveDirectory(sourceDir, destinationDir);
				status = true;
			} catch (IOException e) {
				status = false;
				e.printStackTrace();
			}
    		
    	}
    	
    	JSONObject json = new JSONObject();
    	json.put("status", status);
        return json;
      }
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/copyFolder", method = RequestMethod.POST)
    public  @ResponseBody JSONObject copyFolder(@RequestBody JSONObject o) {
       	
    	String oldPath = o.get("oldPath").toString();
    	String newPath = o.get("newPath").toString();
    	String dirName = o.get("dirName").toString();
    	
    	
    	File sourceDir = new File(workspaceDir + oldPath + File.separator + dirName);
		File destinationDir = new File(workspaceDir + newPath + File.separator + dirName);

		  	
    	Boolean status = null;

    	if (sourceDir.exists())
    	{
    		try {
    			FileUtils.copyDirectory(sourceDir, destinationDir);
				status = true;
			} catch (IOException e) {
				status = false;
				e.printStackTrace();
			}
    		
    	}
    	
    	JSONObject json = new JSONObject();
    	json.put("status", status);
        return json;
      }
    
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/copyFile", method = RequestMethod.POST)
    public  @ResponseBody JSONObject copyFile(@RequestBody JSONObject o) {
       	
    	String oldPath = o.get("oldPath").toString();
    	String newPath = o.get("newPath").toString();
    	String fileName = o.get("fileName").toString();
    	
    	
    	File sourceFile = new File(workspaceDir + oldPath + File.separator + fileName);
		File destinationFile = new File(workspaceDir + newPath + File.separator + fileName);

		  	
    	Boolean status = null;

    	if (sourceFile.exists())
    	{
    		try {
    			FileUtils.copyFile(sourceFile, destinationFile);
    			
				status = true;
			} catch (IOException e) {
				status = false;
				e.printStackTrace();
			}
    		
    	}
    	
    	JSONObject json = new JSONObject();
    	json.put("status", status);
        return json;
      
      }
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/download", method = RequestMethod.POST)
    public  @ResponseBody JSONObject dowload(@RequestBody JSONObject o) {
       	
    	String name = o.get("name").toString();
    	String path = o.get("path").toString();
    	//String type = o.get("type").toString();
    	
    	
    	File sourceFile = new File(workspaceDir + path + File.separator + name);
    	
    	Boolean status = null;
    	String command;
    	
    	if(sourceFile.exists()){
	    	if (sourceFile.isDirectory())
	    	{
	    		//command = "zip -r " + "download.zip " + actualPath;
	    		String[] command2 = {"/agentScripts/download_zip.sh","1",path,name};
				String output = executeCommand(command2);
				status = true;
	    		
	    	}else{
	    		//command = "zip " + "download.zip " + actualPath;
	    		String[] command2 = {"/agentScripts/download_zip.sh","2",path,name};
				String output = executeCommand(command2);
				status = true;
	    	}
    	}
    	
    	File destFile = new File(File.separator + "download.zip");
    	
    	String content = null;
		try {
			content = convertFileToString(destFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		command = "rm -rf " + File.separator + "download.zip";
		String output1 = executeCommand(command);
    	
        JSONObject data_file = new JSONObject();
        data_file.put("name", name);
        data_file.put("path", path);
        data_file.put("content", content);
    	
        return data_file;
   }
    
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/getProperties", method = RequestMethod.POST)
    public  @ResponseBody JSONObject getProperties(@RequestBody JSONObject o) {
       	
    	String name = o.get("name").toString();
    	String path = o.get("path").toString();
    	//String type = o.get("type").toString();
    	    	
    	File sourceFile = new File(workspaceDir + path + File.separator + name);
    	Path file = Paths.get(workspaceDir + path + File.separator + name);
    	
    	Boolean status = null;
    	String command;
    	
    	JSONObject json = new JSONObject();
          
    	
    	if(sourceFile.exists()){
    		
    		 BasicFileAttributeView bfv = Files.getFileAttributeView(file,BasicFileAttributeView.class);
    		 try {
				BasicFileAttributes bfa = bfv.readAttributes();
				BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
				PosixFileAttributes permAttr = Files.readAttributes(file, PosixFileAttributes.class);
				System.out.format("%s %s %s%n",
					    permAttr.owner().getName(),
					    permAttr.group().getName(),
					    PosixFilePermissions.toString(permAttr.permissions()));
				json.put("name", name);
				json.put("path", workspaceDir + path + File.separator);
				if(attr.isDirectory()){
					json.put("fileType", "directory");
				}else{
					json.put("fileType", "file");
				}
				
				json.put("creationTime", attr.creationTime().toString());
				json.put("modifiedTime", attr.lastModifiedTime().toString());
				json.put("size", attr.size());
				json.put("owner", permAttr.owner().getName());
				json.put("group", permAttr.group().getName());
				json.put("filePermissions", PosixFilePermissions.toString(permAttr.permissions()));
				json.put("status", "success");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				json.put("status", "fail");
			}
    		 		
    	}
    	
    
        
        return json;
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
			
			BufferedReader readErrorProc=new BufferedReader(new InputStreamReader(p.getErrorStream()));
		      while(readErrorProc.ready()) {
		        String output1 = readErrorProc.readLine();
		        System.out.println("Error while compiling: " + output1);
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
    
    
    
    
    
    
    
    
    //executeBashCommand
    String executeBashCommand(String command) {

  		StringBuffer output = new StringBuffer();

  		Process p;
  		try {
  			p = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
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
    
    
    
    
    
    
    @Consumes({"application/xml", "application/json","text/html"})
   	@Produces({"text/html", "application/json"})
   	@ResponseStatus(value = HttpStatus.CREATED)
   	@RequestMapping(value="/stop", method = RequestMethod.POST)
    public  @ResponseBody JSONObject stopProject(@RequestBody JSONObject o) throws Exception {
    	
    	String projectName = o.get("projectName").toString();
       	
        String dir = workspaceDir + projectName;
       	System.out.println("Param1: Dir:--->"+dir);
    	System.out.println("Param2: projectName:--->"+projectName);
       //	String[] command = {"/agentScripts/mvn_execute.sh",dir,projectName};
   		
    	
    	String command = "/agentScripts/mvn_stop.sh "+dir+" "+projectName;
    	System.out.println("Command:"+command);
       	//Executing user application on port 8080	
       			String output = executeBashCommand(command);


       			JSONObject data_file = new JSONObject();
       	        data_file.put("status", "stopped");
       	        return data_file;
    }
    
    
  //openStreamChannel
  	@RequestMapping(value="/openStreamChannel",method = RequestMethod.POST, consumes =
      	    "application/json" , produces = "application/json")
      @ResponseStatus(HttpStatus.CREATED)
      @ResponseBody

      public JSONObject openStreamChannel(@RequestBody JSONObject o ) {
       
  		
  		System.out.println("Start streaming");
        String logFile;
        String goal = o.get("goal").toString();
        if(goal.equalsIgnoreCase("execute")){
      	  logFile = "/agent/logs/*_execute.log";
        }else
      	  logFile = "/agent/logs/*_compile.log";
        String nc = "nohup tail -F "+ logFile +"|nc -lp 7777 &";
    	
    	
       	//Executing user application on port 8080	
       	String result = executeBashCommand(nc);

       	System.out.println("End streaming");
		JSONObject data_file = new JSONObject();
        data_file.put("status", "streaming");
        return data_file;
  	
  	}
    
    
    
    
    
    
}
