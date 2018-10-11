package ztestb.iptv.aidl;

interface ServiceIPTVAidl {
    String GetIPTVOTTPassword();
    void SetIPTVOTTPassword(String strPassword);
	String GetInfo();
	void TriggerRelogin();
	void TVRelogin(int PlatformType);
	void setInterParam(String ServiceName,String ServicePswd,int PlatformType);
	void setOperaParams(String url,String ServiceName,String ServicePswd,int PlatformType);
 	String getIPTVPlatFormUrl();
  	String getIPTVPlatFormUser();
  	String getIPTVPlatFormPwd();
 	String getOTTPlatFormUrl();
	String getOTTPlatFormUser();   
  	String getOTTPlatFormPwd();
 	String getEpgServer2();
 	String getParams(String keyName);
 	int getDefalutPlatType();
 	void   setDefaultPlatType(int PlatformType);
 	
 	 	
}