package unix.parsing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

public class ParseNew {
	// 원본 소스파일 읽어올 경로
	private final String workspace_path = "/app/target";

	public static void main(String[] args) {
		// 시스템의 파일 인코딩을 UTF-8로 설정
		System.setProperty("file.encoding", "UTF-8");

		ParseNew ps = new ParseNew();
		// 원본 소스파일 이름
		ps.exec_run("vsttrest2_2023-04-21");
	}

	// 취합된 로그파일 정보들을 linePrint()를 통해 report.로그파일명.log로 묶어 파일 형태로 내보내기
	// 매개변수는 실제 파싱할 로그파일명(vsttrest2_2023-04-20)
	public void exec_run(String logFile) {
		// 파일 존재 여부를 먼저 확인
		File file = new File(this.workspace_path + "/" + "report." + logFile + ".txt");
		if (file.exists()) {
			// 파일이 이미 존재하는 경우 에러 메시지 출력 및 프로그램 중지
			System.err.println("Error: File <report." + logFile + ".txt> already exists. Execution stopped.");
			return; // 프로그램 종료 또는 추가적인 처리
		}

		List<String> lines = getBaseDataKeyList(this.workspace_path, logFile + ".log");
		String printStr = createTextLog(lines);

		// 출력할 파일 이름
		linePrint("report." + logFile, printStr);
	}

	// 원본 로그 파일을 읽어와서 지정한 workspace_path 폴더 아래 뿌려주는 메소드
	public void linePrint(String category, String outputStr) {
		File file = new File(this.workspace_path + "/" + category + ".txt");

		// 파일이 이미 존재하는 경우 실행 중지 및 false 반환
		if(file.exists()) {
			return;
		}

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
			writer.write(outputStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getBaseDataKeyList(String fpath, String fname){
		List<String> keyList = null;

		try {
			// 파일경로와 파일명을 UTF-8로 전부 읽어와서 List에 저장
			keyList = Files.readAllLines(Paths.get(fpath + "/" + fname), StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.getMessage();
		}

		return keyList;
	}

	// 패턴 파악
	//	1. 로그 내 사용 채널번호 체크(불러온 텍스트 중에 [CH]가 있는지?)
	public boolean checkCH(int ch, String text) {
		boolean returnVal = false;

		if(text.contains("[" + ch + "]")) { returnVal = true;}

		return returnVal;
	}

	//	2. 로그 내 사용 채널 추출(순수 채널 숫자만 채번)
	public int getTextCH(String text) {
		int returnInt = 0;

		// '['를 선택하려면 표현식으로 '\[' 근데 자바에서 '\'를 표현하려면 
		// '\\' => '[' 를 자바에서 정규식으로 선택시 '\\[' 로 써야 함!!
		returnInt = Integer.parseInt(text.split("\\[")[1].split("\\]")[0]);

		return returnInt;
	}

	//	3. WELCOME 체크
	public boolean checkWELCOME(String text) {
		boolean returnVal = false;

		if(text.contains("WELCOME")) { returnVal = true; }

		return returnVal;
	}

	//	4. ENDING 체크
	public boolean checkENDING(String text) {
		boolean returnVal = false;

		if(text.contains("ENDING")) { returnVal = true; }

		return returnVal;
	}

	// ※ 하나의 덩어리(WELCOME ~ ENDING)으로 묶기
	//	 lines : 동일한 채널로 이루어진 문자열 묶음(한 개의 콜)
	public List<String> setCHScopeList(int lineNO, int chNUM, List<String> lines){
		List<String> chScopeList = new ArrayList<>();
		int indexI = lineNO;

		for(indexI=lineNO; indexI<lines.size(); indexI++) {
			if(checkCH(chNUM, lines.get(indexI))) {
				chScopeList.add(lines.get(indexI));
				if(checkENDING(lines.get(indexI))) {
					break;
				}
			}
		}

		return chScopeList;
	}

	// 각 필요 정보별 추출 작업
	public String getLogInfo(List<String> chScopeList) {
		String returnStr = "";

		// []안의 채널 숫자를 2자리로 맞추기 위해 문자열로 변환
		String CH = getTextCH(chScopeList.get(0)) + "";
		String STT_CODE = "STTCD_NONE"; // vsqa 코드
		String STT_TXT = null; // STT 결과
		String REC_KEY = null;
		String REC_TIME_START = null;
		String REC_TIME_END = null;
		String REC_TEL = null;
		String REC_INFO = null; // 파일명
		String REC_CODE = null; // 콜 분기 위치정보

		// 덩어리 내용이 2개(WELCOME, ENDING)밖에 없다면 작업안하고 다음으로
		if(chScopeList.size() == 2) { return ""; }

		for(int indexI=0; indexI<chScopeList.size(); indexI++) {
			if(chScopeList.get(indexI).contains("send_dlg_update: 1 0.00 0")) {
				continue;
			}
			if(chScopeList.get(indexI).contains("send_chat")) {
				continue;
			}

			if(chScopeList.get(indexI).contains("WELCOME")) {
				// 시작시간 추출 => 00:05:56.299
				REC_TIME_START = chScopeList.get(indexI).split("INFO")[0].trim().split(" ")[1];
				// REC_INFO => 2023042000033814120045_14_01057300531_L000000
				REC_INFO = chScopeList.get(indexI).split(" i=")[1].trim().split(",")[0];
				REC_KEY = REC_INFO.split("_")[0]; // 2023042000033814120045
				REC_TEL = REC_INFO.split("_")[2]; // 01057300531
				// 위치코드
				REC_CODE = REC_INFO.split("_")[3];
			}

			if(chScopeList.get(indexI).contains("stage detected")) {
				continue;
			}

			// STT 결과 추출
			if(chScopeList.get(indexI).contains(" c=")
					&& !chScopeList.get(indexI).contains("WELCOME")
					&& !chScopeList.get(indexI).contains("ENDING")) {
				// (space)c=캡스 에서 'space + c='를 기준으로 뒤만 남김
				STT_TXT = chScopeList.get(indexI).split(" c=")[1];
			}

			if(chScopeList.get(indexI).contains("send_dlg_update")) {
				// QA 정보 추출
				//  2023042000033814120045, P020100|AS접수|0001 =>
				STT_CODE = chScopeList.get(indexI).split("send_dlg_update")[1].trim().split(",")[1].trim();
			}

			if(chScopeList.get(indexI).contains("ENDING")) {
				// 종료시간 정보
				REC_TIME_END = chScopeList.get(indexI).split("INFO")[0].trim().split(" ")[1];
			}
		} // for문 종료

		// CH 자리수 2자리로 통일하기
		if(CH.length() == 1) {
			CH = "[0" + CH + "]";
		}else {
			CH = "[" + CH + "]";
		}

		returnStr = CH + "	'" + REC_TIME_START + "	'" + REC_TIME_END
				+ "	'" + REC_TEL + "	" + STT_CODE + "	" + REC_CODE
				+ "	" + STT_TXT + "		'" + REC_KEY + "	" + REC_INFO;

		if(REC_TIME_START == null || REC_TIME_END == null) {
			returnStr = "";
		}else {
			System.out.println(returnStr);
		}

		return returnStr;
	}

	// 위의 정보들을 취합해서 로그파일 생성
	public String createTextLog(List<String> lines) {
		int indexI = 0;
		int chNUM = 0;
		List<String> scopeList = null;
		String strTmp = "";
		String returnStr = "";

		for(indexI=0; indexI<lines.size(); indexI++) {
			if(checkWELCOME(lines.get(indexI))) {
				// 채널번호 추출
				chNUM = getTextCH(lines.get(indexI));
				// 채널별 동일 덩어리(WELCOME ~ ENDING)를 scopeList에 넣음
				scopeList = setCHScopeList(indexI, chNUM, lines);
				// 위의 변수를 로그취합 메소드에 넣음
				strTmp = getLogInfo(scopeList);

				// 내용이 있다면 returnStr에 추가
				if(strTmp.length() > 0) {
					returnStr += strTmp + "\n";
				}
			}
		}

		return returnStr;
	}
}