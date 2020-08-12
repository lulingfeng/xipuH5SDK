package com.startobj.util.device;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Method;

public class SOTelephoneInfo {
    private static final String TAG = SOTelephoneInfo.class.getSimpleName();
    private static SOTelephoneInfo mTelephoneInfo;
    private static Context mContext;
    private String imeiSIM1;// IMEI
    private String imeiSIM2;// IMEI
    private String meidSIM1;// MEID
    private String meidSIM2;// MEID
    private String iNumeric1;// sim1 code number
    private String iNumeric2;// sim2 code number
    private boolean isSIM1Ready;// sim1
    private boolean isSIM2Ready;// sim2
    private String iDataConnected1 = "0";// sim1 0 no, 1 connecting, 2
    // connected, 3 suspended.
    private String iDataConnected2 = "0";// sim2

    private SOTelephoneInfo() {
    }

    public synchronized static SOTelephoneInfo getInstance(Context context) {
        if (mTelephoneInfo == null) {
            mTelephoneInfo = new SOTelephoneInfo();
        }
        mContext = context;
        return mTelephoneInfo;
    }

    /**
     * @param slotId slotId为卡槽Id，它的值为 0、1；
     * @return
     */
    private static String getIMEI(Context context, int slotId) {
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method method = manager.getClass().getMethod("getImei", int.class);
            String imei = (String) method.invoke(manager, slotId);
            return imei;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * @param slotId slotId为卡槽Id，它的值为 0、1；
     * @return
     */
    private static String getMEID(Context context, int slotId) {
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method method = manager.getClass().getMethod("getMeid", int.class);
            String meid = (String) method.invoke(manager, slotId);
            return meid;
        } catch (Exception e) {
            return "";
        }
    }

    private static String getOperatorBySlot(Context context, String predictedMethodName, int slotID)
            throws GeminiMethodNotFoundException {
        String inumeric = null;
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);
            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephony, obParameter);
            if (ob_phone != null) {
                inumeric = ob_phone.toString();
            }
        } catch (Exception e) {
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }
        return inumeric;
    }

    private static boolean getSIMStateBySlot(Context context, String predictedMethodName, int slotID)
            throws GeminiMethodNotFoundException {

        boolean isReady = false;

        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {

            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimStateGemini.invoke(telephony, obParameter);

            if (ob_phone != null) {
                int simState = Integer.parseInt(ob_phone.toString());
                if (simState == TelephonyManager.SIM_STATE_READY) {
                    isReady = true;
                }
            }
        } catch (Exception e) {
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return isReady;
    }

    public String getImeiSIM1() {
        return TextUtils.isEmpty(imeiSIM1) ? "" : imeiSIM1;
    }

    public String getImeiSIM2() {
        return TextUtils.isEmpty(imeiSIM2) ? "" : imeiSIM2;
    }


    public String getMeidSIM1() {
        return TextUtils.isEmpty(meidSIM1) ? "" : meidSIM1;
    }

    public String getMeidSIM2() {
        return TextUtils.isEmpty(meidSIM2) ? "" : meidSIM2;
    }


    public boolean isSIM1Ready() {
        return isSIM1Ready;
    }

    public boolean isSIM2Ready() {
        return isSIM2Ready;
    }

    public boolean isDualSim() {
        return imeiSIM2 != null;
    }

    public boolean isDataConnected1() {
        if (TextUtils.equals(iDataConnected1, "2") || TextUtils.equals(iDataConnected1, "1"))
            return true;
        else
            return false;
    }

    public boolean isDataConnected2() {
        if (TextUtils.equals(iDataConnected2, "2") || TextUtils.equals(iDataConnected2, "1"))
            return true;
        else
            return false;
    }

    public String getINumeric1() {
        return TextUtils.isEmpty(iNumeric1) ? "" : iNumeric1;
    }

    public String getINumeric2() {
        return TextUtils.isEmpty(iNumeric2) ? "" : iNumeric2;
    }

    public String getINumeric() {
        if (imeiSIM2 != null) {
            if (iNumeric1 != null && iNumeric1.length() > 1)
                return iNumeric1;

            if (iNumeric2 != null && iNumeric2.length() > 1)
                return iNumeric2;
        }
        return iNumeric1;
    }

    public void setTelephoneInfo() {
        TelephonyManager telephonyManager = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE));

        mTelephoneInfo.imeiSIM1 = getIMEI(mContext, 0);
        mTelephoneInfo.imeiSIM2 = getIMEI(mContext, 1);
        mTelephoneInfo.meidSIM1 = getMEID(mContext, 0);
        mTelephoneInfo.meidSIM2 = getMEID(mContext, 1);

        try {
            if (TextUtils.isEmpty(mTelephoneInfo.imeiSIM1))
                mTelephoneInfo.imeiSIM1 = getOperatorBySlot(mContext, "getDeviceIdGemini", 0);
            if (TextUtils.isEmpty(mTelephoneInfo.imeiSIM2))
                mTelephoneInfo.imeiSIM2 = getOperatorBySlot(mContext, "getDeviceIdGemini", 1);
            mTelephoneInfo.iNumeric1 = getOperatorBySlot(mContext, "getSimOperatorGemini", 0);
            mTelephoneInfo.iNumeric2 = getOperatorBySlot(mContext, "getSimOperatorGemini", 1);
            mTelephoneInfo.iDataConnected1 = getOperatorBySlot(mContext, "getDataStateGemini", 0);
            mTelephoneInfo.iDataConnected2 = getOperatorBySlot(mContext, "getDataStateGemini", 1);
        } catch (GeminiMethodNotFoundException e) {
            try {
                if (TextUtils.isEmpty(mTelephoneInfo.imeiSIM1))
                    mTelephoneInfo.imeiSIM1 = getOperatorBySlot(mContext, "getDeviceId", 0);
                if (TextUtils.isEmpty(mTelephoneInfo.imeiSIM2))
                    mTelephoneInfo.imeiSIM2 = getOperatorBySlot(mContext, "getDeviceId", 1);
                mTelephoneInfo.iNumeric1 = getOperatorBySlot(mContext, "getSimOperator", 0);
                mTelephoneInfo.iNumeric2 = getOperatorBySlot(mContext, "getSimOperator", 1);
                mTelephoneInfo.iDataConnected1 = getOperatorBySlot(mContext, "getDataState", 0);
                mTelephoneInfo.iDataConnected2 = getOperatorBySlot(mContext, "getDataState", 1);
            } catch (GeminiMethodNotFoundException e1) {
            }
        }
        mTelephoneInfo.isSIM1Ready = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
        mTelephoneInfo.isSIM2Ready = false;

        try {
            mTelephoneInfo.isSIM1Ready = getSIMStateBySlot(mContext, "getSimStateGemini", 0);
            mTelephoneInfo.isSIM2Ready = getSIMStateBySlot(mContext, "getSimStateGemini", 1);
        } catch (GeminiMethodNotFoundException e) {
            try {
                mTelephoneInfo.isSIM1Ready = getSIMStateBySlot(mContext, "getSimState", 0);
                mTelephoneInfo.isSIM2Ready = getSIMStateBySlot(mContext, "getSimState", 1);
            } catch (GeminiMethodNotFoundException e1) {
            }
        }

        if (TextUtils.isEmpty(mTelephoneInfo.imeiSIM1))
            mTelephoneInfo.imeiSIM1 = telephonyManager.getDeviceId();
    }

    private static class GeminiMethodNotFoundException extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = -3241033488141442594L;

        public GeminiMethodNotFoundException(String info) {
            super(info);
        }
    }

}
