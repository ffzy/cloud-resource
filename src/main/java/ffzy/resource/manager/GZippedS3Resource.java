package ffzy.resource.manager;

import ffzy.resource.manager.exception.ResourceNotFoundException;
import ffzy.resource.manager.util.GZipUtil;
import ffzy.resource.manager.util.Md5Util;
import ffzy.resource.manager.util.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by Zhang Yue on 5/12/2018
 */
public class GZippedS3Resource extends S3Resource implements UploadableResource {
    private static Logger logger = LoggerFactory.getLogger(ThreadUtil.getClassName());

    public GZippedS3Resource(String tag, String name, String description) {
        super(tag, name, description);
    }

    /**
     * Gets the decompressed file in local.
     * @return The decompressed file object.
     */
    @Override
    public File getResourceFile() {
        String resFilePath = resourceFile.getPath();
        if(resFilePath.lastIndexOf(".gz") < 0) {
            String message = "File " + resFilePath + " is not a GZIP file.";
            logger.error(message);
            throw new RuntimeException(message);
        }

        File decompressedFile = new File(resFilePath.substring(0, resFilePath.lastIndexOf(".gz")));

        if(decompressedFile.exists()) {
            return decompressedFile;
        }

        if(resourceFile.exists()) {
            GZipUtil.decompress(resourceFile, decompressedFile);
            return decompressedFile;
        }

        if(isAvailableInResourceBucket()) {
            downloadFromResourceBucket();
        } else if(isAvailableInResourceStore()) {
            downloadFromResourceStore();
            uploadToResourceBucket();
        } else {
            throw new ResourceNotFoundException(this);
        }

        GZipUtil.decompress(resourceFile, decompressedFile);
        Md5Util.getMd5(decompressedFile);
        return decompressedFile;
    }
}
