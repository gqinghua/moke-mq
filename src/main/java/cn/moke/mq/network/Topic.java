package cn.moke.mq.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Topic implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String topic;
	
	private int readCounter;
	
	private int writeCounter;
	
	private List<Object> contents = new ArrayList<Object>();

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public List<Object> getContents() {
		return contents;
	}
	
	public void addContent(Serializable content){
		contents.add(content);
	}

	public int getReadCounter() {
		return readCounter;
	}

	public void setReadCounter(int readCounter) {
		this.readCounter = readCounter;
	}

	public int getWriteCounter() {
		return writeCounter;
	}

	public void setWriteCounter(int writeCounter) {
		this.writeCounter = writeCounter;
	}

	public String toString(){
		return String.format("topic:%s,read counter:%d,write counter:%d,content:%s", topic, readCounter, writeCounter, contents.toString());
	}
	
	

}
