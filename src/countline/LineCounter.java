package countline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LineCounter {

    // 소스 디렉토리의 경로를 저장할 멤버 변수
    private final String srcDirectory;

    // 제외할 폴더 리스트
    private final List<String> excludeFolders = Arrays.asList(".idea", ".venv", ".git", "etc");

    // 제외할 파일 확장자 목록
    private final List<String> excludeFileExtensions = Arrays.asList(".pdf", ".jpg", ".png");

    public LineCounter(String srcDirectory) {
        // 생성자 매개변수로 받은 경로를 멤버 변수에 할당
        this.srcDirectory = srcDirectory;
    }

    public static void main(String[] args) {
        String srcDirectory = "D:\\private\\study\\SQLD"; // 디렉토리 경로 설정
        LineCounter counter = new LineCounter(srcDirectory); // LineCounter 인스턴스 생성

        // srcDirectory 에서 마지막 "\"의 인덱스를 찾으면,
        // 이는 "main" 바로 앞의 "\"를 가리킵니다.
//        String dir = srcDirectory.substring(srcDirectory.lastIndexOf(File.separator) + 1);

        try {
            // 파일 종류별 라인 수 계산
            Map<String, Long> linesByFileType = counter.countLinesByFileType();
            // 결과 출력
            linesByFileType.forEach((type, lines) -> System.out.println(type + " files total lines : " + lines));

            // 총 라인 수 계산
//            long totalLines = linesByFileType.values().stream().mapToLong(n -> n.longValue()).sum();
            long totalLines = linesByFileType.values().stream().mapToLong(Long::longValue).sum();
            // 총 라인 수 출력
            System.out.println("Total lines in '" + counter.srcDirectory + "' directory: " + totalLines);
        } catch (IOException e) { // 입출력 관련 예외 처리
            System.err.println("Error reading files: " + e.getMessage());
        } catch (IllegalArgumentException e) { // 경로 관련 예외 처리
            System.err.println(e.getMessage());
        }
    }

    // 파일 종류별 라인 수 계산 메서드
    public Map<String, Long> countLinesByFileType() throws IOException, IllegalArgumentException {
        Path path = Paths.get(srcDirectory); // String 경로를 Path 객체로 변환
        if (!Files.exists(path) || !Files.isDirectory(path)) { // 경로의 존재 여부 및 디렉토리 여부 확인
            // 예외 발생
            throw new IllegalArgumentException("경로가 존재하지 않거나 디렉토리가 아닙니다 -> "
                    + srcDirectory);
        }

        // 파일 종류별 라인 수를 저장할 맵 생성
        Map<String, Long> lineCountByType = new HashMap<>();

        // 모든 파일을 리스트로 생성
        List<Path> files = Files.walk(path)     // 주어진 경로에서 시작하여 모든 파일 탐색
                .filter(Files::isRegularFile)   // 파일인지 확인
                .filter(this::isValidPath) // isValidPath 메서드를 참조하여 조건을 만족하는 파일만 필터링
                .collect(Collectors.toList());  // 결과를 리스트로 수집

        for (Path file : files) {   // 모든 파일에 대해 반복
            String fileName = file.getFileName().toString();    // 파일 이름 추출
            // 마지막 '.' 이후의 문자열을 추출해 fileType에 저장 -> 파일 확장자 추출
            // 확장자가 없는 경우 "Unknown" 으로 출력
            String fileType = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "Unknown";

            // 파일을 읽어 각 파일의 라인 수를 계산 (try-with-resources 구문)
            try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
                long lines = reader.lines().count();    // 현재 파일의 라인 수를 계산
                lineCountByType.merge(fileType, lines, Long::sum);  // 같은 확장자를 가진 파일들의 라인 수를 합산
            }
        }

        return lineCountByType; // 계산된 파일 종류별 라인 수를 담은 맵을 반환
    }

    private boolean isValidPath(Path path) {
        String pathStr = path.toString();
        // 제외할 폴더를 포함하고 있는지 확인
        for (String folder : excludeFolders) {
            if (pathStr.contains(folder)) {
                return false; // 하나라도 포함되어 있으면 false 반환
            }
        }
        // 파일 확장자 체크 추가
        for (String extension : excludeFileExtensions) {
            if (pathStr.endsWith(extension)) {
                return false;
            }
        }
        return true; // 모든 제외 폴더를 포함하지 않으면 true 반환
    }
}