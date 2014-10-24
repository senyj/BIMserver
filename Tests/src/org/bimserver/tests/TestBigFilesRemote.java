package org.bimserver.tests;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.interfaces.objects.SDatabaseInformation;
import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
import org.bimserver.interfaces.objects.SJavaInfo;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.shared.ChannelConnectionException;
import org.bimserver.shared.PublicInterfaceNotFoundException;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.utils.Formatters;

public class TestBigFilesRemote {
	 public static void main(String[] args) {
		new TestBigFilesRemote().start(args);
	}

	private void start(String[] args) {
		String address = args[0];
		String username = args[1];
		String password = args[2];
		String basepath = args[3];
		System.out.println("Address: " + address);
		System.out.println("Username: " + username);
		System.out.println("Password: " + password);
		System.out.println("Basepath: " + basepath);
		JsonBimServerClientFactory factory = new JsonBimServerClientFactory(address);
		try {
			BimServerClient client = factory.create(new UsernamePasswordAuthenticationInfo(args[1], args[2]));
			
			String[] fileNames = new String[]{
				"4NC Whole Model.ifc",
				"1006 General withIFC_exportLayerCombos.ifc",
				"12001_17 MOS_AC17SpecialBigVersion.ifc",
				"12510_MASTER_Drofus_Test.ifc",
				"BondBryan10-134 (06) Proposed Site-1.ifc",
				"HLM_39090_12259 University of Sheffield NEB  [PR-BIM-01-bhelberg].ifc"
			};
			
			SDeserializerPluginConfiguration deserializer = client.getBimsie1ServiceInterface().getSuggestedDeserializerForExtension("ifc");

			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
			for (String fileName : fileNames) {
				String projectName = fileName.substring(0, fileName.lastIndexOf(".ifc"));
				System.out.println("Creating project " + fileName);
				System.out.println(dateFormat.format(new Date()));
				SDatabaseInformation databaseInformation = client.getAdminInterface().getDatabaseInformation();
				System.out.println("Database size: " + Formatters.bytesToString(databaseInformation.getDatabaseSizeInBytes()) + " (" + databaseInformation.getDatabaseSizeInBytes() + ")");
				SJavaInfo javaInfo = client.getAdminInterface().getJavaInfo();
				System.out.println("Used: " + Formatters.bytesToString(javaInfo.getHeapUsed()) + ", Free: " + Formatters.bytesToString(javaInfo.getHeapFree()) + ", Max: " + Formatters.bytesToString(javaInfo.getHeapMax()) + ", Total: " + Formatters.bytesToString(javaInfo.getHeapTotal()));
				SProject project = client.getBimsie1ServiceInterface().addProject(projectName);
				String downloadUrl = URLEncoder.encode(basepath + fileName, "UTF-8");
				client.getServiceInterface().checkinFromUrl(project.getOid(), fileName, deserializer.getOid(), fileName, downloadUrl, false, true);
				System.out.println("Done checking in " + fileName);
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (ChannelConnectionException e) {
			e.printStackTrace();
		} catch (PublicInterfaceNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}