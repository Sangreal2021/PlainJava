package charsetex;

import java.io.UnsupportedEncodingException;

public class CharLeft {
    public static void main(String[] args) {
        CharLeft cl = new CharLeft();

        try {
            String value = "나라의 말이 중국과는 달라\n" +
                    "문자(한자)와는 서로 맞지 아니하므로\n" +
                    "이런 까닭으로 글을 모르는 백성들이 말하고자 하는 바 있어도\n" +
                    "마침내 제 뜻을 능히 펴지 못할 사람이 많으니라\n" +
                    "내 이를 위하여, 가엾게 여겨\n" +
                    "새로 스물여덟 자를 만드노니\n" +
                    "사람마다 쉽게 익혀 날마다 씀에 편안케 하고자 할 따름이니라";
            int maxBytes = 2000;
            String charSet = "EUC-KR";
//            String charSet = "UTF-8";

            long startTime = System.nanoTime(); // 시작 시간 측정

            String result = cl.leftbc(value, maxBytes, charSet);

            long endTime = System.nanoTime(); // 종료 시간 측정
            long duration = endTime - startTime; // 실행 시간 계산

            // 바이트 크기와 결과 출력
            int byteCount = value.getBytes(charSet).length;
            System.out.println("Result: " + result);
            System.out.println(); // 공백 줄로 구분
            System.out.println("Charset: " + charSet);
            System.out.println("Byte size of input string: " + byteCount + " bytes");
            System.out.println("Execution time: " + duration + " nanoSec");
            System.out.println("Execution time: " + duration / 1000000 + " milliSec");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    public String leftbc(String value, int maxBytes, String charset) throws UnsupportedEncodingException {
        int chatLen, totalBytes = 0;
        byte[] bytes = value.getBytes(charset);

        for (char c : value.toCharArray()){
            chatLen = String.valueOf(c).getBytes(charset).length;
            if(totalBytes + chatLen > maxBytes){
                break;
            }else {
                totalBytes += chatLen;
            }
        }

        return new String(bytes, 0, totalBytes, charset);
    }

//    public String leftbc(String value, int maxBytes, String charset) throws UnsupportedEncodingException {
//        byte[] bytes = value.getBytes(charset);
//        int totalBytes = 0;
//
//        for (int index = 0; index < bytes.length; ) {
//            int byteLength;
//            if ((bytes[index] & 0x80) == 0) {
//                byteLength = 1;  // ASCII (1-byte)
//            } else if ((bytes[index] & 0xE0) == 0xC0) {
//                byteLength = 2;  // 2-byte character (could be EUC-KR or UTF-8 continuation)
//            } else if ((bytes[index] & 0xF0) == 0xE0) {
//                byteLength = 3;  // UTF-8 (3-byte)
//            } else if ((bytes[index] & 0xF8) == 0xF0) {
//                byteLength = 4;  // UTF-8 (4-byte)
//            } else {
//                byteLength = 2;  // Assume EUC-KR (2-byte) for cases not explicitly handled
//            }
//
//            if (totalBytes + byteLength > maxBytes) {
//                break;  // Ensures we don't exceed maxBytes
//            }
//            totalBytes += byteLength;
//            index += byteLength;
//        }
//
//        return new String(bytes, 0, totalBytes, charset);
//    }
}
