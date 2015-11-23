package com.ironsource.mobilcore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.ironsource.mobilcore.ReportingConsts.EReportType;

class ZipUtils {
	private static final int BUFFER_SIZE = 1024;

	public static boolean zipFileToInternalMemory(InputStream zipIn, String destDirectory) {
		ZipInputStream zis;
		try {
			zis = new ZipInputStream(new BufferedInputStream(zipIn));
			ZipEntry ze;
			/* create dest directory if not exist */
			File destDir = new File(destDirectory);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			/* extract files */
			while ((ze = zis.getNextEntry()) != null) {
				String filePath = destDirectory + File.separator + ze.getName();
				if (!ze.isDirectory()) {
					/* if the entry is a file, extracts it */
					extractFile(zis, filePath);
				} else {
					/* if the entry is a directory, make the directory */
					File dir = new File(filePath);
					dir.mkdir();
				}
				zis.closeEntry();
			}
			zis.close();
		} catch (IOException e) {
			IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
			return false;
		}
		return true;
	}

	private static boolean extractFile(ZipInputStream zis, String filePath) {
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
			int count = 0;
			while ((count = zis.read(buffer)) != -1) {
				bos.write(buffer, 0, count);
			}
			bos.close();
			return true;
		} catch (Exception ex) {
			IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(ex).send();
			ex.printStackTrace();
		}
		return false;
	}

	public static boolean zipFileToInternalMemory(String fileDir, File zipFile) {
		InputStream is;
		ZipInputStream zis;
		try {
			is = new FileInputStream(zipFile);
			zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;
			String dirName = zipFile.getName().substring(0, zipFile.getName().length() - 4);
			File filePath = new File(fileDir + "/" + dirName);
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			while ((ze = zis.getNextEntry()) != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int count;
				if (ze.isDirectory()) {
					continue;
				}
				File dirFile = new File(fileDir + "/" + dirName + "/" + ze.getName());
				if (!dirFile.getParentFile().exists()) {
					dirFile.getParentFile().mkdirs();
				}
				String fileName = ze.getName();
				File currFile = new File(fileDir + "/" + dirName, fileName);
				currFile.getParentFile().mkdirs();
				FileOutputStream fout = new FileOutputStream(currFile);
				while ((count = zis.read(buffer)) != -1) {
					baos.write(buffer, 0, count);
					byte[] bytes = baos.toByteArray();
					fout.write(bytes);
					baos.reset();
				}
				fout.close();
				baos.close();
				zis.closeEntry();
			}
			zis.close();
		} catch (IOException e) {
			IronBeastReportData.openReport(EReportType.REPORT_TYPE_ERROR).setError(e).send();
			return false;
		}
		return true;
	}
}
