package bft_log.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import bft_log.aontrs.Aont;
import bft_log.aontrs.ReedSolomonShardGenerator;
import bft_log.aontrs.ReedSolomonShardReconstructor;

public class AontrsTest {
	public static void main(String args[]) throws IOException{
		File f = new File("/Users/BortolameottiR/workspace/bft_log/src/bft_log/Test1");
		String aontPackageFilePath = "/Users/BortolameottiR/workspace/bft_log/src/bft_log/Test1AONTPACKAGE.txt";
		Aont test = new Aont(f);
		System.out.println("The AONT package of the file is: " + Arrays.toString(test.aontPackage));
		
		try {
			test.getAontEncodedPackageAsFile(aontPackageFilePath);
			File f1 = new File(aontPackageFilePath);
			ReedSolomonShardGenerator rsg = new ReedSolomonShardGenerator(f1);
			ReedSolomonShardReconstructor rsr = new ReedSolomonShardReconstructor(f1);
			File f2 = new File(aontPackageFilePath + ".decoded");
			byte[] decodedAont = test.AontDecoding(test.getBytesFromFile(f1), "/Users/BortolameottiR/workspace/bft_log/src/bft_log/Test1Decoded.txt");

		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
