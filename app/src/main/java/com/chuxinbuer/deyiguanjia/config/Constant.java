package com.chuxinbuer.deyiguanjia.config;

import android.os.Environment;

import com.chuxinbuer.deyiguanjia.MyApplication;
import com.chuxinbuer.deyiguanjia.utils.SPUtil;


/**
 * Created by Administrator on 2017/10/24 0024.
 */

public class Constant {
    public static final String BASE_URL = "https://api.zhdyhb.com/";//请求数据域名
    //        public static final String BASE_URL = "http://47.106.252.214/";//请求数据域名
    public static final String STORE_PATH = Environment.getExternalStorageDirectory() + "/Android/data/" + MyApplication.mContext.getPackageName() + "/images/";//图片保存文件夹
    public static final String OKHTTP_CACHE_DIR = "okhttp_cache_dir";//网络请求数据缓存
    public static final long MAX_CACHE_SIZE_INBYTES = 1024 * 1024 * 50;//网络请求数据缓存大小 50M

    public static final String SERIAL_PORT_WEIGHT = "serial_port_weight";//称重串口
    public static final String SERIAL_PORT_LOCK = "serial_port_lock";//门锁串口
    //* @param baudRate 波特率
    // * @param parity 奇偶校验，0 None（默认）； 1 Odd； 2 Even
    // * @param dataBits 数据位，5 ~ 8  （默认8）
    // * @param stopBit 停止位，1 或 2  （默认 1）
    public static final String BAUD_RATE = "baud_rate";
    public static final String CHECK_DIGIT = "check_digit";
    public static final String DATA_BITS = "data_bits";
    public static final String STOP_BIT = "stop_bit";

    public static final String LASTTIME = "lasttime";//进入界面的时间戳
    public static final String IS_FINISHPAGE = "is_finishpage";//是否已经关闭界面

    public static final String IS_DOWNLOADING = "isDownloading";//是否正在下载安装包


    public static final String DEVICE_TOKEN = "device_token";//设备唯一标识

    public static final String WEIGHT_ZHIZHANG1 = "weight_zhizhang1";
    public static final String WEIGHT_ZHIZHANG2 = "weight_zhizhang2";
    public static final String WEIGHT_FANGZHI = "weight_fangzhi";
    public static final String WEIGHT_BOLI = "weight_boli";
    public static final String WEIGHT_SULIAO = "weight_suliao";
    public static final String WEIGHT_JINSHU = "weight_jinshu";


    public static final String WEIGHT_ZHIZHANG1_CUR = "weight_zhizhang1_cur";
    public static final String WEIGHT_ZHIZHANG2_CUR = "weight_zhizhang2_cur";
    public static final String WEIGHT_FANGZHI_CUR = "weight_fangzhi_cur";
    public static final String WEIGHT_BOLI_CUR = "weight_boli_cur";
    public static final String WEIGHT_SULIAO_CUR = "weight_suliao_cur";
    public static final String WEIGHT_JINSHU_CUR = "weight_jinshu_cur";


    public static final int REFUSEKIND_ZHIZHANG1 = 1;//纸张1
    public static final int REFUSEKIND_ZHIZHANG2 = 2;//纸张2
    public static final int REFUSEKIND_BOLI = 3;//玻璃
    public static final int REFUSEKIND_FANGZHI = 4;//纺织
    public static final int REFUSEKIND_JINSHU = 5;//金属
    public static final int REFUSEKIND_SULIAO = 6;//塑料

    public static final String FACELIST = "api/allFace";//获取人脸信息
    public static final String BANNER = "api/getBanner";//获取banner
    public static final String LOGIN_PASSWORD = "api/dev/login";//设备端账号密码登录
    public static final String LOGIN_SCAN = "api/dev/qrcodeLogin";//扫码登录
    public static final String LOGIN_MANAGER = "api/devAdmin/login";//管理员登录
    public static final String POST_REFUSE_SETTLEMENT = "api/dev/junkSettlement";//投递垃圾结算
    public static final String EXCHANGE_REFUSEBOX_MANAGER = "api/devadmin/junkBatch";//管理员更换垃圾箱
    public static final String UPDATE_APK = "api/dev/updatePackage";//安装包更新
    public static final String GETDEVICEINFO = "api/dev/selDevice";//获取设备信息
    public static final String ADDJUNKLOG = "api/dev/insertJunkLog";//添加垃圾投递记录
    public static final String FULLNOTICE = "api/fullNotice";//垃圾箱满报警
    public static final String GETCONFATTR = "api/getConfAttr";//获取后台配置参数
    public static final String FACELOG = "api/faceLog";//上传登录信息至后台
}
