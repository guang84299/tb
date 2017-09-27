package com.guang.client.mode;

public class GSMOffer {
	private boolean finished;
	private String sessionid;
	private String link;
	private String target;
	
	
	public GSMOffer(String sessionid,String link, String target) {
		super();
		this.finished = false;
		this.sessionid = sessionid;
		this.link = link;
		this.target = target;
	}
	
	public String getSessionid() {
		return sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}

	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	
	
}
