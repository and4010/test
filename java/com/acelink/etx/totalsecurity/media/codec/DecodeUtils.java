package com.acelink.etx.totalsecurity.media.codec;

import android.media.MediaCodecInfo;
import android.util.Log;

/**
 * Created by Gregory on 2016/8/8.
 */
public class DecodeUtils {

	protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();
	static final int ADTS_SIZE = 7;
	private static final int[] FREQUENCIES = new int[] {
			96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050,
			16000, 12000, 11025, 8000, 7350
	};
	/**
	 * Find H264 frame head
	 *
	 * @param offset
	 * @param buffer
	 * @param len
	 * @return the offset of frame head, return 0 if can not find one
	 */
	public static int findHead(int offset, byte[] buffer, int len) {
		int i;
		for (i = offset; i < len; i++) {
			if (i + 3 >= buffer.length) {
				return 0;
			}

			if (checkHead(buffer, i)) {
				break;
			}
		}

		if (i == len) {
			return 0;
		}

		if (i == offset) {
			return 0;
		}

		//Log.i("find head", "" + i);

		return i;
	}

	/**
	 * Check if is H264 frame head
	 *
	 * @param buffer
	 * @param offset
	 * @return whether the src buffer is frame head
	 */
	public static boolean checkHead(byte[] buffer, int offset) {

		// 0x00 0x00 0x00 0x01, csd-0
		if (buffer[offset] == 0 && buffer[offset + 1] == 0
				&& buffer[offset + 2] == 0 && buffer[offset + 3] == 1) {
			Log.e("yoyo", "checkHead pass");
			return true;
		}

		// 0x00 0x00 0x01
		/*if (buffer[offset] == 0 && buffer[offset + 1] == 0
				&& buffer[offset + 2] == 1)
			return true;*/
		return false;
	}

	/**
	 * AAAAAAAA AAAABCCD EEFFFFGH HHIJKLMM MMMMMMMM MMMOOOOO OOOOOOPP (QQQQQQQQ QQQQQQQQ)
	 * 11111111 11110001 11011100 01000000 01100000 11111111 11111100  00000001 01000000
	 *	Letter	Length (bits)	Description
	 *		A	12				syncword 0xFFF, all bits must be 1
	 *		B	1				MPEG Version: 0 for MPEG-4, 1 for MPEG-2
	 *		C	2				Layer: always 0
	 *		D	1				protection absent, Warning, set to 1 if there is no CRC and 0 if there is CRC
	 *		E	2				profile, the MPEG-4 Audio Object Type minus 1
	 *		F	4				MPEG-4 Sampling Frequency Index (15 is forbidden)
	 *		G	1				private bit, guaranteed never to be used by MPEG, set to 0 when encoding, ignore when decoding
	 *		H	3				MPEG-4 Channel Configuration (in the case of 0, the channel configuration is sent via an inband PCE)
	 *		I	1				originality, set to 0 when encoding, ignore when decoding
	 *		J	1				home, set to 0 when encoding, ignore when decoding
	 *		K	1				copyrighted id bit, the next bit of a centrally registered copyright identifier, set to 0 when encoding, ignore when decoding
	 *		L	1				copyright id start, signals that this frame's copyright id bit is the first bit of the copyright id, set to 0 when encoding, ignore when decoding
	 *		M	13				frame length, this value must include 7 or 9 bytes of header length: FrameLength = (ProtectionAbsent == 1 ? 7 : 9) + size(AACFrame)
	 *		O	11				Buffer fullness
	 *		P	2				Number of AAC frames (RDBs) in ADTS frame minus 1, for maximum compatibility always use 1 AAC frame per ADTS frame
	 *		Q	16				CRC if protection absent is 0
	 *
	 *
	 * 	Sampling Frequency Index, 13 supported frequencies:
	 *	0: 96000 Hz
	 *	1: 88200 Hz
	 *	2: 64000 Hz
	 *	3: 48000 Hz
	 *	4: 44100 Hz
	 *	5: 32000 Hz
	 *	6: 24000 Hz
	 *	7: 22050 Hz
	 *	8: 16000 Hz
	 *	9: 12000 Hz
	 *	10: 11025 Hz
	 *	11: 8000 Hz
	 *	12: 7350 Hz
	 *	13: Reserved
	 *	14: Reserved
	 *	15: frequency is written explictly
	 *
	 * */
	public static void addADTStoPacket(byte[] packet, int packetLen,int frequencyIndex,int channelCount) {
		int profile =MediaCodecInfo.CodecProfileLevel.AACObjectLC;// MediaCodecInfo.CodecProfileLevel.AACObjectLTP;//test
		//int profile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
		//int freqIdx = 8; // 16.0 Khz
		//int chanCfg = 1;  // CPE

		// fill in ADTS data
		packet[0] = (byte) 0xFF;
		//packet[1] = (byte) 0xF1;
		packet[1] = (byte)0xF9;
		packet[2] = (byte) (((profile - 1) /* MPEG-4 minus 1 */ << 6) + (frequencyIndex << 2) + (channelCount >> 2));
		//packet[3] = (byte) (((chanCfg & 0x03) << 6) + (packetLen >> 11));
		packet[3] = (byte)(((channelCount&3)<<6) + (packetLen>>11));
		packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
		//packet[5] = (byte) (((packetLen & 0x07) << 5) + 0x1F);
		packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
		packet[6] = (byte) 0xFC;

		/*packet[0] = (byte)0xFF;
		packet[1] = (byte)0xF9;
		packet[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
		packet[3] = (byte)(((chanCfg&3)<<6) + (packetLen>>11));
		packet[4] = (byte)((packetLen&0x7FF) >> 3);
		packet[5] = (byte)(((packetLen&7)<<5) + 0x1F);
		packet[6] = (byte)0xFC;*/
	}

	/**
	 * AAAAAAAA AAAABCCD EEFFFFGH HHIJKLMM MMMMMMMM MMMOOOOO OOOOOOPP (QQQQQQQQ QQQQQQQQ)
	 * 11111111 11110001 11011100 01000000 01100000 11111111 11111100  00000001 01000000
	 * Letter	Length (bits)	Description
	 * A	12	syncword 0xFFF, all bits must be 1
	 * B	01	MPEG Version: 0 for MPEG-4, 1 for MPEG-2
	 * C	02	Layer: always 0
	 * D	01	protection absent, Warning, set to 1 if there is no CRC and 0 if there is CRC
	 * E	02	profile, the MPEG-4 Audio Object Type minus 1
	 * F	04	MPEG-4 Sampling Frequency Index (15 is forbidden)
	 * G	01	private bit, guaranteed never to be used by MPEG, set to 0 when encoding, ignore when
	 * decoding
	 * H	03	MPEG-4 Channel Configuration (in the case of 0, the channel configuration is sent via an
	 * inband PCE)
	 * I	01	originality, set to 0 when encoding, ignore when decoding
	 * J	01	home, set to 0 when encoding, ignore when decoding
	 * K	01	copyrighted id bit, the next bit of a centrally registered copyright identifier, set to 0
	 * when encoding, ignore when decoding
	 * L	01	copyright id start, signals that this frame's copyright id bit is the first bit of the
	 * copyright id, set to 0 when encoding, ignore when decoding
	 * M	13	frame length, this value must include 7 or 9 bytes of header length: FrameLength =
	 * (ProtectionAbsent == 1 ? 7 : 9) + size(AACFrame)
	 * O	11	Buffer fullness
	 * P	02	Number of AAC frames (RDBs) in ADTS frame minus 1, for maximum compatibility always use 1
	 * AAC frame per ADTS frame
	 * Q	16	CRC if protection absent is 0
	 */
	static byte[] getAdts(int profile, int frequencyIndex, int channelCount, int packetLength) {
		byte[] adts = new byte[ADTS_SIZE];
		adts[0] = (byte) 0xFF;
		adts[1] = (byte) 0xF9;
		adts[2] =
				(byte) (((profile - 1) /* MPEG-4 minus 1 */ << 6) + (frequencyIndex << 2) + (channelCount
						>> 2));
		adts[3] = (byte) (((channelCount & 0x03) << 6) + (packetLength >> 11));
		adts[4] = (byte) ((packetLength & 0x7FF) >> 3);
		adts[5] = (byte) (((packetLength & 7) << 5) + 0x1F);
		adts[6] = (byte) 0xFC;
		return adts;
	}

	/**
	 * Gets 1 byte value. (HEX)
	 *
	 * @param aByte
	 * @return
	 */
	public static String byte2HEX(byte aByte) {
		char[] chars = new char[2];
		chars[0] = hexArray[(aByte & 0xF0) >> 4];
		chars[1] = hexArray[(aByte & 0x0F)];
		return new String(chars);
	}
}
