package bft_log.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import bft_log.aontrs.AontRS;

public class AontrsTest {
	public static void main(String args[]) throws FileNotFoundException{
		File f = new File("/Users/BortolameottiR/workspace/bft_log/src/bft_log/Test1");
		AontRS test = new AontRS(f);
		System.out.println("The AONT package of the file is: " + Arrays.toString(test.aontPackage));
		try {
			byte[] decodedAont = test.AontDecoding(test.aontPackage, "/Users/BortolameottiR/workspace/bft_log/src/bft_log/Test1Decoded.txt");

		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
