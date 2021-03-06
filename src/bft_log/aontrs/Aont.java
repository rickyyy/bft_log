package bft_log.aontrs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import bft_log.Utils;

public class Aont {
	private String cryptoAlgorithm = "AES";
	private Utils ut;
	private int sizePadding;
	private int wordSize = 16;
	private String canary = "Iamacanaryword11";
	private int keySize = wordSize*8;
	public byte[] aontPackage;
	
	public Aont() {
		ut = new Utils();
	}
	
	//Constructor, when you receive an "already prepared" Aont Package and you just need to "decode" it.
	public Aont(File f) throws IOException{
		ut = new Utils();
		this.aontPackage = ut.getBytesFromFile(f);
	}
	
	//Store on disk (generates a file) of the AONT package
	public void getAontEncodedPackageAsFile(String filePath){
		getFileFromBytes(this.aontPackage, filePath);
	}
	
	//Encode a file into an AONT package
	public byte[] AontEncoding(File f) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		//Transform the file into a byte array and append the canary word.
		try {
			this.aontPackage = getBytesFromFile(f);
			this.aontPackage = appendCanaryToData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Generate an AES key.
		SecretKey encryptionKey = generateSecretKey();
		Cipher ci = Cipher.getInstance(cryptoAlgorithm);
		ci.init(Cipher.ENCRYPT_MODE, encryptionKey);
		
		//Verify if the padding was added correctly.
		if (this.aontPackage.length % this.wordSize != 0){
			Exception exception = new Exception("Something went wrong with padding. Not divisible for size of words");
		}
		
		//Compute the number of words with specific size (e.g., 16byte = wordSize) are needed
		int words = this.aontPackage.length/this.wordSize;
		
		//Encrypt the file+padding with AES
		byte[] dataEncrypted = new byte[this.aontPackage.length];
		for (int i=0; i<words; i++){
			byte[] codeWordPartial = new byte[wordSize];
			System.arraycopy(aontPackage, i*wordSize, codeWordPartial, 0, wordSize);
			byte[] encVal = ci.doFinal(String.valueOf(i+1).getBytes());
			byte[] xoredCodeWord = new byte[wordSize];	
			for (int j=0; j<wordSize;j++){
				xoredCodeWord[j] = (byte) (codeWordPartial[j] ^ encVal[j]);
			}
			System.arraycopy(xoredCodeWord, 0, dataEncrypted, i*wordSize, wordSize);
		}
		
		//Generate an hash value of the encrypted data. XOR the hash with the key previously generated.
		//Append everything to the encrypted data.
		byte[] hashEncData = ut.hashBytes(dataEncrypted);
		byte[] encodedKey = encryptionKey.getEncoded();
		byte[] xoredHashKey = new byte [hashEncData.length];
		//System.out.println("ENCODED KEY: " + Arrays.toString(encodedKey) + "\n");		
		for (int i=0; i<encodedKey.length; i++){
			xoredHashKey[i] = (byte) (hashEncData[i] ^ encodedKey[i]);
		}
		byte[] aontPackage = new byte[dataEncrypted.length+xoredHashKey.length];
		System.arraycopy(dataEncrypted, 0, aontPackage, 0, dataEncrypted.length);
		System.arraycopy(xoredHashKey, 0, aontPackage, dataEncrypted.length, xoredHashKey.length);
		
		return aontPackage;
	}
	
	//Decode a specific AONT Package and write the decoded file in the specified Path.
	public void AontDecoding(byte[] aontPackage, String pathFile) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		//Compute the length of encrypted data. (= Subtract the size of the hash (i.e., SHA-256) from the AONT package)
		int dataEncLength = aontPackage.length-(wordSize*2);	
		byte[] dataEncrypted = new byte[dataEncLength];
		byte[] xoredHashKey = new byte[wordSize*2];
		System.arraycopy(aontPackage, 0, dataEncrypted, 0, dataEncLength);
		System.arraycopy(aontPackage, dataEncLength, xoredHashKey, 0, wordSize*2);
		
		//Derive the key
		byte[] encodedKey = new byte[wordSize];
		byte[] hashEncData = ut.hashBytes(dataEncrypted);
		for (int i=0; i<encodedKey.length; i++){
			encodedKey[i] = (byte) (hashEncData[i] ^ xoredHashKey[i]);
		}
		//System.out.println("DECODED KEY: " + Arrays.toString(encodedKey) + "\n");
		
		//Instantiate the key
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, cryptoAlgorithm);
		Cipher ci = Cipher.getInstance(cryptoAlgorithm);
		ci.init(Cipher.ENCRYPT_MODE, key);
		
		//Decrypt the data
		byte[] originalData = new byte[dataEncrypted.length];
		int words = dataEncrypted.length/this.wordSize;
		for (int i=0; i<words; i++){
			byte[] codeWordPartial = new byte[wordSize];
			System.arraycopy(dataEncrypted, i*wordSize, codeWordPartial, 0, wordSize);
			byte[] decVal = ci.doFinal(String.valueOf(i+1).getBytes());
			byte[] xoredCodeWord = new byte[wordSize];	
			for (int j=0; j<wordSize;j++){
				xoredCodeWord[j] = (byte) (codeWordPartial[j] ^ decVal[j]);
			}
			System.arraycopy(xoredCodeWord, 0, originalData, i*wordSize, wordSize);
		}
		
		//Extract the canary word appendend in the encoding phase.
		byte[] extractedCanary = new byte[wordSize];
		System.arraycopy(originalData, originalData.length-wordSize, extractedCanary, 0, wordSize);
		
		//Store in a new array the original data (removing also the padding, if any).
		byte[] originalFile = new byte[originalData.length-wordSize];
		System.arraycopy(originalData, 0, originalFile, 0, originalData.length-wordSize);
		originalFile = removePaddingToData(originalFile);
		
		//Print the original file (mainly for debug)
		String file = new String(originalFile);
		System.out.println(file + "\n");
		
		//Check the integrity verifying if the extracted canary word matches with the original one.
		if (Arrays.equals(extractedCanary, this.canary.getBytes())){
			System.out.println("CANARY WORDS ARE THE SAME, INTEGRITY PRESERVED\n");
		} else {
			System.out.println("CANARY WORDS DO NOT MATCH!!!! ERROR!!!\n");
		}
		
		//Write the decoded file on disk.
		getFileFromBytes(originalFile, pathFile);
	}
	
	
	
	private SecretKey generateSecretKey() throws NoSuchAlgorithmException{
		KeyGenerator keyGen = KeyGenerator.getInstance(cryptoAlgorithm);
		keyGen.init(keySize);
		return keyGen.generateKey();
	}
	
	//Remove the padding that has been previously added.
	private byte[] removePaddingToData(byte[] end){
		if (this.sizePadding == 0){
			return end;
		} else {
			byte[] result = new byte [end.length-this.sizePadding];
			System.arraycopy(end, 0, result, 0, end.length-this.sizePadding);
			return result;
		}
	}
	
	//Add padding to an array based on the wordsize parameter.
	private byte[] addPaddingToData(byte[] start){
		int r = start.length % this.wordSize;
		if (r == 0){
			this.sizePadding = 0;
			return start;
		} else {
			byte[] pad = new byte[this.wordSize-r];
			byte[] result = new byte[start.length + pad.length];
			System.arraycopy(start, 0, result, 0, start.length);
			System.arraycopy(pad, 0, result, start.length, pad.length);
			this.sizePadding = this.wordSize-r;
			return result;
		}
	}
	
	private byte[] appendCanaryToData(){
		byte[] canaryBytes = this.canary.getBytes();
		byte[] res = new byte[this.aontPackage.length + canaryBytes.length];
		System.arraycopy(this.aontPackage, 0, res, 0, this.aontPackage.length);
		System.arraycopy(canaryBytes, 0, res, this.aontPackage.length, canaryBytes.length);
		return res;
	}
	
	public byte[] getBytesFromFile(File f) throws IOException{
		byte[] fileToBytes;
		FileInputStream fis = new FileInputStream(f);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		try{
			for (int readNum; (readNum = fis.read(buffer)) != -1;){
				bos.write(buffer, 0, readNum);
			}
		} catch (IOException ex){
			System.out.println("Error while transforming the file into a bytes array.");
		}
		fileToBytes = bos.toByteArray();
		bos.close();
		fis.close();
		fileToBytes = addPaddingToData(fileToBytes);
		return fileToBytes;
	}
	
	//Given an array of bytes, create on disk a file (specified by filePath)
	private void getFileFromBytes(byte[] bytesOfFile, String filePath){
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filePath);
			fos.write(bytesOfFile);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
