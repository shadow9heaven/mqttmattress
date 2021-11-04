/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

package MAVLink.mavlink_main;

import java.util.HashMap;
import java.util.Map;

/**
 * CRC-16/MCRF4XX calculation for MAVlink messages. The checksum must be
 * initialized, updated with which field of the message, and then finished with
 * the message id.
 *
 */
public class CRC {
    private static final Map<Integer, Integer> MAVLINK_MESSAGE_CRCS;
    private static final int CRC_INIT_VALUE = 0xffff;
    private int crcValue;

    static {
        MAVLINK_MESSAGE_CRCS = new HashMap<>();
        MAVLINK_MESSAGE_CRCS.put(1, 217);
        MAVLINK_MESSAGE_CRCS.put(2, 63);
        MAVLINK_MESSAGE_CRCS.put(3, 19);
        MAVLINK_MESSAGE_CRCS.put(20, 7);
        MAVLINK_MESSAGE_CRCS.put(21, 219);
        MAVLINK_MESSAGE_CRCS.put(30, 5);
        MAVLINK_MESSAGE_CRCS.put(31, 63);
        MAVLINK_MESSAGE_CRCS.put(32, 112);
        MAVLINK_MESSAGE_CRCS.put(33, 227);
        MAVLINK_MESSAGE_CRCS.put(34, 2);
        MAVLINK_MESSAGE_CRCS.put(51, 219);
        MAVLINK_MESSAGE_CRCS.put(52, 32);
        MAVLINK_MESSAGE_CRCS.put(53, 222);
        MAVLINK_MESSAGE_CRCS.put(54, 61);
        MAVLINK_MESSAGE_CRCS.put(55, 240);
        MAVLINK_MESSAGE_CRCS.put(56, 133);
        MAVLINK_MESSAGE_CRCS.put(57, 210);
        MAVLINK_MESSAGE_CRCS.put(58, 144);
        MAVLINK_MESSAGE_CRCS.put(59, 30);
        MAVLINK_MESSAGE_CRCS.put(60, 201);
        MAVLINK_MESSAGE_CRCS.put(61, 95);
        
    }

    /**
     * Accumulate the CRC by adding one char at a time.
     *
     * The checksum function adds the hash of one char at a time to the 16 bit
     * checksum (uint16_t).
     *
     * @param data new char to hash
     **/
    public void update_checksum(int data) {
        data = data & 0xff; //cast because we want an unsigned type
        int tmp = data ^ (crcValue & 0xff);
        tmp ^= (tmp << 4) & 0xff;
        crcValue = ((crcValue >> 8) & 0xff) ^ (tmp << 8) ^ (tmp << 3) ^ ((tmp >> 4) & 0xf);
    }

    /**
     * Finish the CRC calculation of a message, by running the CRC with the
     * Magic Byte.
     *
     * @param msgid The message id number
     * @return boolean True if the checksum was successfully finished. Otherwise false
     */
    public boolean finish_checksum(int msgid) {
        if (MAVLINK_MESSAGE_CRCS.containsKey(msgid)) {
            update_checksum(MAVLINK_MESSAGE_CRCS.get(msgid));
            return true;
        }
        return false;
    }

    /**
     * Initialize the buffer for the CRC16/MCRF4XX
     */
    public void start_checksum() {
        crcValue = CRC_INIT_VALUE;
    }

    public int getMSB() {
        return ((crcValue >> 8) & 0xff);
    }

    public int getLSB() {
        return (crcValue & 0xff);
    }

    public CRC() {
        start_checksum();
    }

}
        