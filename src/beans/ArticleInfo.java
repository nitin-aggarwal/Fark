package beans;

public class ArticleInfo {

	String content;
	String title;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		if(title.trim().length()>= 500)
			this.title = title.substring(0,300);
		else
			this.title = title.trim();
	}
	
}
