package com.startobj.util.file;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.startobj.util.common.SOCommonUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * @Explain
 * @Version 1.0
 * @CreateDate 2016/05/27 下午7:10
 * @Author Eagle Email:lizhengpei@gmail.com
 */
@SuppressLint("NewApi")
public class SOFileUtil {
	private static String TAG = SOFileUtil.class.getName();

	/**
	 * 获得文件路径
	 *
	 * @return
	 */
	public static String obtainFilePath(Context context, String foldername, String filename) {
		return SOCommonUtil.hasContext(context) && !TextUtils.isEmpty(filename) && !TextUtils.isEmpty(foldername)
				? getSDPath(context, false) + File.separator + foldername + File.separator + filename : "";
	}

	public static String obtainFilePath(Context context, String filename) {
		return SOCommonUtil.hasContext(context) && !TextUtils.isEmpty(filename)
				? getSDPath(context, false) + File.separator + filename : "";
	}

	/**
	 * 写数据到文件
	 *
	 * @param filePath
	 * @param write_str
	 * @throws IOException
	 */
	public static void writeFileSdcardFile(String filePath, String write_str, boolean append) throws IOException {
		if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(write_str))
			return;
		int position = filePath.lastIndexOf(File.separator);
		String dirPath = filePath.substring(0, position);
		if (!isFolderExist(dirPath))
			// 文件夹不存在
			mrDir(dirPath);
		// 文件存在
		if (!isFileExist(filePath)) {
			// 文件不存在，创建
			try {
				createFile(filePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream fout = new FileOutputStream(filePath, append);
			byte[] bytes = write_str.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读SD中的文件
	 *
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String readFileSdcardFile(String fileName) throws IOException {
		if (TextUtils.isEmpty(fileName))
			return "";
		// 得到输入流之后
		FileInputStream inStream = new FileInputStream(fileName);
		// 创建一个往内存输出流对象
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			// 把每次读到的数据写到内存中
			outStream.write(buffer, 0, len);
		}
		// 得到存放在内存中的所有的数据
		byte[] data = outStream.toByteArray();
		return new String(data);
	}

	/**
	 * 创建文件夹
	 *
	 * @param path
	 */
	public static String mrDir(String path) {
		if (TextUtils.isEmpty(path))
			return "";
		if (checkSDCard()) {
			File dirFile = new File(path);
			dirFile.mkdir();
			return path;
		} else {
			return "";
		}
	}

	/**
	 * 判断文件夹是否存在
	 *
	 * @param folderName
	 */
	public static boolean isFolderExist(String folderName) {
		if (TextUtils.isEmpty(folderName))
			return false;
		File file = new File(folderName);
		return file.exists();
	}

	/**
	 * 判断文件是否存在
	 *
	 * @param fileName
	 */
	public static boolean isFileExist(String fileName) {
		if (TextUtils.isEmpty(fileName))
			return false;
		File file = new File(fileName);
		return file.exists();
	}

	public static File createFile(String fileName) throws IOException {
		if (TextUtils.isEmpty(fileName))
			return null;
		File file = new File(fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * 用户名解密
	 *
	 * @param ssoToken
	 *            字符串
	 * @return String 返回加密字符串
	 */
	public static String decrypt(String ssoToken) {
		if (TextUtils.isEmpty(ssoToken))
			return "";
		try {
			StringBuffer name = new StringBuffer();
			java.util.StringTokenizer st = new java.util.StringTokenizer(ssoToken, "&");
			while (st.hasMoreElements()) {
				int asc = Integer.parseInt((String) st.nextElement()) - 88;
				name = name.append((char) asc);
			}

			return name.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 用户名加密
	 *
	 * @param ssoToken
	 *            字符串
	 * @return String 返回加密字符串
	 */
	public static String encrypt(String ssoToken) {
		if (TextUtils.isEmpty(ssoToken))
			return "";
		try {
			byte[] _ssoToken = ssoToken.getBytes("ISO-8859-1");
			StringBuffer name = new StringBuffer();
			for (int i = 0; i < _ssoToken.length; i++) {
				int asc = _ssoToken[i];
				_ssoToken[i] = (byte) (asc + 88);
				name = name.append((asc + 88) + "&");
			}
			return name.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param sPath
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String sPath) {
		if (TextUtils.isEmpty(sPath))
			return false;
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * 判断SD卡是否存在
	 *
	 * @return boolean
	 */
	public static boolean checkSDCard() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取SD路径
	 * <p/>
	 * 默认获取内置，如果外置不存在则取内置 谷歌官方不推荐使用外置存储卡，并且正在淘汰 Android API 23(6.0) 中获取外置SD被移除
	 *
	 * @param context
	 *            上下文
	 * @param is_outlay
	 *            false:内置,true:外置
	 */
	public static String getSDPath(Context context, boolean is_outlay) {
		if (!SOCommonUtil.hasContext(context))
			return "";
		String path;
		if (Build.VERSION.SDK_INT >= 23) {
			path = getStoragePath(context, false);
		} else {
			path = getStoragePath(context, is_outlay);
			if (TextUtils.isEmpty(path)) {
				path = getStoragePath(context, !is_outlay);
			}
		}
		return path;
	}

	/**
	 * 获取SD路径
	 * <p/>
	 * 通过反射的方式使用在sdk中被 隐藏 的类 StroageVolume 中的方法getVolumeList()，
	 * 获取所有的存储空间（Stroage Volume），然后通过参数is_removable控制， 来获取内部存储和外部存储（内外sd卡）的路径
	 *
	 * @param context
	 *            上下文
	 * @param is_removale
	 *            false:内置,true:外置
	 * @return
	 */
	private static String getStoragePath(Context context, boolean is_removale) {
		if (!SOCommonUtil.hasContext(context))
			return "";
		StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
		Class<?> storageVolumeClazz = null;
		try {
			storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
			Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
			Method getPath = storageVolumeClazz.getMethod("getPath");
			Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
			Object result = getVolumeList.invoke(mStorageManager);
			final int length = Array.getLength(result);
			for (int i = 0; i < length; i++) {
				Object storageVolumeElement = Array.get(result, i);
				String path = (String) getPath.invoke(storageVolumeElement);
				boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
				Log.i(TAG, storageVolumeElement.toString());
				if (is_removale == removable) {
					return path;
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取手机可用的存储路径
	 * <p/>
	 * 如果SD卡不可用,则使用App路径 context.getFilesDir().getAbsolutePath() ==
	 * /data/data/com.xxx.xxx/files 并优先判断手机内置SD卡容量大小，后判断手机
	 */
	public static String getPhoneMemoryPath(Context context) {
		if (!SOCommonUtil.hasContext(context))
			return "";
		String memoryPath = context.getFilesDir().getAbsolutePath();
		String sdStatus = Environment.getExternalStorageState();
		boolean sdCardExist = sdStatus.equals(Environment.MEDIA_MOUNTED);

		if (TextUtils.isEmpty(sdStatus) || !sdCardExist)
			return memoryPath;

		try {
			long sdcardSpace = 0;
			try {
				sdcardSpace = getSDcardAvailableSpace(context);
			} catch (Exception e) {
				Log.d(TAG, "error:" + e.getMessage());
			}
			if (sdcardSpace >= 5) {
				return getSDPath(context, false);
			}

			long phoneSpace = getDataStorageAvailableSpace();
			if (phoneSpace >= 5) {
				return memoryPath;
			}
			// Log.d(TAG, String.format("get storage space, phone: %d, sdcard:
			// %d", (int) (phoneSpace / 1024 / 1024),
			// (int) (sdcardSpace / 1024 / 1024)));
		} catch (Exception e) {
			// Log.d(TAG, "error:" + e.getMessage());
		}

		return memoryPath;
	}

	/**
	 * 获取手机内部可用空间大小
	 *
	 * @return
	 */
	public static long getDataStorageAvailableSpace() {
		return getFolderAvailableSpace(Environment.getDataDirectory().getPath());
	}

	/**
	 * 获取手机内置SD卡可用空间大小
	 */
	public static long getSDcardAvailableSpace(Context context) {
		if (!SOCommonUtil.hasContext(context))
			return 0;
		if (checkSDCard()) {
			String path = getSDPath(context, false);
			if (path == null) {
				return 0;
			}
			return getFolderAvailableSpace(path);
		} else {
			return 0;
		}
	}

	/**
	 * 获取目录容量大小
	 *
	 * @param folderPath
	 *            目录路径
	 * @return
	 */
	public static long getFolderAvailableSpace(String folderPath) {
		if (TextUtils.isEmpty(folderPath)) {
			return 0;
		}
		StatFs stat = new StatFs(folderPath);
		long blockSize;
		long availableBlocks;
		if (Build.VERSION.SDK_INT >= 18) {
			blockSize = stat.getBlockSizeLong();
			availableBlocks = stat.getAvailableBlocksLong();
		} else {
			blockSize = stat.getBlockSize();
			availableBlocks = stat.getAvailableBlocks();
		}
		return availableBlocks * blockSize;
	}

	/**
	 * 获取文件的大小
	 *
	 * @param fileSize
	 *            文件的大小
	 * @return
	 */
	public static String formetFileSize(long fileSize) {// 转换文件大小
		DecimalFormat df = new DecimalFormat("###.00");
		String fileSizeString = "";
		if (fileSize < 1024) {
			fileSizeString = df.format((double) fileSize) + "B";
		} else if (fileSize < 1048576) {
			fileSizeString = df.format((double) fileSize / 1024) + "K";
		} else if (fileSize < 1073741824) {
			fileSizeString = df.format((double) fileSize / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileSize / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 获取文件MD5值
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String getMd5ByFile(File file) throws FileNotFoundException {
		String value = null;
		FileInputStream in = new FileInputStream(file);
		try {
			MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			value = bi.toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return value;
	}

	/**
	 * 把图片压缩到200K
	 * 
	 * @param oldpath
	 *            压缩前的图片路径
	 * @param newPath
	 *            压缩后的图片路径
	 * @return
	 */
	/**
	 * 把图片压缩到200K
	 * 
	 * @param oldpath
	 *            压缩前的图片路径
	 * @param newPath
	 *            压缩后的图片路径
	 * @return
	 */
	public static File compressFile(String oldpath, String newPath) {
		Bitmap compressBitmap = SOFileUtil.decodeFile(oldpath);
		Bitmap newBitmap = ratingImage(oldpath, compressBitmap);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		newBitmap.compress(CompressFormat.PNG, 100, os);
		byte[] bytes = os.toByteArray();

		File file = null;
		try {
			file = SOFileUtil.getFileFromBytes(bytes, newPath);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (newBitmap != null) {
				if (!newBitmap.isRecycled()) {
					newBitmap.recycle();
				}
				newBitmap = null;
			}
			if (compressBitmap != null) {
				if (!compressBitmap.isRecycled()) {
					compressBitmap.recycle();
				}
				compressBitmap = null;
			}
		}
		return file;
	}

	private static Bitmap ratingImage(String filePath, Bitmap bitmap) {
		int degree = readPictureDegree(filePath);
		return rotaingImageView(degree, bitmap);
	}

	/**
	 * 旋转图片
	 * 
	 * @param angle
	 * @param bitmap
	 * @return Bitmap
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		;
		matrix.postRotate(angle);
		// System.out.println("angle2=" + angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 把字节数组保存为一个文件
	 * 
	 * @param b
	 * @param outputFile
	 * @return
	 */
	public static File getFileFromBytes(byte[] b, String outputFile) {
		File ret = null;
		BufferedOutputStream stream = null;
		try {
			ret = new File(outputFile);
			FileOutputStream fstream = new FileOutputStream(ret);
			stream = new BufferedOutputStream(fstream);
			stream.write(b);
		} catch (Exception e) {
			// log.error("helper:get file from byte process error!");
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// log.error("helper:get file from byte process error!");
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	/**
	 * 图片压缩
	 * 
	 * @param fPath
	 * @return
	 */
	public static Bitmap decodeFile(String fPath) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		opts.inDither = false; // Disable Dithering mode
		opts.inPurgeable = true; // Tell to gc that whether it needs free
		opts.inInputShareable = true; // Which kind of reference will be used to
		BitmapFactory.decodeFile(fPath, opts);
		final int REQUIRED_SIZE = 200;
		int scale = 1;
		if (opts.outHeight > REQUIRED_SIZE || opts.outWidth > REQUIRED_SIZE) {
			final int heightRatio = Math.round((float) opts.outHeight / (float) REQUIRED_SIZE);
			final int widthRatio = Math.round((float) opts.outWidth / (float) REQUIRED_SIZE);
			scale = heightRatio < widthRatio ? heightRatio : widthRatio;//
		}
		Log.i("scale", "scal =" + scale);
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = scale;
		Bitmap bm = BitmapFactory.decodeFile(fPath, opts).copy(Config.ARGB_8888, false);
		return bm;
	}

	/**
	 * 获取目录名称
	 * 
	 * @param url
	 * @return FileName
	 */
	public static String getFileName(String url) {
		int lastIndexStart = url.lastIndexOf("/");
		if (lastIndexStart != -1) {
			return url.substring(lastIndexStart + 1, url.length());
		} else {
			return null;
		}
	}

	/**
	 * 删除该目录下的文件
	 * 
	 * @param path
	 */
	public static void delFile(String path) {
		if (!TextUtils.isEmpty(path)) {
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
		}
	}

	/**
	 * 根据资源ID获取Assets文件数据
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public static String getFromRaw(Context context, int resId) {
		try {
			InputStreamReader inputReader = new InputStreamReader(context.getResources().openRawResource(resId));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			String Result = "";
			while ((line = bufReader.readLine()) != null)
				Result += line;
			return Result;
		} catch (Exception e) {
		}
		return "";
	}

	/*
	 * 根据资源名称获取Assets文件数据
	 *
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String getAssetsFile(Context mContext, String fileName) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			AssetManager assets = mContext.getAssets();
			BufferedReader bf = new BufferedReader(new InputStreamReader(
					assets.open(fileName)));
			String line;
			while ((line = bf.readLine()) != null) {
				stringBuilder.append(line);
			}
			return stringBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	/*
	 * 从apk中获取版本信息
	 *
	 * @param context
	 * @param channelKey
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String getChannelFromApk(Context context, String channelKey) {
		// 从apk包中获取
		ApplicationInfo appinfo = context.getApplicationInfo();
		String sourceDir = appinfo.sourceDir;
		// 注意这里：默认放在meta-inf/里， 所以需要再拼接一下
		String key = "META-INF/" + channelKey;
		String ret = "";
		ZipFile zipfile = null;
		try {
			zipfile = new ZipFile(sourceDir);
			Enumeration<?> entries = zipfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = ((ZipEntry) entries.nextElement());
				String entryName = entry.getName();
				if (entryName.startsWith(key)) {
					ret = entryName;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (zipfile != null) {
				try {
					zipfile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ret.replace(key, "");
	}
}
