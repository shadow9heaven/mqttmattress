/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */
        
package MAVLink;

import java.io.Serializable;
import MAVLink.Messages.MAVLinkPayload;
import MAVLink.Messages.MAVLinkMessage;
import MAVLink.common.msg_radio_status;
import MAVLink.mavlink_main.CRC;

import MAVLink.mavlink_main.*;

import MAVLink.bootloader.*;

import MAVLink.smartmattress.*;

import MAVLink.logger.*;

import MAVLink.bluetooth.*;

/**
 * Common interface for all MAVLink Messages
 * Packet Anatomy
 * This is the anatomy of one packet. It is inspired by the CAN and SAE AS-4 standards.
 *
 * MAVLink 1 Packet Format
 *
 * Byte Index  Content              Value       Explanation
 * 0            Packet start sign  v1.0: 0xFE   Indicates the start of a new packet.  (v0.9: 0x55; v1.0: 0xFE; v2.0 0xFD)
 * 1            Payload length      0 - 255     Indicates length of the following payload.
 * 2            Packet sequence     0 - 255     Each component counts up its send sequence. Allows to detect packet loss
 * 3            System ID           1 - 255     ID of the SENDING system. Allows to differentiate different MAVs on the same network.
 * 4            Component ID        0 - 255     ID of the SENDING component. Allows to differentiate different components of the same system, e.g. the IMU and the autopilot.
 * 5            Message ID          0 - 255     ID of the message - the id defines what the payload means and how it should be correctly decoded.
 * 6 to (n+6)   Payload             0 - 255     Data of the message, depends on the message id.
 * (n+7)to(n+8) Checksum (low byte, high byte)  CRC16/MCRF4XX hash, excluding packet start sign, so bytes 1..(n+6) Note: The checksum also includes MAVLINK_CRC_EXTRA (Number computed from message fields. Protects the packet from decoding a different version of the same packet but with different variables).
 *
 * The checksum is the CRC16/MCRF4XX. Please see the MAVLink source code for a documented C-implementation of it. LINK TO CHECKSUM
 * The minimum packet length is 8 bytes for acknowledgement packets without payload
 * The maximum packet length is 263 bytes for full payload
 *
 *
 * MAVLink 2 Packet Format
 *
 * Byte Index     Content             Value              Explanation
 * 0              Packet start sign  v2.0: 0xFD          Indicates the start of a new packet.  (v0.9: 0x55; v1.0: 0xFE; v2.0 0xFD)
 * 1              Payload length      0 - 255            Indicates length of the following payload.
 * 2              Incompatible Flags  0 - 255            Flags that must be understood
 * 3              Compatible Flags    0 - 255            Flags that can be ignored if not understood
 * 4              Packet sequence     0 - 255            Each component counts up its send sequence. Allows to detect packet loss
 * 5              System ID           1 - 255            ID of the SENDING system. Allows to differentiate different MAVs on the same network.
 * 6              Component ID        0 - 255            ID of the SENDING component. Allows to differentiate different components of the same system, e.g. the IMU and the autopilot.
 * 7 to 9         Message ID          0 - 16777216       ID of the message - the id defines what the payload means and how it should be correctly decoded.
 * 10             Target System ID    1 - 255            (OPTIONAL) ID of the TARGET system. Only used for point-to-point mode
 * 11             Target Component ID 0 - 255            (OPTIONAL) ID of the TARGET component. Only used for point-to-point mode
 * 12 to (n+12)   Payload             0 - 255            Data of the message, depends on the message id.
 * (n+13)to(n+14) Checksum (low byte, high byte)         CRC16/MCRF4XX hash, excluding packet start sign, so bytes 1..(n+6) Note: The checksum also includes MAVLINK_CRC_EXTRA (Number computed from message fields. Protects the packet from decoding a different version of the same packet but with different variables).
 * (n+15)to(n+27) Signature (typeid, timestamp, sha256)  (OPTIONAL) Signature which allows ensuring that the link is tamper-proof; 13 bytes containing typeid (1 byte), timestamp (6 bytes), and last 6 bytes of SHA256 hash
 *
 * The signature is a combination of a typeid, timestamp, and SHA256 hash.
 * OPTIONAL fields mean that, if they are not used, they do not exist in the MAVLink frame at all. Typically target sysid and target compid are not used, and signature is only used if signing is set up between both ends.
 * 
 * @see <a href="https://mavlink.io">mavlink.io</a> for more documentation on the MAVLink protocol
 */
public class MAVLinkPacket implements Serializable {
    private static final long serialVersionUID = 2095947771227815314L;

    public static final int MAVLINK_STX_MAVLINK1 = 0xFE; // 254
    public static final int MAVLINK_STX_MAVLINK2 = 0xFD; // 253
    public static final int MAVLINK1_HEADER_LEN = 6;
    public static final int MAVLINK2_HEADER_LEN = 10;
    public static final int MAVLINK1_NONPAYLOAD_LEN = MAVLINK1_HEADER_LEN + 2;
    public static final int MAVLINK2_NONPAYLOAD_LEN = MAVLINK2_HEADER_LEN + 2;

    static final boolean V = false;
    static void logv(String str) {
        if(V) System.out.println(String.format("MAVLinkPacket: %s", str));
    }

    /**
     * Payload length
     */
    public final int len;

    /**
     * Message sequence
     */
    public int seq;

    /**
     * ID of the SENDING system. Allows to differentiate different MAVs on the
     * same network.
     */
    public int sysid;

    /**
     * ID of the SENDING component. Allows to differentiate different components
     * of the same system, e.g. the IMU and the autopilot.
     */
    public int compid;

    /**
     * ID of the message - the id defines what the payload means and how it
     * should be correctly decoded.
     */
    public int msgid;

    /**
     * Data of the message, depends on the message id.
     */
    public MAVLinkPayload payload;

    /**
    * CRC-16/MCRF4XX hash, excluding packet start sign, so bytes 1..(n+HEADER-LENGTH)
    * Note: The checksum also includes MAVLINK_CRC_EXTRA (Number computed from
    * message fields. Protects the packet from decoding a different version of
    * the same packet but with different variables).
    */
    public CRC crc;

    // MAVLink 2.0 fields

    /**
     * Flag to indicate which MAVLink version this packet is
     */
    public boolean isMavlink2;

    /**
     * Flags that must be understood
     */
    public int incompatFlags;

    /**
     * Flags that can be ignored if not understood
     */
    public int compatFlags;

    public MAVLinkPacket(int payloadLength) {
        this(payloadLength, false);
    }

    public MAVLinkPacket(final int payloadLength, final boolean isMavlink2) {
        len = payloadLength;
        payload = new MAVLinkPayload();
        this.isMavlink2 = isMavlink2;
    }

    /**
     * Check if the size of the Payload is equal to the "len" byte
     */
    public boolean payloadIsFilled() {
        return payload.size() >= len;
    }

    /**
     * Update CRC for this packet.
     * @return boolean True if the CRC was successfully updated. Otherwise false
     */
    public boolean generateCRC(final int payloadSize) {
        if (crc == null) {
            crc = new CRC();
        } else {
            crc.start_checksum();
        }

        if (isMavlink2) {
            crc.update_checksum(payloadSize);
            crc.update_checksum(incompatFlags);
            crc.update_checksum(compatFlags);
            crc.update_checksum(seq);
            crc.update_checksum(sysid);
            crc.update_checksum(compid);
            crc.update_checksum(msgid);
            crc.update_checksum(msgid >>> 8);
            crc.update_checksum(msgid >>> 16);
        } else {
            crc.update_checksum(payloadSize);
            crc.update_checksum(seq);
            crc.update_checksum(sysid);
            crc.update_checksum(compid);
            crc.update_checksum(msgid);
        }

        payload.resetIndex();

        for (int i = 0; i < payloadSize; i++) {
            crc.update_checksum(payload.getByte());
        }
        return crc.finish_checksum(msgid);
    }

    /**
     * Return length of actual data after triming zeros at the end.
     * @param payload
     * @return minimum length of valid data
     */
    private int mavTrimPayload(final byte[] payload)
    {
        int length = payload.length;
        while (length > 1 && payload[length-1] == 0) {
            length--;
        }
        return length;
    }
    
    /**
     * Encode this packet for transmission.
     *
     * @return Array with bytes to be transmitted
     */
    public byte[] encodePacket() {
        final int bufLen;
        final int payloadSize;
        
        if (isMavlink2) {
            payloadSize = mavTrimPayload(payload.payload.array());
            bufLen = MAVLINK2_HEADER_LEN + payloadSize + 2;
        } else {
            payloadSize = payload.size();
            bufLen = MAVLINK1_HEADER_LEN + payloadSize + 2;

        }
        byte[] buffer = new byte[bufLen];
        
        int i = 0;
        if (isMavlink2) {
            buffer[i++] = (byte) MAVLINK_STX_MAVLINK2;
            buffer[i++] = (byte) payloadSize;
            buffer[i++] = (byte) incompatFlags;
            buffer[i++] = (byte) compatFlags;
            buffer[i++] = (byte) seq;
            buffer[i++] = (byte) sysid;
            buffer[i++] = (byte) compid;
            buffer[i++] = (byte) (msgid & 0XFF);
            buffer[i++] = (byte) ((msgid >>> 8) & 0XFF);
            buffer[i++] = (byte) ((msgid >>> 16) & 0XFF);
        } else {
            buffer[i++] = (byte) MAVLINK_STX_MAVLINK1;
            buffer[i++] = (byte) payloadSize;
            buffer[i++] = (byte) seq;
            buffer[i++] = (byte) sysid;
            buffer[i++] = (byte) compid;
            buffer[i++] = (byte) msgid;
        }

        for (int j = 0; j < payloadSize; ++j) {
            buffer[i++] = payload.payload.get(j);
        }

        generateCRC(payloadSize);
        buffer[i++] = (byte) (crc.getLSB());
        buffer[i++] = (byte) (crc.getMSB());

        logv(String.format("encode: isMavlink2=%s msgid=%d", isMavlink2, msgid));

        return buffer;
    }
        
    /**
     * Unpack the data in this packet and return a MAVLink message
     *
     * @return MAVLink message decoded from this packet
     */
    public MAVLinkMessage unpack() {
        switch (msgid) {
         
            case msg_bl_ack.MAVLINK_MSG_ID_BL_ACK:
                return  new msg_bl_ack(this);
             
            case msg_bl_command.MAVLINK_MSG_ID_BL_COMMAND:
                return  new msg_bl_command(this);
             
            case msg_bl_ota.MAVLINK_MSG_ID_BL_OTA:
                return  new msg_bl_ota(this);
             
            case msg_logger_code.MAVLINK_MSG_ID_LOGGER_CODE:
                return  new msg_logger_code(this);
             
            case msg_logger_msg.MAVLINK_MSG_ID_LOGGER_MSG:
                return  new msg_logger_msg(this);
             
            case msg_ble_ack.MAVLINK_MSG_ID_BLE_ACK:
                return  new msg_ble_ack(this);
             
            case msg_bluetooth_change_name.MAVLINK_MSG_ID_BLUETOOTH_CHANGE_NAME:
                return  new msg_bluetooth_change_name(this);
             
            case msg_wifi_set_ssid_password.MAVLINK_MSG_ID_WIFI_SET_SSID_PASSWORD:
                return  new msg_wifi_set_ssid_password(this);
             
            case msg_mqtt_set_ip_password.MAVLINK_MSG_ID_MQTT_SET_IP_PASSWORD:
                return  new msg_mqtt_set_ip_password(this);
             
            case msg_connect.MAVLINK_MSG_ID_CONNECT:
                return  new msg_connect(this);
             
            case msg_mattress_ack.MAVLINK_MSG_ID_MATTRESS_ACK:
                return  new msg_mattress_ack(this);
             
            case msg_adjust_hardness.MAVLINK_MSG_ID_ADJUST_HARDNESS:
                return  new msg_adjust_hardness(this);
             
            case msg_relieve_stress.MAVLINK_MSG_ID_RELIEVE_STRESS:
                return  new msg_relieve_stress(this);
             
            case msg_meditation.MAVLINK_MSG_ID_MEDITATION:
                return  new msg_meditation(this);
             
            case msg_connection.MAVLINK_MSG_ID_CONNECTION:
                return  new msg_connection(this);
             
            case msg_pressure.MAVLINK_MSG_ID_PRESSURE:
                return  new msg_pressure(this);
             
            case msg_pump_status.MAVLINK_MSG_ID_PUMP_STATUS:
                return  new msg_pump_status(this);
             
            case msg_step_status.MAVLINK_MSG_ID_STEP_STATUS:
                return  new msg_step_status(this);
             
            case msg_request_data.MAVLINK_MSG_ID_REQUEST_DATA:
                return  new msg_request_data(this);
             
            case msg_control_pump.MAVLINK_MSG_ID_CONTROL_PUMP:
                return  new msg_control_pump(this);
             
            case msg_control_step.MAVLINK_MSG_ID_CONTROL_STEP:
                return  new msg_control_step(this);

            case msg_radio_status.MAVLINK_MSG_ID_RADIO_STATUS:
                return  new msg_radio_status(this);

            default:
                return null;
        }
    }

}
