package com.benoitlamothe.evently.utils;

import com.benoitlamothe.evently.Main;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class CloudUtils {
    public static final String STORAGE_URI = "https://storage.googleapis.com/evvnt_assets/";

    public static Blob downloadToBucket(String url) {
        try {
            File tmpImg = new File("/tmp/", UUID.randomUUID().toString());
            HTTPUtils.downloadFromUrl(new URL(url), tmpImg);
            BlobId blobId = BlobId.of("evvnt_assets", UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(url));
            Blob blob = Main.defaultCloudStorage.get("evvnt_assets").create(blobId.getName(),
                    new FileInputStream(tmpImg),
                    Bucket.BlobWriteOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));
            tmpImg.delete();

            return blob;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
