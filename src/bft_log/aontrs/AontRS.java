package bft_log.aontrs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class AontRS {
	private String cryptoAlgorithm = "AES";
	private Utils ut;
	private File file;
	private int sizePadding;
	private byte[] filetoBytes;
	private byte[] filePadCanary;
	private int wordSize = 16;
	private String canary = "Iamacanaryword11";
	private byte[] canaryBytes = canary.getBytes();
	private SecretKey k;
	private int keySize = wordSize*8;
	private byte[] aontPackage;
	private byte[] decodedPackage;
	
	public AontRS(File f) throws FileNotFoundException{
		this.file = f;
		ut = new Utils();
		getBytesFromFile();
		checkCanary();
		this.filePadCanary = appendCanaryToData();
		System.out.println(Arrays.toString(this.filetoBytes));
		try {
			this.aontPackage = AontEncoding();
			this.decodedPackage = AontDecoding(aontPackage);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] AontEncoding() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		generateSecretKey();
		Cipher ci = Cipher.getInstance(cryptoAlgorithm);
		ci.init(Cipher.ENCRYPT_MODE, this.k);
		byte[] dataEncrypted = new byte[this.filePadCanary.length];
		if (this.filePadCanary.length % this.wordSize != 0){
			Exception exception = new Exception("Something went wrong with padding. Not divisible for size of words");
		}
		int words = this.filePadCanary.length/this.wordSize;
		
		for (int i=0; i<words; i++){
			byte[] codeWordPartial = new byte[wordSize];
			System.arraycopy(filePadCanary, i*wordSize, codeWordPartial, 0, wordSize);
			byte[] encVal = ci.doFinal(String.valueOf(i+1).getBytes());
			System.out.println(Arrays.toString(encVal) + "\n");
			byte[] xoredCodeWord = new byte[wordSize];	
			for (int j=0; j<wordSize;j++){
				xoredCodeWord[j] = (byte) (codeWordPartial[j] ^ encVal[j]);
			}
			System.arraycopy(xoredCodeWord, 0, dataEncrypted, i*wordSize, wordSize);
		}
		//System.out.println("DATA ENCRYPTED: " + Arrays.toString(dataEncrypted) + "\n");
		
		byte[] hashEncData = ut.hashBytes(dataEncrypted);
		byte[] encodedKey = k.getEncoded();
		byte[] xoredHashKey = new byte [hashEncData.length];
		
		System.out.println("ENCODED KEY: " + Arrays.toString(encodedKey) + "\n");
		
		for (int i=0; i<encodedKey.length; i++){
			xoredHashKey[i] = (byte) (hashEncData[i] ^ encodedKey[i]);
		}
		
		byte[] aontPackage = new byte[dataEncrypted.length+xoredHashKey.length];
		System.arraycopy(dataEncrypted, 0, aontPackage, 0, dataEncrypted.length);
		System.arraycopy(xoredHashKey, 0, aontPackage, dataEncrypted.length, xoredHashKey.length);
		
		return aontPackage;
	}
	
	public byte[] AontDecoding(byte[] aontPackage) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		int dataEncLength = aontPackage.length-(wordSize*2);
		byte[] dataEncrypted = new byte[dataEncLength];
		byte[] xoredHashKey = new byte[wordSize*2];
		System.arraycopy(aontPackage, 0, dataEncrypted, 0, dataEncLength);
		System.arraycopy(aontPackage, dataEncLength, xoredHashKey, 0, wordSize*2);
		//System.out.println("DATA ENCRYPTED: " + Arrays.toString(dataEncrypted) + "\n");
		
		byte[] encodedKey = new byte[wordSize];
		byte[] hashEncData = ut.hashBytes(dataEncrypted);
		for (int i=0; i<encodedKey.length; i++){
			encodedKey[i] = (byte) (hashEncData[i] ^ xoredHashKey[i]);
		}

		System.out.println("ENCODED KEY: " + Arrays.toString(encodedKey) + "\n");
		
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, cryptoAlgorithm);
		Cipher ci = Cipher.getInstance(cryptoAlgorithm);
		ci.init(Cipher.ENCRYPT_MODE, key);
		
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
		
		byte[] extractedCanary = new byte[wordSize];
		System.arraycopy(originalData, originalData.length-wordSize, extractedCanary, 0, wordSize);
		
		byte[] originalFile = new byte[originalData.length-wordSize];
		System.arraycopy(originalData, 0, originalFile, 0, originalData.length-wordSize);
		originalFile = removePaddingToData(originalFile);
		String file = new String(originalFile);
		System.out.println(file);
		if (Arrays.equals(extractedCanary, this.canaryBytes)){
			System.out.println("CANARY WORDS ARE THE SAME, INTEGRITY PRESERVED");
		} else {
			System.out.println("CANARY WORDS DO NOT MATCH!!!! ERROR!!!");
		}
		
		return originalFile;
	}
	
	
	
	private void generateSecretKey() throws NoSuchAlgorithmException{
		KeyGenerator keyGen = KeyGenerator.getInstance(cryptoAlgorithm);
		keyGen.init(keySize);
		this.k = keyGen.generateKey();
	}
	
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
		byte[] res = new byte[this.filetoBytes.length + this.canaryBytes.length];
		System.arraycopy(this.filetoBytes, 0, res, 0, filetoBytes.length);
		System.arraycopy(this.canaryBytes, 0, res, this.filetoBytes.length, this.canaryBytes.length);
		return res;
	}
	
	private void checkCanary(){
		if (this.wordSize != this.canaryBytes.length){
			System.out.println("Canary has different size than word length");
		}
	}
	
	private void getBytesFromFile() throws FileNotFoundException{
		FileInputStream fis = new FileInputStream(this.file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		try{
			for (int readNum; (readNum = fis.read(buffer)) != -1;){
				bos.write(buffer, 0, readNum);
			}
		} catch (IOException ex){
			System.out.println("Error while transforming the file into a bytes array.");
		}
		this.filetoBytes = bos.toByteArray();
		//System.out.println("Before Padding: " + Arrays.toString(this.filetoBytes));
		//String s = new String(this.filetoBytes);
		this.filetoBytes = addPaddingToData(filetoBytes);
		//System.out.println("After Padding: " + Arrays.toString(this.filetoBytes));
		//String s1 = new String(this.filetoBytes);
		//System.out.println(s1);
	}
}
