/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE MQTT_SET_IP_PASSWORD PACKING
package MAVLink.bluetooth;
import MAVLink.MAVLinkPacket;
import MAVLink.Messages.MAVLinkMessage;
import MAVLink.Messages.MAVLinkPayload;
        
/**
 *  setting wifi ssid and password 
 */
public class msg_mqtt_set_ip_password extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_MQTT_SET_IP_PASSWORD = 33;
    public static final int MAVLINK_MSG_LENGTH = 70;
    private static final long serialVersionUID = MAVLINK_MSG_ID_MQTT_SET_IP_PASSWORD;

      
    /**
     *  setting mqtt broker 
     */
    public short ip[] = new short[30];
      
    /**
     * setting mqtt user
     */
    public short user[] = new short[20];
      
    /**
     * setting mqtt password
     */
    public short password[] = new short[20];
    

    /**
     * Generates the payload for a mavlink message for a message of this type
     * @return
     */
    @Override
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH,isMavlink2);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_MQTT_SET_IP_PASSWORD;
        
        
        for (int i = 0; i < ip.length; i++) {
            packet.payload.putUnsignedByte(ip[i]);
        }
                    
        
        for (int i = 0; i < user.length; i++) {
            packet.payload.putUnsignedByte(user[i]);
        }
                    
        
        for (int i = 0; i < password.length; i++) {
            packet.payload.putUnsignedByte(password[i]);
        }
                    
        
        if (isMavlink2) {
            
        }
        return packet;
    }

    /**
     * Decode a mqtt_set_ip_password message into this class fields
     *
     * @param payload The message to decode
     */
    @Override
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        
         
        for (int i = 0; i < this.ip.length; i++) {
            this.ip[i] = payload.getUnsignedByte();
        }
                
         
        for (int i = 0; i < this.user.length; i++) {
            this.user[i] = payload.getUnsignedByte();
        }
                
         
        for (int i = 0; i < this.password.length; i++) {
            this.password[i] = payload.getUnsignedByte();
        }
                
        
        if (isMavlink2) {
            
        }
    }

    /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_mqtt_set_ip_password() {
        this.msgid = MAVLINK_MSG_ID_MQTT_SET_IP_PASSWORD;
    }
    
    /**
     * Constructor for a new message, initializes msgid and all payload variables
     */
    public msg_mqtt_set_ip_password( short[] ip, short[] user, short[] password) {
        this.msgid = MAVLINK_MSG_ID_MQTT_SET_IP_PASSWORD;

        this.ip = ip;
        this.user = user;
        this.password = password;
        
    }
    
    /**
     * Constructor for a new message, initializes everything
     */
    public msg_mqtt_set_ip_password( short[] ip, short[] user, short[] password, int sysid, int compid, boolean isMavlink2) {
        this.msgid = MAVLINK_MSG_ID_MQTT_SET_IP_PASSWORD;
        this.sysid = sysid;
        this.compid = compid;
        this.isMavlink2 = isMavlink2;

        this.ip = ip;
        this.user = user;
        this.password = password;
        
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     *
     */
    public msg_mqtt_set_ip_password(MAVLinkPacket mavLinkPacket) {
        this.msgid = MAVLINK_MSG_ID_MQTT_SET_IP_PASSWORD;
        
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.isMavlink2 = mavLinkPacket.isMavlink2;
        unpack(mavLinkPacket.payload);
    }

          
    /**
     * Returns a string with the MSG name and data
     */
    @Override
    public String toString() {
        return "MAVLINK_MSG_ID_MQTT_SET_IP_PASSWORD - sysid:"+sysid+" compid:"+compid+" ip:"+ip+" user:"+user+" password:"+password+"";
    }
    
    /**
     * Returns a human-readable string of the name of the message
     */
    @Override
    public String name() {
        return "MAVLINK_MSG_ID_MQTT_SET_IP_PASSWORD";
    }
}
        