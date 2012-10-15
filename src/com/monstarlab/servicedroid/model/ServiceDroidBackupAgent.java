package com.monstarlab.servicedroid.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;

@TargetApi(8)
public class ServiceDroidBackupAgent extends BackupAgent {

	private static final String APP_DATA_KEY = "alldata";

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
		//determine if backup should happen based on oldState
		boolean doBackup = (oldState == null); //no old state means first time backing up
		if (!doBackup) {
			//is an old state, so find out if old state is the same as current state
			doBackup = true;
		}
		
		//do backup
		if (doBackup) {
			BackupWorker bw = new BackupWorker();
			String serializedData = bw.backup(getContentResolver());

			ByteArrayOutputStream bufStream = new ByteArrayOutputStream();

			// We use a DataOutputStream to write structured data into
			// the buffering stream
			DataOutputStream outWriter = new DataOutputStream(bufStream);
			outWriter.writeChars(serializedData);
			
			// Okay, we've flattened the data for transmission.  Pull it
			// out of the buffering stream object and send it off.
			byte[] buffer = bufStream.toByteArray();
			int len = buffer.length;
			data.writeEntityHeader(APP_DATA_KEY, len);
			data.writeEntityData(buffer, len);
		}
		
		//record a newState
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		while (data.readNextHeader()) {
			String key = data.getKey();
			int dataSize = data.getDataSize();
			
			if (APP_DATA_KEY.equals(key)) {
				byte[] dataBuf = new byte[dataSize];
				data.readEntityData(dataBuf, 0, dataSize);
				ByteArrayInputStream baStream = new ByteArrayInputStream(dataBuf);
				DataInputStream in = new DataInputStream(baStream);
				
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = in.readLine()) != null) {
					sb.append(line);
				}
				String serializedData = sb.toString();
				
				//import data into the DB via BackupWorker
				BackupWorker bw = new BackupWorker();
				bw.restore(getContentResolver(), serializedData);
			}
		}
	}

}
