package com.kxzhu.timing_food_delivery.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;

import java.util.concurrent.CompletableFuture;



/**
 * 短信发送工具类
 */
public class SMSUtils {

	/**
	 * 发送短信
	 * @param signName 签名
	 * @param templateCode 模板
	 * @param phoneNumbers 手机号
	 * @param param 参数 需要填在"${code}"处的内容
	 */
	public static void sendMessageOriginal(String signName, String templateCode,String phoneNumbers,String param){
		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI5tSohViGtYJ4LQoM5aht", "sy8NXRd4OKktRea7PYdSQG81LhqYqA");
		IAcsClient client = new DefaultAcsClient(profile);

		SendSmsRequest request = new SendSmsRequest();
		request.setSysRegionId("cn-hangzhou");
		request.setPhoneNumbers(phoneNumbers);
		request.setSignName(signName);
		request.setTemplateCode(templateCode);
		request.setTemplateParam("{\"code\":\""+param+"\"}");
		try {
			SendSmsResponse response = client.getAcsResponse(request);
			System.out.println("短信发送成功");
		}catch (ClientException e) {
			e.printStackTrace();
		}
	}

	public static void sendMessage(String param){

		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI5tSohViGtYJ4LQoM5aht", "sy8NXRd4OKktRea7PYdSQG81LhqYqA");
		/** use STS Token
		 DefaultProfile profile = DefaultProfile.getProfile(
		 "<your-region-id>",           // The region ID
		 "<your-access-key-id>",       // The AccessKey ID of the RAM account
		 "<your-access-key-secret>",   // The AccessKey Secret of the RAM account
		 "<your-sts-token>");          // STS Token
		 **/

		IAcsClient client = new DefaultAcsClient(profile);

		SendSmsRequest request = new SendSmsRequest();
		request.setSignName("阿里云短信测试");
		request.setTemplateCode("SMS_154950909");
		request.setPhoneNumbers("17665266183");
		request.setTemplateParam("{\"code\":\"1234\"}");//{"code":"1234"} 测试专用模板中的变量只支持4-6位纯数字，所以先固定为1234

		try {
			SendSmsResponse response = client.getAcsResponse(request);
			System.out.println(new Gson().toJson(response));
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			System.out.println("ErrCode:" + e.getErrCode());
			System.out.println("ErrMsg:" + e.getErrMsg());
			System.out.println("RequestId:" + e.getRequestId());
		}

	}

}
