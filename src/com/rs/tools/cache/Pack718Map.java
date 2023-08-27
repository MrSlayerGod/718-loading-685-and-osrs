package com.rs.tools.cache;

import com.alex.store.Store;
import com.alex.utils.Constants;
import com.rs.cache.Cache;

import java.io.IOException;

public class Pack718Map {

    private static boolean packMap(int fromMapID, int toMapID, boolean check) {
        int fromMapX = (fromMapID >> 8) ;
        int fromMapY = (fromMapID & 0xff);
        int toMapX = (toMapID >> 8) ;
        int toMapY = (toMapID & 0xff);


        int mapArchiveID = osrsData.getIndexes()[5].getArchiveId("m"+fromMapX+"_"+fromMapY);
        int landscapeArchiveID = osrsData.getIndexes()[5].getArchiveId("l"+fromMapX+"_"+fromMapY);
        if (mapArchiveID == -1 || landscapeArchiveID == -1) {
            //	System.out.println("Failed to pack "+fromMapID);
        }
        int umapArchiveID = osrsData.getIndexes()[5].getArchiveId("um"+fromMapX+"_"+fromMapY);
        int ulandscapeArchiveID = osrsData.getIndexes()[5].getArchiveId("ul"+fromMapX+"_"+fromMapY);

        int toMapArchiveID = Cache.STORE.getIndexes()[5].getArchiveId("m"+toMapX+"_"+toMapY);
        int toLandscapeArchiveID = Cache.STORE.getIndexes()[5].getArchiveId("l"+toMapX+"_"+toMapY);
        if ((toMapArchiveID != -1 || toLandscapeArchiveID != -1) && check) {
            //	System.out.println("Already exists.");
            return false;
        }
        int utoMapArchiveID = Cache.STORE.getIndexes()[5].getArchiveId("um"+toMapX+"_"+toMapY);
        int utoLandscapeArchiveID = Cache.STORE.getIndexes()[5].getArchiveId("ul"+toMapX+"_"+toMapY);



        byte[] mapSettingsData = osrsData.getIndexes()[5].getFile(mapArchiveID);
        byte[] objectsData = osrsData.getIndexes()[5].getFile(landscapeArchiveID, 0, null);

        byte[] umapSettingsData = osrsData.getIndexes()[5].getFile(umapArchiveID);
        byte[] uobjectsData = osrsData.getIndexes()[5].getFile(ulandscapeArchiveID, 0, null);

        if (mapSettingsData == null || objectsData == null) {
            //	System.out.println("OSRS map "+fromMapID+" data is null.");
            return false;
        }
        if (toMapArchiveID == -1)
            toMapArchiveID = Cache.STORE.getIndexes()[5].getLastArchiveId()+1;
        if (toLandscapeArchiveID == -1)
            toLandscapeArchiveID = Cache.STORE.getIndexes()[5].getLastArchiveId()+2;
        if (utoMapArchiveID == -1)
            utoMapArchiveID = Cache.STORE.getIndexes()[5].getLastArchiveId()+3;
        if (utoLandscapeArchiveID == -1)
            utoLandscapeArchiveID = Cache.STORE.getIndexes()[5].getLastArchiveId()+4;


        Cache.STORE.getIndexes()[5].putFile(toMapArchiveID, 0, Constants.GZIP_COMPRESSION, mapSettingsData, null, false, false, ("m"+toMapX+"_"+toMapY).hashCode(), -1);
        Cache.STORE.getIndexes()[5].putFile(toLandscapeArchiveID, 0, Constants.GZIP_COMPRESSION, objectsData, null, false, false, ("l"+toMapX+"_"+toMapY).hashCode(), -1);

        if (umapSettingsData != null)
            Cache.STORE.getIndexes()[5].putFile(utoMapArchiveID, 0, Constants.GZIP_COMPRESSION, umapSettingsData, null, false, false, ("um"+toMapX+"_"+toMapY).hashCode(), -1);
        if (uobjectsData != null)
            Cache.STORE.getIndexes()[5].putFile(utoLandscapeArchiveID, 0, Constants.GZIP_COMPRESSION, uobjectsData, null, false, false, ("ul"+toMapX+"_"+toMapY).hashCode(), -1);
        if (umapSettingsData != null)
            System.out.println("pack ug");
        //System.out.println("Packed map "+fromMapID);
        System.out.print(", "+toMapID);
        return true;
    }

    public static void main(String[] args) throws IOException {
        Cache.init();
        osrsData = new Store(
                "C:\\Users\\Alex\\Downloads\\cache (1)\\");
        boolean rewrite = false;
        rewrite |= packMap(12342, 12342, false);
        rewrite |= packMap(12341, 12341, false);
        if (rewrite) {
            Cache.STORE.getIndexes()[5].rewriteTable();
            System.out.println("Done");
        }
    }


    static Store osrsData;


}
