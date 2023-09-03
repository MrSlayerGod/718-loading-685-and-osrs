/**
 *
 */
package com.rs.tools.cache;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;

import javax.imageio.ImageIO;

import com.alex.store.Store;
import com.alex.utils.Constants;
import com.rs.cache.Cache;
import com.rs.cache.loaders.IndexedColorImageFile;

/**
 * @author dragonkk(Alex)
 * Sep 16, 2017
 */
public class Sprites {

	//1455- ranks

	public static void main66(String[] args) throws IOException {
		Cache.init();
		boolean sucess = true;
		/*int i = 8698; //task sprite
		IndexedColorImageFile sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/taskbutton.png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);
		*/
		Store from = new Store("data/cacheBackup/");

		Cache.init();
		IndexedColorImageFile sprite = new IndexedColorImageFile(from, 1455, 0);
		//sprite.replaceImage(ImageIO.read(new File("extra/sprites/ranks/1455_0_1.png")), 1);
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(1455, 0, sprite.encodeFile());
		Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].rewriteTable();
		System.out.println(sucess);

	}

	public static void main2(String[] args) throws IOException {
		Cache.init();
		boolean sucess = true;

		int i = 19008; //remmeber me box
		IndexedColorImageFile sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19010; //remmeber me box flag
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19028; //remmeber me text
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19011; //forums
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19012; //forums hover
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19013; //discord
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19014; //discord hover
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19019; //vote
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19020; //vote hover
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19017; //donate
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19018; //donate hover
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19015; //login button
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19016; //login button hover
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);


		i = 19021; //username
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19022; //username hover
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19029; //login box
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		for (i = 19000; i<= 19007; i++) { //bg
			sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
			sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);
		}

		i = 19026; //login box
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		i = 19027; //login box
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);


		i = 19037; //login box
		sprite = new IndexedColorImageFile(ImageIO.read(new File("extra/sprites/login/"+i+".png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);


		System.out.println("Packed login:"+sucess);
		Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].rewriteTable();

	}

	//498, 3778 logo
	//3769 bg load
	//4129 to 4136 originally
	public static void main2232323(String[] args) throws IOException {
		Cache.init();
		//Cache.STORE = new Store("C:/Users/alex_/git/aurost-server/Configuration/Game/Cache/");
		boolean sucess = true;

		for (int i = 22612; i<= 22624; i++) { //bg
			IndexedColorImageFile sprite = new IndexedColorImageFile(ImageIO.read(new File("C:\\Users\\Administrator\\Desktop\\textures\\14006-0\\"+i+".png")));
			System.out.println("pACK "+i);
			sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		}
		/*

		for (int i = 3769; i<= 3772; i++) { //bg
			IndexedColorImageFile sprite = new IndexedColorImageFile(ImageIO.read(new File("sprites/"+i+"_2.png")));

			sucess |= Cache.STORE.getIndexes()[34].putFile(i, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);

		}
		System.out.println("Packed bg:"+sucess);
		Cache.STORE.getIndexes()[34].rewriteTable();
		IndexedColorImageFile sprite; /*= new IndexedColorImageFile(ImageIO.read(new File("sprites/498.png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(498, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);
		*/
		/*IndexedColorImageFile sprite = new IndexedColorImageFile(ImageIO.read(new File("sprites/pixel.png")));
		sucess |= Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(2498, 0, Constants.GZIP_COMPRESSION, sprite.encodeFile(), null, false, false, -1, -1);
		*/System.out.println("Packed bg:"+sucess);
		Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].rewriteTable();
	}


	public static void mainb(String[] args) throws IOException {
		BufferedImage background = ImageIO.read(new File("extra/sprites/login/bg.png"));

		int sx = background.getWidth() / 4;
		int sy = background.getHeight() / 2;

		int id = 19000;
		for(int y = 0; y < 2; y++) {
			for(int x = 0; x < 4; x++) {
				System.out.println("id "+id);
				BufferedImage part = background.getSubimage(x * sx, y * sy, sx, sy);
				ImageIO.write(part, "png", new File("extra/sprites/login/"+(id++)+".png"));
			}
		}
	/*	BufferedImage background = ImageIO.read(new File("sprites/bg_2.png"));
		int sx = background.getWidth() / 2;
		int sy = background.getHeight() / 2;

		int id = 3769;
		for(int y = 0; y < 2; y++) {
			for(int x = 0; x < 2; x++) {
				System.out.println("id "+id);
				BufferedImage part = background.getSubimage(x * sx, y * sy, sx, sy);
				ImageIO.write(part, "png", new File("sprites/"+(id++)+"_2.png"));
			}
		}*/
	}




/*	public static void main(String[] args) throws IOException {
		Cache.init();
	System.out.println(Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].getArchive(19998).getDecompressedLength());
	}*/

	public static void main(String[] args) throws IOException {
		Cache.init();
		File[] tabs = new File("C:\\Users\\Administrator\\Desktop\\textures\\").listFiles();
		for (File tab : tabs) {
			int id = Integer.parseInt(tab.getName().replace(".png", ""));
			System.out.println(id);
			//IndexedColorImageFile sprite = new IndexedColorImageFile(Cache.STORE, id, 0);
		//	sprite.trim(1);
		///	sprite.addImage(ImageIO.read(tab));
			IndexedColorImageFile sprite = new IndexedColorImageFile(ImageIO.read(tab));
			Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(id, id >= 13900  ? 0 : 1, sprite.encodeFile());
		}
		Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].rewriteTable();
	}

	public static void main111(String[] args) throws IOException {
		Cache.init();
		IndexedColorImageFile sprite = new IndexedColorImageFile(Cache.STORE, 1455, 0);
		sprite.trim(21);
		sprite.addImage(ImageIO.read(new File("sprites/ranks/21.png")));
		 Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(1455, 0, sprite.encodeFile());
		 Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].rewriteTable();
		/*for (int i = 20; i <= 20; i++) {
			sprite.addImage(ImageIO.read(new File("sprites/ranks/"+i+".png")));
		}
		 Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(1455, 0, sprite.encodeFile());
		 Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].rewriteTable();*/
		/* Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].putFile(2173, 0, new IndexedColorImageFile(ImageIO.read(new File("sprites/2173.bmp"))).encodeFile());
		 Cache.STORE.getIndexes()[Constants.SPRITES_INDEX].rewriteTable();*/
	}
	//4139 - 4146


}
