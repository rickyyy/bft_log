package bft_log.Tests;

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
		File f = new File("/home/riccardo/git/bft_log/src/bft_log/Test1");
		String aontPackageFilePath = "/home/riccardo/git/bft_log/src/bft_log/Test1AONTPACKAGE.txt";
		Aont test = new Aont();
		try {
			test.aontPackage = test.AontEncoding(f);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("The AONT package of the file is: " + Arrays.toString(test.aontPackage));
		
		try {
			test.getAontEncodedPackageAsFile(aontPackageFilePath);
			File f1 = new File(aontPackageFilePath);
			ReedSolomonShardGenerator rsg = new ReedSolomonShardGenerator(f1);
			ReedSolomonShardReconstructor rsr = new ReedSolomonShardReconstructor(f1);
			File f2 = new File(aontPackageFilePath + ".decoded");
			byte[] decodedAont = test.AontDecoding(test.getBytesFromFile(f2), "/home/riccardo/git/bft_log/src/bft_log/Test1Decoded.txt");

		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
