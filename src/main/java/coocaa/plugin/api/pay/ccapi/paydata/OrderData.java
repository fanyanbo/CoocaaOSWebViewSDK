package coocaa.plugin.api.pay.ccapi.paydata;

public class OrderData {
    public String appcode;	
    public String ProductName;	
    public String ProductType; //movie,game or other product 
    public String TradeId;	
    public String SpecialType;
    public double amount;	
    public String imageUrl;
	public String getImageUrl()
    {
        return imageUrl;
    }
    public void setImgUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }
    public int getCount()
    {
        return count;
    }
    public void setCount(int count)
    {
        this.count = count;
    }
    public String getProductSubName()
    {
        return ProductSubName;
    }
    public void setProductsubName(String productSubName)
    {
        ProductSubName = productSubName;
    }
    public String getSpec()
    {
        return spec;
    }
    public void setSpec(String spec)
    {
        this.spec = spec;
    }

    public int count;
    public String ProductSubName; 
    public String spec;
	
	public OrderData(){
		
	}
	public OrderData(String appcode,
			String ProductName,
			String ProductType,
			String tradeid,
			String specialtype,
			double amount){
		this.appcode = appcode;	
		this.ProductName = ProductName;	
		this.ProductType = ProductType; //movie,game or other product  virtual or physical
		this.TradeId = tradeid;
		this.amount =  amount;
		this.SpecialType = specialtype;
	}
	public void setappcode(String appcode) {
		this.appcode = appcode;
	}

	public String getappcode() {
		return appcode;
	}
	
	public void setProductName(String ProductName) {
		this.ProductName = ProductName;
	}

	public String getProductName() {
		return ProductName;
	}
	
	public void setProductType(String ProductType) {
		this.ProductType = ProductType;
	}

	public String getProductType() {
		return ProductType;
	}
	
	public void setTradeId(String TradeId) {
		this.TradeId = TradeId;
	}

	public String getTradeId() {
		return TradeId;
	}
	public void setSpecialType(String specialType) {
		this.SpecialType = specialType;
	}

	public String getSpecialType() {
		return SpecialType;
	}
	
	public void setamount(double amount) {
		this.amount = amount;
	}

	public double getamount() {
		return amount;
	}	
	
	
}
