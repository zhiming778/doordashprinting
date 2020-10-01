package model;

public class Commands {
    public static final byte[] LF = { 0x0a }; // line feed
    public static final byte[] CUT_PAPER = { 0x1d, 0x56, 0x42, 0x0 };
    public static final byte[] CHAR_SIZE_X1 = { 0x1d, 0x21, 0x0 };
    public static final byte[] CHAR_SIZE_X2 = { 0x1d, 0x21, 0x11 };
    public static final byte[] CHAR_SIZE_X3 = { 0x1d, 0x21, 0x20 };
    public static final byte[] CHAR_SIZE_X4 = { 0x1d, 0x21, 0x30 };
    public static final byte[] LEFT_ALIGN = { 0x1b, 0x61, 0x30 };
    public static final byte[] CENTER_ALIGN = { 0x1b, 0x61, 0x31 };
    public static final byte[] RIGHT_ALIGN = { 0x1b, 0x61, 0x32 };
    public static final byte[] CHINESE_CHARSET = { 0x1B, 0x52, 0x0F };
    public static final byte[] CHINESE_MODE_ON = { 0x1C, 0x26 };
    public static final byte[] CHINESE_MODE_OFF = { 0x1C, 0x2E };
    

    public static final int NUM_OF_LINE_CHAR = 48;
    public static final int SINGLE_DIVIDER = '-';
    public static final int DOUBLE_DIVIDER = '=';
}
