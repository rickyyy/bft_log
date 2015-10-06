package bft_log.aontrs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import bft_log.ComputationConfig;

public class ReedSolomonShardReconstructor {
	private ComputationConfig config;
	private int DATA_SHARDS = config.t;		//t	
    private int PARITY_SHARDS = config.n-config.t;	//n-t
    private int TOTAL_SHARDS = config.n;
    private int BYTES_IN_INT = 4;
    private File file;
    
    //Give the name (prefix) of a requested file. It reconstruct the original file from its shares.
    public ReedSolomonShardReconstructor(File f) throws IOException{
    	this.file = f;
		this.config = new ComputationConfig();
    	ShardReconstruction();
    }
    
    public void ShardReconstruction() throws IOException{
    	// Read in any of the shards that are present.
        // (There should be checking here to make sure the input
        // shards are the same size, but there isn't.)
        final byte [] [] shards = new byte [TOTAL_SHARDS] [];
        final boolean [] shardPresent = new boolean [TOTAL_SHARDS];
        int shardSize = 0;
        int shardCount = 0;
        for (int i = 0; i < TOTAL_SHARDS; i++) {
            File shardFile = new File(
                    this.file.getParentFile(),
                    this.file.getName() + ".share" + i);
            if (shardFile.exists()) {
                shardSize = (int) shardFile.length();
                shards[i] = new byte [shardSize];
                shardPresent[i] = true;
                shardCount += 1;
                InputStream in = new FileInputStream(shardFile);
                in.read(shards[i], 0, shardSize);
                in.close();
                System.out.println("Read " + shardFile);
            }
        }
        
        // We need at least DATA_SHARDS to be able to reconstruct the file.
        if (shardCount < DATA_SHARDS) {
            System.out.println("Not enough shards present");
            return;
        }
        
        // Make empty buffers for the missing shards.
        for (int i = 0; i < TOTAL_SHARDS; i++) {
            if (!shardPresent[i]) {
                shards[i] = new byte [shardSize];
            }
        }
        
        // Use Reed-Solomon to fill in the missing shards
        ReedSolomon reedSolomon = new ReedSolomon(DATA_SHARDS, PARITY_SHARDS);
        reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);

        // Combine the data shards into one buffer for convenience.
        // (This is not efficient, but it is convenient.)
        byte [] allBytes = new byte [shardSize * DATA_SHARDS];
        for (int i = 0; i < DATA_SHARDS; i++) {
            System.arraycopy(shards[i], 0, allBytes, shardSize * i, shardSize);
        }

        // Extract the file length
        int fileSize = ByteBuffer.wrap(allBytes).getInt();

        // Write the decoded file
        File decodedFile = new File(this.file.getParentFile(), this.file.getName() + ".decoded");
        OutputStream out = new FileOutputStream(decodedFile);
        out.write(allBytes, BYTES_IN_INT, fileSize);
        System.out.println("Wrote " + decodedFile);
    }
    
}
