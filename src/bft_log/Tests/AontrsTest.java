package bft_log.tests;

import java.io.File;
import java.io.FileNotFoundException;

import bft_log.aontrs.AontRS;

public class AontrsTest {
	public static void main(String args[]) throws FileNotFoundException{
		File f = new File("/Users/BortolameottiR/workspace/bft_log/src/bft_log/Test1");
		AontRS test = new AontRS(f);
	}
}
