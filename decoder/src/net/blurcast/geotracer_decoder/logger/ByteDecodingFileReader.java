package net.blurcast.geotracer_decoder.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

public class ByteDecodingFileReader {
	private File file;
	private FileInputStream fis;
	
	public int bytes_read;
	private long fileLength;

	public ByteDecodingFileReader(File _file) {
		bytes_read = 0;
		file = _file;
		if(!file.isFile()) {
			System.err.println("\""+file.getName()+"\" is not a file.");
			System.exit(1);
		}
		else if(!file.canRead()) {
			System.err.println("i don't have permission to read file \""+file.getName()+"\".");
			System.exit(1);
		}

		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		fileLength = file.length();
	}

	public int read() {
		try {
			int byte_val = fis.read();
            bytes_read += 1;
            return byte_val;
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		return -1;
	}

	public void read(byte[] b) {
		try {
			if(fis.read(b) != -1) {
				bytes_read += b.length;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public byte read_byte() {
		try {
			byte b = (byte) fis.read();
            bytes_read += 1;
			return (byte) (b & 0xff);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public char read_char() {
		byte[] b = new byte[2];
		read(b);
		return (char) ((((char) (b[0] & 0xff)) << 8) | ((char) (b[1] & 0xff)));
	}

    public short read_short() {
        byte[] b = new byte[2];
        read(b);
        return (short) ((((short) (b[0] & 0xff)) << 8) | ((short) (b[1] & 0xff)));
    }

    public int read_int_2() {
        byte[] b = new byte[2];
        read(b);
        return (int) ((((int) (b[0] & 0xff)) << 8) | ((int) (b[1] & 0xff)));
    }

    public int read_int_3() {
        byte[] b = new byte[3];
        read(b);
        return (((b[0] & 0xff) << 16) | (b[1] &  0xff) << 8) | (b[2] & 0xff);
    }

    public int read_int() {
		byte[] b = new byte[4];
		read(b);
		return (int) (((int) (b[0] & 0xff) << 24) | ((int) (b[1] & 0xff) << 16) | ((int) (b[2] &  0xff) << 8) | ((int) (b[3] & 0xff)));
	}

	public long read_long() {
		byte[] b = new byte[8];
		read(b);
		return ((long) (b[0] & 0xff) << 56) | ((long) (b[1] & 0xff) << 48) | ((long) (b[2] & 0xff) << 40) | ((long) (b[3] & 0xff) << 32)
				| ((long) (b[4] & 0xff) << 24) | ((long) (b[5] & 0xff) << 16) | ((long) (b[6] & 0xff) << 8) | ((long) (b[7] & 0xff));
	}
	
	public float read_float() {
		return Float.intBitsToFloat(read_int());
	}

	public double read_double() {
		return Double.longBitsToDouble(read_long());
	}


    private static final Charset CHARSET_ISO_8859_1 = Charset.forName("ISO-8859-1");

    public String read_string() {
        int size = read_byte();
        byte[] b = new byte[size];
        read(b);
        return new String(b, CHARSET_ISO_8859_1);
    }

	public long size() {
		return file.length();
	}
	
	public boolean hasBytes() {
		return (bytes_read < fileLength);
	}
	
	public void close() {
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

    public String offset() {
        return "@"+Integer.toHexString(bytes_read);
    }

    public void skipTo(long position) {
        long skip = position - bytes_read;
        System.out.println("Skipping "+skip+" bytes");
        try {
            fis.skip(skip);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void mark() {
        try {
            fis.mark(1024*1024);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        try {
            fis.reset();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
