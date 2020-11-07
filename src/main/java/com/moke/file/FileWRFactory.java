package com.moke.file;


/**
 *
 */
public class FileWRFactory {
    public FileWRFactory(){

    }
    FileHandler GetDefaultFileHandler(String filePath) {
	//	return new DefaulteFileHandler();
        assert(false);
        return null;
    }
    FileHandler getRandomAccessFileHandler() {
        return new RandomAccessFileHandler();
    }
    FileHandler getChannelFileHandler(){
        return new ChannelFileHander();
    }
}
