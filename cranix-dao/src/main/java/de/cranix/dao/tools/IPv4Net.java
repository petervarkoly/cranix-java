/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
/*
Copyright (c) 2010, Saddam Abu Ghaida, Nicolai TufarAll rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
* Neither the name of the Saddam Abu Ghaida or Nicolai Tufar nor the
names of its contributors may be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Saddam Abu Ghaida or Nicolai Tufar BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package de.cranix.dao.tools;

import java.util.ArrayList;
import java.util.List;

public class IPv4Net {
    int  baseIPnumeric;
    private int  netmaskNumeric;

    /**
     * Specify IP address and netmask like: new
     * IPv4Net("10.1.0.25","255.255.255.16")
     *
     *@param symbolicIP
     *@param netmask
     */
    public IPv4Net(String symbolicIP, String netmask) throws NumberFormatException {

        /* IP */
        baseIPnumeric = IPv4.convertSymbolicIPToNumeric(symbolicIP);

        /* Netmask */
        String[] st = netmask.split("\\.");

        if (st.length != 4)
            throw new NumberFormatException("Invalid netmask address: "

                    + netmask);

        int i = 24;
        setNetmaskNumeric(0);

        if (Integer.parseInt(st[0]) < 255) {

            throw new NumberFormatException(
                    "The first byte of netmask can not be less than 255");
        }
        for (int n = 0; n < st.length; n++) {

            int value = Integer.parseInt(st[n]);

            if (value != (value & 0xff)) {

                throw new NumberFormatException("Invalid netmask address: "  + netmask);
            }

            setNetmaskNumeric(getNetmaskNumeric() + (value << i));
            i -= 8;

        }
/*
* see if there are zeroes inside netmask, like: 1111111101111 This is
* illegal, throw exception if encountered. Netmask should always have
* only ones, then only zeroes, like: 11111111110000
*/
        boolean encounteredOne = false;
        int ourMaskBitPattern = 1;

        for (i = 0; i < 32; i++) {

            if ((getNetmaskNumeric() & ourMaskBitPattern) != 0) {

                encounteredOne = true; // the bit is 1
            } else { // the bit is 0
                if (encounteredOne == true)

                    throw new NumberFormatException("Invalid netmask: " + netmask + " (bit " + (i + 1) + ")");
            }

            ourMaskBitPattern = ourMaskBitPattern << 1;
        }
    }

/**
* Specify IP in CIDR format like: new IPv4Net("10.1.0.25/16");
*
*@param IPinCIDRFormat
*/
    public IPv4Net(String IPinCIDRFormat) throws NumberFormatException {

        String[] st = IPinCIDRFormat.split("\\/");
        if (st.length != 2)

            throw new NumberFormatException("Invalid CIDR format '"
                    + IPinCIDRFormat + "', should be: xx.xx.xx.xx/xx");

        String symbolicIP = st[0];
        String symbolicCIDR = st[1];

        Integer numericCIDR = new Integer(symbolicCIDR);
        if (numericCIDR > 32)

            throw new NumberFormatException("CIDR can not be greater than 32");

        /* IP */
        baseIPnumeric = IPv4.convertSymbolicIPToNumeric(symbolicIP);
        /* netmask from CIDR */
        if (numericCIDR < 8)
            throw new NumberFormatException("Netmask CIDR can not be less than 8");
        setNetmaskNumeric(0xffffffff);
        setNetmaskNumeric(getNetmaskNumeric() << (32 - numericCIDR));
        baseIPnumeric = baseIPnumeric & getNetmaskNumeric();

    }

    public int getNetmaskNumeric() {
	return netmaskNumeric;
}

public void setNetmaskNumeric(int netmaskNumeric) {
	this.netmaskNumeric = netmaskNumeric;
}

	/**
* Get the IP in symbolic form, i.e. xxx.xxx.xxx.xxx
*
*@return
*/
    public String getBase() {
        return IPv4.convertNumericIpToSymbolic(baseIPnumeric);
    }

/**
* Return the next network
*
*@return
*/
    public String getLast() {

        int numberOfBits;
        for (numberOfBits = 0; numberOfBits < 32; numberOfBits++) {

            if ((getNetmaskNumeric() << numberOfBits) == 0)
                break;
        }
        Integer numberOfIPs = 0;
        for (int n = 0; n < (32 - numberOfBits); n++) {

            numberOfIPs = numberOfIPs << 1;
            numberOfIPs = numberOfIPs | 0x01;

        }

        Integer baseIP = baseIPnumeric & getNetmaskNumeric();
        return IPv4.convertNumericIpToSymbolic(baseIP + numberOfIPs );
    }

/**
* Get the net mask in symbolic form, i.e. xxx.xxx.xxx.xxx
*
*@return
*/

    public String getNetmask() {
        StringBuffer sb = new StringBuffer(15);

        for (int shift = 24; shift > 0; shift -= 8) {

            // process 3 bytes, from high order byte down.
            sb.append(Integer.toString((getNetmaskNumeric() >>> shift) & 0xff));

            sb.append('.');
        }
        sb.append(Integer.toString(getNetmaskNumeric() & 0xff));

        return sb.toString();
    }

/**
* Get the IP and netmask in CIDR form, i.e. xxx.xxx.xxx.xxx/xx
*
*@return
*/

    public String getCIDR() {
        int i;
        for (i = 0; i < 32; i++) {

            if ((getNetmaskNumeric() << i) == 0)
                break;

        }
        return IPv4.convertNumericIpToSymbolic(baseIPnumeric & getNetmaskNumeric()) + "/" + i;
    }

/**
* Get an arry of all the IP addresses available for the IP and netmask/CIDR
* given at initialization
*
*@return
*/
    public List<String> getAvailableIPs(Integer numberofIPs) {

        ArrayList<String> result = new ArrayList<String>();
        int numberOfBits;

        for (numberOfBits = 0; numberOfBits < 32; numberOfBits++) {

            if ((getNetmaskNumeric() << numberOfBits) == 0)
                break;

        }
        Integer numberOfIPs = 0;
        for (int n = 0; n < (32 - numberOfBits); n++) {

            numberOfIPs = numberOfIPs << 1;
            numberOfIPs = numberOfIPs | 0x01;

        }
        numberOfIPs = numberOfIPs +1;
        Integer baseIP = baseIPnumeric & getNetmaskNumeric();
	    if( numberofIPs == 0 ) {
		numberofIPs = numberOfIPs;
	    }

        for (int i = 0; i < (numberOfIPs) && i < numberofIPs; i++) {

            Integer ourIP = baseIP + i;

            String ip = IPv4.convertNumericIpToSymbolic(ourIP);

            result.add(ip);
        }
        return result;
    }

/**
* Return the next network
*
*@return
*/
    public String getNext() {

        int numberOfBits;
        for (numberOfBits = 0; numberOfBits < 32; numberOfBits++) {

            if ((getNetmaskNumeric() << numberOfBits) == 0)
                break;
        }
        Integer numberOfIPs = 0;
        for (int n = 0; n < (32 - numberOfBits); n++) {

            numberOfIPs = numberOfIPs << 1;
            numberOfIPs = numberOfIPs | 0x01;

        }

        Integer baseIP = baseIPnumeric & getNetmaskNumeric();
        String nextIP = IPv4.convertNumericIpToSymbolic(baseIP + numberOfIPs + 1);
        return nextIP;
    }

/**
* Range of hosts
*
*@return
*/
    public String getHostAddressRange() {

        int numberOfBits;
        for (numberOfBits = 0; numberOfBits < 32; numberOfBits++) {

            if ((getNetmaskNumeric() << numberOfBits) == 0)
                break;
        }
        Integer numberOfIPs = 0;
        for (int n = 0; n < (32 - numberOfBits); n++) {

            numberOfIPs = numberOfIPs << 1;
            numberOfIPs = numberOfIPs | 0x01;

        }

        Integer baseIP = baseIPnumeric & getNetmaskNumeric();
        String firstIP = IPv4.convertNumericIpToSymbolic(baseIP + 1);
        String lastIP = IPv4.convertNumericIpToSymbolic(baseIP + numberOfIPs - 1);
        return firstIP + " - " + lastIP;
    }

/**
* Returns number of hosts available in given range
*
*@return number of hosts
*/
    public Long getNumberOfHosts() {
        int numberOfBits;

        for (numberOfBits = 0; numberOfBits < 32; numberOfBits++) {

            if ((getNetmaskNumeric() << numberOfBits) == 0)
                break;

        }

        Double x = Math.pow(2, (32 - numberOfBits));

        if (x == -1)
            x = 1D;

        return x.longValue();
    }

/**
* The XOR of the netmask
*
*@return wildcard mask in text form, i.e. 0.0.15.255
*/

    public String getWildcardMask() {
        Integer wildcardMask = getNetmaskNumeric() ^ 0xffffffff;

        StringBuffer sb = new StringBuffer(15);
        for (int shift = 24; shift > 0; shift -= 8) {

            // process 3 bytes, from high order byte down.
            sb.append(Integer.toString((wildcardMask >>> shift) & 0xff));

            sb.append('.');
        }
        sb.append(Integer.toString(wildcardMask & 0xff));

        return sb.toString();

    }

    public String getBroadcastAddress() {

        if (getNetmaskNumeric() == 0xffffffff)
            return "0.0.0.0";

        int numberOfBits;
        for (numberOfBits = 0; numberOfBits < 32; numberOfBits++) {

            if ((getNetmaskNumeric() << numberOfBits) == 0)
                break;

        }
        Integer numberOfIPs = 0;
        for (int n = 0; n < (32 - numberOfBits); n++) {

            numberOfIPs = numberOfIPs << 1;
            numberOfIPs = numberOfIPs | 0x01;
        }

        Integer baseIP = baseIPnumeric & getNetmaskNumeric();
        Integer ourIP = baseIP + numberOfIPs;

        String ip = IPv4.convertNumericIpToSymbolic(ourIP);

        return ip;
    }

    public String getNetmaskInBinary() {

        return IPv4.getBinary(getNetmaskNumeric());
    }

/**
* Checks if the given IP address contains in subnet
*
*@param IPaddress
*@return
*/
    public boolean contains(String IPaddress) {

        Integer checkingIP = 0;
        String[] st = IPaddress.split("\\.");

        if (st.length != 4)
            throw new NumberFormatException("Invalid IP address: " + IPaddress);

        int i = 24;
        for (int n = 0; n < st.length; n++) {

            int value = Integer.parseInt(st[n]);

            if (value != (value & 0xff)) {

                throw new NumberFormatException("Invalid IP address: "
                        + IPaddress);
            }

            checkingIP += value << i;
            i -= 8;
        }

        if ((baseIPnumeric & getNetmaskNumeric()) == (checkingIP & getNetmaskNumeric()))

            return true;
        else
            return false;
    }

    public boolean contains(IPv4Net child) {

        Integer subnetID = child.baseIPnumeric;

        Integer subnetMask = child.getNetmaskNumeric();

        if ((subnetID & this.getNetmaskNumeric()) == (this.baseIPnumeric & this.getNetmaskNumeric())) {

            if ((this.getNetmaskNumeric() < subnetMask) == true
                    && this.baseIPnumeric <= subnetID) {

                return true;
            }

        }
        return false;

    }

/**
*@param args

    public static void main(String[] args) {

        IPv4Net ipv4 = new IPv4Net("12.12.12.0/23");

        System.out.println(ipv4.getIP());
        System.out.println(ipv4.getNetmask());

        System.out.println(ipv4.getAvailableIPs(0));
        System.out.println(ipv4.getNext());
        System.out.println(ipv4.getCIDR());
	    System.out.println(ipv4.contains("192.168.50.11"));
        System.out.println("======= MATCHES =======");
        System.out.println(ipv4.getBinary(ipv4.baseIPnumeric));
        System.out.println(ipv4.getBinary(ipv4.netmaskNumeric));
        System.out.println(ipv4.getBinary(ipv4.baseIPnumeric));
        System.out.println(ipv4.getBinary(ipv4.netmaskNumeric));
        System.out.println("==============output================");

    }
    */
}


