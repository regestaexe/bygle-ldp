package org.bygle.utils.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;



public class FileInfoReader {
	private static int kDEFAULT_CHUNK_SIZE = 256;

	public static String extractText(InputStream input) throws IOException {
		String result = "";
		String resultSplitted = "";
		String words[] = null;
		LinkedHashSet<String> uniqueWords = null;
		try {
			ContentHandler handler = new BodyContentHandler(10 * 1024 * 1024);
			Metadata metadata = new Metadata();
			Parser parser = new AutoDetectParser();
			parser.parse(input, handler, metadata, new ParseContext());
			result = handler.toString();

			uniqueWords = new LinkedHashSet<String>();
			words = result.split("[\\W]+");
			for (int i = 0; i < words.length; i++) {
				uniqueWords.add(words[i].toLowerCase());
			}
			int i = 0;
			for (String unique : uniqueWords) {
				if (((1 + i) % 100) == 0) {
					resultSplitted += unique + ",\n";
				} else
					resultSplitted += unique + ", ";
				i++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null)
				input.close();
		}

		return stripWhiteSpace(resultSplitted);

	}
    
	public static String stripWhiteSpace(String in) {
		StringBuilder out = new StringBuilder();
		char current;

		if (in == null || ("".equals(in)))
			return "";
		for (int i = 0; i < in.length(); i++) {
			current = in.charAt(i);
			if ((current == 0x9) || (current == 0xA) || (current == 0xD) || ((current >= 0x20) && (current <= 0xD7FF)) || ((current >= 0xE000) && (current <= 0xFFFD)) || ((current >= 0x10000) && (current <= 0x10FFFF)))
				out.append(current);
		}
		return out.toString();
	}
	
	
	public static String extractMD5(InputStream input) throws IOException {
		String result = "";
		try {
			result = DigestUtils.md5Hex(input);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null)
				input.close();
		}
		return result;
	}

	public static TreeMap<String, String> extractMetaData(InputStream input) throws IOException {
		TreeMap<String, String> treeMap = new TreeMap<String, String>();
		try {
			ContentHandler handler = new DefaultHandler();
			Metadata metadata = new Metadata();
			Parser parser = new AutoDetectParser();
			parser.parse(input, handler, metadata, new ParseContext());
			for (int i = 0; i < metadata.names().length; i++) {
				String name = metadata.names()[i];
				treeMap.put(name, stripWhiteSpace(metadata.get(name)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null)
				input.close();
		}
		return treeMap;
	}

	
	public static String extractStringMetaData(InputStream input) throws IOException {
		String result="";
		try {
			ContentHandler handler = new DefaultHandler();
			Metadata metadata = new Metadata();
			Parser parser = new AutoDetectParser();
			parser.parse(input, handler, metadata, new ParseContext());
			for (int i = 0; i < metadata.names().length; i++) {
				String name = metadata.names()[i];
				result+=name.toUpperCase()+" : "+stripWhiteSpace(metadata.get(name))+"\n";
			}
		} catch (Exception e) {
		} finally {
			if (input != null)
				input.close();
		}
		return result;
	}
	
	public static String getMetaData(InputStream input, String metaData) throws IOException {
		String result = "";
		try {
			ContentHandler handler = new DefaultHandler();
			Metadata metadata = new Metadata();
			Parser parser = new AutoDetectParser();
			parser.parse(input, handler, metadata, new ParseContext());
			result = metadata.get(metaData);
		} catch (Exception e) {
		} finally {
			if (input != null)
				input.close();
		}
		return result;
	}

	public static byte[] loadBytesFromURL(URL url) throws Exception {
		byte[] b = null;

		URLConnection con = url.openConnection();

		int size = con.getContentLength();
		InputStream in = null;

		try {
			if ((in = con.getInputStream()) != null)
				b = (size != -1) ? loadBytesFromStreamForSize(in, size) : loadBytesFromStream(in);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException ioe) {
				}
		}

		return b;
	}

	private static byte[] loadBytesFromStream(InputStream in) throws IOException {
		return loadBytesFromStream(in, kDEFAULT_CHUNK_SIZE);
	}

	private static byte[] loadBytesFromStreamForSize(InputStream in, int size) throws IOException {
		int count, index = 0;
		byte[] b = new byte[size];
		while ((count = in.read(b, index, size)) > 0) {
			size -= count;
			index += count;
		}
		return b;
	}

	private static byte[] loadBytesFromStream(InputStream in, int chunkSize) throws IOException {
		if (chunkSize < 1)
			chunkSize = kDEFAULT_CHUNK_SIZE;

		int count;
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		byte[] b = new byte[chunkSize];
		try {
			while ((count = in.read(b, 0, chunkSize)) > 0)
				bo.write(b, 0, count);
			byte[] thebytes = bo.toByteArray();
			return thebytes;
		} finally {
			bo.close();
			bo = null;
		}
	}
	public static String getFileSizeString(long sizeInBytes){
		String result="";
		double bytes = sizeInBytes;
		double kilobytes = Math.round((bytes / 1024));
		double megabytes = Math.round((kilobytes / 1024));
		double gigabytes = Math.round((megabytes / 1024));
		double terabytes = Math.round((gigabytes / 1024));
		if(terabytes>=1)
			result = StringUtils.substringBeforeLast(Double.toString(terabytes), ".")+" TB";
		else if(gigabytes>=1)
			result = StringUtils.substringBeforeLast(Double.toString(gigabytes), ".")+" GB";
		else if(megabytes>=1)
			result = StringUtils.substringBeforeLast(Double.toString(megabytes), ".")+" MB";
		else if(kilobytes>=1)
			result = StringUtils.substringBeforeLast(Double.toString(kilobytes), ".")+" KB";
		else
			result = StringUtils.substringBeforeLast(Double.toString(bytes),".")+" byte";
		return result;
	}
	
	public static void main(String args[]) throws Exception {
		try {

//			byte[] bytes = FileInfoReader.loadBytesFromURL(new URL("http://www.repubblica.it"));
//			InputStream input = new ByteArrayInputStream(bytes);
//			System.out.println(FileInfoReader.extractMetaData(input).toString());
			
			InputStream input = new FileInputStream(new File("C:\\Users\\sandro.REGESTAEXE\\Desktop\\usa.JPG"));
			//System.out.println(FileInfoReader.extractMetaData(input).toString());
			System.out.println(FileInfoReader.extractStringMetaData(input));
			
			
			// String[] words = null;
			// String test1 = "";
			// LinkedHashSet<String> uniqueWords = null;
			// File f = new File("/home/diego/Scaricati/D13104.pdf");
			// InputStream input = new FileInputStream(f);
			// System.out.println(FileInfoReader.extractMetaData(input).toString());
			// input = new FileInputStream(f);
			// try {
			//
			// String text = FileInfoReader.extractText(input);
			// uniqueWords = new LinkedHashSet<String>();
			// words = text.split("[\\W]+");
			// System.err.println(words.length);
			// for (int i = 0; i < words.length; i++) {
			// if ((i % 100) == (i / 100))
			// uniqueWords.add(words[i] + ",\n");
			// else
			// uniqueWords.add(words[i] + ",");
			//
			// }
			// } catch (IOException e) {
			// System.out.println("intercettata");
			// }
			//
			// for (String test : uniqueWords) {
			//
			// test1 += test;
			//
			// }
			//
			// input = new FileInputStream(f);
			// System.out.println(FileInfoReader.extractMD5(input));
			// input = new FileInputStream(f);
			// System.out.println(FileInfoReader.getMetaData(input,
			// "Content-Type"));

			/*
			 * byte[] bytes = FileInfoReader.loadBytesFromURL(new URL(
			 * "http://www.salute.gov.it/imgs/C_17_pubblicazioni_605_allegato.pdf"
			 * )); InputStream input = new ByteArrayInputStream(bytes);
			 * System.out
			 * .println(FileInfoReader.extractMetaData(input).toString()); input
			 * = new ByteArrayInputStream(bytes);
			 * System.out.println(FileInfoReader.extractText(input)); input =
			 * new ByteArrayInputStream(bytes);
			 * System.out.println(FileInfoReader.extractMD5(input)); input = new
			 * ByteArrayInputStream(bytes);
			 * System.out.println(FileInfoReader.getMetaData
			 * (input,"Content-Type"));
			 */

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}