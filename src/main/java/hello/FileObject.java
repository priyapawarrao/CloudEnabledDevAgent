package hello;

import java.io.File;

public class FileObject {

    private String name;
    private String content;
    private String path;
    
	public FileObject(String name, String content, String path) {
		this.name = name;
		this.content = content;
		this.path = path;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public String getContent() {
		return content;
	}

    
}
