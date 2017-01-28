package com.benoitlamothe.evently.entity;

import com.benoitlamothe.evently.Main;
import com.benoitlamothe.evently.utils.HTTPUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

/**
 * Created by olivier on 2017-01-28.
 */
public class Asset {

    @SerializedName("attractionId")
    public int attactionId;

    @SerializedName("eventId")
    public int eventId;

    @SerializedName("type")
    public String type;

    @SerializedName("url")
    public String url;

    /*
        * CREATE TABLE `Assets` (
      `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
      `attraction_id` int(11) NOT NULL,
      `event_id` int(11) NOT NULL,
      `type` enum('Image','Video') NOT NULL DEFAULT 'Image',
      `url` varchar(254) NOT NULL DEFAULT '',
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
    */

    public PreparedStatement getSQLInsert(Connection connection) throws SQLException {
        PreparedStatement pst = connection.prepareStatement("INSERT INTO Assets(attraction_id, event_id, `type`, url   ) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        int i = 0;
        pst.setObject(++i, this.attactionId);
        pst.setObject(++i, this.eventId);
        pst.setObject(++i, this.type);
        pst.setObject(++i, this.url);

        return pst;
    }

}
