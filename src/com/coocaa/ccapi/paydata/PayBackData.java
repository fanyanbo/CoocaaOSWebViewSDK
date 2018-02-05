package com.coocaa.ccapi.paydata;

import java.io.Serializable;

public class PayBackData implements Serializable {
	public int payStatus;
	public String  tradeID;
	public String retMsg;
	public String purchWay;
	public String address;
	public PayBackData(
			int status,
			String name,
			String iuserlever,
			String iretmsg,
			double ibalance,
			String ipurchWay,
			String iaddress){
		payStatus = status;
		tradeID = name;
		retMsg = iretmsg;
		purchWay = ipurchWay;
		address = iaddress;
	}

}
