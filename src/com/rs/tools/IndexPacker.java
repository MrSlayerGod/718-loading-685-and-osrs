package com.rs.tools;

import java.io.IOException;

import com.alex.store.Store;
import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.utils.Logger;


/**
 *
 * ataraxia-server
 * paolo 05/09/2019
 * #Shnek6969
 */
public class IndexPacker {

    public static void main(String[] args) throws IOException {
        Store from = new Store("D:\\servers\\Decay-718-Github\\Decay-Server-Build\\data\\cache\\");
        Store toPack = new Store(Settings.CACHE_PATH);
        int[] indexes = {12};
        for(int i :  indexes){
          boolean result =  toPack.getIndexes()[i].packIndex(from);
            System.out.println("Packing index "+i+", pack result ->"+result);
            }
        }
    }
