/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package com.mamashai.alipay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.HashMap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;


@Kroll.module(name="AlipayAndroid", id="com.mamashai.alipay")
public class AlipayAndroidModule extends KrollModule
{

	// Standard Debugging variables
	private static final String LCAT = "AlipayAndroidModule";
	private static final boolean DBG = TiConfig.LOGD;	
	
	//商户私钥，pkcs8格式
	public static String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAPKQNq01zk8X8KvEvPcU9fQM/OABzz2Q1576f303DGzL5Jy2ihNHzMbXOt6o707fMCvtA+jb98FycSuOoZcGe4miLitJeiyrfALQ7axs+TNpWJVfGOBjClyTDx3s1DOvBqbknAz7VqiwttWqkg5XBLlRAPQ0oO19EqRCYA6kr96lAgMBAAECgYEA76aIPs3ATejLQgoY4M12y27hkLh49szaHBpGR4JR5lP0RNkcxjvUGEihw0eJWJWuVFfR2wkpWZkmMvCyujIPblnBg2WOoq2a23ljnkddLTLRZsdDbIucAMDHeK8ZHumeJ/tkD/ypqHBVi4kYmnICBLpJV5lDnk/PoaIM4/fO9IECQQD60568OKtH6AZCieZEzcdswNX+fCOTvEmLWmCV+oCGCH3l/eDwj84sD06j1zZPBMLMlmt0gfzDg2VS9CilrV01AkEA95D2d2BLsHaC+t289PCtDmHECkNqwlnAhZkK1jj3poqQes1ptWMI8TLOym2PM8frtvGZjx9IMrM+AucwhR9ZsQJAIGTUS1rGRDMjG9TTeG9bIiCFgqhlr97RYL37W2NO1gCiweFX+7mW1vnjHiXdTbc/sUx79EAVdOqzW1NNLJiHQQJAX9myc1nHNFVONQ7w/+zHNBBKNKcRiJnzXkZ42aRIziRL+B/b06y6Y5iGU/3DOgsnijdUewNjkq2vTrRwJrqSoQJAEYf93Tyu8DGhWnsx0cCOgcWPmVBMnSDaN0CxIJ+x7dKShvk3Ejl0NWM7kQ1ZaYJp/dms6MYwQkTGletdcQk1WQ==";
	//支付宝公钥
	public static String RSA_PUBLIC = "mw0mqdblunyhqb72l6xit24jhmvx73xx";
	
	private static final int SDK_PAY_FLAG = 1;
	
	public String trade_no = "";

	private static final int SDK_CHECK_FLAG = 2;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SDK_PAY_FLAG: {
				PayResult payResult = new PayResult((String) msg.obj);
				
				// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
				String resultInfo = payResult.getResult();
				
				String resultStatus = payResult.getResultStatus();

				// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
				HashMap<String, Object> event = new HashMap<String, Object>();
				event.put("resultStatus", resultStatus);
				event.put("trade_no", trade_no);
				if (TextUtils.equals(resultStatus, "9000")) {
					event.put("desc", "支付成功");
				} else {
					// 判断resultStatus 为非“9000”则代表可能支付失败
					// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						event.put("desc", "支付结果确认中");
						
					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						event.put("desc", "支付失败");
					}
				}
				fireEvent("paid", event);
				break;
			}
			case SDK_CHECK_FLAG: {
				Toast.makeText(getActivity(), "检查结果为：" + msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			}
			default:
				break;
			}
		};
	};
	
	@Kroll.method
	public void alipay(Object arg){
		HashMap<String, String> kd = (HashMap<String, String>)arg;
		
		RSA_PRIVATE = kd.get("private_key");
		RSA_PUBLIC = kd.get("public_key");
		trade_no = kd.get("id");
		
		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + kd.get("partner") + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + kd.get("seller") + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + kd.get("id") + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + kd.get("subject") + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + kd.get("body") + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + kd.get("price") + "\"";

		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + kd.get("notify_url") + "\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		// 对订单做RSA 签名
		String sign = sign(orderInfo);
		try {
			// 仅需对sign 做URL编码
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 完整的符合支付宝参数规范的订单信息
		final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
				+ getSignType();

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(getActivity());
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo);

				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}


	public AlipayAndroidModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(LCAT, "inside onAppCreate");
		// put module init code that needs to run when the application is created
	}

	// Methods
	@Kroll.method
	public String example()
	{
		Log.d(LCAT, "example called");
		return trade_no;
	}

	// Properties
	@Kroll.getProperty
	public String getExampleProp()
	{
		Log.d(LCAT, "get example property");
		return "hello world";
	}


	@Kroll.setProperty
	public void setExampleProp(String value) {
		Log.d(LCAT, "set example property: " + value);
	}
	
	/**
	 * create the order info. 创建订单信息
	 * 
	 */

	
	public String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
				Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);

		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}
	
	/**
	 * sign the order info. 对订单信息进行签名
	 * 
	 * @param content
	 *            待签名订单信息
	 */
	public String sign(String content) {
		return SignUtils.sign(content, RSA_PRIVATE);
	}

	/**
	 * get the sign type we use. 获取签名方式
	 * 
	 */
	public String getSignType() {
		return "sign_type=\"RSA\"";
	}

}

