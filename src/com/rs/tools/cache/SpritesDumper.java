/**
 * 
 */
package com.rs.tools.cache;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.alex.store.Index;
import com.alex.store.Store;
import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.IndexedColorImageFile;
import com.rs.cache.loaders.LoaderImageArchive;

/**
 * @author dragonkk(Alex)
 * Sep 5, 2017
 */
public class SpritesDumper {

	public static void main2(String[] args) throws IOException {
		Cache.init();
		Store cache = Cache.STORE;
		Index sprites = cache.getIndexes()[32];
		for (int archiveId : sprites.getTable().getValidArchiveIds()) {
			for (int fileId : sprites.getTable().getArchives()[archiveId].getValidFileIds()) {
				LoaderImageArchive file = new LoaderImageArchive(cache, 32, archiveId, fileId);
				if (file.getImage() == null)
					continue;
				System.out.println(file.getImage().getWidth(null));
				if (file.getImage().getWidth(null) <= 0)
					continue;
		//		for (int count = 0; count < file.getImage().length; count++) {
					String name = "sprites32/" + archiveId + "_" + fileId + "_" + 0;
					BufferedImage image = toBufferedImage(file.getImage());
					if (image == null) {
						System.out.println("NULL: " + name);
						continue;
					}
					ImageIO.write(image, "png", new File(name + ".png"));
					System.out.println(name);
			//	}
			}
		}
	}
	
	public static BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	
	public static void main(String[] args) throws IOException {
		Cache.init();
		//Cache.init();
	//	Cache.STORE = new Store("C:\\Users\\Admin\\Desktop\\Onyx\\server\\onyx-server\\data\\cache\\");
		Store cache = Cache.STORE;
		Index sprites = cache.getIndexes()[8];
		for (int archiveId : sprites.getTable().getValidArchiveIds()) {
		/*	if (archiveId < 20000)
				continue;*/
			for (int fileId : sprites.getTable().getArchives()[archiveId].getValidFileIds()) {
	
				IndexedColorImageFile file = new IndexedColorImageFile(cache, 8, archiveId, fileId);
				if (file.getImages() == null)
					continue;
				for (int count = 0; count < file.getImages().length; count++) {
					String name = "extra/sprites/" + archiveId + "_" + fileId + "_" + count;
					BufferedImage image = file.getImages()[count];
					if (image == null) {
						System.out.println("NULL: " + name);
						continue;
					}
					ImageIO.write(image, "png", new File(name + ".png"));
					System.out.println(name);
				}
			}
		}
	}
}
