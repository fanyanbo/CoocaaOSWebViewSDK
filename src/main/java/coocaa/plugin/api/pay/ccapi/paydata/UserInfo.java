package coocaa.plugin.api.pay.ccapi.paydata;

public class UserInfo {
	public String mac;
	public String barcode;
	public String tel;
	public int loginstatus;
	public String userlever;
	public String token;
	public UserInfo(){
		
	}
	public UserInfo(int status,
			String mac,
			String barcode,
			String tel,
			String userlever,
	        String token){
		this.loginstatus =  status;
		this.mac = mac;
		this.barcode = barcode;
		this.tel =  tel;
		this.userlever = userlever;
		this.token = token;
	}
}
