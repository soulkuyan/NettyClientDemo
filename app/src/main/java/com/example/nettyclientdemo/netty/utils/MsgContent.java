package com.example.nettyclientdemo.netty.utils;

import java.io.Serializable;

public class MsgContent implements Serializable {
	String Content;
	Boolean Result = true;
	client.ProtobufProto.ProtobufMessage.Type type;
	int number = 0;

	public MsgContent(String content) {
		super();
		Content = content;
	}

	public MsgContent(int number) {
		super();
		this.number = number;
	}

	public MsgContent() {
		super();
	}

	public String getContent() {
		return Content;
	}

	public void setContent(String content) {
		Content = content;
	}

	public Boolean getResult() {
		return Result;
	}

	public void setResult(Boolean result) {
		Result = result;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public client.ProtobufProto.ProtobufMessage.Type getType() {
		return type;
	}

	public void setType(client.ProtobufProto.ProtobufMessage.Type type) {
		this.type = type;
	}

}
