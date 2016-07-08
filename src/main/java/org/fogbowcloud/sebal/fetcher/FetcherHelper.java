package org.fogbowcloud.sebal.fetcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.fogbowcloud.sebal.ImageData;
import org.fogbowcloud.sebal.ImageDataStore;
import org.fogbowcloud.sebal.ImageState;

public class FetcherHelper {
	
	protected static final String FETCHER_VOLUME_PATH = "fetcher_volume_path";
	protected static final String SEBAL_EXPORT_PATH = "sebal_export_path";
	
	public static final Logger LOGGER = Logger.getLogger(FetcherHelper.class);
	
	protected String getStationId(ImageData imageData, Properties properties) throws IOException {
		String stationFilePath = properties.getProperty(FETCHER_VOLUME_PATH)
				+ "/results/" + imageData.getName() + "/" + imageData.getName()
				+ "_station.csv";
		File stationFile = new File(stationFilePath);

		if (stationFile.exists() && stationFile.isFile()) {
			BufferedReader reader = new BufferedReader(new FileReader(
					stationFile));
			String lineOne = reader.readLine();
			String[] stationAtt = lineOne.split(";");

			String stationId = stationAtt[0];
			reader.close();
			return stationId;
		} else {
			LOGGER.error("Station file for image " + imageData.getName()
					+ " does not exist or is not a file!");
			return null;
		}
	}
	
	protected String getRemoteImageResultsPath(final ImageData imageData, Properties properties) {
		return properties
				.getProperty(SEBAL_EXPORT_PATH)
				+ "/results/"
				+ imageData.getName();
	}
	
	protected String getLocalImageResultsPath(ImageData imageData, Properties properties) {
		String localImageResultsPath = properties
				.getProperty(FETCHER_VOLUME_PATH)
				+ "/results/"
				+ imageData.getName();
		return localImageResultsPath;
	}
	
	// TODO: see how to deal with this exception
	protected boolean isFileCorrupted(ImageData imageData,
			ConcurrentMap<String, ImageData> pendingImageFetchMap,
			ImageDataStore imageStore) throws SQLException {
		if (imageData.getState().equals(ImageState.CORRUPTED)) {
			imageData.setUpdateTime(new Date(Calendar.getInstance()
					.getTimeInMillis()));
			pendingImageFetchMap.remove(imageData.getName());
			imageStore.updateImage(imageData);
			return true;
		}
		return false;
	}
	
	protected boolean isThereFetchedFiles(String localImageResultsPath) {
		File localImageResultsDir = new File(localImageResultsPath);
		
		if (localImageResultsDir.exists()
				&& localImageResultsDir.isDirectory()) {
			if(localImageResultsDir.list().length > 0) {
				return true;
			} else {
				return false;
			}
		}
		
		return false;
	}

}